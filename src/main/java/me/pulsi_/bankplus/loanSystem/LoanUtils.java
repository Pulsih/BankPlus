package me.pulsi_.bankplus.loanSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.List;

public class LoanUtils {

    /**
     * Initialize a loan request, the other player will need to accept the request to process the loan.
     *
     * @param sender       The player sender.
     * @param receiver     The player receiver.
     * @param amount       The loan amount.
     * @param senderBank   The sender bank from where to take the money.
     * @param receiverBank The receiver bank where the money will be deposited.
     * @param action       The request action between give and request
     */
    public static void sendRequest(Player sender, Player receiver, BigDecimal amount, Bank senderBank, Bank receiverBank, String action) {
        BigDecimal senderBalance = senderBank.getBankEconomy().getBankBalance(sender);
        if (senderBalance.compareTo(BigDecimal.ZERO) <= 0) {
            BPMessages.sendIdentifier(sender, "Insufficient-Money");
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
            BigDecimal capacity = BankUtils.getCapacity(loan.getReceiverBank(), receiver);
            if (moneyToReturn.compareTo(capacity) > 0) {
                BPMessages.sendIdentifier(sender, "Cannot-Afford-Loan-Others", "%player%$" + receiver.getName());
                return;
            }

            BPMessages.sendIdentifier(receiver, "Loan-Give-Request-Received", BPUtils.placeValues(sender, amount));
            request.setSenderIsReceiver(true);
        } else { // "request"
            BigDecimal capacity = BankUtils.getCapacity(senderBank, sender);
            if (moneyToReturn.compareTo(capacity) > 0) {
                BPMessages.sendIdentifier(sender, "Cannot-Afford-Loan");
                return;
            }

            BPMessages.sendIdentifier(receiver, "Loan-Request-Received", BPUtils.placeValues(sender, amount));
            request.setSenderIsReceiver(false);
        }

        BPMessages.sendIdentifier(sender, "Loan-Request-Sent", "%player%$" + receiver.getName());
        BPLoanRegistry.queueLoanRequest(sender, request);
    }

    /**
     * Accept the request.
     *
     * @param receiver The player that has typed /bp loan accept.
     */
    public static void acceptRequest(Player receiver) {
        if (!hasRequest(receiver)) {
            BPMessages.sendIdentifier(receiver, "No-Loan-Requests");
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
            List<String> replacers = BPUtils.placeValues(sender, amount);
            replacers.addAll(BPUtils.placeValues(extra, "extra"));
            BPMessages.sendIdentifier(receiver, "Received-Loan-Full", replacers);
        } else {
            receiverBankEconomy.addBankBalance(receiver, amount, TransactionType.LOAN);
            BPMessages.sendIdentifier(receiver, "Received-Loan", BPUtils.placeValues(sender, amount));
        }
        BPMessages.sendIdentifier(sender, "Given-Loan", BPUtils.placeValues(receiver, amount));

        BPLoanRegistry.registerLoan(loan);
    }

    /**
     * Deny a loan request received.
     *
     * @param p The player that received a loan request.
     */
    public static void denyRequest(Player p) {
        if (!hasRequest(p)) {
            BPMessages.sendIdentifier(p, "No-Loan-Requests");
            return;
        }

        Player sender = getLoanSenderFromRequest(p);
        if (sender == null) return;

        BPLoanRegistry.getRequests().remove(sender.getUniqueId());
        BPMessages.sendIdentifier(p, "Loan-Request-Received-Denied", "%player%$" + sender.getName());
        BPMessages.sendIdentifier(sender, "Loan-Request-Sent-Denied", "%player%$" + p.getName());
    }

    /**
     * Cancel a loan request sent.
     *
     * @param p The player that sent the request.
     */
    public static void cancelRequest(Player p) {
        if (!hasSentRequest(p)) {
            BPMessages.sendIdentifier(p, "No-Loan-Sent");
            return;
        }

        Player sender = getLoanSenderFromRequest(p);
        if (sender == null) return;

        BPLoanRegistry.getRequests().remove(sender.getUniqueId());
        BPMessages.sendIdentifier(p, "Loan-Request-Received-Cancelled", "%player%$" + p.getName());
        BPMessages.sendIdentifier(sender, "Loan-Request-Sent-Cancelled");
    }

    /**
     * Create and process instantly a loan request, used for bank-to-player loans.
     *
     * @param receiver   The player that will receive the money.
     * @param bankSender The bank that will give the loan. It is the same where the money will be deposited for that player.
     * @param amount     The loan amount.
     */
    public static void sendLoan(Player receiver, Bank bankSender, BigDecimal amount) {
        BPEconomy bankSenderEconomy = bankSender.getBankEconomy();
        BigDecimal receiverBalance = bankSenderEconomy.getBankBalance(receiver);

        if (receiverBalance.compareTo(amount) < 0) amount = receiverBalance;

        BPLoan loan = new BPLoan(receiver, bankSender, amount);

        BigDecimal receiverCapacity = BankUtils.getCapacity(bankSender, receiver);
        if (ConfigValues.isLoanCheckEnoughMoney())
            if (loan.getMoneyToReturn().compareTo(receiverCapacity) > 0) {
                BPMessages.sendIdentifier(receiver, "Cannot-Afford-Loan");
                return;
            }

        // If the bank is full, instead of loosing money they will be added to the vault balance
        if (receiverBalance.add(amount).compareTo(receiverCapacity) >= 0 && receiverCapacity.compareTo(BigDecimal.ZERO) > 0) {
            bankSenderEconomy.setBankBalance(receiver, receiverCapacity, TransactionType.LOAN);
            BigDecimal extra = amount.subtract(receiverCapacity.subtract(receiverBalance));

            BankPlus.INSTANCE().getVaultEconomy().depositPlayer(receiver, extra.doubleValue());

            List<String> replacers = BPUtils.placeValues(bankSender.getIdentifier(), amount);
            replacers.addAll(BPUtils.placeValues(extra, "extra"));
            BPMessages.sendIdentifier(receiver, "Received-Loan-Full-Bank", replacers);
        } else {
            bankSenderEconomy.addBankBalance(receiver, amount, TransactionType.LOAN);
            BPMessages.sendIdentifier(receiver, "Received-Loan-Bank", BPUtils.placeValues(bankSender.getIdentifier(), amount));
        }

        BPLoanRegistry.registerLoan(loan);
    }

    /**
     * Check if the selected player has any loan request.
     *
     * @param p The player.
     * @return true if the player has a request, false otherwise.
     */
    public static boolean hasRequest(Player p) {
        boolean hasRequest = false;
        for (BPLoanRegistry.LoanRequest request : BPLoanRegistry.getRequests().values()) {
            if (!request.getReceiver().equals(p)) continue;
            hasRequest = true;
            break;
        }
        return hasRequest;
    }

    /**
     * Checks if the selected player has sent a request.
     *
     * @param p The player.
     * @return true if he sent a request, false otherwise.
     */
    public static boolean hasSentRequest(Player p) {
        return BPLoanRegistry.getRequests().containsKey(p.getUniqueId());
    }

    /**
     * Loop through all the request, check if the player p is the loan target, if yes, get the sender from that loan request.
     *
     * @param p The player that received a request.
     * @return The player that sent the loan request.
     */
    public static Player getLoanSenderFromRequest(Player p) {
        for (BPLoanRegistry.LoanRequest request : BPLoanRegistry.getRequests().values())
            if (request.getReceiver().equals(p)) return request.getSender();
        return null;
    }
}