package me.pulsi_.bankplus.utils;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class BPDebugger {

    private boolean transactionsDebuggerEnabled = false;

    public void debugTransactions(Player p, BigDecimal amount, EconomyResponse response) {
        if (!isTransactionsDebuggerEnabled()) return;


    }

    public boolean isTransactionsDebuggerEnabled() {
        return transactionsDebuggerEnabled;
    }

    public void setTransactionsDebuggerEnabled(boolean enabled) {
        this.transactionsDebuggerEnabled = enabled;
    }
}