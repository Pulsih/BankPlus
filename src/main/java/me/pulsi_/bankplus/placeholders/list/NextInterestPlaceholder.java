package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import org.bukkit.entity.Player;

public class NextInterestPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_interest";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return "&cThe selected bank does not exist.";
        return getFormat(identifier, BankPlus.INSTANCE().getInterest().getInterestMoney(target, p, BankUtils.getInterestRate(target, p)));
    }
}