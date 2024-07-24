package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;

public class NextLevelCompoundPlaceholder extends BPPlaceholder {
    @Override
    public String getIdentifier() {
        return "next_level_[cost/interest_rate/offline_interest_rate/capacity/required_items]";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return "&cThe selected bank does not exist.";
        Bank bank = BankUtils.getBank(target);
        if (!BankUtils.hasNextLevel(bank, p)) return ConfigValues.getUpgradesMaxedPlaceholder();
        String[] args = getSelectedVariantParts(identifier);

        switch (args[2]) {
            case "cost":
                BigDecimal output = BankUtils.getLevelCost(bank, BankUtils.getCurrentLevel(bank, p) + 1);
                return getFormat(identifier, output);

            case "interest_rate":
                return BankUtils.getInterestRate(bank, p, BankUtils.getCurrentLevel(bank, p) + 1) + "";

            case "capacity":
                BigDecimal capacity = BankUtils.getCapacity(bank, BankUtils.getCurrentLevel(bank, p) + 1);
                if (capacity.longValue() <= 0) return ConfigValues.getInfiniteCapacityText();
                return getFormat(identifier, capacity);

            case "offline_interest_rate":
                return BankUtils.getOfflineInterestRate(bank, p, BankUtils.getCurrentLevel(bank, p) + 1) + "";

            case "required_items":
                List<ItemStack> requiredItems = BankUtils.getRequiredItems(bank, BankUtils.getCurrentLevel(bank, p) + 1);
                if (requiredItems.isEmpty()) return ConfigValues.getNoUpgradeItemsMessage();
                return BPUtils.getRequiredItems(requiredItems);
        }
        return "Placeholder not found!";
    }

    @Override
    public boolean hasPlaceholders() {
        return false;
    }

    @Override
    public boolean hasVariables() {
        return true;
    }
}
