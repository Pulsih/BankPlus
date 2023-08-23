package me.pulsi_.bankplus.listeners;

import javafx.util.Pair;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.debt.DebtUtils;
import me.pulsi_.bankplus.events.BPAfterTransactionEvent;
import me.pulsi_.bankplus.events.BPPreTransactionEvent;
import me.pulsi_.bankplus.logSystem.BPLogUtils;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
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

    private final HashMap<UUID, Pair<BigDecimal, Double>> logHolder = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void processDebt(BPPreTransactionEvent e) {
        OfflinePlayer p = e.getPlayer();

        if (Values.CONFIG.isLogTransactions())
            logHolder.put(p.getUniqueId(), new Pair<>(e.getCurrentBalance(), e.getCurrentVaultBalance()));

        BigDecimal debt = DebtUtils.getDebt(p);
        if (debt.doubleValue() <= 0d ||
                (!e.getTransactionType().equals(BPPreTransactionEvent.TransactionType.ADD) &&
                        !e.getTransactionType().equals(BPPreTransactionEvent.TransactionType.DEPOSIT) &&
                        !e.getTransactionType().equals(BPPreTransactionEvent.TransactionType.SET))) return;

        BigDecimal debtLeft = debt.subtract(e.getTransactionAmount()), newAmount;

        if (debtLeft.doubleValue() > 0d) {
            newAmount = BigDecimal.valueOf(0);
            debtLeft = debt.subtract(e.getTransactionAmount());

            BPMessages.send(Bukkit.getPlayer(p.getUniqueId()), "Debt-Money-Taken",
                    BPMethods.placeValues(e.getTransactionAmount()),
                    BPMethods.placeValues(debtLeft, "debt")
            );
        } else {
            newAmount = debtLeft.multiply(BigDecimal.valueOf(-1));
            debtLeft = BigDecimal.valueOf(0);

            BPMessages.send(Bukkit.getPlayer(p.getUniqueId()), "Debt-Money-Taken",
                    BPMethods.placeValues(newAmount),
                    BPMethods.placeValues(debtLeft, "debt")
            );
        }

        DebtUtils.setDebt(p, debtLeft);
        e.setTransactionAmount(newAmount);
    }

    @EventHandler
    public void onTransactionEnd(BPAfterTransactionEvent e) {
        if (Values.CONFIG.isLogTransactions()) log(e);
    }

    private void log(BPAfterTransactionEvent e) {
        BPLogUtils logUtils = BankPlus.INSTANCE.getBpLogUtils();
        File logFile = logUtils.getLogFile();

        OfflinePlayer p = e.getPlayer();
        Pair<BigDecimal, Double> pair = logHolder.remove(p.getUniqueId());

        if (logFile == null) return;

        if (logUtils.checkDayChanged()) {
            logUtils.setupLoggerFile();
            log(e);
            return;
        }

        String date = new SimpleDateFormat("ss:mm:HH").format(System.currentTimeMillis());
        StringBuilder builder = new StringBuilder(date + " | " + p.getName() + " -> Transaction type: ");

        builder.append(e.getTransactionType().name())
                .append(" - ")
                .append(BPFormatter.formatBigDouble(e.getTransactionAmount()))
                .append(" [")
                .append(e.getBankName())
                .append("] -> Bank balance before - after: ")
                .append(BPFormatter.formatBigDouble(pair.getKey()))
                .append(" - ")
                .append(BPFormatter.formatBigDouble(e.getNewBalance()))
                .append(" -> Vault balance before - after: ")
                .append(pair.getValue())
                .append(" - ")
                .append(e.getNewVaultBalance())
                .append("\n");

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            bw.append(builder.toString());
            bw.close();
        } catch (IOException ex) {
            BPLogger.error("Could not write transaction logs to the file: " + ex.getMessage());
        }
    }
}