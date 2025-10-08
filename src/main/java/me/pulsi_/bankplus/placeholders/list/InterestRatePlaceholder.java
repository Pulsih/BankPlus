package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import org.bukkit.entity.Player;

public class InterestRatePlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "interest_rate";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return bankDoesNotExist;
        return BankUtils.getOnlineInterestRate(BankRegistry.getBank(target), p) + "";
    }
}