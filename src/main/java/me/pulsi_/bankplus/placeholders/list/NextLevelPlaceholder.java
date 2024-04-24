package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;

public class NextLevelPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_level";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankManager.exist(target)) return "&cThe selected bank does not exist.";
        if (!BankManager.hasNextLevel(target, p)) return Values.CONFIG.getUpgradesMaxedPlaceholder();

        return (BankManager.getCurrentLevel(target, p) + 1) + "";
    }
}