package me.pulsi_.bankplus.mySQL;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class SQLPlayerManager {

    private final String uuid;
    private final OfflinePlayer p;
    private final BPSQLMethods methods;

    public SQLPlayerManager(OfflinePlayer p) {
        this.p = p;
        this.uuid = p.getUniqueId().toString();
        this.methods = BankPlus.INSTANCE.getSql().getSqlMethods();
    }

    public void setMoney(BigDecimal amount, String bankName) {
        methods.update(bankName, "uuid", uuid, "money", amount.toString());
    }

    public BigDecimal getMoney(String bankName) {
        return new BigDecimal(methods.get(bankName, "uuid", uuid, "money"));
    }

    public void setDebt(BigDecimal amount, String bankName) {
        methods.update(bankName, "uuid", uuid, "debt", amount.toString());
    }

    public BigDecimal getDebt(String bankName) {
        return new BigDecimal(methods.get(bankName, "uuid", uuid, "debt"));
    }

    public void saveBankBalance(BigDecimal money, String bankName) {
        if (!methods.exist(bankName, "uuid", uuid)) methods.insertInto(bankName, uuid, p.getName());
        else methods.update(bankName, uuid, p.getName(), "money", money.toString());
    }
}