package me.pulsi_.bankplus.sql;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * BankPlus SQL storage (MySQL or SQLite). Player balances are saved
 * here only; old playerdata files can still be imported with /bank transfer.
 */
public class BPSQL {

    public enum SQLSearch {
        BANK_LEVEL,
        DEBT,
        INTEREST,
        MONEY
    }

    // Balances are stored as plain strings, and a huge one overflowed the old varchar(255) ("Data too long for column 'money'").
    private static final int AMOUNT_COLUMN_LENGTH = 1024;

    /**
     * Get the string of default arguments that a bankplus table has.
     *
     * @return The default arguments of the bankplus table.
     */
    public static String GET_TABLE_ARGUMENTS() {
        return "uuid varchar(255)," +
                "account_name varchar(255) DEFAULT ''," +
                "bank_level varchar(255) DEFAULT '1'," +
                "money varchar(" + AMOUNT_COLUMN_LENGTH + ") DEFAULT '0'," +
                "interest varchar(" + AMOUNT_COLUMN_LENGTH + ") DEFAULT '0'," +
                "debt varchar(" + AMOUNT_COLUMN_LENGTH + ") DEFAULT '0'," +
                "PRIMARY KEY (uuid)";
    }

    /**
     * Return a string representing all the default arguments for that specified player.
     * More specifically, returns the following values:
     * - UUID
     * - Name
     * - BankLevel = 1
     * - Money = 0 (Or default amount)
     * - Interest = 0
     * - Debt = 0
     *
     * @param p        The player where to retrieve the arguments.
     * @param bankName The bank where to get the information.
     * @return The argument based on the specified player.
     */
    public static String GET_DEFAULT_PLAYER_ARGUMENTS(OfflinePlayer p, String bankName) {
        String name = p.getName() == null ? "" : p.getName().replace("'", "''");
        String money = ConfigValues.getMainGuiName().equals(bankName) ? ConfigValues.getStartAmount().toPlainString() : "0";
        return "'" + p.getUniqueId() + "','" + name + "','1','" + money + "','0','0'";
    }

    // One shared connection, so everything that touches it has to wait its turn.
    private static final Object LOCK = new Object();
    private static Connection connection;

    // True while the plugin wants a live connection, so a lost one gets reopened on demand.
    private static boolean wantsConnection;
    private static long lastCheck, lastReconnectTry;
    private static final long CHECK_INTERVAL = 5000, RECONNECT_INTERVAL = 10000;

    /**
     * Thrown when the data of a player cannot be trusted: the database is unreachable or the stored value is
     * not valid. It is a different case from "no record yet", which is a normal situation for new players.
     * Callers must not fall back to default values when they get it, or the real data would get overwritten.
     */
    public static class DataException extends RuntimeException {

        public DataException(String message) {
            super(message);
        }

        public DataException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Get the connection, that can be to MySQL or SQLite.
     *
     * @return The connection object.
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Get the connection to use for a query, reopening it if MySQL dropped it (idle timeout, restart...).
     * Must be called while holding the lock.
     *
     * @return The connection, or null if there is none and it could not be restored.
     */
    private static Connection current() {
        long now = System.currentTimeMillis();
        if (connection == null) {
            // Don't retry on every query, a dead server would make each one wait for the connect timeout.
            if (!wantsConnection || now - lastReconnectTry < RECONNECT_INTERVAL) return null;
            reconnect(now);
            return connection;
        }

        if (!ConfigValues.isMySqlEnabled() || now - lastCheck < CHECK_INTERVAL) return connection;
        lastCheck = now;

        try {
            if (connection.isValid(2)) return connection;
        } catch (SQLException ignored) {
        }

        BPLogger.Console.warn("Lost the MySQL connection, trying to reconnect...");
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
        connection = null;
        reconnect(now);
        return connection;
    }

    private static void reconnect(long now) {
        lastReconnectTry = now;
        if (ConfigValues.isMySqlEnabled()) MySQL.connect();
        else SQLite.connect();
    }

    public static void disconnect() {
        final Connection toClose;
        synchronized (LOCK) {
            wantsConnection = false;
            toClose = connection;
            connection = null;
        }
        if (toClose == null) return;

        // Close with a timeout. SQLite close can hang forever if another async
        // task still has a statement open, and we don't want that freezing stop.
        Thread closer = new Thread(() -> {
            try {
                toClose.close();
            } catch (SQLException e) {
                BPLogger.Console.error(e, "Could not close SQL connection.");
            }
        }, "BankPlus-SQL-Close");
        closer.setDaemon(true);
        closer.start();

        try {
            closer.join(3000);
        } catch (InterruptedException ignored) {
        }

        if (closer.isAlive()) BPLogger.Console.warn("SQL close is taking too long, continuing disable anyway.");
    }

