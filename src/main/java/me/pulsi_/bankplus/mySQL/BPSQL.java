package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankGuiRegistry;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BPSQL {

    private final BankGuiRegistry bankRegistry;
    private final BPSQLMethods sqlMethods;
    private String username, password, url;
    private Connection connection;

    public BPSQL() {
        sqlMethods = new BPSQLMethods(this);
        bankRegistry = BankPlus.INSTANCE.getBankGuiRegistry();
    }

    public void setupMySQL() {
        String host = Values.CONFIG.getSqlHost();
        String port = Values.CONFIG.getSqlPort();
        String database = Values.CONFIG.getSqpDatabase();
        username = Values.CONFIG.getSqlUsername();
        password = Values.CONFIG.getSqlPassword();

        url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + Values.CONFIG.isSqlUseSSL();

        BPLogger.info("MySQL setup finished!");
    }

    public boolean isPlayerRegistered(OfflinePlayer p) {
        String uuid = p.getUniqueId().toString();
        for (String bankName : bankRegistry.getBanks().keySet())
            if (getSqlMethods().exist(bankName, "uuid", uuid)) return true;
        return false;
    }

    public void registerPlayer(OfflinePlayer p) {
        String uuid = p.getUniqueId().toString();
        BPEconomy economy = BankPlus.getBPEconomy();

        for (String bankName : bankRegistry.getBanks().keySet()) {
            if (!getSqlMethods().exist(bankName, "uuid", uuid))
                getSqlMethods().insertInto(
                        bankName,
                        uuid,
                        p.getName(),
                        "" + new BankManager().getCurrentLevel(p),
                        Values.CONFIG.getStartAmount().toString(),
                        economy.getOfflineInterest(p).toString(),
                        economy.getDebts(p).toString()
                );
        }
    }

    public void setupTables() {
        for (String bankName : bankRegistry.getBanks().keySet())
           getSqlMethods().createTable(
                   bankName,
                   "uuid varchar(100)",
                   "account_name varchar(100)",
                   "bank_level varchar(100)",
                   "money varchar(100)",
                   "interest varchar(100)",
                   "debt varchar(100)",
                   "PRIMARY KEY (uuid)"
           );
    }

    public boolean isConnected() {
        return connection != null;
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