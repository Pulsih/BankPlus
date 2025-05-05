package me.pulsi_.bankplus.values;

import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;

public class ValueLoader {

    /**
     * Simplify the process of getting a BigDecimal amount
     * from the config with an automatic warn of invalid number.
     *
     * @param config The config.
     * @param path   The path.
     * @return A BigDecimal.
     */
    protected static BigDecimal getBigDecimal(FileConfiguration config, String path) {
        String amount = config.getString(path);
        if (BPUtils.isInvalidNumber(amount))
            BPLogger.Console.warn("\"" + path + "\" is an invalid number, please correct it in the config.yml.");
        return BPFormatter.getStyledBigDecimal(amount);
    }

    /**
     * Get the delay from the given path.
     * @param config The config.
     * @param path The path.
     * @return The delay in milliseconds.
     */
    protected static long getDelayMilliseconds(FileConfiguration config, String path) {
        long delayMillis = 0;
        String time = config.getString(path);
        if (time == null) return delayMillis;
        if (!time.contains(" ")) return BPUtils.minutesInMilliseconds(Integer.parseInt(time));

        String[] split = time.split(" ");
        int delay;
        try {
            delay = Integer.parseInt(split[0]);
        } catch (NumberFormatException e) {
            BPLogger.Console.warn("\"" + path + "\" is an invalid number, please correct it in the config.yml.");
            return BPUtils.minutesInMilliseconds(5);
        }

        String delayType = split[1];
        switch (delayType) {
            case "s":
                return BPUtils.secondsInMilliseconds(delay);
            default:
                return BPUtils.minutesInMilliseconds(delay);
            case "h":
                return BPUtils.hoursInMilliseconds(delay);
            case "d":
                return BPUtils.daysInMilliseconds(delay);
        }
    }
}