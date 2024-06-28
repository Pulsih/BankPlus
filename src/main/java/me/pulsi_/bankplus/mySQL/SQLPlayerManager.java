package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class SQLPlayerManager {

    private final String uuid;
    private final OfflinePlayer p;
    private final BPSQLMethods methods;
    private final BPSQL bpsql;

    public SQLPlayerManager(OfflinePlayer p) {
        this(p.getUniqueId());
    }

    public SQLPlayerManager(UUID uuid) {
        this.p = Bukkit.getOfflinePlayer(uuid);
        this.uuid = uuid.toString();
        this.bpsql = BankPlus.INSTANCE().getMySql();
        this.methods = bpsql.getSqlMethods();
    }

    /**
     * Get the selected bank level.
     * @param bankName The bank table name.
     * @return An integer.
     */
    public int getLevel(String bankName) {
        String level = get(bankName, "bank_level");
        try {
            return Integer.parseInt(level);
        } catch (Throwable t) {
            return 1;
        }
    }

    /**
     * Get the selected bank balance.
     * @param bankName The bank table name.
     * @return A BigDecimal.
     */
    public BigDecimal getMoney(String bankName) {
        String money = get(bankName, "money");
        return BPFormatter.getStyledBigDecimal(money);
    }

    /**
     * Get the selected bank debt.
     * @param bankName The bank table name.
     * @return A BigDecimal.
     */
    public BigDecimal getDebt(String bankName) {
        String debt = get(bankName, "debt");
        return BPFormatter.getStyledBigDecimal(debt);
    }

    /**
     * Get the selected offline interest.
     * @param bankName The bank table name.
     * @return A BigDecimal.
     */
    public BigDecimal getOfflineInterest(String bankName) {
        String interest = get(bankName, "interest");
        return BPFormatter.getStyledBigDecimal(interest);
    }

    /**
     * Update the player database values.
     *
     * @param bankName        The bank table name.
     * @param debt            Player's debt.
     * @param balance         Player's balance.
     * @param level           Player's level.
     * @param offlineInterest Player's offline interest.
     */
    public void updatePlayer(String bankName, BigDecimal debt, BigDecimal balance, int level, BigDecimal offlineInterest) {
        bpsql.fillEmptyRecords(p);

        String condition;
        if (ConfigValues.isStoringUUIDs()) condition = "WHERE uuid='" + uuid + "'";
        else condition = "WHERE account_name='" + p.getName() + "'";

        methods.update(
                bankName,
                condition,
                "debt='" + BPFormatter.styleBigDecimal(debt) + "', " +
                        "money='" + BPFormatter.styleBigDecimal(balance) + "', " +
                        "bank_level='" + Math.max(1, level) + "', " +
                        "interest='" + BPFormatter.styleBigDecimal(offlineInterest) + "'"
        );
    }

    private String get(String bankName, String valueName) {
        List<String> result = resultSet(bankName, valueName);
        if (result.isEmpty()) return null;

        return result.get(0);
    }

    private List<String> resultSet(String bankName, String valueName) {
        List<String> result;
        if (ConfigValues.isStoringUUIDs()) result = methods.get(bankName, valueName, "WHERE uuid='" + uuid + "'");
        else result = methods.get(bankName, valueName, "WHERE account_name='" + p.getName() + "'");
        return result;
    }
}