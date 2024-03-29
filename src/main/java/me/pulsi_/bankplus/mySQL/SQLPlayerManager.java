package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPFormatter;
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
        String level = get("bank_level", bankName);
        return Integer.parseInt(level == null ? "1" : level);
    }

    public BigDecimal getMoney(String bankName) {
        String money = get("money", bankName);
        return new BigDecimal(money == null ? "0" : money);
    }

    public BigDecimal getDebt(String bankName) {
        String debt = get("debt", bankName);
        return new BigDecimal(debt == null ? "0" : debt);
    }

    public BigDecimal getOfflineInterest(String bankName) {
        String interest = get("interest", bankName);
        return new BigDecimal(interest == null ? "0" : interest);
    }

    public void setLevel(int level, String bankName) {
        set("bank_level", "" + level, bankName);
    }

    public void setMoney(BigDecimal amount, String bankName) {
        set("money", BPFormatter.formatBigDecimal(amount.max(BigDecimal.ZERO)), bankName);
    }

    public void setDebt(BigDecimal amount, String bankName) {
        set("debt", BPFormatter.formatBigDecimal(amount.max(BigDecimal.ZERO)), bankName);
    }

    public void setOfflineInterest(BigDecimal amount, String bankName) {
        set("interest", BPFormatter.formatBigDecimal(amount.max(BigDecimal.ZERO)), bankName);
    }

    public void set(String valueName, String newValue, String bankName) {
        if (methods.get(bankName, valueName, "WHERE uuid=" + uuid).isEmpty()) bpsql.createNewDefault(bankName, p);
        methods.update(bankName, valueName, newValue, "WHERE uuid=" + uuid);
    }

    public String get(String valueName, String bankName) {
        List<String> result = methods.get(bankName, valueName, "WHERE uuid=" + uuid);
        if (result.isEmpty()) return null;

        return result.get(0);
    }
}