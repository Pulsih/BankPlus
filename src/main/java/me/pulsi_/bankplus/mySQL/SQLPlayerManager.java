package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.UUID;

public class SQLPlayerManager {

    private final String uuid;
    private final OfflinePlayer p;

    public SQLPlayerManager(OfflinePlayer p) {
        this(p.getUniqueId());
    }

    public SQLPlayerManager(UUID uuid) {
        this.p = Bukkit.getOfflinePlayer(uuid);
        this.uuid = uuid.toString();
    }

    /**
     * Get the debt value from the specified bank table.
     *
     * @param bankName The name of the MySql table.
     * @return The debt as BigDecimal.
     */
    public BigDecimal getDebt(String bankName) {
        return new BigDecimal(get(bankName, "debt", "0"));
    }

    /**
     * Get the money value from the specified bank table.
     *
     * @param bankName The name of the MySql table.
     * @return The money as BigDecimal.
     */
    public BigDecimal getMoney(String bankName) {
        return new BigDecimal(get(bankName, "money", "0"));
    }

    /**
     * Get the level value from the specified bank table.
     *
     * @param bankName The name of the MySql table.
     * @return The level as BigDecimal.
     */
    public int getLevel(String bankName) {
        return Integer.parseInt(get(bankName, "bank_level", "1"));
    }

    /**
     * Get the interest value from the specified bank table.
     *
     * @param bankName The name of the MySql table.
     * @return The interest as BigDecimal.
     */
    public BigDecimal getOfflineInterest(String bankName) {
        return new BigDecimal(get(bankName, "interest", "0"));
    }

    /**
     * Update the player database values, if something goes wrong it won't save the data and will return false.
     *
     * @param bankName        The bank table name.
     * @param debt            Player's debt.
     * @param balance         Player's balance.
     * @param level           Player's level.
     * @param offlineInterest Player's offline interest.
     */
    public void updatePlayer(String bankName, BigDecimal debt, BigDecimal balance, int level, BigDecimal offlineInterest) {
        BPSQL.registerPlayer(p);

        String check;
        if (ConfigValues.isStoringUUIDs()) check = "uuid='" + uuid + "'";
        else check = "account_name='" + p.getName() + "'";

        String update = "UPDATE " + bankName + " SET " +
                "debt='" + BPFormatter.styleBigDecimal(debt) + "'," +
                "money='" + BPFormatter.styleBigDecimal(balance) + "'," +
                "bank_level='" + Math.max(1, level) + "'," +
                "interest='" + BPFormatter.styleBigDecimal(offlineInterest) + "' " +
                "WHERE " + check;

        BPSQL.BPSQLResponse response = BPSQL.update(update);
        if (!response.success) BPLogger.Console.warn("Could not update values of player \"" + p.getName() + "\", reason: " + response.errorMessage);
    }

    /**
     * Get the specified value from the given bank table, with a fall-back value in case the value is not available.
     *
     * @param bankName  The bank where to get the information.
     * @param valueName The identifier of the value to get.
     * @return The value, always non-null, or null if there is a problem with the database.
     */
    private String get(String bankName, String valueName, String fallBack) {
        String check;
        if (ConfigValues.isStoringUUIDs()) check = "uuid='" + p.getUniqueId() + "'";
        else check = "account_name='" + p.getName() + "'";

        String query = "SELECT " + valueName + " FROM " + bankName + " WHERE " + check;

        BPSQL.BPSQLResponse response = BPSQL.query(query, valueName);
        if (response.success) return response.result.isEmpty() ? fallBack : response.result.getFirst();
        else {
            BPLogger.Console.warn("Cannot get \"" + valueName + "\" value from \"" + bankName + "\" table for player \"" + p.getName() + ", using \"" + fallBack + "\" as fall-back, reason: " + response.errorMessage);
            return fallBack;
        }
    }
}