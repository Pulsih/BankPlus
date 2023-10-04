package me.pulsi_.bankplus.loanSystem;

import me.pulsi_.bankplus.values.Values;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;

public class BPLoan {

    private final OfflinePlayer sender, receiver;
    private final String fromBankName, toBankName;

    private BigDecimal moneyGiven, moneyToReturn;
    private BukkitTask task;
    private long timeLeft;
    private int instalments, instalmentsPoint;

    public BPLoan(OfflinePlayer sender, OfflinePlayer receiver, BigDecimal amount, String fromBankName, String toBankName) {
        this.sender = sender;
        this.receiver = receiver;
        this.moneyGiven = amount;
        this.moneyToReturn = amount.add(amount.divide(BigDecimal.valueOf(100)).multiply(Values.CONFIG.getLoanInterest()));
        this.fromBankName = fromBankName;
        this.toBankName = toBankName;
        this.instalments = Values.CONFIG.getLoanInstalments();
    }

    public BPLoan(OfflinePlayer sender, OfflinePlayer receiver, String fromBankName, String toBankName) {
        this.sender = sender;
        this.receiver = receiver;
        this.fromBankName = fromBankName;
        this.toBankName = toBankName;
    }

    public OfflinePlayer getSender() {
        return sender;
    }

    public OfflinePlayer getReceiver() {
        return receiver;
    }

    public String getFromBankName() {
        return fromBankName;
    }

    public String getToBankName() {
        return toBankName;
    }


    public BigDecimal getMoneyGiven() {
        return moneyGiven;
    }

    public BigDecimal getMoneyToReturn() {
        return moneyToReturn;
    }

    public BukkitTask getTask() {
        return task;
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    public int getInstalments() {
        return instalments;
    }

    public int getInstalmentsPoint() {
        return instalmentsPoint;
    }

    public void setMoneyToReturn(BigDecimal moneyToReturn) {
        this.moneyToReturn = moneyToReturn;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    public void setTimeLeft(long timeLeft) {
        this.timeLeft = timeLeft;
    }

    public void setInstalments(int instalments) {
        this.instalments = instalments;
    }

    public void setInstalmentsPoint(int instalmentsPoint) {
        this.instalmentsPoint = instalmentsPoint;
    }
}