package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;

public class LevelPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "level";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankManager.exist(target)) return "&cThe selected bank does not exist.";
        return String.valueOf(BankManager.getCurrentLevel(target, p));
    }
}