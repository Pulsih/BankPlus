package me.pulsi_.bankplus.placeholders;

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
        List<List<String>> parts = BPPlaceholderUtil.compileParts(getIdentifier());
        List<String> selectedParts = new ArrayList<>(List.of(identifier.split("_"))), variants = BPPlaceholderUtil.compileVariants(parts, 0), reParts = BPPlaceholderUtil.getRecompiledParts(parts, BPPlaceholderUtil.compileParts(identifier));
        String potentialIdentifier = String.join("_", reParts);

        // verify if the placeholders exist
        if (variants.contains(potentialIdentifier)) {
            // join original parts that have "_" in them
            for (int i = 0; i < reParts.size(); i++) {
                String part = reParts.get(i);
                if (part.contains("_")) {
                    int count = part.split("_").length - 1;
                    selectedParts.set(i, reParts.get(i));
                    while (count > 0) {
                        selectedParts.remove(i + count);
                        count--;
                    }
                }
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