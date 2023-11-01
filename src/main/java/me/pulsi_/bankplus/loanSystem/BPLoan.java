package me.pulsi_.bankplus.loanSystem;

import me.pulsi_.bankplus.values.Values;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;

public class BPLoan {

    private OfflinePlayer sender, receiver;
    private final String fromBankName, toBankName, requestedBank;
    private BigDecimal moneyGiven, moneyToReturn;
    private BukkitTask task;
    private long timeLeft;
    private int instalments, instalmentsPoint;

    // Used for loans from player to player
    public BPLoan(OfflinePlayer sender, OfflinePlayer receiver, BigDecimal amount, String fromBankName, String toBankName) {
        this.sender = sender;
        this.receiver = receiver;
        this.moneyGiven = amount;
        this.moneyToReturn = amount.add(amount.divide(BigDecimal.valueOf(100)).multiply(Values.CONFIG.getLoanInterest()));
        this.fromBankName = fromBankName;
        this.toBankName = toBankName;
        this.instalments = Values.CONFIG.getLoanInstalments();
        this.requestedBank = null;
    }

    // Used for loans from player to bank
    public BPLoan(OfflinePlayer receiver, String bank, BigDecimal amount) {
        this.sender = null; // The sender would be the bank.
        this.receiver = receiver;
        this.moneyGiven = amount;
        this.moneyToReturn = amount.add(amount.divide(BigDecimal.valueOf(100)).multiply(Values.CONFIG.getLoanInterest()));
        this.fromBankName = null;
        this.toBankName = null;
        this.instalments = Values.CONFIG.getLoanInstalments();
        this.requestedBank = bank;
    }

    public BPLoan(OfflinePlayer sender, OfflinePlayer receiver, String fromBankName, String toBankName) {
        this.sender = sender;
        this.receiver = receiver;
        this.fromBankName = fromBankName;
        this.toBankName = toBankName;
        this.requestedBank = null;
    }

    public BPLoan(OfflinePlayer receiver, String requestedBank) {
        this.sender = null;
        this.receiver = receiver;
        this.fromBankName = null;
        this.toBankName = null;
        this.requestedBank = requestedBank;
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

    public String getRequestedBank() {
        return requestedBank;
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

    public void setSender(OfflinePlayer sender) {
        this.sender = sender;
    }

    public void setReceiver(OfflinePlayer receiver) {
        this.receiver = receiver;
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