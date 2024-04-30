package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;

public class NextLevelInterestRatePlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_level_interest_rate";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return "&cThe selected bank does not exist.";
        Bank bank = BankUtils.getBank(target);
        if (!BankUtils.hasNextLevel(bank, p)) return Values.CONFIG.getUpgradesMaxedPlaceholder();

        return BankUtils.getInterestRate(bank, p, BankUtils.getCurrentLevel(bank, p) + 1) + "";
    }
}