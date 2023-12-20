package me.pulsi_.bankplus.placeholders;

import me.pulsi_.bankplus.utils.BPFormatter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

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

        if (formatter.contains("_long")) return String.valueOf(value);
        if (formatter.contains("_formatted")) return BPFormatter.format(value);
        if (formatter.contains("_formatted_long")) return BPFormatter.formatLong(value);
        return BPFormatter.formatCommas(value);
    }
}