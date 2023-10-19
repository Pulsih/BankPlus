package me.pulsi_.bankplus.loanSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

public class LoanUtils {

    public static void sendRequest(Player from, Player to, BigDecimal amount, String fromBankName, String toBankName, String action) {
        BigDecimal fBal = BankPlus.getBPEconomy().getBankBalance(from, fromBankName);
        if (fBal.doubleValue() < amount.doubleValue()) amount = fBal;

        BPLoan loan = new BPLoan(from, to, amount, fromBankName, toBankName);

        if (action.equals("give")) {
            BigDecimal capacity = new BankReader(loan.getToBankName()).getCapacity(to);
            if (loan.getMoneyToReturn().doubleValue() > capacity.doubleValue()) {
                BPMessages.send(from, "Cannot-Afford-Loan-Others", "%player%$" + to.getName());
                return;
            }
            BPMessages.send(to, "Loan-Give-Request-Received", BPUtils.placeValues(from, amount));
        } else {
            BigDecimal capacity = new BankReader(loan.getFromBankName()).getCapacity(from);
            if (loan.getMoneyToReturn().doubleValue() > capacity.doubleValue()) {
                BPMessages.send(from, "Cannot-Afford-Loan");
                return;
            }
            BPMessages.send(to, "Loan-Request-Received", BPUtils.placeValues(from, amount));
        }
        BPMessages.send(from, "Request-Sent", "%player%$" + to.getName());

        BankPlus.INSTANCE.getLoanRegistry().getRequestsReceived().put(to.getUniqueId(), from.getUniqueId());
        BankPlus.INSTANCE.getLoanRegistry().getRequestsSent().put(from.getUniqueId(), loan);

        Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> {
            BankPlus.INSTANCE.getLoanRegistry().getRequestsReceived().remove(to.getUniqueId());
            BankPlus.INSTANCE.getLoanRegistry().getRequestsSent().remove(from.getUniqueId());
        }, Values.CONFIG.getLoanAcceptTime() * 20L);
    }

    public static void acceptRequest(Player p) {
        if (!hasRequest(p)) {
            BPMessages.send(p, "No-Loan-Requests");
            return;
        }

        BPEconomy economy = BankPlus.getBPEconomy();
        LoanRegistry registry = BankPlus.INSTANCE.getLoanRegistry();
        HashMap<UUID, UUID> received = registry.getRequestsReceived();
        HashMap<UUID, BPLoan> sent = registry.getRequestsSent();

        Player sender = Bukkit.getPlayer(received.get(p.getUniqueId()));
        BPLoan loan = sent.get(sender.getUniqueId());

        received.remove(p.getUniqueId());
        sent.remove(sender.getUniqueId());

        BigDecimal amount = loan.getMoneyGiven();
        BPMessages.send(sender, "Loan-Request-Sent-Accepted", "%player%$" + p.getName());

        economy.removeBankBalance(sender, amount, loan.getFromBankName()); // Already checked that the amount isn't > than the balance.
        BigDecimal capacity = new BankReader(loan.getToBankName()).getCapacity(p), balance = economy.getBankBalance(p, loan.getToBankName());

        // If the bank is full, instead of loosing money they will be added to the vault balance
        if (balance.add(amount).doubleValue() >= capacity.doubleValue() && capacity.doubleValue() > 0d) {
            economy.setBankBalance(p, capacity, loan.getToBankName(), TransactionType.LOAN);
            BigDecimal extra = amount.subtract(capacity.subtract(balance));
            BankPlus.INSTANCE.getVaultEconomy().depositPlayer(p, extra.doubleValue());
            BPMessages.send(p, "Loan-Request-Received-Accepted-Full", BPUtils.placeValues(sender, amount), BPUtils.placeValues(extra, "extra"));
        } else {
            economy.addBankBalance(p, amount, loan.getToBankName(), TransactionType.LOAN);
            BPMessages.send(p, "Loan-Request-Received-Accepted", BPUtils.placeValues(sender, amount));
        }
        registry.getLoans().add(loan);
        startLoanTask(loan);
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
        if (!hasSentRequest(p)) {
            BPMessages.send(p, "No-Loan-Sent");
            return;
        }

        LoanRegistry registry = BankPlus.INSTANCE.getLoanRegistry();
        HashMap<UUID, BPLoan> sent = registry.getRequestsSent();

        Player receiver = Bukkit.getPlayer(sent.get(p.getUniqueId()).getReceiver().getUniqueId());

        sent.remove(p.getUniqueId());
        registry.getRequestsReceived().remove(receiver.getUniqueId());

        BPMessages.send(receiver, "Loan-Request-Received-Cancelled", "%player%$" + p.getName());
        BPMessages.send(p, "Loan-Request-Sent-Cancelled");
    }

    public static void startLoanTask(BPLoan loan) {
        int delay = loan.getTimeLeft() <= 0 ? Values.CONFIG.getLoanDelay() : BPUtils.millisecondsInTicks(loan.getTimeLeft());
        loan.setTask(Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> loanTask(loan), delay));
    }

    private static void loanTask(BPLoan loan) {
        loan.setTimeLeft(System.currentTimeMillis() + BPUtils.ticksInMilliseconds(Values.CONFIG.getLoanDelay()));
        loan.setInstalmentsPoint(loan.getInstalmentsPoint() + 1);
        int instalments = loan.getInstalments();

        BigDecimal amount = loan.getMoneyToReturn().divide(BigDecimal.valueOf(instalments));
        OfflinePlayer sender = loan.getSender(), target = loan.getReceiver();
        BPEconomy economy = BankPlus.getBPEconomy();

        // Add back "amount" to the sender of the loan.
        BigDecimal addedToSender = economy.addBankBalance(sender, amount, loan.getFromBankName(), TransactionType.LOAN), extra = amount.subtract(addedToSender);
        if (extra.doubleValue() <= 0) BPMessages.send(sender, "Loan-Payback", BPUtils.placeValues(target, addedToSender));
        else {
            BPMessages.send(sender, "Loan-Payback-Full", BPUtils.placeValues(amount), BPUtils.placeValues(target, extra, "extra"));
            BankPlus.INSTANCE.getVaultEconomy().depositPlayer(sender, extra.doubleValue());
        }

        // Remove "amount" from the receiver of the loan.
        BigDecimal removedToReceiver = economy.setBankBalance(target, BigDecimal.valueOf(0), loan.getToBankName(), TransactionType.LOAN), debt = amount.subtract(removedToReceiver);
        if (debt.doubleValue() <= 0D) BPMessages.send(target, "Loan-Returned", BPUtils.placeValues(sender, amount));
        else {
            BPMessages.send(target, "Loan-Returned-Debt", BPUtils.placeValues(sender, debt));
            BankPlus.getBPEconomy().setDebt(target, debt);
        }

        // Was the loan at his final instalment?
        if (loan.getInstalmentsPoint() >= instalments) BankPlus.INSTANCE.getLoanRegistry().getLoans().remove(loan);
        else loan.setTask(Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> loanTask(loan), Values.CONFIG.getLoanDelay()));
    }

    public static boolean hasRequest(Player p) {
        return BankPlus.INSTANCE.getLoanRegistry().getRequestsReceived().containsKey(p.getUniqueId());
    }

    public static boolean hasSentRequest(Player p) {
        return BankPlus.INSTANCE.getLoanRegistry().getRequestsSent().containsKey(p.getUniqueId());
    }
}