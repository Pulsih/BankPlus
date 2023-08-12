package me.pulsi_.bankplus.loanSystem;

import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class BPLoan {

    private final Player target;
    private final BigDecimal amount;
    private final String fromBankName, toBankName;

    public BPLoan(Player target, BigDecimal amount, String fromBankName, String toBankName) {
        this.target = target;
        this.amount = amount;
        this.fromBankName = fromBankName;
        this.toBankName = toBankName;
    }

    public Player getTarget() {
        return target;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getFromBankName() {
        return fromBankName;
    }

    public String getToBankName() {
        return toBankName;
    }
}