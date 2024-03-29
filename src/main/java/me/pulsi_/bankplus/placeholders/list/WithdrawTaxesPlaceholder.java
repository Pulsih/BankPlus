package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;

public class WithdrawTaxesPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "withdraw_taxes";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        return Values.CONFIG.getWithdrawTaxesString();
    }
}