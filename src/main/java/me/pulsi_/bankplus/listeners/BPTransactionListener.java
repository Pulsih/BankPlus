package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.events.BPAfterTransactionEvent;
import me.pulsi_.bankplus.events.BPPreTransactionEvent;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BPTransactionListener implements Listener {

    public record Pair<K, V>(K key, V value) {

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

        BigDecimal removed = economy.removeBankBalance(p, debt, true);
        if (removed.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal newDebt = debt.subtract(removed);
        economy.setDebt(p, newDebt);

        List<String> replacers = BPUtils.placeValues(e.getTransactionAmount().min(debt));
        replacers.addAll(BPUtils.placeValues(newDebt, "debt"));
        BPMessages.sendIdentifier(Bukkit.getPlayer(p.getUniqueId()), "Debt-Money-Taken", replacers);
    }

    private void log(BPAfterTransactionEvent e) {
        TransactionType type = e.getTransactionType();
        StringBuilder builder = new StringBuilder(e.getPlayer().getName() + " -> " + type.name());

        if (type.equals(TransactionType.DEPOSIT)) {
            BigDecimal taxes = ConfigValues.getDepositTaxes();
            if (taxes.doubleValue() > 0) builder.append(" (Taxes: ").append(taxes).append("%)");
        }

        if (type.equals(TransactionType.WITHDRAW)) {
            BigDecimal taxes = ConfigValues.getWithdrawTaxes();
            if (taxes.doubleValue() > 0) builder.append(" (Taxes: ").append(taxes).append("%)");
        }

        Pair<BigDecimal, Double> pair = logHolder.get(e.getPlayer().getUniqueId());
        builder.append(
                " - %1 [%2] Bank: [%3 -> %4] Vault: [%5 -> %6]\n"
                        .replace("%1", BPFormatter.styleBigDecimal(e.getTransactionAmount()))
                        .replace("%2", e.getBankName())
                        .replace("%3", BPFormatter.styleBigDecimal(pair.key()))
                        .replace("%4", BPFormatter.styleBigDecimal(e.getNewBalance()))
                        .replace("%5", pair.value() + "")
                        .replace("%6", e.getNewVaultBalance() + "")
        );

        BPLogger.LogsFile.log(builder.toString());
    }
}