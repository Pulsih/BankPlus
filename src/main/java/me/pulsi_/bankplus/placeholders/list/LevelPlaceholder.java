package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import org.bukkit.entity.Player;

public class LevelPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "level";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return "&cThe selected bank does not exist.";
        return String.valueOf(BankUtils.getCurrentLevel(BankUtils.getBank(target), p));
    }
}