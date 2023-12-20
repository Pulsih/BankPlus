package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import org.bukkit.entity.Player;

public class OfflineInterestRatePlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "offline_interest_rate";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankManager.exist(target)) return "&cThe selected bank does not exist.";
        return BankManager.getOfflineInterestRate(target, p) + "";
    }
}