    /**
     * Get the bank level of the specified player in the specified bank.
     *
     * @param player   The player.
     * @param bankName The bank.
     * @return The bank level or 1 if not found.
     */
    public static int getBankLevel(OfflinePlayer player, String bankName) {
        String level = get(player, bankName, SQLSearch.BANK_LEVEL);
        if (level.isEmpty()) return 1;

        try {
            return Integer.parseInt(level);
        } catch (NumberFormatException e) {
            throw new DataException("Invalid bank level \"" + level + "\" stored for " + player.getName() + " in bank " + bankName + ".", e);
        }
    }

    /**
     * Get the debt of the specified player in the specified bank.
     *
     * @param player   The player.
     * @param bankName The bank.
     * @return The debt or ZERO if not found.
     */
    public static BigDecimal getDebt(OfflinePlayer player, String bankName) {
        String debt = get(player, bankName, SQLSearch.DEBT);
        return parseAmount(debt, "debt", player, bankName);
    }

    /**
     * Get the interest of the specified player in the specified bank.
     *
     * @param player   The player.
     * @param bankName The bank.
     * @return The interest or ZERO if not found.
     */
    public static BigDecimal getInterest(OfflinePlayer player, String bankName) {
        String interest = get(player, bankName, SQLSearch.INTEREST);
        return parseAmount(interest, "interest", player, bankName);
    }

    /**
     * Get the money of the specified player in the specified bank.
     *
     * @param player   The player.
     * @param bankName The bank.
     * @return The money or ZERO if not found.
     */
    public static BigDecimal getMoney(OfflinePlayer player, String bankName) {
        String money = get(player, bankName, SQLSearch.MONEY);
        return parseAmount(money, "money", player, bankName);
    }

    private static BigDecimal parseAmount(String value, String column, OfflinePlayer player, String bankName) {
        if (value.isEmpty()) return BigDecimal.ZERO;

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new DataException("Invalid " + column + " \"" + value + "\" stored for " + player.getName() + " in bank " + bankName + ".", e);
        }
    }

    /**
     * Set the bank level of the specified player in the specified bank.
     *
     * @param player   The player.
     * @param bankName The bank.
     * @param newValue The new level.
     */
    public static void setBankLevel(OfflinePlayer player, String bankName, BigDecimal newValue) {
        set(player, bankName, SQLSearch.BANK_LEVEL, newValue.toPlainString());
    }

    /**
     * Set the debt of the specified player in the specified bank to the selected amount.
     *
     * @param player   The player.
     * @param bankName The bank.
     * @param newValue The new debt.
     */
    public static void setDebt(OfflinePlayer player, String bankName, BigDecimal newValue) {
        set(player, bankName, SQLSearch.DEBT, newValue.toPlainString());
    }

    /**
     * Set the interest (interest earned while being offline to show to the player when
     * entering the server) of the specified player in the specified bank to the selected amount.
     *
     * @param player   The player.
     * @param bankName The bank.
     * @param newValue The new value for the money.
     */
    public static void setInterest(OfflinePlayer player, String bankName, BigDecimal newValue) {
        set(player, bankName, SQLSearch.INTEREST, newValue.toPlainString());
    }

    /**
     * Set the money of the specified player in the specified bank to the selected amount.
     *
     * @param player   The player.
     * @param bankName The bank.
     * @param newValue The new value for the money.
     */
    public static void setMoney(OfflinePlayer player, String bankName, BigDecimal newValue) {
        set(player, bankName, SQLSearch.MONEY, newValue.toPlainString());
    }

