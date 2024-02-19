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

    public static String format(BigDecimal balance) {
        double bal = balance.doubleValue();

        int i = 0;
        double amount = 1000d;

        while (bal >= amount && i < order.length - 1) {
            i++;
            amount *= 1000d;
        }

        return setMaxDigits(bal / (amount / 1000d), Values.CONFIG.getMaxDecimalsAmount()) + order[i];
    }

    public static String format(double balance) {
        int i = 0;
        double amount = 1000d;

        while (balance >= amount && i < order.length - 1) {
            i++;
            amount *= 1000d;
        }

        return setMaxDigits(balance / (amount / 1000d), Values.CONFIG.getMaxDecimalsAmount()) + order[i];
    }

    public static String formatLong(BigDecimal balance) {
        double bal = balance.doubleValue();

        int i = 0;
        long amount = 1000l;

        while (bal >= amount && i < order.length - 1) {
            i++;
            amount *= 1000l;
        }

        return Math.round(bal / (amount / 1000l)) + order[i];
    }

    public static String formatCommas(Object amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }

    public static String formatBigDecimal(BigDecimal amount) {
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
            String decimals = balance.split("\\.")[1];
            if (decimals.length() > maxDecimals) {
                String correctedDecimals = decimals.substring(0, maxDecimals);
                balance = balance.split("\\.")[0] + "." + correctedDecimals;
            }
        }
        return balance;
    }

    public static BigDecimal getBigDecimalFormatted(BigDecimal amount) {
        return new BigDecimal(formatBigDecimal(amount));
    }

    public static BigDecimal getBigDecimalFormatted(String amount) {
        BigDecimal bD;
        try {
            bD = new BigDecimal(amount);
        } catch (Exception e) {
            bD = BigDecimal.ZERO;
        }
        return new BigDecimal(formatBigDecimal(bD));
    }

    private static String setMaxDigits(double balance, int digits) {
        NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
        format.setMaximumFractionDigits(digits);
        format.setMinimumFractionDigits(0);
        return format.format(balance);
    }
}