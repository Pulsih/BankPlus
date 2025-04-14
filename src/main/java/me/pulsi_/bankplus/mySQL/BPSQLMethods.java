package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.utils.BPLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BPSQLMethods {

    private final BPSQL sql;
    private Connection connection;

    public BPSQLMethods(BPSQL sql) {
        this.sql = sql;
    }

    /**
     * Allow to process the task after doing the necessary calculations.
     *
     * @param task The MySQL task to run.
     */
    private SQLResponse process(SQLTask task, String errorMessage) {
        // Theoretically, this method will only be called when MySQL
        // is enabled, if the connection is null it means that this
        // class has not been connected yet.
        if (connection == null) {
            BPLogger.warn("Could not process database request because bankplus isn't connected to the database yet!");
            return SQLResponse.fail();
        }

        try {
            if (connection.isClosed()) {
                sql.connect(); // Update the SQL origin connection.
                connectToDatabase(); // Re-assign the new connection.
                if (connection == null) return SQLResponse.fail();

                if (connection.isClosed()) { // If the new connection is closed or null, warn about it.
                    BPLogger.warn("Could not update database, BankPlus tried to re-connect to it but it was closed.");
                    return SQLResponse.fail();
                }
            }
        } catch (SQLException e) {
            BPLogger.warn("Could not update database, BankPlus tried to re-connect to it but it was closed.");
            return SQLResponse.fail();
        }

        try {
            return SQLResponse.success(task.run());
        } catch (SQLException e) {
            BPLogger.error(e, errorMessage);
            return SQLResponse.fail();
        }
    }

    /**
     * Create a new table.
     *
     * @param tableName The name of the table you want to create.
     * @param args      The columns of the table. Format: argumentName argumentType(optionalArgs) | uuid varchar(255)
     */
    public SQLResponse createTable(String tableName, String... args) {
        return process(() -> {
            StringBuilder argsString = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                argsString.append(args[i]);
                if (i + 1 < args.length) argsString.append(",");
            }

            connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (" + argsString + ")").executeUpdate();
            return null;
        }, "Could not create the table \"" + tableName + "\"!");
    }

    /**
     * Delete a database table.
     *
     * @param tableName The table to delete.
     */
    public SQLResponse deleteTable(String tableName) {
        return process(
                () -> {
                    connection.prepareStatement("DROP TABLE " + tableName).executeUpdate();
                    return null;
                }, "Could not delete the table \"" + tableName + "\"!"
        );
    }

    /**
     * Insert a new non-existing element in the selected table.
     * <p>
     * Make sure to put the values in the same order of the database.
     *
     * @param tableName The selected table.
     * @param values    The values of the element in the same order of the table args.
     */
    public SQLResponse insertInto(String tableName, String... values) {
        return process(() -> {
            StringBuilder valuesString = new StringBuilder("?");
            if (values.length > 1)
                valuesString.append(",?".repeat(values.length - 1));

            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tableName + " VALUES (" + valuesString + ")");
            for (int i = 1; i <= values.length; i++)
                statement.setString(i, values[i - 1]);

            statement.executeUpdate();
            return null;
        }, "Could not insert the values in the table \"" + tableName + "\"!");
    }

    /**
     * Update an existing values in a column of a table.
     * <p>
     *
     * @param tableName The table name.
     * @param values    The new values to set. Format: ColumnName='NewValue', ColumnName='NewValue'...
     */
    public SQLResponse update(String tableName, String values) {
        return update(tableName, null, values);
    }

    /**
     * Update an existing values in a column of a table.
     * <p>
     *
     * @param tableName The table name.
     * @param values    The new values to set. Format: ColumnName='NewValue', ColumnName='NewValue'...
     * @param condition A condition that must be met to update the column. Example: "WHERE uuid=playerUUID"
     */
    public SQLResponse update(String tableName, String condition, String values) {
        return process(() -> {
            String check = condition != null ? " " + condition : "";
            PreparedStatement statement = connection.prepareStatement("UPDATE " + tableName + " SET " + values + check);
            statement.executeUpdate();
            return null;
        }, "Could not update the table \"" + tableName + "\"!");
    }

    /**
     * Check if the specified table has the selected value (columnName) respecting the given condition.
     *
     * @param tableName  The table name.
     * @param columnName The name of the column from which to take the values.
     * @param condition  The condition that must be met to return as a valid result.
     * @return SQLResponse with result true if it contains, or false.
     */
    public SQLResponse has(String tableName, String columnName, String condition) {
        return process(() -> {
            List<String> result = new ArrayList<>();
            String check = condition != null ? " " + condition : "";
            PreparedStatement statement = connection.prepareStatement("SELECT " + columnName + " FROM " + tableName + check);

            ResultSet set = statement.executeQuery();
            while (set.next()) result.add(set.getString(columnName));
            return !result.isEmpty();
        }, "Could not get the value \"" + columnName + "\" from the table \"" + tableName + "\"!");
    }

    /**
     * Get a result of values from the selected columns of that table.
     *
     * @param tableName  The table name.
     * @param columnName The name of the column from which to take the values.
     * @return A list of values.
     */
    public SQLResponse get(String tableName, String columnName) {
        return get(tableName, columnName, null);
    }

    /**
     * Get a result of values from the selected column of that table.
     *
     * @param tableName  The table name.
     * @param columnName The name of the column from which to take the values.
     * @param condition  The condition that must be met to return as a valid result.
     * @return A list of values. (List<String>)
     */
    public SQLResponse get(String tableName, String columnName, String condition) {
        return process(() -> {
            List<String> result = new ArrayList<>();
            String check = condition != null ? " " + condition : "";
            PreparedStatement statement = connection.prepareStatement("SELECT " + columnName + " FROM " + tableName + check);

            ResultSet set = statement.executeQuery();
            while (set.next()) result.add(set.getString(columnName));
            return result;
        }, "Could not get the value \"" + columnName + "\" from the table \"" + tableName + "\"!");
    }

    /**
     * Remove an element from the table.
     *
     * @param tableName  The table name.
     * @param columnName The name of the column to check in.
     * @param valueName  The name that has to be removed from the table.
     */
    public SQLResponse removeFrom(String tableName, String columnName, String valueName) {
        return process(
                () -> connection.prepareStatement("REMOVE FROM " + tableName + " WHERE " + columnName + "=" + valueName).executeUpdate()
                , "Could not remove the value \"" + valueName + "\" from the table \"" + tableName + "\"!"
        );
    }

    /**
     * Assign the database connection when the database has been connected successfully.
     */
    public void connectToDatabase() {
        connection = sql.getConnection();
    }

    public static class SQLResponse {
        public boolean success = false; // Returns true if everything went as expected, false otherwise.
        public Object result = null; // Return the result of the task if available, null otherwise.

        public static SQLResponse fail() {
            return new SQLResponse();
        }

        public static SQLResponse success() {
            SQLResponse response = new SQLResponse();
            response.result = true;
            return response;
        }

        public static SQLResponse success(Object result) {
            SQLResponse response = success();
            response.result = result;
            return response;
        }
    }

    private interface SQLTask {
        Object run() throws SQLException;
    }
}