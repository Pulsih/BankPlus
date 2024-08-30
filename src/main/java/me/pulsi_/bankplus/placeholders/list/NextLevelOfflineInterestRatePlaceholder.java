package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

public class NextLevelOfflineInterestRatePlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_level_offline_interest_rate";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return "&cThe selected bank does not exist.";
        Bank bank = BankUtils.getBank(target);
        if (!BankUtils.hasNextLevel(bank, p)) return ConfigValues.getUpgradesMaxedPlaceholder();

        return BankUtils.getOfflineInterestRate(bank, p, BankUtils.getCurrentLevel(bank, p) + 1) + "";
    }
}