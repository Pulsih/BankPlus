package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.OfflinePlayer;

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

    /**
     * Get the string of default arguments that a bankplus table has.
     *
     * @return The default arguments of the bankplus table.
     */
    public static String GET_TABLE_ARGUMENTS() {
        return "uuid varchar(255)," +
                "account_name varchar(255)," +
                "bank_level varchar(255)," +
                "money varchar(255)," +
                "interest varchar(255)," +
                "debt varchar(255)," +
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

    private static String username, password, url;
    private static Connection connection;

    public static Connection getConnection() {
        return connection;
    }

    /**
     * Initialize the MySQL system, loading all the necessary values.
     */
    public static void setupMySQL() {
        String host = ConfigValues.getSqlHost();
        String port = ConfigValues.getSqlPort();
        String database = ConfigValues.getSqlDatabase();
        username = ConfigValues.getSqlUsername();
        password = ConfigValues.getSqlPassword();

        url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + ConfigValues.isSqlUsingSSL();
    }

    /**
     * Register the player create a new record on each bank tables.
     *
     * @param p The player to register.
     */
    public static void registerPlayer(OfflinePlayer p) {
        for (String bankName : BPEconomy.nameList())
            if (!isRegistered(p, bankName))
                update("INSERT INTO " + bankName + " VALUES (" + GET_DEFAULT_PLAYER_ARGUMENTS(p, bankName) + ")");
    }

    /**
     * Check if the player record is present in the given bank table.
     *
     * @param p        The player to check for registration.
     * @param bankName The bank table name.
     * @return true if there is a player record, false otherwise.
     */
    public static boolean isRegistered(OfflinePlayer p, String bankName) {
        String name = p.getName(), check;
        if (ConfigValues.isStoringUUIDs()) check = "uuid='" + p.getUniqueId() + "'";
        else check = "account_name='" + name + "'";

        String query = "SELECT * FROM " + bankName + " WHERE " + check;

        // Execute the query having as result a list where there should be the player's UUID.
        BPSQLResponse response = query(query, "uuid");
        if (!response.success) {
            BPLogger.Console.warn(true, "Could not check if player \"" + name + "\" is registered to the database. (Reason: " + response.errorMessage + ")");
            return false;
        }

        // If the result is empty, it means that the player hasn't been registered yet.
        return !response.result.isEmpty();
    }

    /**
     * Creates a new instance of the connection and assign it.
     * This method also enables the SQLMethods.
     */
    public static void connect() {
        try {
            connection = DriverManager.getConnection(url, username, password);

            // Create all the missing tables.
            for (String bankName : BPEconomy.nameList()) {
                BPSQLResponse r = update("CREATE TABLE IF NOT EXISTS " + bankName + " (" + GET_TABLE_ARGUMENTS() + ")");
                if (!r.success) BPLogger.Console.warn(true, "Cannot create table +\"" + bankName + "\", reason: " + r.errorMessage);
            }

            BPLogger.Console.info("MySQL database successfully connected!");
        } catch (SQLException e) {
            BPLogger.Console.warn(e, "Could not connect bankplus to it's database!");
        }
    }

    /**
     * Disconnect the database.
     */
    public static void disconnect() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
                BPLogger.Console.info("MySQL database successfully disconnected!");
            }
        } catch (SQLException e) {
            BPLogger.Console.warn(e, "Could not disconnect bankplus from his database!");
        }
    }

    /**
     * Check if the connection to the database is present and isn't closed.
     *
     * @return true if it's correctly connected, false otherwise.
     */
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Try to execute the given update.
     *
     * @param update The update to process in the database.
     * @return A SQL response with useful information in case something fails.
     */
    public static BPSQLResponse update(String update) {
        try {
            connection.prepareStatement(update).executeUpdate();
            return BPSQLResponse.success();
        } catch (SQLException e) {
            return BPSQLResponse.fail(e);
        }
    }

    /**
     * Try to execute the given query.
     *
     * @param query      The query to execute and retrieve from the database.
     * @param columnName The column where to get the list of results.
     * @return A SQL response with useful information in case something fails.
     */
    public static BPSQLResponse query(String query, String columnName) {
        try {
            ResultSet set = connection.prepareStatement(query).executeQuery();

            List<String> result = new ArrayList<>();
            while (set.next()) result.add(set.getString(columnName));

            return BPSQLResponse.success(result);
        } catch (SQLException e) {
            return BPSQLResponse.fail(e);
        }
    }

    public static class BPSQLResponse {

        public final List<String> result;
        public final boolean success;
        public final String errorMessage;

        public BPSQLResponse(boolean success, String errorMessage) {
            this.result = new ArrayList<>();
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public BPSQLResponse(boolean success, String errorMessage, List<String> result) {
            this.result = result;
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static BPSQLResponse success() {
            return new BPSQLResponse(true, null);
        }

        public static BPSQLResponse success(List<String> result) {
            return new BPSQLResponse(true, null, result);
        }

        public static BPSQLResponse fail(SQLException e) {
            return new BPSQLResponse(false, e.getMessage());
        }
    }
}