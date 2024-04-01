package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.values.Values;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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

        if (Values.CONFIG.getMaxDecimalsAmount() > 0) {
            String[] split = number.split("\\.");
            numbers = split[0];
            decimals = split[1];
        }

        StringBuilder builder = new StringBuilder();
        for (int i = numbers.length() - 1, count = 0; i > 0; i--, count++) {
            if (count > 3) {
                builder.append(".");
                count = 0;
            }
            builder.append(numbers.charAt(i));
        }
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
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            if (maxDecimals <= 0) return "0";

            StringBuilder decimals = new StringBuilder(maxDecimals);
            for (int i = 0; i < maxDecimals; i++) decimals.append("0");
            return "0." + decimals;
        }

        boolean hasDecimals = balance.contains(".");
        if (maxDecimals <= 0) return hasDecimals ? balance.split("\\.")[0] : balance;

        if (hasDecimals) {
            String[] split = balance.split("\\.");
            String decimals = split[1];

            if (decimals.length() > maxDecimals) {
                String correctedDecimals = decimals.substring(0, maxDecimals);
                balance = split[0] + "." + correctedDecimals;
            }
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
}