package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;

import java.sql.*;

public class BPSQL {

    private final BPSQLMethods sqlMethods;
    private String username, password, url;
    private Connection connection;

    public BPSQL() {
        sqlMethods = new BPSQLMethods(this);
    }

    public void setupMySQL() {
        String host = Values.CONFIG.getSqlHost();
        String port = Values.CONFIG.getSqlPort();
        String database = Values.CONFIG.getSqpDatabase();
        username = Values.CONFIG.getSqlUsername();
        password = Values.CONFIG.getSqlPassword();

        url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + Values.CONFIG.isSqlUseSSL();
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void connect() {
        if (isConnected()) return;
        try {
            connection = DriverManager.getConnection(url, username, password);
            sqlMethods.connectToDatabase();
        } catch (SQLException e) {
            BPLogger.warn(e, "Could not connect bankplus to it's database!");
        }
    }

    public void disconnect() {
        if (!isConnected()) return;
        try {
            connection.close();
        } catch (SQLException e) {
            BPLogger.warn(e, "Could not disconnect bankplus from his database!");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public BPSQLMethods getSqlMethods() {
        return sqlMethods;
    }
}