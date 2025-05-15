package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class CapacityPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "capacity";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return bankDoesNotExist;

        BigDecimal capacity = BankUtils.getCapacity(BankRegistry.getBank(target), p);
        if (capacity.compareTo(BigDecimal.ZERO) <= 0) return ConfigValues.getInfiniteCapacityText();
        return getFormat(identifier, capacity);
    }
}