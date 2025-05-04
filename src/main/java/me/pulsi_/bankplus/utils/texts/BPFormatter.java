package me.pulsi_.bankplus.utils.texts;

import me.pulsi_.bankplus.values.ConfigValues;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class BPFormatter {

    private static final String[] order = new String[]
            {
                    "", ConfigValues.getK(), ConfigValues.getM(), ConfigValues.getB(),
                    ConfigValues.getT(), ConfigValues.getQ(), ConfigValues.getQq()
            };

    private static final int limit = order.length - 1;

    /**
     * Return a percentage removing unnecessary 0 in the decimals part.
     * Example: 0.0%, 43.0%, 12.50% -> 0%, 43%, 12.5%
     *
     * @param amount The amount.
     * @return A string.
     */
    public static String formatPercentage(double amount) {
        DecimalFormat f = new DecimalFormat("#");
        f.setMaximumFractionDigits(2);
        f.setMinimumFractionDigits(0);
        return f.format(amount);
    }

    /**
     * Return numbers like as 23.54k, 765.536M, 91.3T.
     * This formatting method correspond to the placeholder %balance_formatted%.
     *
     * @param amount The amount.
     * @return A string.
     */
    public static String formatPrecise(BigDecimal amount) {
        BigDecimal thousand = new BigDecimal(1000), scale = thousand;

        int count = 0;
        while (amount.compareTo(scale) >= 0 && count < limit) {
            scale = scale.multiply(thousand);
            count++;
        }

        return styleBigDecimal(amount.divide(scale.divide(thousand)), 2) + order[count];
    }

    /**
     * Return numbers like as 657k, 123k, 97M.
     * This formatting method correspond to the placeholder %balance_formatted_long%.
     *
     * @param amount The amount.
     * @return A string.
     */
    public static String formatLong(BigDecimal amount) {
        BigDecimal thousand = new BigDecimal(1000), scale = thousand;

        int count = 0;
        while (amount.compareTo(scale) >= 0 && count < limit) {
            scale = scale.multiply(thousand);
            count++;
        }

        return styleBigDecimal(amount.divide(scale.divide(thousand)), 0) + order[count];
    }

    /**
     * Return numbers like as 14.243,12, 75.249, 231.785.
     * This formatting method correspond to the placeholder %balance%.
     *
     * @param amount The amount.
     * @return A string.
     */
    public static String formatCommas(BigDecimal amount) {
        String number = styleBigDecimal(amount), numbers = number, decimals = "", result;

        if (number.contains(".")) { // In BigDecimals numbers, decimals are divided by .
            String[] split = number.split("\\.");
            numbers = split[0];
            decimals = split[1];
        }

        StringBuilder builder = new StringBuilder();
        for (int i = numbers.length() - 1, count = 0; i >= 0; i--, count++) {
            if (count >= 3) {
                builder.append(ConfigValues.getThousandsSeparator());
                count = 0;
            }
            builder.append(numbers.charAt(i));
        }
        builder.reverse();
        result = builder + (decimals.isEmpty() ? "" : (ConfigValues.getDecimalsSeparator() + decimals));

        return result;
    }

    /**
     * Get a BigDecimal amount and format it with the BankPlus style.
     *
     * @param amount The amount.
     * @return A string.
     */
    public static String styleBigDecimal(BigDecimal amount) {
        return styleBigDecimal(amount, ConfigValues.getMaxDecimalsAmount());
    }

    /**
     * Get a BigDecimal amount and format it with the BankPlus style.
     *
     * @param amount         The amount.
     * @param decimalsAmount The max decimals amount.
     * @return A string.
     */
    public static String styleBigDecimal(BigDecimal amount, int decimalsAmount) {
        return amount.setScale(decimalsAmount, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Create a BigDecimal formatted from the given string.
     *
     * @param amount The amount as string.
     * @return A BigDecimal or ZERO if invalid.
     */
    public static BigDecimal getStyledBigDecimal(String amount) {
        BigDecimal bD;
        try {
            bD = new BigDecimal(amount.replace("%", ""));
        } catch (Exception e) {
            bD = BigDecimal.ZERO;
        }
        return new BigDecimal(styleBigDecimal(bD));
    }

    /**
     * Format the given milliseconds in the BankPlus's time format.
     *
     * @param milliseconds The milliseconds time.
     * @return A string of formatted time.
     */
    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        String format = ConfigValues.getInterestTimeFormat();
        String[] parts = format.split("%");
        int amount = parts.length;

        for (int i = 1; i < amount; i++) {
            String identifier = parts[i];
            long time = 0;

            switch (identifier) {
                case "s":
                    time = seconds - (minutes * 60);
                    break;
                case "m":
                    time = minutes - (hours * 60);
                    break;
                case "h":
                    time = hours - (days * 24);
                    break;
                case "d":
                    time = days;
                    break;
            }
            if (time <= 0) format = format.replace("%" + identifier, "");
        }

        parts = format.split("%");
        amount = parts.length;

        for (int i = 1; i < amount; i++) {
            String identifier = parts[i], timeIdentifier = "";
            long time = 0;

            switch (identifier) {
                case "s":
                    time = seconds - (minutes * 60);
                    timeIdentifier = "seconds";
                    break;
                case "m":
                    time = minutes - (hours * 60);
                    timeIdentifier = "minutes";
                    break;
                case "h":
                    time = hours - (days * 24);
                    timeIdentifier = "hours";
                    break;
                case "d":
                    time = days;
                    timeIdentifier = "days";
                    break;
            }

            int last = i + 2;
            String separator = last > amount ? "" : last == amount ? ConfigValues.getInterestTimeFinalSeparator() : ConfigValues.getInterestTimeSeparator();
            String replacer = time <= 0 ? "" : time + getTimeIdentifier(timeIdentifier, time) + separator;

            format = format.replace("%" + identifier, replacer);
        }

        return format;
    }

    private static String getTimeIdentifier(String id, long time) {
        switch (id) {
            case "seconds":
                return time == 1 ? ConfigValues.getSecond() : ConfigValues.getSeconds();
            case "minutes":
                return time == 1 ? ConfigValues.getMinute() : ConfigValues.getMinutes();
            case "hours":
                return time == 1 ? ConfigValues.getHour() : ConfigValues.getHours();
            case "days":
                return time == 1 ? ConfigValues.getDay() : ConfigValues.getDays();
        }
        return "";
    }
}