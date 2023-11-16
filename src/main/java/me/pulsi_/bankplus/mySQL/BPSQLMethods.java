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
        StringBuilder argsString;
        for (int i = 0; i < args.length; i++) {

        }
        try {
            PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (" + args + ")");
            statement.executeUpdate();
        } catch (SQLException e) {
            BPLogger.error(e, "Could not create the table \"" + tableName + "\"!");
        }
    }

    public void deleteTable(String tableName) {
        if (!isConnected()) return;
        try {
            PreparedStatement statement = connection.prepareStatement("DROP TABLE " + tableName);
            statement.executeUpdate();
        } catch (SQLException e) {
            BPLogger.error(e, "Could not create the table \"" + tableName + "\"!");
        }
    }

    public void insertInto(String tableName, String args, String... values) {
        if (!isConnected()) return;

        StringBuilder valuesString = new StringBuilder("?");
        if (values.length > 1)
            for (int i = 1; i < values.length; i++) valuesString.append(",?");

        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tableName + " (" + args + ") VALUES (" + valuesString + ")");
            statement.executeUpdate();
        } catch (SQLException e) {
            BPLogger.error(e, "Could not create the table \"" + tableName + "\"!");
        }
    }

    public void removeFrom(String tableName, String args) {
        if (!isConnected()) return;
        try {
            PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (" + args + ")");
            statement.executeUpdate();
        } catch (SQLException e) {
            BPLogger.error(e, "Could not create the table \"" + tableName + "\"!");
        }
    }

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