package me.pulsi_.bankplus.loanSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

public class LoanUtils {

    public static void sendRequest(Player from, Player to, BigDecimal amount, String fromBankName, String toBankName) {
        BPMessages.send(to, "Loan-Request-Received", BPMethods.placeValues(from, amount));
        BPMessages.send(from, "Loan-Request-Sent", "%player%$" + to.getName());

        BankPlus.INSTANCE.getLoanRegistry().getRequestsReceived().put(to.getUniqueId(), from.getUniqueId());
        BankPlus.INSTANCE.getLoanRegistry().getRequestsSent().put(from.getUniqueId(), new BPLoan(to, amount, fromBankName, toBankName));

        Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> {
            BankPlus.INSTANCE.getLoanRegistry().getRequestsReceived().remove(to.getUniqueId());
            BankPlus.INSTANCE.getLoanRegistry().getRequestsSent().remove(from.getUniqueId());
        }, Values.CONFIG.getLoanAcceptTime() * 20l);

    }

    public static void acceptRequest(Player p) {
        if (!hasRequest(p)) {
            BPMessages.send(p, "No-Loan-Requests");
            return;
        }

        LoanRegistry registry = BankPlus.INSTANCE.getLoanRegistry();
        HashMap<UUID, UUID> received = registry.getRequestsReceived();
        HashMap<UUID, BPLoan> sent = registry.getRequestsSent();

        Player sender = Bukkit.getPlayer(received.get(p.getUniqueId()));
        BPLoan loan = sent.get(sender.getUniqueId());

        received.remove(p.getUniqueId());
        sent.remove(sender.getUniqueId());

        BPMessages.send(p, "Loan-Request-Received-Accepted", BPMethods.placeValues(sender, loan.getAmount()));
        BPMessages.send(sender, "Loan-Request-Sent-Accepted", "%player%$" + p.getName());



    }

    public static void denyRequest(Player p) {
        if (!hasRequest(p)) {
            BPMessages.send(p, "No-Loan-Requests");
            return;
        }

        LoanRegistry registry = BankPlus.INSTANCE.getLoanRegistry();
        HashMap<UUID, UUID> received = registry.getRequestsReceived();

        Player sender = Bukkit.getPlayer(received.get(p.getUniqueId()));

        received.remove(p.getUniqueId());
        registry.getRequestsSent().remove(sender.getUniqueId());

        BPMessages.send(p, "Loan-Request-Received-Denied", "%player%$" + sender.getName());
        BPMessages.send(sender, "Loan-Request-Sent-Denied", "%player%$" + p.getName());
    }

    public static void cancelRequest(Player p) {
        if (!sentRequest(p)) {
            BPMessages.send(p, "No-Loan-Sent");
            return;
        }

        LoanRegistry registry = BankPlus.INSTANCE.getLoanRegistry();
        HashMap<UUID, BPLoan> sent = registry.getRequestsSent();

        Player receiver = Bukkit.getPlayer(sent.get(p.getUniqueId()).getTarget().getUniqueId());

        sent.remove(p.getUniqueId());
        registry.getRequestsReceived().remove(receiver.getUniqueId());

        BPMessages.send(receiver, "Loan-Request-Received-Cancelled", "%player%$" + p.getName());
        BPMessages.send(p, "Loan-Request-Sent-Cancelled");
    }

    public static boolean hasRequest(Player p) {
        return BankPlus.INSTANCE.getLoanRegistry().getRequestsReceived().containsKey(p.getUniqueId());
    }

    public static boolean sentRequest(Player p) {
        return BankPlus.INSTANCE.getLoanRegistry().getRequestsSent().containsKey(p.getUniqueId());
    }
}