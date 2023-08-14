package me.pulsi_.bankplus.loanSystem;

import me.pulsi_.bankplus.values.Values;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;

public class BPLoan {

    private final OfflinePlayer sender, target;
    private final BigDecimal moneyGiven, moneyToReturn;
    private final String fromBankName, toBankName;
    private final int instalments;

    public BukkitTask task;
    public long timeLeft;
    public int instalmentPoint;

    public BPLoan(OfflinePlayer sender, OfflinePlayer target, BigDecimal amount, String fromBankName, String toBankName) {
        this.sender = sender;
        this.target = target;
        this.moneyGiven = amount;
        this.moneyToReturn = amount.add(amount.divide(BigDecimal.valueOf(100)).multiply(Values.CONFIG.getLoanInterest()));
        this.fromBankName = fromBankName;
        this.toBankName = toBankName;
        this.instalments = Values.CONFIG.getLoanInstalments();
    }

    public OfflinePlayer getSender() {
        return sender;
    }

    public OfflinePlayer getTarget() {
        return target;
    }

    public BigDecimal getMoneyGiven() {
        return moneyGiven;
    }

    public BigDecimal getMoneyToReturn() {
        return moneyToReturn;
    }

    public String getFromBankName() {
        return fromBankName;
    }

    public String getToBankName() {
        return toBankName;
    }

    public int getInstalments() {
        return instalments;
    }
}