package me.pulsi_.bankplus.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.placeholders.list.*;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static me.pulsi_.bankplus.placeholders.BPPlaceholderUtil.getRegisteredPlaceholderIdentifiers;

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
        if (p == null) return "Player not online";

        String target = ConfigValues.getMainGuiName();
        if (identifier.contains("{") && identifier.endsWith("}"))
            target = identifier.substring(identifier.indexOf("{") + 1, identifier.indexOf("}"));

        if (identifier.contains("<amount>")) {
            if (identifier.matches("calculate_(deposit||withdraw)_(percentage||number)_\\d+")) {
                return new CalculatePercentagePlaceholder().getPlaceholder(p, target, identifier);
            }
        }

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
        placeholders.add(new CalculatePercentagePlaceholder());
        placeholders.add(new NamePlaceholder());

        List<BPPlaceholder> expandedPlaceholders = new ArrayList<>();
        for (BPPlaceholder placeholder : placeholders) {
            String identifier = placeholder.getIdentifier();
            if (identifier.contains("/")) {
                // Split the identifier into parts for variation generation
                String[] parts = identifier.split("_");
                List<String[]> variations = BPPlaceholderUtil.constructVariations(parts, 0);
                for (String[] variation : variations) {
                    // Clean and reassemble the variation into an identifier
                    for (int i = 0; i < variation.length; i++) {
                        variation[i] = variation[i].replace("[", "").replace("]", "");
                    }
                    String varIdentifier = String.join("_", variation);
                    // Create a new placeholder for each variation
                    expandedPlaceholders.add(new BPPlaceholder() {
                        @Override
                        public String getIdentifier() {
                            return varIdentifier;
                        }

                        @Override
                        public String getPlaceholder(Player p, String target, String identifier) {
                            return placeholder.getPlaceholder(p, target, identifier);
                        }
                    });
                }
            }
        }

        placeholders.addAll(expandedPlaceholders);


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
        return new ArrayList<>(getRegisteredPlaceholderIdentifiers(this.placeholders));
    }

    private String buildPattern(String identifier) {
        String[] parts = identifier.split("_");
        StringBuilder pattern = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.contains("/")) {
                pattern.append("(?:");
                String[] options = part.split("/");
                for (int j = 0; j < options.length; j++) {
                    pattern.append(options[j]);
                    if (j < options.length - 1) pattern.append("|");
                }
                pattern.append(")");
            } else {
                pattern.append(part);
            }
            if (i < parts.length - 1) pattern.append("_");
        }

        return pattern.toString();
    }
}