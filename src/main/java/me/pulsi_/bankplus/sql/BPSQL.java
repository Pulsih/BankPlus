package me.pulsi_.bankplus.sql;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * BankPlus MySQL system save both database and files to synchronize
 * them, local files data is always available and updated, and will
 * be updated on the database.
 * <p>
 * In case the database is not available or the connection has been
 * closed, the data will continue to be saved locally, and once the
 * database will connect again the local data will be updated.
 */
public class BPSQL {

    public enum SQLSearch {
        BANK_LEVEL,
        DEBT,
        INTEREST,
        MONEY
    }

    /**
     * Get the string of default arguments that a bankplus table has.
     *
     * @return The default arguments of the bankplus table.
     */
    public static String GET_TABLE_ARGUMENTS() {
        return "uuid varchar(255)," +
                "account_name varchar(255) DEFAULT ''," +
                "bank_level varchar(255) DEFAULT '1'," +
                "money varchar(255) DEFAULT '0'," +
                "interest varchar(255) DEFAULT '0'," +
                "debt varchar(255) DEFAULT '0'," +
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
        return p.getUniqueId() + "," +
                p.getName() + "," +
                "1," +
                (ConfigValues.getMainGuiName().equals(bankName) ? ConfigValues.getStartAmount() : "0") + "," +
                "0," +
                "0";
    }

    private static Connection connection;

    /**
     * Get the connection, that can be to MySQL or SQLite.
     *
     * @return The connection object.
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Get the bank level of the specified player in the specified bank.
     *
     * @param player   The player.
     * @param bankName The bank.
     * @return The bank level or 0 if not found.
     */
    public static int getBankLevel(OfflinePlayer player, String bankName) {
        String level = get(player, bankName, SQLSearch.BANK_LEVEL);
        if (level.isEmpty()) return 0;

        return Integer.parseInt(level);
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
        if (debt.isEmpty()) return BigDecimal.ZERO;

        return new BigDecimal(debt);
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
        if (interest.isEmpty()) return BigDecimal.ZERO;

        return new BigDecimal(interest);
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
        if (money.isEmpty()) return BigDecimal.ZERO;

        return new BigDecimal(money);
    }

    /**
     * Fill all the different tables with the base values as if a
     * new player join, this method will only place the values
     * if they are not there, otherwise it won't update anything.
     *
     * @param player The player to register.
     */
    public static void fillRecords(OfflinePlayer player) {
        if (ConfigValues.isMySqlEnabled())
            for (String bank : BPEconomy.nameList()) {
                try {
                    connection.prepareStatement(
                            "INSERT IGNORE INTO " + bank + " VALUES (" + GET_DEFAULT_PLAYER_ARGUMENTS(player, bank) + ")"
                    ).executeUpdate();
                } catch (SQLException e) {
                    BPLogger.Console.error(e, "Cannot insert base value in bank " + bank + " for player " + player.getName() + ".");
                }
            }
        else
            for (String bank : BPEconomy.nameList()) {
                try {
                    connection.prepareStatement(
                            "INSERT OR IGNORE INTO " + bank + " VALUES (" + GET_DEFAULT_PLAYER_ARGUMENTS(player, bank) + ")"
                    ).executeUpdate();
                } catch (SQLException e) {
                    BPLogger.Console.error(e, "Cannot insert base value in bank " + bank + " for player " + player.getName() + ".");
                }
            }
    }

    public static class MySQL {

        /**
         * Creates a new instance of the connection and assign it.
         * This method also enables the SQLMethods.
         */
        public static void connect() {
            if (connection != null) {
                BPLogger.Console.warn("MySQL is already connected.");
                return;
            }

            String host = ConfigValues.getMySqlHost();
            String port = ConfigValues.getMySqlPort();
            String database = ConfigValues.getMySqlDatabase();

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + ConfigValues.isMySqlUsingSSL();

            try {
                connection = DriverManager.getConnection(url, ConfigValues.getMySqlUsername(), ConfigValues.getMySqlPassword());

                // Create all the missing tables.
                for (String bankName : BPEconomy.nameList()) {
                    String query = "CREATE TABLE IF NOT EXISTS " + bankName + " (" + GET_TABLE_ARGUMENTS() + ")";
                    connection.prepareStatement(query).execute();
                }
            } catch (SQLException e) {
                BPLogger.Console.error(e, "Could not connect to MySQL database.");
            }
            BPLogger.Console.info("MySQL database successfully connected.");
        }

        public static void disconnect() {
            if (connection == null) {
                BPLogger.Console.info("MySQL database is already disconnected.");
                return;
            }

            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                BPLogger.Console.error(e, "Could not close MySQL connection.");
                return;
            }
            BPLogger.Console.info("MySQL successfully disconnected.");
        }

    }

    public static class SQLite {

        public static void connect() {
            if (connection != null) {
                BPLogger.Console.warn("SQLite is already connected.");
                return;
            }

            File dbFile = new File(BankPlus.INSTANCE().getDataFolder(), "data.db");

            if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();

            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            try {
                connection = DriverManager.getConnection(url);

                for (String bankName : BPEconomy.nameList()) {
                    String query = "CREATE TABLE IF NOT EXISTS " + bankName + " (" + GET_TABLE_ARGUMENTS() + ")";
                    connection.prepareStatement(query).execute();
                }
            } catch (SQLException e) {
                BPLogger.Console.error(e, "Could not connect to SQLite database.");
                return;
            }
            BPLogger.Console.info("SQLite successfully connected.");
        }

        public static void disconnect() {
            if (connection == null) {
                BPLogger.Console.info("SQLite database is already disconnected.");
                return;
            }

            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                BPLogger.Console.error(e, "Could not close SQLite connection.");
                return;
            }
            BPLogger.Console.info("SQLite successfully disconnected.");
        }


    }

    /**
     * Method to retrieve the specified value from the selected player in a particular bank.
     *
     * @param player   The player.
     * @param bankName The bank where to search the value.
     * @param search   The value type.
     * @return A string representing the bank level, debt, interest or money value.
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

        ResultSet set;
        try {
            set = connection.prepareStatement(query).executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            return set.getString(columnName);
        } catch (SQLException e) {
            BPLogger.Console.error(e, "Could not get data for player " + player.getName() + ".");
            return "";
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

        if (ConfigValues.isMySqlEnabled())
            query = "INSERT INTO " + bankName + " (uuid, " + columnName + ") VALUES(" + player.getUniqueId() + ", " + newValue + ") " +
                    "ON DUPLICATE KEY UPDATE " + columnName + " = VALUES(" + newValue + ")";
        else
            query = "INSERT INTO " + bankName + " (uuid, " + columnName + ") VALUES(" + player.getUniqueId() + ", " + newValue + ") " +
                    "ON CONFLICT(uuid) DO UPDATE SET " + columnName + " = '" + newValue + "'";

        try {
            connection.prepareStatement(query).executeUpdate();
        } catch (SQLException e) {
            BPLogger.Console.error(e, "Cannot set " + columnName + " for player " + player.getName() + " in bank " + bankName + ".");
        }
    }
}