package me.pulsi_.bankplus.loanSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class LoanUtils {

    public static void sendRequest(Player from, Player to, BigDecimal amount, String fromBankName, String toBankName, String action) {
        BigDecimal fBal = BPEconomy.getBankBalance(from, fromBankName);
        if (fBal.doubleValue() <= 0d) {
            BPMessages.send(from, "Insufficient-Money");
            return;
        }

        if (fBal.doubleValue() < amount.doubleValue()) amount = fBal;

        LoanRegistry.BPRequest request = new LoanRegistry.BPRequest();
        request.setSender(from);
        request.setTarget(to);

        BPLoan loan = new BPLoan(from, to, amount, fromBankName, toBankName);
        request.setLoan(loan);

        if (action.equals("give")) {
            BigDecimal capacity = BankManager.getCapacity(loan.getToBankName(), to);
            if (loan.getMoneyToReturn().doubleValue() > capacity.doubleValue()) {
                BPMessages.send(from, "Cannot-Afford-Loan-Others", "%player%$" + to.getName());
                return;
            }
            BPMessages.send(to, "Loan-Give-Request-Received", BPUtils.placeValues(from, amount));
            request.setLoanSender(true);
        } else {
            BigDecimal capacity = BankManager.getCapacity(loan.getFromBankName(), from);
            if (loan.getMoneyToReturn().doubleValue() > capacity.doubleValue()) {
                BPMessages.send(from, "Cannot-Afford-Loan");
                return;
            }
            BPMessages.send(to, "Loan-Request-Received", BPUtils.placeValues(from, amount));
            request.setLoanSender(false);
        }
        BPMessages.send(from, "Loan-Request-Sent", "%player%$" + to.getName());

        LoanRegistry registry = BankPlus.INSTANCE().getLoanRegistry();
        registry.getRequests().put(from.getUniqueId(), request);

        Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> registry.getRequests().remove(to.getUniqueId()), Values.CONFIG.getLoanAcceptTime() * 20L);
    }

    public static void acceptRequest(Player p) {
        if (!hasRequest(p)) {
            BPMessages.send(p, "No-Loan-Requests");
            return;
        }

        LoanRegistry registry = BankPlus.INSTANCE().getLoanRegistry();

        Player sender = getLoanSenderTargetPlayer(p);
        if (sender == null) return;

        LoanRegistry.BPRequest request = registry.getRequests().remove(sender.getUniqueId());
        BPLoan loan = request.getLoan();
        BigDecimal amount = loan.getMoneyGiven();

        if (!request.isLoanSender()) {
            Player tempPlayer = sender;
            sender = p;
            p = tempPlayer;
            loan.setSender(p);
            loan.setReceiver(sender);
        }

        BPEconomy.removeBankBalance(sender, amount, loan.getFromBankName()); // Already checked that the amount isn't > than the balance.

        BigDecimal capacity = BankManager.getCapacity(loan.getToBankName(), p), balance = BPEconomy.getBankBalance(p, loan.getToBankName());
        // If the bank is full, instead of loosing money they will be added to the vault balance
        if (balance.add(amount).doubleValue() >= capacity.doubleValue() && capacity.doubleValue() > 0d) {
            BPEconomy.setBankBalance(p, capacity, loan.getToBankName(), TransactionType.LOAN);
            BigDecimal extra = amount.subtract(capacity.subtract(balance));

            BankPlus.INSTANCE().getVaultEconomy().depositPlayer(p, extra.doubleValue());
            BPMessages.send(p, "Received-Loan-Full", BPUtils.placeValues(sender, amount), BPUtils.placeValues(extra, "extra"));
        } else {
            BPEconomy.addBankBalance(p, amount, loan.getToBankName(), TransactionType.LOAN);
            BPMessages.send(p, "Received-Loan", BPUtils.placeValues(sender, amount));
        }
        BPMessages.send(sender, "Given-Loan", BPUtils.placeValues(p, amount));
        registry.getLoans().add(loan);
        startLoanTask(loan);
    }

    public static void denyRequest(Player p) {
        if (!hasRequest(p)) {
            BPMessages.send(p, "No-Loan-Requests");
            return;
        }

        Player sender = getLoanSenderTargetPlayer(p);
        if (sender == null) return;

        BankPlus.INSTANCE().getLoanRegistry().getRequests().remove(sender.getUniqueId());
        BPMessages.send(p, "Loan-Request-Received-Denied", "%player%$" + sender.getName());
        BPMessages.send(sender, "Loan-Request-Sent-Denied", "%player%$" + p.getName());
    }

    public static void cancelRequest(Player p) {
        if (!hasSentRequest(p)) {
            BPMessages.send(p, "No-Loan-Sent");
            return;
        }

        Player sender = getLoanSenderTargetPlayer(p);
        if (sender == null) return;

        BankPlus.INSTANCE().getLoanRegistry().getRequests().remove(sender.getUniqueId());
        BPMessages.send(p, "Loan-Request-Received-Cancelled", "%player%$" + p.getName());
        BPMessages.send(sender, "Loan-Request-Sent-Cancelled");
    }

    public static void sendLoan(Player receiver, String fromBank, BigDecimal amount) {
        BigDecimal balance = BPEconomy.getBankBalance(receiver, fromBank);
        if (balance.doubleValue() <= 0d) {
            BPMessages.send(receiver, "Insufficient-Money");
            return;
        }

        if (balance.doubleValue() < amount.doubleValue()) amount = balance;

        BPLoan loan = new BPLoan(receiver, fromBank, amount);

        BigDecimal capacity = BankManager.getCapacity(fromBank, receiver);
        if (loan.getMoneyToReturn().doubleValue() > capacity.doubleValue()) {
            BPMessages.send(receiver, "Cannot-Afford-Loan");
            return;
        }

        // If the bank is full, instead of loosing money they will be added to the vault balance
        if (balance.add(amount).doubleValue() >= capacity.doubleValue() && capacity.doubleValue() > 0d) {
            BPEconomy.setBankBalance(receiver, capacity, fromBank, TransactionType.LOAN);
            BigDecimal extra = amount.subtract(capacity.subtract(balance));
            BankPlus.INSTANCE().getVaultEconomy().depositPlayer(receiver, extra.doubleValue());
            BPMessages.send(receiver, "Received-Loan-Full-Bank", BPUtils.placeValues(fromBank, amount), BPUtils.placeValues(extra, "extra"));
        } else {
            BPEconomy.addBankBalance(receiver, amount, fromBank, TransactionType.LOAN);
            BPMessages.send(receiver, "Received-Loan-Bank", BPUtils.placeValues(fromBank, amount));
        }

        BankPlus.INSTANCE().getLoanRegistry().getLoans().add(loan);
        startLoanTask(loan);
    }

    public static void startLoanTask(BPLoan loan) {
        int delay = loan.getTimeLeft() <= 0 ? Values.CONFIG.getLoanDelay() : BPUtils.millisecondsInTicks(loan.getTimeLeft());
        loan.setTask(Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> loanTask(loan), delay));
    }

    private static void loanTask(BPLoan loan) {
        loan.setTimeLeft(System.currentTimeMillis() + BPUtils.ticksInMilliseconds(Values.CONFIG.getLoanDelay()));
        loan.setInstalmentsPoint(loan.getInstalmentsPoint() + 1);
        int instalments = loan.getInstalments();

        BigDecimal amount = loan.getMoneyToReturn().divide(BigDecimal.valueOf(instalments));
        OfflinePlayer sender = loan.getSender(), receiver = loan.getReceiver();
        boolean isPlayerToBank = sender == null;

        String fromBank = loan.getFromBankName() == null ? loan.getRequestedBank() : loan.getFromBankName(),
                toBank = loan.getToBankName() == null ? loan.getRequestedBank() : loan.getToBankName();

        // Means that the loan has been requested from a player to another player, so we need to give back the money.
        // If the sender == null, means that the loan has been requested from a player to a bank, so we just remove the money to the player receiver.
        if (!isPlayerToBank) {
            // Add back "amount" to the sender of the loan.
            BigDecimal addedToSender = BPEconomy.addBankBalance(sender, amount, fromBank, TransactionType.LOAN), extra = amount.subtract(addedToSender);
            if (extra.doubleValue() <= 0) BPMessages.send(sender, "Loan-Payback", BPUtils.placeValues(receiver, addedToSender));
            else {
                BPMessages.send(sender, "Loan-Payback-Full", BPUtils.placeValues(amount), BPUtils.placeValues(receiver, extra, "extra"));
                BankPlus.INSTANCE().getVaultEconomy().depositPlayer(sender, extra.doubleValue());
            }
        }

        // Remove "amount" from the receiver of the loan.
        BigDecimal removedToReceiver = BPEconomy.removeBankBalance(receiver, amount, toBank, TransactionType.LOAN), debt = amount.subtract(removedToReceiver);
        if (debt.doubleValue() <= 0D) {
            if (isPlayerToBank) BPMessages.send(receiver, "Loan-Returned-Bank", BPUtils.placeValues(loan.getRequestedBank(), amount));
            else BPMessages.send(receiver, "Loan-Returned", BPUtils.placeValues(sender, amount));
        } else {
            BigDecimal newDebt = BPEconomy.getDebt(receiver).add(debt);
            if (isPlayerToBank) BPMessages.send(receiver, "Loan-Returned-Debt-Bank", BPUtils.placeValues(loan.getRequestedBank(), newDebt));
            else BPMessages.send(receiver, "Loan-Returned-Debt", BPUtils.placeValues(sender, newDebt));
            BPEconomy.setDebt(receiver, newDebt, loan.getRequestedBank());
        }

        // Was the loan at his final instalment?
        if (loan.getInstalmentsPoint() >= instalments) BankPlus.INSTANCE().getLoanRegistry().getLoans().remove(loan);
        else loan.setTask(Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> loanTask(loan), Values.CONFIG.getLoanDelay()));
    }

    public static boolean hasRequest(Player p) {
        boolean hasRequest = false;
        for (LoanRegistry.BPRequest request : BankPlus.INSTANCE().getLoanRegistry().getRequests().values()) {
            if (!request.getTarget().equals(p)) continue;
            hasRequest = true;
            break;
        }
        return hasRequest;
    }

    public static boolean hasSentRequest(Player p) {
        return BankPlus.INSTANCE().getLoanRegistry().getRequests().containsKey(p.getUniqueId());
    }

    public static Player getLoanSenderTargetPlayer(Player p) {
        for (LoanRegistry.BPRequest request : BankPlus.INSTANCE().getLoanRegistry().getRequests().values())
            if (request.getTarget().equals(p)) return request.getSender();
        return null;
    }
}