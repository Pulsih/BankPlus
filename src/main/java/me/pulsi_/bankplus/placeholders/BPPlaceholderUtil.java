package me.pulsi_.bankplus.placeholders;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class BPPlaceholderUtil {
    public static List<BPPlaceholder> registerVariations(List<BPPlaceholder> placeholders) {
        List<BPPlaceholder> registeredPlaceholders = new ArrayList<>();
        for (BPPlaceholder placeholder : placeholders) {
            // deconstruct the placeholder into parts to register all variations
            if (placeholder.hasVariables()) {
                List<List<String>> parts = compileParts(placeholder.getIdentifier());
                List<String> variations = compileVariants(parts, 0);
                for (String variation : variations) {
                    registeredPlaceholders.add(new BPPlaceholder() {
                        @Override
                        public String getIdentifier() {
                            return variation;
                        }

                        @Override
                        public String getPlaceholder(Player p, String target, String identifier) {
                            return placeholder.getPlaceholder(p, target, identifier);
                        }

                        @Override
                        public boolean hasPlaceholders() {
                            return placeholder.hasPlaceholders();
                        }

                        @Override
                        public boolean hasVariables() {
                            return placeholder.hasVariables();
                        }
                    });
                }
            }
        }
        return registeredPlaceholders;
    }

    public static List<String> getRegisteredPlaceholderIdentifiers(List<BPPlaceholder> placeholders) {
        List<String> registeredPlaceholderIdentifiers = new ArrayList<>();
        for (BPPlaceholder placeholder : registerVariations(placeholders)) {
            registeredPlaceholderIdentifiers.add(placeholder.getIdentifier());
        }
        return registeredPlaceholderIdentifiers;
    }

    public static BPPlaceholder parsePlaceholderPlaceholders(List<BPPlaceholder> placeholders, String identifier) {
        // compile a list of special placeholders
        for (BPPlaceholder placeholder : placeholders) {
            if (placeholder.getIdentifier().matches(".*<.*?>.*")) {
                String regex = placeholder.getRegex(placeholder.getIdentifier());
                if (identifier.matches(regex)) {
                    return placeholder;
                }
            }
        }
        return null;
    }

    public static List<List<String>> compileParts(String input) {
        List<List<String>> result = new ArrayList<>();
        StringBuilder currentSegment = new StringBuilder();
        boolean insideBrackets = false;

        for (char c : input.toCharArray()) {
            if (c == '[' || c == ']' || (c == '_' && !insideBrackets)) {
                if (!currentSegment.isEmpty()) {
                    result.add(c == ']' ? Arrays.asList(currentSegment.toString().split("/")) : List.of(currentSegment.toString()));
                    currentSegment.setLength(0);
                }
                insideBrackets = (c == '[');
            } else {
                currentSegment.append(c);
            }
        }

        if (!currentSegment.isEmpty()) {
            result.add(List.of(currentSegment.toString()));
        }

        return result;
    }

    public static List<String> compileVariants(List<List<String>> parsed, int i) {
        if (i >= parsed.size()) {
            return Collections.singletonList("");
        }

        List<String> result = new ArrayList<>();
        List<String> currentSegment = parsed.get(i);
        List<String> nextVariants = compileVariants(parsed, i + 1);

        for (String segment : currentSegment) {
            for (String variant : nextVariants) {
                result.add(segment + (variant.isEmpty() ? "" : "_" + variant));
            }
        }

        return result;
    }
}