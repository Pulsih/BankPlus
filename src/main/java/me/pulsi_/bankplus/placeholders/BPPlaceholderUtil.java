package me.pulsi_.bankplus.placeholders;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BPPlaceholderUtil {

    public static List<String[]> constructVariations(String[] parts, int index) {
        List<String[]> variations = new ArrayList<>();
        if (index >= parts.length) {
            variations.add(parts.clone());
            return variations;
        }

        String part = parts[index];
        if (part.contains("/")) {
            String[] options = part.split("/");
            for (String option : options) {
                parts[index] = option;
                variations.addAll(constructVariations(parts, index + 1));
            }
            parts[index] = part; // Restore original state for consistency
        } else {
            variations.addAll(constructVariations(parts, index + 1));
        }

        return variations;
    }

    public static List<BPPlaceholder> getRegisteredPlaceholders(List<BPPlaceholder> placeholders) {
        List<BPPlaceholder> registeredPlaceholders = new ArrayList<>();
        for (BPPlaceholder placeholder : placeholders) {
            // deconstruct the placeholder into parts to register all variations
            if (placeholder.toString().contains("/")) {
                String[] args = placeholder.toString().split("_");
                List<String[]> variations = constructVariations(args, 0);
                for (String[] variation : variations) {
                    for (int i = 0; i < variation.length; i++) {
                        variation[i] = variation[i].replace("[", "").replace("]", "");
                    }
                    registeredPlaceholders.add(new BPPlaceholder() {
                        @Override
                        public String getIdentifier() {
                            return String.join("_", variation);
                        }

                        @Override
                        public String getPlaceholder(Player p, String target, String identifier) {
                            return placeholder.getPlaceholder(p, target, identifier);
                        }
                    });
                }
            } else {
                registeredPlaceholders.add(placeholder);
            }
        }
        return registeredPlaceholders;
    }

    public static List<String> getRegisteredPlaceholderIdentifiers(List<BPPlaceholder> placeholders) {
        List<String> registeredPlaceholderIdentifiers = new ArrayList<>();
        for (BPPlaceholder placeholder : getRegisteredPlaceholders(placeholders)) {
            registeredPlaceholderIdentifiers.add(placeholder.getIdentifier());
        }
        return registeredPlaceholderIdentifiers;
    }
}