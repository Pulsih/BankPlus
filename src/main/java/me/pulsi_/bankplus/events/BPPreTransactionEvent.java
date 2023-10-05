package me.pulsi_.bankplus.events;

import me.pulsi_.bankplus.economy.TransactionType;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * This event will be fired BEFORE the transaction is done, meaning that you can still edit values like transaction amount and cancel the event.
 * <p>
 * To get the information AFTER the transaction check {@link BPAfterTransactionEvent this}
 */
public class  BPPreTransactionEvent extends Event implements Cancellable {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean isCancelled;
    private final OfflinePlayer player;
    private final TransactionType transactionType;
    private final BigDecimal currentBalance;
    private final double currentVaultBalance;
    private BigDecimal transactionAmount;
    private final String bankName;

    public BPPreTransactionEvent(OfflinePlayer player, TransactionType transactionType, BigDecimal currentBalance, double currentVaultBalance, BigDecimal transactionAmount, String bankName) {
        this.player = player;
        this.transactionType = transactionType;
        this.currentBalance = currentBalance;
        this.currentVaultBalance = currentVaultBalance;
        this.transactionAmount = transactionAmount;
        this.bankName = bankName;

        this.isCancelled = false;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public double getCurrentVaultBalance() {
        return currentVaultBalance;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
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