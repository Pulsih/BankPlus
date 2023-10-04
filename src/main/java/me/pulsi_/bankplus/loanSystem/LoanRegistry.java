package me.pulsi_.bankplus.loanSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.managers.BPConfigs;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LoanRegistry {

    private final List<BPLoan> loans = new ArrayList<>();
    private final HashMap<UUID, UUID> requestsReceived = new HashMap<>();
    private final HashMap<UUID, BPLoan> requestsSent = new HashMap<>();

    /**
     * List to track each loan made by players.
     * @return A list of loans.
     */
    public List<BPLoan> getLoans() {
        return loans;
    }

    /**
     * Used to track the player who sent the request, with the player that received the loan request as key, and the player that sent the request as value.
     * @return List of player's UUIDs
     */
    public HashMap<UUID, UUID> getRequestsReceived() {
        return requestsReceived;
    }

    /**
     * A hashmap holding all player requests, with the sender as key, and loan request as value.
     * The loan will contain useful information such as loan target, amount and other..
     * @return
     */
    public HashMap<UUID, BPLoan> getRequestsSent() {
        return requestsSent;
    }

    public void loadAllLoans() {
        FileConfiguration saves = BankPlus.INSTANCE.getConfigs().getConfig(BPConfigs.Type.SAVES.name);

        ConfigurationSection section = saves.getConfigurationSection("loans");
        if (section == null) return;

        for (String receiverUUID : section.getKeys(false)) {
            ConfigurationSection values = section.getConfigurationSection(receiverUUID);
            if (values == null) continue;

            OfflinePlayer receiver, sender;

            try {
                receiver = Bukkit.getOfflinePlayer(UUID.fromString(receiverUUID));
                sender = Bukkit.getOfflinePlayer(UUID.fromString(values.getString("sender")));
            } catch (IllegalArgumentException e) {
                BPLogger.warn(e, "Could not load \"" + receiverUUID + "\" loan! (Invalid UUID specified)");
                continue;
            }

            String moneyToReturn = values.getString("money-to-return");
            if (moneyToReturn == null || BPUtils.isInvalidNumber(moneyToReturn)) {
                BPLogger.warn("Could not load \"" + receiverUUID + "\" loan! (An invalid money-to-return amount has been specified)");
                continue;
            }

            String fromBank = Values.CONFIG.getMainGuiName(), toBank = Values.CONFIG.getMainGuiName();
            String fromBankString = values.getString("from"), toBankString = values.getString("to");

            if (fromBankString == null) BPLogger.warn("The loan \"" + receiverUUID + "\" did not specify a bank to take the money, using the main bank.");
            else {
                if (new BankReader(fromBankString).exist()) fromBank = fromBankString;
                else BPLogger.warn("The loan \"" + receiverUUID + "\" specified an invalid bank to take the money, using the main bank.");
            }

            if (toBankString == null) BPLogger.warn("The loan \"" + receiverUUID + "\" did not specify a bank to give the money, using the main bank.");
            else {
                if (new BankReader(toBankString).exist()) toBank = toBankString;
                else BPLogger.warn("The loan \"" + receiverUUID + "\" specified an invalid bank to give the money, using the main bank.");
            }

            BPLoan loan = new BPLoan(sender, receiver, fromBank, toBank);
            loan.setMoneyToReturn(new BigDecimal(moneyToReturn));
            loan.setInstalments(values.getInt("instalments"));
            loan.setInstalmentsPoint(values.getInt("instalments-point"));

            LoanUtils.startLoanTask(loan);
        }
    }

    public void saveAllLoans(FileConfiguration savesConfig) {
        for (BPLoan loan : loans) {
            String path = "loans." + loan.getReceiver().getUniqueId() + ".";

            savesConfig.set(path + "sender", loan.getSender().getUniqueId());
            savesConfig.set(path + "money-to-return", loan.getMoneyToReturn());
            savesConfig.set(path + "instalments", loan.getInstalments());
            savesConfig.set(path + "instalments-point", loan.getInstalmentsPoint());
            savesConfig.set(path + "time-left", loan.getTimeLeft());
            savesConfig.set(path + "from", loan.getFromBankName());
            savesConfig.set(path + "to", loan.getToBankName());
        }
    }
}