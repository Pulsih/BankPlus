package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.utils.BPLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BPSQLMethods {

    private final BPSQL sql;
    private Connection connection;

    public BPSQLMethods(BPSQL sql) {
        this.sql = sql;
    }

    /**
     * Method used to easily create a new table.
     * @param tableName The name of the table you want to create.
     * @param args An argument is identifier as: "[ArgumentName] [<a href="https://www.w3schools.com/sql/sql_datatypes.asp">ArgumentType</a>(OptionalArgs)]".
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
     * Insert a new specified element in the selected table.
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
     * Update a value that is already present in the table.
     * <p>
     * Example: Updating the "money" value in the table "test" of the player "TestPlayer" to "500" (The column for player names is called "accountname").
     * <p>
     * #update("test", "accountname", "TestPlayer", "money", "500");
     * @param tableName The table name.
     * @param columnName The name of the table values column to compare with "elementToCheck". (Example: uuid)
     * @param elementToCheck The element to parse with the columnName.
     * @param newValue The new value to set.
     */
    public void update(String tableName, String columnName, String elementToCheck, String valueArgumentName, String newValue) {
        if (!isConnected()) return;
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + tableName + " SET " + valueArgumentName + "=? WHERE " + columnName + "=?");
            statement.setString(1, newValue);
            statement.setString(2, elementToCheck);

            statement.executeUpdate();
        } catch (SQLException e) {
            BPLogger.error(e, "Could not update the value \"" + valueArgumentName + "\" of the table \"" + tableName + "\"!");
        }
    }

    /**
     * Get a string from the selected table.
     * <p>
     * Example: Getting from the table "test" the value "money" from a player that is called "TestName" (The column for player names is called "accountname").
     * <p>
     * #get("test", "accountname", "TestName", "money");
     * @param tableName The table name.
     * @param columnName The name of the table values column to compare with "elementToCheck". (Example: UUID)
     * @param elementToCheck The element to parse with the columnName.
     * @param valueNameToGet The name of the column where to take the values.
     * @return A string or null if not found.
     */
    public String get(String tableName, String columnName, String elementToCheck, String valueNameToGet) {
        if (!isConnected()) return null;
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT " + valueNameToGet + " FROM " + tableName + " WHERE " + columnName + "=?");
            statement.setString(1, elementToCheck);
            ResultSet set = statement.executeQuery();

            return (set.next() ? set.getString(valueNameToGet) : null);
        } catch (SQLException e) {
            BPLogger.error(e, "Could not get the value \"" + valueNameToGet + "\" from the table \"" + tableName + "\"!");
            return null;
        }
    }

    /**
     * Remove an element from the table.
     * <p>
     * Example: Removing from the table "test" the element that has as name "TestName" (The column for player names is called "accountname").
     * <p>
     * #removeFrom("test", "accountname", "TestName");
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
     * Check if the selected value exists in the selected table.
     * <p>
     * Example: Check if the player named "TestName" exists in the "test" table. (The column for player names is called "accountname").
     * <p>
     * #exist("test", "accountname", "TestName");
     * @param tableName The table name.
     * @param columnName The name of the column to check in.
     * @param valueNameToCheck The value to check if it exists in the selected columnName.
     * @return true if it exists, otherwise false.
     */
    public boolean exist(String tableName, String columnName, String valueNameToCheck) {
        if (!isConnected()) return false;
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE " + columnName + "=?");
            statement.setString(1, valueNameToCheck);
            return statement.executeQuery().next();
        } catch (SQLException e) {
            BPLogger.error(e, "Could not check for existence of the value \"" + valueNameToCheck + "\" in the table \"" + tableName + "\"!");
        }
        return false;
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