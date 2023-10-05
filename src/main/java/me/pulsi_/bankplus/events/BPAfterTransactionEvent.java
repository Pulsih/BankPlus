package me.pulsi_.bankplus.events;

import me.pulsi_.bankplus.economy.TransactionType;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * This event will be fired AFTER the transaction is done, often used to read useful information.
 * <p>
 * To get the information BEFORE the transaction check {@link BPPreTransactionEvent this}
 */
public class BPAfterTransactionEvent extends Event {

    public static final HandlerList HANDLER_LIST = new HandlerList();
    private final OfflinePlayer player;
    private final TransactionType transactionType;
    private final BigDecimal newBalance;
    private final double newVaultBalance;
    private final BigDecimal transactionAmount;
    private final String bankName;

    public BPAfterTransactionEvent(OfflinePlayer player, TransactionType transactionType, BigDecimal newBalance, double newVaultBalance, BigDecimal transactionAmount, String bankName) {
        this.player = player;
        this.transactionType = transactionType;
        this.newBalance = newBalance;
        this.newVaultBalance = newVaultBalance;
        this.transactionAmount = transactionAmount;
        this.bankName = bankName;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public double getNewVaultBalance() {
        return newVaultBalance;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public String getBankName() {
        return bankName;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}