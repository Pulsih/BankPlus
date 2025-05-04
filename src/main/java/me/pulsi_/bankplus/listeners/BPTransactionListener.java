package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.events.BPAfterTransactionEvent;
import me.pulsi_.bankplus.events.BPPreTransactionEvent;
import me.pulsi_.bankplus.logSystem.BPLogUtils;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
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
import java.util.List;
import java.util.UUID;

public class BPTransactionListener implements Listener {

    public static class Pair<K, V> {
        private final K key;
        private final V value;

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final HashMap<UUID, Pair<BigDecimal, Double>> logHolder = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTransactionStart(BPPreTransactionEvent e) {
        if (ConfigValues.isLoggingTransactions())
            logHolder.put(e.getPlayer().getUniqueId(), new Pair<>(e.getCurrentBalance(), e.getCurrentVaultBalance()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTransactionEnd(BPAfterTransactionEvent e) {
        if (ConfigValues.isLoggingTransactions()) log(e);

        BPEconomy economy = BPEconomy.get(e.getBankName());
        if (economy == null) return;

        OfflinePlayer p = e.getPlayer();
        BigDecimal debt = economy.getDebt(p);
        if (debt.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal removed = economy.removeBankBalance(p, debt);
        if (removed.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal newDebt = debt.subtract(removed);
        economy.setDebt(p, newDebt);

        List<String> replacers = BPUtils.placeValues(e.getTransactionAmount().min(debt));
        replacers.addAll(BPUtils.placeValues(newDebt, "debt"));
        BPMessages.send(Bukkit.getPlayer(p.getUniqueId()), "Debt-Money-Taken", replacers);
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
            BigDecimal taxes = ConfigValues.getDepositTaxes();
            if (taxes.doubleValue() > 0) builder.append(" (Taxes: ").append(taxes).append("%)");
        }

        if (type.equals(TransactionType.WITHDRAW)) {
            BigDecimal taxes = ConfigValues.getWithdrawTaxes();
            if (taxes.doubleValue() > 0) builder.append(" (Taxes: ").append(taxes).append("%)");
        }

        builder.append(
                " - %1 [%2] Bank before-after: [%3 -> %4] Vault before-after: [%5 -> %6]\n"
                        .replace("%1", BPFormatter.styleBigDecimal(e.getTransactionAmount()))
                        .replace("%2", e.getBankName())
                        .replace("%3", BPFormatter.styleBigDecimal(pair.getKey()))
                        .replace("%4", BPFormatter.styleBigDecimal(e.getNewBalance()))
                        .replace("%5", pair.getValue() + "")
                        .replace("%6", e.getNewVaultBalance() + "")
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