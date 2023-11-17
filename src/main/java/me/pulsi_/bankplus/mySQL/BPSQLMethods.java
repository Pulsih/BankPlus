package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.utils.BPLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
     *             For more arguments, separate them using ",".
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
            BPLogger.error(e, "Could not create the table \"" + tableName + "\"!");
        }
    }

    /**
     * Insert the specified element in the selected table.
     * <p>
     * Make sure to put the values in the same order of the database.
     * @param tableName The selected table.
     * @param values The values of the element.
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
            BPLogger.error(e, "Could not create the table \"" + tableName + "\"!");
        }
    }

    /**
     * Remove an element from the table.
     * Example: Removing from the table "test" the element that is named "TestName" in the "Names" row.
     * <p>
     * #removeFrom("test", "Names", "TestName");
     * @param tableName The name of the table to remove the elemt.
     * @param argumentName The name of the argument to check in [identifier].
     * @param identifier The name that has to be removed from the table.
     */
    public void removeFrom(String tableName, String argumentName, String identifier) {
        if (!isConnected()) return;
        try {
            connection.prepareStatement("REMOVE FROM " + tableName + " WHERE " + argumentName + "=" + identifier).executeUpdate();
        } catch (SQLException e) {
            BPLogger.error(e, "Could not create the table \"" + tableName + "\"!");
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