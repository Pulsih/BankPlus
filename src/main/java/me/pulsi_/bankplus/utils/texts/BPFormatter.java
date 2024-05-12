package me.pulsi_.bankplus.utils.texts;

import me.pulsi_.bankplus.values.Values;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class BPFormatter {

    private static final String[] order = new String[]
            {
                    "", Values.CONFIG.getK(), Values.CONFIG.getM(), Values.CONFIG.getB(),
                    Values.CONFIG.getT(), Values.CONFIG.getQ(), Values.CONFIG.getQq()
            };

    private static final int limit = order.length - 1;

    /**
     * Return numbers like as 23.54k, 765.536M, 91.3T.
     * @param amount The amount.
     * @return A string.
     */
    public static String formatPrecise(BigDecimal amount) {
        return formatPrecise(amount.doubleValue());
    }

    /**
     * Return numbers like as 23.54k, 765.536M, 91.3T.
     * @param amount The amount.
     * @return A string.
     */
    public static String formatPrecise(double amount) {
        int i = 0;
        double scale = 1000D;

        while (amount >= scale && i < limit) {
            i++;
            scale *= 1000D;
        }

        return setMaxDigits(amount / (scale / 1000D)) + order[i];
    }

    /**
     * Return numbers like as 657k, 123k, 97M.
     * @param amount The amount.
     * @return A string.
     */
    public static String formatLong(BigDecimal amount) {
        double bal = amount.doubleValue();

        int i = 0;
        long scale = 1000L;

        while (bal >= scale && i < limit) {
            i++;
            scale *= 1000L;
        }

        return Math.round(bal / (scale / 1000D)) + order[i];
    }

    /**
     * Return numbers like as 14.243,12, 75.249, 231.785.
     * @param amount The amount.
     * @return A string.
     */
    public static String formatCommas(BigDecimal amount) {
        String number = styleBigDecimal(amount), numbers = number, decimals = "", result;

        if (number.contains(".")) {
            String[] split = number.split("\\.");
            numbers = split[0];
            decimals = split[1];
        }

        StringBuilder builder = new StringBuilder();
        for (int i = numbers.length() - 1, count = 0; i >= 0; i--, count++) {
            if (count >= 3) {
                builder.append(".");
                count = 0;
            }
            builder.append(numbers.charAt(i));
        }
        builder.reverse();
        result = builder + (decimals.isEmpty() ? "" : ("," + decimals));

        return result;
    }

    /**
     * Get a BigDecimal amount and format it with the BankPlus style.
     * @param amount The amount.
     * @return A string.
     */
    public static String styleBigDecimal(BigDecimal amount) {
        String balance = amount.toPlainString();

        int maxDecimals = Values.CONFIG.getMaxDecimalsAmount();

        // 0 or below.
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            if (maxDecimals <= 0) return "0";

            StringBuilder decimals = new StringBuilder(maxDecimals);
            for (int i = 0; i < maxDecimals; i++) decimals.append("0");
            return "0." + decimals;
        }

        boolean hasDecimals = balance.contains(".");
        // if not using decimals, return the number without them.
        if (maxDecimals <= 0) return hasDecimals ? balance.split("\\.")[0] : balance;

        if (hasDecimals) {
            String[] split = balance.split("\\.");
            String decimals = split[1];
            int decimalAmount = decimals.length();

            // if the number has decimals more than the limit, cut them.
            if (decimalAmount > maxDecimals) balance = split[0] + "." + decimals.substring(0, maxDecimals);

            // if the number has fewer decimals than the limit, add the missing ones with a 0.
            if (decimalAmount < maxDecimals) {
                StringBuilder builder = new StringBuilder(maxDecimals);
                for (int i = 0; i < (maxDecimals - decimalAmount); i++) builder.append("0");
                balance = balance + builder;
            }
        } else {
            StringBuilder decimals = new StringBuilder(maxDecimals);
            for (int i = 0; i < maxDecimals; i++) decimals.append("0");
            balance = balance + "." + decimals;
        }

        return balance;
    }

    public static BigDecimal getStyledBigDecimal(BigDecimal amount) {
        return new BigDecimal(styleBigDecimal(amount));
    }

    public static BigDecimal getStyledBigDecimal(String amount) {
        BigDecimal bD;
        try {
            bD = new BigDecimal(amount);
        } catch (Exception e) {
            bD = BigDecimal.ZERO;
        }
        return new BigDecimal(styleBigDecimal(bD));
    }

    private static String setMaxDigits(double balance) {
        NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
        format.setMaximumFractionDigits(Values.CONFIG.getMaxDecimalsAmount());
        format.setMinimumFractionDigits(0);
        return format.format(balance);
    }

    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        String format = Values.CONFIG.getInterestTimeFormat();
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
            String separator = last > amount ? "" : last == amount ? Values.CONFIG.getInterestTimeFinalSeparator() : Values.CONFIG.getInterestTimeSeparator();
            String replacer = time <= 0 ? "" : time + getTimeIdentifier(timeIdentifier, time) + separator;

            format = format.replace("%" + identifier, replacer);
        }

        return format;
    }

    private static String getTimeIdentifier(String id, long time) {
        switch (id) {
            case "seconds":
                return time == 1 ? Values.CONFIG.getSecond() : Values.CONFIG.getSeconds();
            case "minutes":
                return time == 1 ? Values.CONFIG.getMinute() : Values.CONFIG.getMinutes();
            case "hours":
                return time == 1 ? Values.CONFIG.getHour() : Values.CONFIG.getHours();
            case "days":
                return time == 1 ? Values.CONFIG.getDay() : Values.CONFIG.getDays();
        }
        return "";
    }
}