    /**
     * Save all the player's current bank statistics to the database.
     *
     * @param player   The player to save.
     * @param bankEconomy The economy of the bank.
     * @return true if the data has been written, false if it could not be saved.
     */
    public static boolean savePlayer(OfflinePlayer player, BPEconomy bankEconomy) {
        String bankName = bankEconomy.getOriginBank().getIdentifier();

        int level = bankEconomy.getBankLevel(player);
        String debt = bankEconomy.getDebt(player).toPlainString();
        String money = bankEconomy.getBankBalance(player).toPlainString();

        String insert = "INSERT INTO " + bankName + " (uuid, bank_level, debt, money) VALUES('" +
                player.getUniqueId() + "', '" + level + "', '" + debt + "', '" + money + "')";

        String set = "bank_level='" + level + "', debt='" + debt + "', money='" + money + "'";

        String query;
        if (ConfigValues.isMySqlEnabled()) query = insert + " ON DUPLICATE KEY UPDATE " + set;
        else query = insert + " ON CONFLICT(uuid) DO UPDATE SET " + set;

        synchronized (LOCK) {
            Connection connection = current();
            if (connection == null) return false;
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                BPLogger.Console.error(e, "Cannot save player " + player.getName() + " in bank " + bankName + ".");
                return false;
            }
        }
    }

    /**
     * Check if the specified player is registered in the given bank table (= bank name)
     * @param player The player to check.
     * @param bankName The table name.
     * @return true if a record with its uuid exists, false otherwise.
     * @throws DataException If the database cannot be queried.
     */
    public static boolean isRegistered(OfflinePlayer player, String bankName) {
        synchronized (LOCK) {
            Connection connection = current();
            if (connection == null) throw new DataException("There is no SQL connection.");
            try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM " + bankName + " WHERE uuid='" + player.getUniqueId() + "' LIMIT 1");
                 ResultSet set = statement.executeQuery()) {
                return set.next();
            } catch (SQLException e) {
                throw new DataException("Cannot check if player " + player.getName() + " is registered in the bank " + bankName + ".", e);
            }
        }
    }

    /**
     * Fill all the different tables with the base values as if a
     * new player join, this method will only place the values
     * if they are not there, otherwise it won't update anything.
     *
     * @param player The player to register.
     */
    public static void fillRecords(OfflinePlayer player) {
        synchronized (LOCK) {
            Connection connection = current();
            if (connection == null) return;

            if (ConfigValues.isMySqlEnabled())
                for (String bank : BPEconomy.nameList()) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT IGNORE INTO " + bank + " VALUES (" + GET_DEFAULT_PLAYER_ARGUMENTS(player, bank) + ")"
                    )) {
                        statement.executeUpdate();
                    } catch (SQLException e) {
                        BPLogger.Console.error(e, "Cannot insert base value in bank " + bank + " for player " + player.getName() + ".");
                    }
                }
            else
                for (String bank : BPEconomy.nameList()) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT OR IGNORE INTO " + bank + " VALUES (" + GET_DEFAULT_PLAYER_ARGUMENTS(player, bank) + ")"
                    )) {
                        statement.executeUpdate();
                    } catch (SQLException e) {
                        BPLogger.Console.error(e, "Cannot insert base value in bank " + bank + " for player " + player.getName() + ".");
                    }
                }
        }
    }

    public static class MySQL {

        /**
         * Creates a new instance of the connection and assign it.
         * This method also enables the SQLMethods.
         */
        public static void connect() {
            synchronized (LOCK) {
                if (connection != null) {
                    BPLogger.Console.warn("MySQL is already connected.");
                    return;
                }
                wantsConnection = true;

                String host = ConfigValues.getMySqlHost();
                String port = ConfigValues.getMySqlPort();
                String database = ConfigValues.getMySqlDatabase();

                // Timeouts so a dead mysql doesn't freeze the whole server on disable.
                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + ConfigValues.isMySqlUsingSSL() + "&connectTimeout=5000&socketTimeout=15000";

                try {
                    connection = DriverManager.getConnection(url, ConfigValues.getMySqlUsername(), ConfigValues.getMySqlPassword());

                    // Create all the missing tables.
                    for (String bankName : BPEconomy.nameList()) {
                        String query = "CREATE TABLE IF NOT EXISTS " + bankName + " (" + GET_TABLE_ARGUMENTS() + ")";
                        try (PreparedStatement statement = connection.prepareStatement(query)) {
                            statement.execute();
                        }

                        // Tables created by older versions still have the 255 chars columns.
                        String widen = "ALTER TABLE " + bankName +
                                " MODIFY money varchar(" + AMOUNT_COLUMN_LENGTH + ") DEFAULT '0'," +
                                " MODIFY interest varchar(" + AMOUNT_COLUMN_LENGTH + ") DEFAULT '0'," +
                                " MODIFY debt varchar(" + AMOUNT_COLUMN_LENGTH + ") DEFAULT '0'";
                        // Not fatal: a restricted user without ALTER privileges can still work with the old columns.
                        try (PreparedStatement statement = connection.prepareStatement(widen)) {
                            statement.execute();
                        } catch (SQLException e) {
                            BPLogger.Console.warn("Could not widen the columns of the bank \"" + bankName + "\", very big balances may fail to save. (" + e.getMessage() + ")");
                        }
                    }
                } catch (SQLException e) {
                    connection = null;
                    BPLogger.Console.error(e, "Could not connect to MySQL database.");
                    return;
                }
                BPLogger.Console.info("MySQL database successfully connected.");
            }
        }
    }

    public static class SQLite {

        public static void connect() {
            synchronized (LOCK) {
                if (connection != null) {
                    BPLogger.Console.warn("SQLite is already connected.");
                    return;
                }
                wantsConnection = true;

                File dbFile = new File(BankPlus.INSTANCE().getDataFolder(), "data.db");

                if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();

                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                try {
                    connection = DriverManager.getConnection(url);

                    // Don't wait forever if the db file is busy.
                    try (Statement statement = connection.createStatement()) {
                        statement.execute("PRAGMA busy_timeout = 3000");
                    }

                    for (String bankName : BPEconomy.nameList()) {
                        String query = "CREATE TABLE IF NOT EXISTS " + bankName + " (" + GET_TABLE_ARGUMENTS() + ")";
                        try (PreparedStatement statement = connection.prepareStatement(query)) {
                            statement.execute();
                        }
                    }
                } catch (SQLException e) {
                    connection = null;
                    BPLogger.Console.error(e, "Could not connect to SQLite database.");
                    return;
                }
                BPLogger.Console.info("SQLite successfully connected.");
            }
        }
    }

    /**
     * Method to retrieve the specified value from the selected player in a particular bank.
     *
     * @param player   The player.
     * @param bankName The bank where to search the value.
     * @param search   The value type.
     * @return A string representing the bank level, debt, interest or money value, empty if the player has no record.
     * @throws DataException If the database cannot be queried.
     */
    private static String get(OfflinePlayer player, String bankName, SQLSearch search) {
        String columnName;
        switch (search) {
            case BANK_LEVEL -> columnName = "bank_level";
            case DEBT -> columnName = "debt";
            case INTEREST -> columnName = "interest";
            case MONEY -> columnName = "money";

            default -> {
                BPLogger.Console.warn("Invalid SQLSearch specified as parameter for query.");
                return "";
            }
        }

        String query = "SELECT " + columnName + " FROM " + bankName + " WHERE uuid='" + player.getUniqueId() + "'";

        synchronized (LOCK) {
            Connection connection = current();
            if (connection == null) throw new DataException("There is no SQL connection.");
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet set = statement.executeQuery()) {
                if (!set.next()) return "";

                String value = set.getString(columnName);
                return value == null ? "" : value;
            } catch (SQLException e) {
                throw new DataException("Could not get data for player " + player.getName() + " in bank " + bankName + ".", e);
            }
        }
    }

    /**
     * Set or update a value for the specified player in the specified bank.
     * This method automatically creates a new record if missing.
     *
     * @param player   The player.
     * @param bankName The bank where to update the value.
     * @param search   The value to search.
     * @param newValue The new value.
     */
    private static void set(OfflinePlayer player, String bankName, SQLSearch search, String newValue) {
        String columnName;
        switch (search) {
            case BANK_LEVEL -> columnName = "bank_level";
            case DEBT -> columnName = "debt";
            case INTEREST -> columnName = "interest";
            case MONEY -> columnName = "money";

            default -> {
                BPLogger.Console.warn("Invalid SQLSearch specified as parameter for query.");
                return;
            }
        }

        String query;

        String insert = "INSERT INTO " + bankName + " (uuid, " + columnName + ") VALUES('" + player.getUniqueId() + "', '" + newValue + "')";
        String set = columnName + "='" + newValue + "'";
        if (ConfigValues.isMySqlEnabled()) query = insert + " ON DUPLICATE KEY UPDATE " + set;
        else query = insert + " ON CONFLICT(uuid) DO UPDATE SET " + set;

        synchronized (LOCK) {
            Connection connection = current();
            if (connection == null) return;
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                BPLogger.Console.error(e, "Cannot set " + columnName + " for player " + player.getName() + " in bank " + bankName + ".");
            }
        }
    }
}