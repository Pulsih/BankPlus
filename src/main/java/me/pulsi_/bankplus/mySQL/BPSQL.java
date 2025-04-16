package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

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

    private final BPSQLMethods sqlMethods;
    private String username, password, url;
    private Connection connection;

    public BPSQL() {
        sqlMethods = new BPSQLMethods(this);
    }

    public void setupMySQL() {
        String host = ConfigValues.getSqlHost();
        String port = ConfigValues.getSqlPort();
        String database = ConfigValues.getSqlDatabase();
        username = ConfigValues.getSqlUsername();
        password = ConfigValues.getSqlPassword();

        url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + ConfigValues.isSqlUsingSSL();

        BPLogger.info("MySQL setup finished!");
    }

    public void fillEmptyRecords(OfflinePlayer p) {
        boolean storeWithUUID = ConfigValues.isStoringUUIDs();
        UUID uuid = p.getUniqueId();
        String name = p.getName();

        for (String bankName : BPEconomy.nameList()) {
            String v, check;
            if (storeWithUUID) {
                v = "uuid";
                check = "WHERE uuid='" + uuid + "'";
            } else {
                v = "account_name";
                check = "WHERE account_name='" + name + "'";
            }

            Object bool = getSqlMethods().has(bankName, v, check).result;
            if (bool == null) {
                BPLogger.warn("Could not check if player \"" + name + "\" is registered to the database. (Probably no connection or timeout)");
                return;
            }
            if (!(boolean) bool) createNewDefault(bankName, p);
        }
    }

    public void createNewDefault(String bankName, OfflinePlayer p) {
        getSqlMethods().insertInto(
                bankName,
                p.getUniqueId().toString(),
                p.getName(),
                "1",
                BPFormatter.styleBigDecimal(ConfigValues.getStartAmount()),
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

    /**
     * Creates a new instance of the connection and assign it.
     * This method also enables the SQLMethods.
     */
    public void connect() {
        try {
            connection = DriverManager.getConnection(url, username, password);
            sqlMethods.connectToDatabase();
            BPLogger.info("Database successfully connected!");
        } catch (SQLException e) {
            BPLogger.warn(e, "Could not connect bankplus to it's database!");
        }
    }

    /**
     * Disconnect the database.
     */
    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
                BPLogger.info("Database successfully disconnected!");
            }
        } catch (SQLException e) {
            BPLogger.warn(e, "Could not disconnect bankplus from his database!");
        }
    }

    /**
     * Check if the connection to the database is present and isn't closed.
     * @return true if it's correctly connected, false otherwise.
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public BPSQLMethods getSqlMethods() {
        return sqlMethods;
    }
}