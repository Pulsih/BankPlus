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

    public static String formatBigDouble(BigDecimal balance) {
        String bal = balance.toString();

        int maxDecimals = Values.CONFIG.getMaxDecimalsAmount();
        if (maxDecimals <= 0) return bal.contains(".") ? bal.split("\\.")[0] : bal;

        if (balance.doubleValue() > 0 && bal.contains(".")) {
            String decimals = bal.split("\\.")[1];
            if (decimals.length() > maxDecimals) {
                String correctedDecimals = decimals.substring(0, maxDecimals);
                bal = bal.split("\\.")[0] + "." + correctedDecimals;
            }
        } else {
            StringBuilder decimals = new StringBuilder();
            for (int i = 0; i < maxDecimals; i++) decimals.append("0");
            if (balance.doubleValue() <= 0) bal = "0." + decimals;
            else bal += "." + decimals;
        }
        return bal;
    }

    private static String setMaxDigits(double balance, int digits) {
        NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
        format.setMaximumFractionDigits(digits);
        format.setMinimumFractionDigits(0);
        return format.format(balance);
    }
}