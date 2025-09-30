package me.pulsi_.bankplus.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.placeholders.list.*;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BPPlaceholders extends PlaceholderExpansion {

    private final List<BPPlaceholder> placeholders = new ArrayList<>();

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "Pulsi_";
    }

    @Override
    public String getIdentifier() {
        return "bankplus";
    }

    @Override
    public String getVersion() {
        return BankPlus.INSTANCE().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        if (p == null) return "Could not get player from PlaceholderAPI request.";

        String target = ConfigValues.getMainGuiName();
        if (identifier.contains("{") && identifier.endsWith("}"))
            target = identifier.substring(identifier.indexOf("{") + 1, identifier.indexOf("}"));

        for (BPPlaceholder placeholder : placeholders) {
            if (identifier.toLowerCase().startsWith(placeholder.getIdentifier().toLowerCase()))
                return placeholder.getPlaceholder(p, target, identifier);
        }
        return null;
    }

    public void registerPlaceholders() {
        placeholders.clear();

        placeholders.add(new BalancePlaceholder());
        placeholders.add(new BankTopMoneyPlaceholder());
        placeholders.add(new BankTopNamePlaceholder());
        placeholders.add(new BankTopPositionPlaceholder());
        placeholders.add(new CalculateDepositTaxesPlaceholder());
        placeholders.add(new CalculateWithdrawTaxesPlaceholder());
        placeholders.add(new CapacityPlaceholder());
        placeholders.add(new DebtPlaceholder());
        placeholders.add(new DepositTaxesPlaceholder());
        placeholders.add(new InterestCooldownMillisPlaceholder());
        placeholders.add(new InterestCooldownPlaceholder());
        placeholders.add(new InterestRatePlaceholder());
        placeholders.add(new LevelPlaceholder());
        placeholders.add(new NextInterestPlaceholder());
        placeholders.add(new NextLevelCapacityPlaceholder());
        placeholders.add(new NextLevelCostPlaceholder());
        placeholders.add(new NextLevelInterestRatePlaceholder());
        placeholders.add(new NextLevelOfflineInterestRatePlaceholder());
        placeholders.add(new NextLevelPlaceholder());
        placeholders.add(new NextLevelRequiredItemsPlaceholder());
        placeholders.add(new NextOfflineInterestPlaceholder());
        placeholders.add(new OfflineInterestRatePlaceholder());
        placeholders.add(new WithdrawTaxesPlaceholder());

        List<BPPlaceholder> orderedPlaceholders = new ArrayList<>(), copy = new ArrayList<>(placeholders);
        while (!copy.isEmpty()) {
            BPPlaceholder longest = null;

            int highestLength = 0;
            for (BPPlaceholder placeholder : copy) {
                String identifier = placeholder.getIdentifier();
                int length = identifier.length();

                if (length > highestLength) {
                    highestLength = length;
                    longest = placeholder;
                }
            }

            orderedPlaceholders.add(longest);
            copy.remove(longest);
        }

        placeholders.clear();
        placeholders.addAll(orderedPlaceholders);
    }

    public List<String> getRegisteredPlaceholders() {
        List<String> placeholders = new ArrayList<>();
        for (BPPlaceholder placeholder : this.placeholders) placeholders.add(placeholder.getIdentifier());
        return placeholders; }
}