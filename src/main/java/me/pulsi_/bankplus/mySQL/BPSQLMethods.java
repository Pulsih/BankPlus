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
     * Create a new table.
     * @param tableName The name of the table you want to create.
     * @param args The columns of the table. Format: argumentName argumentType(optionalArgs) | uuid varchar(255)
     */
    public void createTable(String tableName, String... args) {
        if (!isConnected()) return;

        StringBuilder argsString = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            argsString.append(args[i]);
            if (i + 1 < args.length) argsString.append(",");
        }

        try {
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (" + argsString + ")").executeUpdate();
        } catch (SQLException e) {
            BPLogger.error(e, "Could not create the table \"" + tableName + "\"!");
        }
    }

    /**
     * Delete a database table.
     * @param tableName The table to delete.
     */
    public void deleteTable(String tableName) {
        if (!isConnected()) return;

        try {
            connection.prepareStatement("DROP TABLE " + tableName).executeUpdate();
        } catch (SQLException e) {
            BPLogger.error(e, "Could not delete the table \"" + tableName + "\"!");
        }
    }

    /**
     * Insert a new non-existing element in the selected table.
     * <p>
     * Make sure to put the values in the same order of the database.
     * @param tableName The selected table.
     * @param values The values of the element in the same order of the table args.
     */
    public void insertInto(String tableName, String... values) {
        if (!isConnected()) return;

        StringBuilder valuesString = new StringBuilder("?");
        if (values.length > 1)
            for (int i = 1; i < values.length; i++) valuesString.append(",?");

        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tableName + " VALUES (" + valuesString + ")");
            for (int i = 1; i <= values.length; i++)
                statement.setString(i, values[i - 1]);

            statement.executeUpdate();
        } catch (SQLException e) {
            BPLogger.error(e, "Could not insert the values in the table \"" + tableName + "\"!");
        }
    }

    /**
     * Update an existing value in a column of a table.
     * <p>
     * @param tableName The table name.
     * @param columnName The name of the columns where to update the value.
     * @param value The new value to set.
     */
    public void update(String tableName, String columnName, String value) {
        update(tableName, columnName, value, null);
    }

    /**
     * Update an existing value in a column of a table.
     * <p>
     * @param tableName The table name.
     * @param columnName The name of the columns where to update the value.
     * @param value The new value to set.
     * @param condition A condition that must be met to update the column. Example: "WHERE uuid=playerUUID"
     */
    public void update(String tableName, String columnName, String value, String condition) {
        if (!isConnected()) return;

        try {
            String check = condition != null ? " " + condition : "";

            PreparedStatement statement = connection.prepareStatement("UPDATE " + tableName + " SET " + columnName + "=?" + check);
            statement.setString(1, value);

            statement.executeUpdate();
        } catch (SQLException e) {
            BPLogger.error(e, "Could not update the column \"" + columnName + "\" of the table \"" + tableName + "\"!");
        }
    }

    /**
     * Get a result of values from the selected columns of that table.
     *
     * @param tableName The table name.
     * @param columnName The name of the column from which to take the values.
     * @return A list of values.
     */
    public List<String> get(String tableName, String columnName) {
        return get(tableName, columnName, null);
    }

    /**
     * Get a result of values from the selected column of that table.
     *
     * @param tableName The table name.
     * @param columnName The name of the column from which to take the values.
     * @param condition The condition that must be met to return as a valid result.
     * @return A list of values.
     */
    public List<String> get(String tableName, String columnName, String condition) {
        List<String> result = new ArrayList<>();
        if (!isConnected()) return result;

        try {
            String check = condition != null ? " " + condition : "";
            PreparedStatement statement = connection.prepareStatement("SELECT " + columnName + " FROM " + tableName + check);

            ResultSet set = statement.executeQuery();
            while (set.next()) result.add(set.getString(columnName));
        } catch (SQLException e) {
            BPLogger.error(e, "Could not get the value \"" + columnName + "\" from the table \"" + tableName + "\"!");
        }
        return result;
    }

    /**
     * Remove an element from the table.
     *
     * @param tableName The table name.
     * @param columnName The name of the column to check in.
     * @param valueName The name that has to be removed from the table.
     */
    public void removeFrom(String tableName, String columnName, String valueName) {
        if (!isConnected()) return;

        try {
            connection.prepareStatement("REMOVE FROM " + tableName + " WHERE " + columnName + "=" + valueName).executeUpdate();
        } catch (SQLException e) {
            BPLogger.error(e, "Could not remove the value \"" + valueName + "\" from the table \"" + tableName + "\"!");
        }
    }

    /**
     * Assign the database connection when the database has been connected successfully.
     */
    public void connectToDatabase() {
        connection = sql.getConnection();
    }

    private boolean isConnected() {
        if (connection == null) {
            BPLogger.warn("Could not process database request because bankplus isn't connected to the database yet!");
            return false;
        }
        return true;
    }
}