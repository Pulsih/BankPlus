package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;

import java.sql.*;

public class BPSQL {

    private String host, port, database, username, password, url;
    private boolean useSSL;

    private Connection connection;

    public void setupMySQL() {
        host = Values.CONFIG.getSqlHost();
        port = Values.CONFIG.getSqlPort();
        database = Values.CONFIG.getSqpDatabase();
        username = Values.CONFIG.getSqlUsername();
        password = Values.CONFIG.getSqlPassword();
        useSSL = Values.CONFIG.isSqlUseSSL();

        url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL;
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void connect() {
        if (isConnected()) return;
        try {
            connection = DriverManager.getConnection(url, username, password);
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
}