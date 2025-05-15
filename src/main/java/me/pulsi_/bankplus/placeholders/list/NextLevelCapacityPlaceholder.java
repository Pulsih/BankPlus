package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class NextLevelCapacityPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_level_capacity";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return bankDoesNotExist;

        Bank bank = BankRegistry.getBank(target);
        if (!BankUtils.hasNextLevel(bank, p)) return ConfigValues.getUpgradesMaxedPlaceholder();

        BigDecimal capacity = BankUtils.getCapacity(bank, BankUtils.getCurrentLevel(bank, p) + 1);
        if (capacity.longValue() <= 0) return ConfigValues.getInfiniteCapacityText();

        return getFormat(identifier, capacity);
    }
}