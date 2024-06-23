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

    public int getLevel(String bankName) {
        String level = get(bankName, "bank_level");
        return Integer.parseInt(level == null ? "1" : level);
    }

    public BigDecimal getMoney(String bankName) {
        String money = get(bankName, "money");
        return new BigDecimal(money == null ? "0" : money);
    }

    public BigDecimal getDebt(String bankName) {
        String debt = get(bankName, "debt");
        return new BigDecimal(debt == null ? "0" : debt);
    }

    public BigDecimal getOfflineInterest(String bankName) {
        String interest = get(bankName, "interest");
        return new BigDecimal(interest == null ? "0" : interest);
    }

    public void setLevel(int level, String bankName) {
        set("bank_level", "" + Math.max(1, level), bankName);
    }

    public void setMoney(BigDecimal amount, String bankName) {
        set("money", BPFormatter.styleBigDecimal(amount.max(BigDecimal.ZERO)), bankName);
    }

    public void setDebt(BigDecimal amount, String bankName) {
        set("debt", BPFormatter.styleBigDecimal(amount.max(BigDecimal.ZERO)), bankName);
    }

    public void setOfflineInterest(BigDecimal amount, String bankName) {
        set("interest", BPFormatter.styleBigDecimal(amount.max(BigDecimal.ZERO)), bankName);
    }

    public void set(String valueName, String newValue, String bankName) {
        if (resultSet(bankName, valueName).isEmpty()) bpsql.createNewDefault(bankName, p);
        if (ConfigValues.isStoringUUIDs()) methods.update(bankName, valueName, newValue, "WHERE uuid='" + uuid + "'");
        else  methods.update(bankName, valueName, newValue, "WHERE account_name='" + p.getName() + "'");
    }

    public String get(String bankName, String valueName) {
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