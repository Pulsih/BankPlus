package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.debt.DebtUtils;
import me.pulsi_.bankplus.events.BPTransactionEvent;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.math.BigDecimal;

public class BPTransactionListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onTransaction(BPTransactionEvent e) {
        OfflinePlayer p = e.getPlayer();

        BigDecimal debt = DebtUtils.getDebt(p);
        if (debt.doubleValue() <= 0d ||
                (!e.getTransactionType().equals(BPTransactionEvent.TransactionType.ADD) &&
                !e.getTransactionType().equals(BPTransactionEvent.TransactionType.DEPOSIT) &&
                !e.getTransactionType().equals(BPTransactionEvent.TransactionType.SET))) return;

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
}