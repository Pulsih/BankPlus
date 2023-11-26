package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.events.BPAfterTransactionEvent;
import me.pulsi_.bankplus.events.BPPreTransactionEvent;
import me.pulsi_.bankplus.logSystem.BPLogUtils;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.UUID;

public class BPTransactionListener implements Listener {

    public static class Pair<K, V> {
        private final K key;
        private final V value;

        public K getKey() { return key; }
        public V getValue() { return value; }

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final HashMap<UUID, Pair<BigDecimal, Double>> logHolder = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void processDebt(BPPreTransactionEvent e) {
        OfflinePlayer p = e.getPlayer();

        if (Values.CONFIG.isLogTransactions())
            logHolder.put(p.getUniqueId(), new Pair<>(e.getCurrentBalance(), e.getCurrentVaultBalance()));

        BigDecimal debt = BankPlus.getBPEconomy().getDebt(p, e.getBankName());
        TransactionType type = e.getTransactionType();
        if (debt.doubleValue() <= 0d || (!type.equals(TransactionType.ADD) && !type.equals(TransactionType.DEPOSIT) && !type.equals(TransactionType.SET))) return;

        BigDecimal debtLeft = debt.subtract(e.getTransactionAmount()), newAmount;

        if (debtLeft.doubleValue() > 0d) {
            newAmount = BigDecimal.valueOf(0);
            debtLeft = debt.subtract(e.getTransactionAmount());

            BPMessages.send(Bukkit.getPlayer(p.getUniqueId()), "Debt-Money-Taken",
                    BPUtils.placeValues(e.getTransactionAmount()),
                    BPUtils.placeValues(debtLeft, "debt")
            );
        } else {
            newAmount = debtLeft.multiply(BigDecimal.valueOf(-1));
            debtLeft = BigDecimal.valueOf(0);

            BPMessages.send(Bukkit.getPlayer(p.getUniqueId()), "Debt-Money-Taken",
                    BPUtils.placeValues(newAmount),
                    BPUtils.placeValues(debtLeft, "debt")
            );
        }

        BankPlus.getBPEconomy().setDebt(p, debtLeft, e.getBankName());
        e.setTransactionAmount(newAmount);
    }

    @EventHandler
    public void onTransactionEnd(BPAfterTransactionEvent e) {
        if (Values.CONFIG.isLogTransactions()) log(e);
    }

    private void log(BPAfterTransactionEvent e) {
        BPLogUtils logUtils = BankPlus.INSTANCE().getBpLogUtils();
        File logFile = logUtils.getLogFile();

        OfflinePlayer p = e.getPlayer();
        Pair<BigDecimal, Double> pair = logHolder.remove(p.getUniqueId());

        if (logFile == null) return;

        if (logUtils.checkDayChanged()) {
            logUtils.setupLoggerFile();
            log(e);
            return;
        }

        String date = new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis());
        StringBuilder builder = new StringBuilder(date + " | " + p.getName() + " -> ");

        TransactionType type = e.getTransactionType();

        builder.append(type.name());

        if (type.equals(TransactionType.DEPOSIT)) {
            BigDecimal taxes = Values.CONFIG.getDepositTaxes();
            if (taxes.doubleValue() > 0)
                builder.append(" (Taxes: ")
                    .append(taxes)
                    .append("%)");
        }

        if (type.equals(TransactionType.WITHDRAW)) {
            BigDecimal taxes = Values.CONFIG.getWithdrawTaxes();
            if (taxes.doubleValue() > 0)
                builder.append(" (Taxes: ")
                        .append(taxes)
                        .append("%)");
        }

        builder.append(" - ")
                .append(BPFormatter.formatBigDouble(e.getTransactionAmount()))
                .append(" [")
                .append(e.getBankName())
                .append("] -> Bank bal before/after: [")
                .append(BPFormatter.formatBigDouble(pair.getKey()))
                .append("/")
                .append(BPFormatter.formatBigDouble(e.getNewBalance()))
                .append("] -> Vault bal before/after: [")
                .append(BPFormatter.format(pair.getValue()))
                .append("/")
                .append(BPFormatter.format(e.getNewVaultBalance()))
                .append("]\n");

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            bw.append(builder.toString());
            bw.close();
        } catch (IOException ex) {
            BPLogger.error("Could not write transaction logs to the file: " + ex.getMessage());
        }
    }
}