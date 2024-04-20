package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
        String database = Values.CONFIG.getSqlDatabase();
        username = Values.CONFIG.getSqlUsername();
        password = Values.CONFIG.getSqlPassword();

        url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + Values.CONFIG.isSqlUseSSL();

        BPLogger.info("MySQL setup finished!");
    }

    public boolean isPlayerRegistered(OfflinePlayer p) {
        boolean registered = false, uuid = Values.CONFIG.isStoringUUIDs();
        for (String bankName : BPEconomy.nameList()) {
            if (uuid) {
                if (!getSqlMethods().get(bankName, "uuid", "WHERE uuid='" + p.getUniqueId() + "'").isEmpty()) registered = true;
                else createNewDefault(bankName, p);
            } else {
                if (!getSqlMethods().get(bankName, "account_name", "WHERE account_name='" + p.getName() + "'").isEmpty()) registered = true;
                else createNewDefault(bankName, p);
            }
        }
        return registered;
    }

    public void createNewDefault(String bankName, OfflinePlayer p) {
        getSqlMethods().insertInto(
                bankName,
                p.getUniqueId().toString(),
                p.getName(),
                "1",
                BPFormatter.styleBigDecimal(Values.CONFIG.getStartAmount()),
                "0",
                "0"
        );
    }

    public void setupTables() {
        for (String bankName : BPEconomy.nameList())
           getSqlMethods().createTable(
                   bankName,
                   "uuid varchar(255)",
                   "account_name varchar(255)",
                   "bank_level varchar(255)",
                   "money varchar(255)",
                   "interest varchar(255)",
                   "debt varchar(255)",
                   "PRIMARY KEY (uuid)"
           );
    }

    public boolean isConnected() {
        return Values.CONFIG.isSqlEnabled() && connection != null;
    }

    public void connect() {
        if (isConnected()) return;
        try {
            connection = DriverManager.getConnection(url, username, password);
            sqlMethods.connectToDatabase();
            BPLogger.info("Database successfully connected!");
        } catch (SQLException e) {
            BPLogger.warn(e, "Could not connect bankplus to it's database!");
        }
    }

    public void disconnect() {
        if (!isConnected()) return;
        try {
            connection.close();
            connection = null;
            BPLogger.info("Database successfully disconnected!");
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