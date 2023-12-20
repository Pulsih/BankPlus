package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class CapacityPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "capacity";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankManager.exist(target)) return "&cThe selected bank does not exist.";

        BigDecimal capacity = BankManager.getCapacity(target, p);
        if (capacity.longValue() <= 0) return Values.CONFIG.getInfiniteCapacityText();
        return getFormat(identifier, capacity);
    }
}