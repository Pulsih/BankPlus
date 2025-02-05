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
     *
     * @return A string.
     */
    public abstract String getIdentifier();

    /**
     * Get the placeholder string.
     *
     * @param p          The player.
     * @param target     The subject of the placeholder (A player or bank to display information more precisely).
     * @param identifier The full placeholder identifier.
     * @return A string.
     */
    public abstract String getPlaceholder(Player p, String target, String identifier);

    /**
     * From the given placeholder and amount, get the formatted amount based on the placeholder format keyword.
     *
     * @param placeholder The placeholder id.
     * @param value       The amount to format.
     * @return The formatted amount.
     */
    public String getFormat(String placeholder, BigDecimal value) {
        if (value == null) return "Invalid number!";

        if (placeholder.contains("_formatted_commas")) return BPFormatter.formatCommas(value);
        if (placeholder.contains("_formatted_long")) return BPFormatter.formatLong(value);
        if (placeholder.contains("_formatted")) return BPFormatter.formatPrecise(value);
        if (placeholder.contains("_long")) return value.toPlainString();
        
        return BPFormatter.formatDecimals(value);
    }
}