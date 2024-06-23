package me.pulsi_.bankplus.loanSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BPLoanRegistry {

    private static final List<BPLoan> loans = new ArrayList<>();
    private static final HashMap<UUID, LoanRequest> requests = new HashMap<>();

    /**
     * List to track each loan made by players.
     *
     * @return A list of loans.
     */
    public static List<BPLoan> getLoans() {
        return loans;
    }

    /**
     * K = The player UUID that initialized the request.
     * V = The loan request object.
     */
    public static HashMap<UUID, LoanRequest> getRequests() {
        return requests;
    }

    /**
     * Loan storage style:
     * loans:
     * loan-receiver-UUID:
     * sender: loan-sender-UUID
     * loan-values ....
     */
    public static void loadAllLoans() {
        FileConfiguration saves = BankPlus.INSTANCE().getConfigs().getConfig("saves.yml");

        ConfigurationSection section = saves.getConfigurationSection("loans");
        if (section == null) return;

        for (String receiverUUID : section.getKeys(false)) {
            ConfigurationSection values = section.getConfigurationSection(receiverUUID);
            if (values == null) continue;

            OfflinePlayer receiver, sender = null;

            try {
                receiver = Bukkit.getOfflinePlayer(UUID.fromString(receiverUUID));

                String senderUUID = values.getString("sender");
                if (senderUUID != null) sender = Bukkit.getOfflinePlayer(UUID.fromString(senderUUID));
            } catch (IllegalArgumentException e) {
                BPLogger.warn(e, "Could not load \"" + receiverUUID + "\" loan! (Invalid UUID specified)");
                continue;
            }

            String moneyToReturn = values.getString("money-to-return");
            if (moneyToReturn == null || BPUtils.isInvalidNumber(moneyToReturn)) {
                BPLogger.warn("Could not load \"" + receiverUUID + "\" loan! (An invalid money-to-return amount has been specified)");
                continue;
            }

            BPLoan loan;

            String requestedBank = values.getString("requested-bank");
            if (requestedBank != null) {
                if (!BankUtils.exist(requestedBank)) {
                    BPLogger.warn("The loan \"" + receiverUUID + "\" specified an invalid bank to take the money, using the main bank.");
                    requestedBank = ConfigValues.getMainGuiName();
                }

                loan = new BPLoan(receiver, BankUtils.getBank(requestedBank));
            } else {

                String fromBank = ConfigValues.getMainGuiName(), toBank = ConfigValues.getMainGuiName();
                String fromBankString = values.getString("from"), toBankString = values.getString("to");

                if (fromBankString == null) BPLogger.warn("The loan \"" + receiverUUID + "\" did not specify a bank to take the money, using the main bank.");
                else if (!BankUtils.exist(fromBankString)) {
                    BPLogger.warn("The loan \"" + receiverUUID + "\" specified an invalid bank to take the money, using the main bank.");
                    fromBank = fromBankString;
                }

                if (toBankString == null) BPLogger.warn("The loan \"" + receiverUUID + "\" did not specify a bank to give the money, using the main bank.");
                else if (!BankUtils.exist(toBankString)) {
                    BPLogger.warn("The loan \"" + receiverUUID + "\" specified an invalid bank to give the money, using the main bank.");
                    toBank = toBankString;
                }

                loan = new BPLoan(sender, receiver, BankUtils.getBank(fromBank), BankUtils.getBank(toBank));
            }

            loan.setMoneyToReturn(new BigDecimal(moneyToReturn));
            loan.setInstalments(values.getInt("instalments"));
            loan.setInstalmentsPoint(values.getInt("instalments-point"));

            registerLoan(loan);
        }
    }

    /**
     * Save all currently registered loans.
     * @param savesConfig The saves file where to save the loans.
     */
    public static void saveAllLoans(FileConfiguration savesConfig) {
        for (BPLoan loan : loans) {
            String path = "loans." + loan.getReceiver().getUniqueId() + ".";

            if (loan.getSender() != null) savesConfig.set(path + "sender", loan.getSender().getUniqueId());
            savesConfig.set(path + "money-to-return", loan.getMoneyToReturn());
            savesConfig.set(path + "instalments", loan.getInstalments());
            savesConfig.set(path + "instalments-point", loan.getInstalmentsPoint());
            savesConfig.set(path + "time-left", loan.getTimeLeft());
            savesConfig.set(path + "from", loan.getSenderBank());
            savesConfig.set(path + "to", loan.getReceiverBank());
            savesConfig.set(path + "requested-bank", loan.getRequestedBank());
        }
    }

    /**
     * Register the loan to the loan registry and start the returning task.
     * * Note: This method does not give the initial money to the player, you'll have to add them manually.
     *
     * @param loan The loan to register.
     */
    public static void registerLoan(BPLoan loan) {
        loans.add(loan);

        // Check that because not every loan is brand new, there could be loans that have a different time left.
        int delay = loan.getTimeLeft() <= 0 ? ConfigValues.getLoanDelay() : BPUtils.millisecondsInTicks(loan.getTimeLeft());
        loan.setTask(Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> advanceReturningTask(loan), delay));
    }

    /**
     * Queue the loan request for the specified time.
     *
     * @param sender      The sender of the request.
     * @param loanRequest The loan request.
     */
    public static void queueLoanRequest(Player sender, LoanRequest loanRequest) {
        UUID uuid = sender.getUniqueId();
        requests.put(uuid, loanRequest);
        Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> requests.remove(uuid), ConfigValues.getLoanAcceptTime() * 20L);
    }

    private static void advanceReturningTask(BPLoan loan) {
        loan.setTimeLeft(System.currentTimeMillis() + BPUtils.ticksInMilliseconds(ConfigValues.getLoanDelay()));
        loan.setInstalmentsPoint(loan.getInstalmentsPoint() + 1);
        int instalments = loan.getInstalments();

        BigDecimal currentTaskAmount = loan.getMoneyToReturn().divide(BigDecimal.valueOf(instalments));
        OfflinePlayer sender = loan.getSender(), receiver = loan.getReceiver();
        boolean isBankToPlayer = sender == null;

        Bank senderBank = loan.getSenderBank() == null ? loan.getRequestedBank() : loan.getSenderBank(),
                receiverBank = loan.getReceiverBank() == null ? loan.getRequestedBank() : loan.getReceiverBank();

        // Means that the loan has been requested from a player to another player, so we need to give back the money.
        // If the sender == null, means that the loan has been requested from a player to a bank, so we just remove the money to the player receiver.
        if (!isBankToPlayer) {
            // Add back a part of the amount to the sender of the loan.
            BigDecimal addedToSender = senderBank.getBankEconomy().addBankBalance(sender, currentTaskAmount, TransactionType.LOAN), extra = currentTaskAmount.subtract(addedToSender);
            if (extra.compareTo(BigDecimal.ZERO) <= 0) BPMessages.send(sender.getPlayer(), "Loan-Payback", BPUtils.placeValues(receiver, addedToSender));
            else {
                BPMessages.send(sender.getPlayer(), "Loan-Payback-Full", BPUtils.placeValues(currentTaskAmount), BPUtils.placeValues(receiver, extra, "extra"));
                BankPlus.INSTANCE().getVaultEconomy().depositPlayer(sender, extra.doubleValue());
            }
        }

        // Remove a part of the amount from the receiver of the loan.
        BPEconomy receiverBankEconomy = receiverBank.getBankEconomy();
        BigDecimal removedToReceiver = receiverBankEconomy.removeBankBalance(receiver, currentTaskAmount, TransactionType.LOAN), debt = currentTaskAmount.subtract(removedToReceiver);
        if (debt.doubleValue() <= 0D) {
            if (isBankToPlayer) BPMessages.send(receiver.getPlayer(), "Loan-Returned-Bank", BPUtils.placeValues(loan.getRequestedBank().getIdentifier(), currentTaskAmount));
            else BPMessages.send(receiver.getPlayer(), "Loan-Returned", BPUtils.placeValues(sender, currentTaskAmount));
        } else {
            BigDecimal newDebt = receiverBankEconomy.getDebt(receiver).add(debt);
            if (isBankToPlayer) BPMessages.send(receiver.getPlayer(), "Loan-Returned-Debt-Bank", BPUtils.placeValues(loan.getRequestedBank().getIdentifier(), newDebt));
            else BPMessages.send(receiver.getPlayer(), "Loan-Returned-Debt", BPUtils.placeValues(sender, newDebt));
            receiverBankEconomy.setDebt(receiver, newDebt);
        }

        // Was the loan at his final instalment?
        if (loan.getInstalmentsPoint() >= instalments) loans.remove(loan);
        else loan.setTask(Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> advanceReturningTask(loan), ConfigValues.getLoanDelay()));
    }

    public static class LoanRequest {
        private boolean senderIsReceiver;
        private Player sender, receiver;
        private BPLoan loan;

        /**
         * Check if the sender of that request will be the receiver of the money (in cases the player executed "/bp loan request")
         */
        public boolean senderIsReceiver() {
            return senderIsReceiver;
        }

        public Player getSender() {
            return sender;
        }

        public Player getReceiver() {
            return receiver;
        }

        public BPLoan getLoan() {
            return loan;
        }

        public void setSenderIsReceiver(boolean senderIsReceiver) {
            this.senderIsReceiver = senderIsReceiver;
        }

        public void setSender(Player sender) {
            this.sender = sender;
        }

        public void setReceiver(Player receiver) {
            this.receiver = receiver;
        }

        public void setLoan(BPLoan loan) {
            this.loan = loan;
        }
    }
}