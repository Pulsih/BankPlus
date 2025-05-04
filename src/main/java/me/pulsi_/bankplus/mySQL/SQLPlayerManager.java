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
     * Get a PlayerResult in the specified bank.
     *
     * @param bankName The bank identifier where to get the information.
     * @return The PlayerResult.
     */
    public PlayerResult getPlayerResult(String bankName) {
        return new PlayerResult(p, bankName);
    }

    /**
     * Update the player database values, if something goes wrong it won't save the data and will return false.
     *
     * @param bankName        The bank table name.
     * @param debt            Player's debt.
     * @param balance         Player's balance.
     * @param level           Player's level.
     * @param offlineInterest Player's offline interest.
     * @return true on successful load, false otherwise.
     */
    public boolean updatePlayer(String bankName, BigDecimal debt, BigDecimal balance, int level, BigDecimal offlineInterest) {
        bpsql.registerPlayer(p);

        String condition;
        if (ConfigValues.isStoringUUIDs()) condition = "WHERE uuid='" + uuid + "'";
        else condition = "WHERE account_name='" + p.getName() + "'";

        return methods.update(
                bankName,
                condition,
                "debt='" + debt.toPlainString() + "', " +
                        "money='" + balance.toPlainString() + "', " +
                        "bank_level='" + Math.max(1, level) + "', " +
                        "interest='" + offlineInterest.toPlainString() + "'"
        ).success;
    }

    /**
     * Class that loads all 4 different information about the specified player from the MySQL database:
     * - Bank Level
     * - Bank Money
     * - Bank Debt
     * - Offline Interest
     * <p>
     * If something went wrong with the loading from the database, the value
     * "success" will return false, useful to manage various scenarios.
     */
    public static class PlayerResult {

        public OfflinePlayer p;
        public BigDecimal money, debt, offlineInterest;
        public int bankLevel;

        private boolean success = true;

        public PlayerResult(OfflinePlayer p, String bankName) {
            this.p = p;

            String money = get(bankName, "money");
            if (money == null) success = false;
            this.money = BPFormatter.getStyledBigDecimal(money);

            String debt = get(bankName, "debt");
            if (debt == null) success = false;
            this.debt = BPFormatter.getStyledBigDecimal(debt);

            String interest = get(bankName, "interest");
            if (interest == null) success = false;
            this.offlineInterest = BPFormatter.getStyledBigDecimal(interest);

            String level = get(bankName, "bank_level");
            try {
                this.bankLevel = Math.max(1, Integer.parseInt(level));
            } catch (Throwable t) {
                this.bankLevel = 1;
            }
        }

        /**
         * Check if the loading of the values from the MySQL database has finished without problems.
         *
         * @return true if there are no problems, false if something went wrong.
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Get the first value of the set get from the database. (Used to get single values such as money, level or other information)
         *
         * @param bankName  The bank where to get the information.
         * @param valueName The identifier of the value to get.
         * @return The value, always non-null, or null if there is a problem with the database.
         */
        private String get(String bankName, String valueName) {
            BPSQLMethods methods = BankPlus.INSTANCE().getMySql().getSqlMethods();

            BPSQLMethods.SQLResponse response;
            if (ConfigValues.isStoringUUIDs())
                response = methods.get(bankName, valueName, "WHERE uuid='" + p.getUniqueId() + "'");
            else response = methods.get(bankName, valueName, "WHERE account_name='" + p.getName() + "'");

            if (!response.success) return null;
            else {
                Object result = response.result;
                return result == null ? "" : ((List<String>) result).getFirst();
            }
        }
    }
}