package me.pulsi_.bankplus.placeholders;

import me.pulsi_.bankplus.utils.texts.BPFormatter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Arrays;
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

    public String getFormat(String formatter, BigDecimal value) {
        if (value == null) return "Invalid number!";

        if (formatter.contains("_long")) return value.toPlainString();
        if (formatter.contains("_formatted")) return BPFormatter.formatPrecise(value);
        if (formatter.contains("_formatted_long")) return BPFormatter.formatLong(value);
        return BPFormatter.formatCommas(value);
    }

    public String[] getOptions(String identifier) {
        String[] args = identifier.split("_");

        for (String arg : args) {
            if (arg.startsWith("[") && arg.endsWith("]")) {
                String[] options = arg.substring(1, arg.length() - 1).split("/");

                boolean validOption = Arrays.stream(options).anyMatch(option -> option.equalsIgnoreCase(arg));
                if (!validOption) {
                    return new String[]{"Invalid option. must be " + Arrays.toString(options)};
                }
            }
            // No specific action required for placeholders within angle brackets as per original logic
        }

        return args;
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
}