package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class NextLevelRequiredItemsPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_level_required_items";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return bankDoesNotExist;

        Bank bank = BankRegistry.getBank(target);
        if (!BankUtils.hasNextLevel(bank, p)) return ConfigValues.getUpgradesMaxedPlaceholder();

        HashMap<String, ItemStack> requiredItems = BankUtils.getRequiredItems(bank, BankUtils.getCurrentLevel(bank, p) + 1);
        if (requiredItems.isEmpty()) return ConfigValues.getNoUpgradeItemsMessage();
        return BPUtils.getRequiredItemsFormatted(requiredItems.values());
    }
}