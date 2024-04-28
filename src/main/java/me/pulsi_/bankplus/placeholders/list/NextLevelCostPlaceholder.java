package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class NextLevelCostPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_level_cost";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return "&cThe selected bank does not exist.";
        if (!BankUtils.hasNextLevel(target, p)) return Values.CONFIG.getUpgradesMaxedPlaceholder();

        BigDecimal cost = BankUtils.getLevelCost(target, BankUtils.getCurrentLevel(target, p) + 1);
        return getFormat(identifier, cost);
    }
}