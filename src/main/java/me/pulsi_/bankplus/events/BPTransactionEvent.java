package me.pulsi_.bankplus.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class BPTransactionEvent extends Event implements Cancellable {

    public enum TransactionType {
        ADD,
        DEPOSIT,
        REMOVE,
        SET,
        WITHDRAW
    }

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean isCancelled;

    private final OfflinePlayer player;

    private final TransactionType transactionType;

    private final BigDecimal oldBalance;

    private BigDecimal transactionAmount;

    private final boolean singleMode;

    private final String bankName;

    public BPTransactionEvent(OfflinePlayer player, TransactionType transactionType, BigDecimal oldBalance, BigDecimal transactionAmount, boolean isSingleMode, String bankName) {
        this.player = player;
        this.transactionType = transactionType;
        this.oldBalance = oldBalance;
        this.transactionAmount = transactionAmount;
        this.singleMode = isSingleMode;
        this.bankName = bankName;

        this.isCancelled = false;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getOldBalance() {
        return oldBalance;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public boolean isSingleMode() {
        return singleMode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}