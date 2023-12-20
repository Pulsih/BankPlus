package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class NextInterestPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_interest";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankManager.exist(target)) return "&cThe selected bank does not exist.";
        return getFormat(identifier, BankPlus.INSTANCE().getInterest().getInterestMoney(target, p, BankManager.getInterestRate(target, p)));
    }
}