package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
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

        BPEconomy economy = BPEconomy.get(e.getBankName());
        BigDecimal debt = economy.getDebt(p);
        TransactionType type = e.getTransactionType();
        if (debt.doubleValue() <= 0d || (type != TransactionType.ADD && type != TransactionType.DEPOSIT && type != TransactionType.INTEREST && type != TransactionType.SET)) return;

        BigDecimal debtLeft = new BigDecimal(0), newTransactionAmount = e.getTransactionAmount().subtract(debt);

        if (newTransactionAmount.doubleValue() < 0d) {
            debtLeft = newTransactionAmount.abs();
            newTransactionAmount = BigDecimal.valueOf(0);
        }

        BPMessages.send(Bukkit.getPlayer(p.getUniqueId()), "Debt-Money-Taken",
                BPUtils.placeValues(e.getTransactionAmount().min(debt)),
                BPUtils.placeValues(debtLeft, "debt")
        );

        economy.setDebt(p, debtLeft);
        e.setTransactionAmount(newTransactionAmount);
    }

    @EventHandler
    public void onTransactionStart(BPPreTransactionEvent e) {
        if (Values.CONFIG.isLogTransactions())
            logHolder.put(e.getPlayer().getUniqueId(), new Pair<>(e.getCurrentBalance(), e.getCurrentVaultBalance()));
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

        if (logFile == null || pair == null) return;

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

        builder.append(
                " - Amount: %1, Bank: %2, Bank bal before-after %3 -> %4, Vault bal before-after: %5 -> %6\n"
                        .replace("%1", BPFormatter.formatBigDecimal(e.getTransactionAmount()))
                        .replace("%2", e.getBankName())
                        .replace("%3", BPFormatter.formatBigDecimal(pair.getKey()))
                        .replace("%4", BPFormatter.formatBigDecimal(e.getNewBalance()))
                        .replace("%5", BPFormatter.format(pair.getValue()))
                        .replace("%6", BPFormatter.format(e.getNewVaultBalance()))
                );

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            bw.append(builder.toString());
            bw.close();
        } catch (IOException ex) {
            BPLogger.error("Could not write transaction logs to the file: " + ex.getMessage());
        }
    }
}