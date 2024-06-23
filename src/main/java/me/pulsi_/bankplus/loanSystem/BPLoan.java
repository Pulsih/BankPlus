package me.pulsi_.bankplus.loanSystem;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;

public class BPLoan {

    private OfflinePlayer sender, receiver;
    private final Bank senderBank, receiverBank, requestedBank;
    private final BigDecimal moneyGiven;
    private BigDecimal moneyToReturn;
    private BukkitTask task;
    private long timeLeft;
    private int instalments, instalmentsPoint;
    private final LoanType loanType;

    /**
     * Constructor used for loans player-to-player.
     * @param sender The loan sender.
     * @param receiver The loan receiver.
     * @param amount The loan amount.
     * @param senderBank The sender bank.
     * @param receiverBank The receiver bank.
     */
    public BPLoan(OfflinePlayer sender, OfflinePlayer receiver, BigDecimal amount, Bank senderBank, Bank receiverBank) {
        this.sender = sender;
        this.receiver = receiver;
        this.moneyGiven = amount;
        this.moneyToReturn = amount.add(amount.divide(BigDecimal.valueOf(100)).multiply(ConfigValues.getLoanInterest()));
        this.senderBank = senderBank;
        this.receiverBank = receiverBank;
        this.instalments = ConfigValues.getLoanInstalments();
        this.requestedBank = null;
        this.loanType = LoanType.PLAYER_TO_PLAYER;
    }

    /**
     * Constructor used for loans player-to-player where the money to return will be defined later with code.
     * @param sender The loan sender.
     * @param receiver The loan receiver.
     * @param amount The loan amount.
     * @param senderBank The sender bank.
     * @param receiverBank The receiver bank.
     */
    public BPLoan(OfflinePlayer sender, OfflinePlayer receiver, Bank senderBank, Bank receiverBank) {
        this.sender = sender;
        this.receiver = receiver;
        this.moneyGiven = BigDecimal.ZERO;
        this.senderBank = senderBank;
        this.receiverBank = receiverBank;
        this.instalments = ConfigValues.getLoanInstalments();
        this.requestedBank = null;
        this.loanType = LoanType.PLAYER_TO_PLAYER;
    }

    /**
     * Constructor used for loans bank-to-player.
     * The sender and receiver banks will be null, and the requestedBank will be the bank that gave the loan.
     * @param receiver The loan receiver
     * @param bank The bank loan sender.
     * @param amount The loan amount.
     */
    public BPLoan(OfflinePlayer receiver, Bank bank, BigDecimal amount) {
        this.sender = null; // The sender would be the bank.
        this.receiver = receiver;
        this.moneyGiven = amount;
        this.moneyToReturn = amount.add(amount.divide(BigDecimal.valueOf(100)).multiply(ConfigValues.getLoanInterest()));
        this.senderBank = null;
        this.receiverBank = null;
        this.instalments = ConfigValues.getLoanInstalments();
        this.requestedBank = bank;
        this.loanType = LoanType.BANK_TO_PLAYER;
    }

    /**
     * Constructor used for loans bank-to-player where the money to return will be defined later with code.
     * @param receiver The loan receiver
     * @param bank The bank loan sender.
     */
    public BPLoan(OfflinePlayer receiver, Bank bank) {
        this.sender = null; // The sender would be the bank.
        this.receiver = receiver;
        this.moneyGiven = BigDecimal.ZERO;
        this.senderBank = null;
        this.receiverBank = null;
        this.instalments = ConfigValues.getLoanInstalments();
        this.requestedBank = bank;
        this.loanType = LoanType.BANK_TO_PLAYER;
    }

    public OfflinePlayer getSender() {
        return sender;
    }

    public OfflinePlayer getReceiver() {
        return receiver;
    }

    public Bank getSenderBank() {
        return senderBank;
    }

    public Bank getReceiverBank() {
        return receiverBank;
    }

    public Bank getRequestedBank() {
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

    public enum LoanType {
        PLAYER_TO_PLAYER,
        BANK_TO_PLAYER
    }
}