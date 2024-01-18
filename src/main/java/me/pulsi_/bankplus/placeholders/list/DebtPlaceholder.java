package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import org.bukkit.entity.Player;

public class DebtPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "debt";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankManager.exist(target)) return "&cThe selected bank does not exist.";
        return getFormat(identifier, BPEconomy.getDebt(p));
    }
}