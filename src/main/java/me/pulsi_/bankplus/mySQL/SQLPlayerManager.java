package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.UUID;

public class SQLPlayerManager {

    private final String uuid;
    private final OfflinePlayer p;
    private final BPSQLMethods methods;

    public SQLPlayerManager(OfflinePlayer p) {
        this.p = p;
        this.uuid = p.getUniqueId().toString();
        this.methods = BankPlus.INSTANCE.getSql().getSqlMethods();
    }

    public SQLPlayerManager(UUID uuid) {
        this.p = Bukkit.getOfflinePlayer(uuid);
        this.uuid = uuid.toString();
        this.methods = BankPlus.INSTANCE.getSql().getSqlMethods();
    }

    public int getLevel(String bankName) {
        String money = get("bank_level", bankName);
        return Integer.parseInt(money == null ? "1" : money);
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

    public void setLevel(int size, String bankName) {
        set("bank_level", "" + size, bankName);
    }

    public void setMoney(BigDecimal amount, String bankName) {
        set("money", amount.toString(), bankName);
    }

    public void setDebt(BigDecimal amount, String bankName) {
        set("debt", amount.toString(), bankName);
    }

    public void setOfflineInterest(BigDecimal amount, String bankName) {
        set("interest", amount.toString(), bankName);
    }

    public void set(String valueName, String newValue, String bankName) {
        if (!methods.exist(bankName, "uuid", uuid)) methods.insertInto(bankName, uuid, p.getName());
        methods.update(bankName, "uuid", uuid, valueName, newValue);
    }

    public String get(String valueName, String bankName) {
        return methods.get(bankName, "uuid", uuid, valueName);
    }
}