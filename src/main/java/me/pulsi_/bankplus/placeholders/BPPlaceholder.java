package me.pulsi_.bankplus.placeholders;

import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BPPlaceholder {

    /**
     * Get the placeholder identifier
     * @return A string.
     */
    public abstract String getIdentifier();

    /**
     * Get the placeholder string.
     * @param p The player.
     * @param target Target string that can be a bank or player name.
     * @param identifier The full placeholder identifier.
     * @return A string.
     */
    public abstract String getPlaceholder(Player p, String target, String identifier);

    /**
     * Check if the placeholder has any placeholders.
     * Placeholders are anything between < and >.
     * @return A boolean.
     */
    public abstract boolean hasPlaceholders();

    /**
     * Check if the placeholder has any variables.
     * Variables are anything between [ and ].
     * @return A boolean.
     */
    public abstract boolean hasVariables();

    public String getFormat(String formatter, BigDecimal value) {
        if (value == null) return "Invalid number!";

        if (formatter.contains("_long")) return value.toPlainString();
        if (formatter.contains("_formatted")) return BPFormatter.formatPrecise(value);
        if (formatter.contains("_formatted_long")) return BPFormatter.formatLong(value);
        return BPFormatter.formatCommas(value);
    }

    public String[] getSelectedVariantParts(String identifier) {
        String originalIdentifier = identifier;
        identifier = inverseRegex(identifier, getIdentifier());
        List<List<String>> parts = BPPlaceholderUtil.compileParts(getIdentifier()),
                originalParts = BPPlaceholderUtil.compileParts(originalIdentifier);
        List<String> selectedParts = new ArrayList<>(), variants = BPPlaceholderUtil.compileVariants(parts, 0);

        for (List<String> strings : parts) {
            for (String part : strings) {
                if (identifier.contains(part) &&
                        (identifier.startsWith(part + "_") || identifier.endsWith("_" + part) || identifier.contains("_" + part + "_"))) {
                    selectedParts.add(part);
                }
            }
        }

        String potentialIdentifier = String.join("_", selectedParts);

        // verify if the placeholders exist
        if (variants.contains(potentialIdentifier)) {
            // replace the placeholders with the original parts
            for (int i = 0; i < selectedParts.size(); i++) {
                selectedParts.set(i, originalParts.get(i).get(0));
            }
            return selectedParts.toArray(new String[0]);
        }
        return new String[]{"Invalid identifier"};
    }

    public String getRegex(String identifier) {
        String regex = "\\[|]|/|<.*?>";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(identifier);

        return matcher.replaceAll(matchResult -> {
            String match = matchResult.group();
            return switch (match) {
                case "[" -> "(";
                case "]" -> ")";
                case "/" -> "||";
                case "<amount>", "<position>" -> "\\\\d+";
                default -> "";
            };
        });
    }

    public String inverseRegex(String identifier, String trueIdentifier) {
        Pattern placeholderPattern = Pattern.compile("<(.*?)>");
        Matcher placeholderMatcher = placeholderPattern.matcher(trueIdentifier);

        while (placeholderMatcher.find()) {
            String placeholder = placeholderMatcher.group(1);
            identifier = identifier.replaceFirst("\\d+", "<" + placeholder + ">");
        }

        return identifier;
    }
}