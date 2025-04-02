package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import org.bukkit.entity.Player;

public class BalancePlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "balance";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return bankDoesNotExist;
        return getFormat(identifier, BPEconomy.get(target).getBankBalance(p));
    }
}