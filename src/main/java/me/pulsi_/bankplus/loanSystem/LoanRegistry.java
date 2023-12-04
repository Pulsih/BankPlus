package me.pulsi_.bankplus.loanSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.managers.BPConfigs;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
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

public class LoanRegistry {

    private final List<BPLoan> loans = new ArrayList<>();
    private final HashMap<UUID, BPRequest> requests = new HashMap<>();

    /**
     * List to track each loan made by players.
     * @return A list of loans.
     */
    public List<BPLoan> getLoans() {
        return loans;
    }

    public HashMap<UUID, BPRequest> getRequests() {
        return requests;
    }

    public void loadAllLoans() {
        FileConfiguration saves = BankPlus.INSTANCE().getConfigs().getConfig(BPConfigs.Type.SAVES.name);

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
                if (BankManager.exist(requestedBank)) requestedBank = Values.CONFIG.getMainGuiName();
                else BPLogger.warn("The loan \"" + receiverUUID + "\" specified an invalid bank to take the money, using the main bank.");

                loan = new BPLoan(receiver, requestedBank);
            } else {

                String fromBank = Values.CONFIG.getMainGuiName(), toBank = Values.CONFIG.getMainGuiName();
                String fromBankString = values.getString("from"), toBankString = values.getString("to");

                if (fromBankString == null) BPLogger.warn("The loan \"" + receiverUUID + "\" did not specify a bank to take the money, using the main bank.");
                else {
                    if (BankManager.exist(fromBankString)) fromBank = fromBankString;
                    else BPLogger.warn("The loan \"" + receiverUUID + "\" specified an invalid bank to take the money, using the main bank.");
                }

                if (toBankString == null) BPLogger.warn("The loan \"" + receiverUUID + "\" did not specify a bank to give the money, using the main bank.");
                else {
                    if (BankManager.exist(toBankString)) toBank = toBankString;
                    else BPLogger.warn("The loan \"" + receiverUUID + "\" specified an invalid bank to give the money, using the main bank.");
                }

                loan = new BPLoan(sender, receiver, fromBank, toBank);
            }

            loan.setMoneyToReturn(new BigDecimal(moneyToReturn));
            loan.setInstalments(values.getInt("instalments"));
            loan.setInstalmentsPoint(values.getInt("instalments-point"));

            LoanUtils.startLoanTask(loan);
        }
    }

    public void saveAllLoans(FileConfiguration savesConfig) {
        for (BPLoan loan : loans) {
            String path = "loans." + loan.getReceiver().getUniqueId() + ".";

            if (loan.getSender() != null) savesConfig.set(path + "sender", loan.getSender().getUniqueId());
            savesConfig.set(path + "money-to-return", loan.getMoneyToReturn());
            savesConfig.set(path + "instalments", loan.getInstalments());
            savesConfig.set(path + "instalments-point", loan.getInstalmentsPoint());
            savesConfig.set(path + "time-left", loan.getTimeLeft());
            savesConfig.set(path + "from", loan.getFromBankName());
            savesConfig.set(path + "to", loan.getToBankName());
            savesConfig.set(path + "requested-bank", loan.getRequestedBank());
        }
    }

    public static class BPRequest {
        private boolean isLoanSender;
        private Player sender, target;
        private BPLoan loan;

        /**
         * If is not the loan sender, it will be the receiver.
         * @return true if the player is the loan sender.
         */
        public boolean isLoanSender() {
            return isLoanSender;
        }

        public Player getSender() {
            return sender;
        }

        public Player getTarget() {
            return target;
        }

        public BPLoan getLoan() {
            return loan;
        }

        public void setLoanSender(boolean loanSender) {
            isLoanSender = loanSender;
        }

        public void setSender(Player sender) {
            this.sender = sender;
        }

        public void setTarget(Player target) {
            this.target = target;
        }

        public void setLoan(BPLoan loan) {
            this.loan = loan;
        }
    }
}