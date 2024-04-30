package me.pulsi_.bankplus.loanSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
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

    /**
     * Initialize a load request.
     * @param sender The player sender.
     * @param receiver The player receiver.
     * @param amount The loan amount.
     * @param senderBank The sender bank from where to take the money.
     * @param receiverBank The receiver bank where the money will be deposited.
     * @param action The request action between give and request
     */
    public static void sendRequest(Player sender, Player receiver, BigDecimal amount, Bank senderBank, Bank receiverBank, String action) {
        BigDecimal senderBalance = senderBank.getBankEconomy().getBankBalance(sender);
        if (senderBalance.compareTo(BigDecimal.ZERO) <= 0) {
            BPMessages.send(sender, "Insufficient-Money");
            return;
        }

        if (senderBalance.compareTo(amount) < 0) amount = senderBalance;

        // Initialize the loan request.
        BPLoanRegistry.LoanRequest request = new BPLoanRegistry.LoanRequest();
        request.setSender(sender);
        request.setReceiver(receiver);

        // Initialize the loan object that could be processed later.
        BPLoan loan = new BPLoan(sender, receiver, amount, senderBank, receiverBank);
        request.setLoan(loan);

        BigDecimal moneyToReturn = loan.getMoneyToReturn();
        if (action.equals("give")) {
            BigDecimal capacity = BankUtils.getCapacity(BankUtils.getBank(loan.getReceiverBank()), receiver);
            if (moneyToReturn.compareTo(capacity) > 0) {
                BPMessages.send(sender, "Cannot-Afford-Loan-Others", "%player%$" + receiver.getName());
                return;
            }

            BPMessages.send(receiver, "Loan-Give-Request-Received", BPUtils.placeValues(sender, amount));
            request.setSenderIsReceiver(true);
        } else { // "request"
            BigDecimal capacity = BankUtils.getCapacity(senderBank, sender);
            if (moneyToReturn.compareTo(capacity) > 0) {
                BPMessages.send(sender, "Cannot-Afford-Loan");
                return;
            }

            BPMessages.send(receiver, "Loan-Request-Received", BPUtils.placeValues(sender, amount));
            request.setSenderIsReceiver(false);
        }

        BPMessages.send(sender, "Loan-Request-Sent", "%player%$" + receiver.getName());
        BPLoanRegistry.queueLoanRequest(sender, request);
    }

    /**
     * Accept the request.
     * @param receiver The player that has typed /bp loan accept.
     */
    public static void acceptRequest(Player receiver) {
        if (!hasRequest(receiver)) {
            BPMessages.send(receiver, "No-Loan-Requests");
            return;
        }

        Player sender = getLoanSenderFromRequest(receiver);
        if (sender == null) return;

        BPLoanRegistry.LoanRequest request = BPLoanRegistry.getRequests().remove(sender.getUniqueId());
        BPLoan loan = request.getLoan();
        BigDecimal amount = loan.getMoneyGiven();

        if (!request.senderIsReceiver()) {
            Player tempPlayer = sender;
            sender = receiver;
            receiver = tempPlayer;
            loan.setSender(receiver);
            loan.setReceiver(sender);
        }

        loan.getSenderBank().getBankEconomy().removeBankBalance(sender, amount, TransactionType.LOAN); // Already checked that the amount isn't > than the balance.

        Bank receiverBank = loan.getReceiverBank();
        BPEconomy receiverBankEconomy = receiverBank.getBankEconomy();
        BigDecimal receiverCapacity = BankUtils.getCapacity(receiverBank, receiver), receiverBalance = receiverBankEconomy.getBankBalance(receiver);
        // If the bank is full, instead of loosing money they will be added to the vault balance
        if (receiverBalance.add(amount).compareTo(receiverCapacity) >= 0 && receiverCapacity.compareTo(BigDecimal.ZERO) > 0) {
            receiverBankEconomy.setBankBalance(receiver, receiverCapacity, TransactionType.LOAN);
            BigDecimal extra = amount.subtract(receiverCapacity.subtract(receiverBalance));

            BankPlus.INSTANCE().getVaultEconomy().depositPlayer(receiver, extra.doubleValue());
            BPMessages.send(receiver, "Received-Loan-Full", BPUtils.placeValues(sender, amount), BPUtils.placeValues(extra, "extra"));
        } else {
            receiverBankEconomy.addBankBalance(receiver, amount, TransactionType.LOAN);
            BPMessages.send(receiver, "Received-Loan", BPUtils.placeValues(sender, amount));
        }
        BPMessages.send(sender, "Given-Loan", BPUtils.placeValues(receiver, amount));

        registry.getLoans().add(loan);
        startLoanTask(loan);
    }

    public static void denyRequest(Player p) {
        if (!hasRequest(p)) {
            BPMessages.send(p, "No-Loan-Requests");
            return;
        }

        Player sender = getLoanSenderFromRequest(p);
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

        Player sender = getLoanSenderFromRequest(p);
        if (sender == null) return;

        BankPlus.INSTANCE().getLoanRegistry().getRequests().remove(sender.getUniqueId());
        BPMessages.send(p, "Loan-Request-Received-Cancelled", "%player%$" + p.getName());
        BPMessages.send(sender, "Loan-Request-Sent-Cancelled");
    }

    public static void sendLoan(Player receiver, String fromBank, BigDecimal amount) {
        BigDecimal balance = BPEconomy.get(fromBank).getBankBalance(receiver);
        if (balance.doubleValue() <= 0d) {
            BPMessages.send(receiver, "Insufficient-Money");
            return;
        }

        if (balance.compareTo(amount) < 0) amount = balance;

        BPLoan loan = new BPLoan(receiver, fromBank, amount);

        BigDecimal capacity = BankUtils.getCapacity(fromBank, receiver);
        if (loan.getMoneyToReturn().doubleValue() > capacity.doubleValue()) {
            BPMessages.send(receiver, "Cannot-Afford-Loan");
            return;
        }

        // If the bank is full, instead of loosing money they will be added to the vault balance
        if (balance.add(amount).doubleValue() >= capacity.doubleValue() && capacity.doubleValue() > 0d) {
            BPEconomy.get(loan.getReceiverBank()).setBankBalance(receiver, capacity, TransactionType.LOAN);
            BigDecimal extra = amount.subtract(capacity.subtract(balance));
            BankPlus.INSTANCE().getVaultEconomy().depositPlayer(receiver, extra.doubleValue());
            BPMessages.send(receiver, "Received-Loan-Full-Bank", BPUtils.placeValues(fromBank, amount), BPUtils.placeValues(extra, "extra"));
        } else {
            BPEconomy.get(loan.getReceiverBank()).addBankBalance(receiver, amount, TransactionType.LOAN);
            BPMessages.send(receiver, "Received-Loan-Bank", BPUtils.placeValues(fromBank, amount));
        }

        BankPlus.INSTANCE().getLoanRegistry().getLoans().add(loan);
        startLoanTask(loan);
    }

    public static boolean hasRequest(Player p) {
        boolean hasRequest = false;
        for (BPLoanRegistry.LoanRequest request : BankPlus.INSTANCE().getLoanRegistry().getRequests().values()) {
            if (!request.getReceiver().equals(p)) continue;
            hasRequest = true;
            break;
        }
        return hasRequest;
    }

    /**
     * Checks if the selected player has sent a request.
     * @param p The player.
     * @return true if he sent a request, false otherwise.
     */
    public static boolean hasSentRequest(Player p) {
        return BPLoanRegistry.getRequests().containsKey(p.getUniqueId());
    }

    /**
     * Loop through all the request, check if the player p is the loan target, if yes, get the sender from that loan request.
     * @param p The player that received a request.
     * @return The player that sent the loan request.
     */
    public static Player getLoanSenderFromRequest(Player p) {
        for (BPLoanRegistry.LoanRequest request : BPLoanRegistry.getRequests().values())
            if (request.getReceiver().equals(p)) return request.getSender();
        return null;
    }
}