package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class NextLevelInterestRatePlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_level_interest_rate";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankManager.exist(target)) return "&cThe selected bank does not exist.";
        if (!BankManager.hasNextLevel(target, p)) return Values.CONFIG.getUpgradesMaxedPlaceholder();

        return BankManager.getInterestRate(target, p, BankManager.getCurrentLevel(target, p) + 1) + "";
    }
}