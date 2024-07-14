package me.pulsi_.bankplus.placeholders;

import me.pulsi_.bankplus.utils.texts.BPFormatter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Arrays;

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

    public String getFormat(String formatter, BigDecimal value) {
        if (value == null) return "Invalid number!";

        if (formatter.contains("_long")) return value.toPlainString();
        if (formatter.contains("_formatted")) return BPFormatter.formatPrecise(value);
        if (formatter.contains("_formatted_long")) return BPFormatter.formatLong(value);
        return BPFormatter.formatCommas(value);
    }

    public String[] getOptions(String identifier) {
        String[] args = identifier.split("_"), inputArgs = identifier.split("_");

        if (args.length != inputArgs.length) {
            return new String[] {"Invalid input"};
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("[") && args[i].endsWith("]")) {
                String[] options = args[i].replace("[", "").replace("]", "").split("/");

                boolean validOption = false;
                for (String option : options) {
                    if (inputArgs[i].equalsIgnoreCase(option)) {
                        args[i] = inputArgs[i];
                        validOption = true;
                        break;
                    }
                }
                if (!validOption) {
                    return new String[] {"Invalid option. must be " + Arrays.toString(options)};
                }
            }
            if (args[i].startsWith("<") && args[i].endsWith(">")) {
                args[i] = inputArgs[i];
            }
        }

        return args;
    }
}