package me.pulsi_.bankplus.loanSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerFiles;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.debt.DebtUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class LoanUtils {

    public static void sendRequest(Player from, Player to, BigDecimal amount, String fromBankName, String toBankName) {
        BigDecimal fBal = BankPlus.getBPEconomy().getBankBalance(from, fromBankName);
        if (fBal.doubleValue() < amount.doubleValue()) amount = fBal;

        BPLoan loan = new BPLoan(from, to, amount, fromBankName, toBankName);
        BankReader reader = new BankReader(loan.getToBankName());
        BigDecimal capacity = reader.getCapacity(to);

        if (loan.getMoneyToReturn().doubleValue() > capacity.doubleValue()) {
            BPMessages.send(from, "Cannot-Afford-Loan", "%player%$" + to.getName());
            return;
        }

        BPMessages.send(to, "Loan-Request-Received", BPUtils.placeValues(from, amount));
        BPMessages.send(from, "Loan-Request-Sent", "%player%$" + to.getName());

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

    public static void startLoanTask(BPLoan loan) {
        int delay = loan.timeLeft <= 0 ? Values.CONFIG.getLoanDelay() : BPUtils.millisecondsInTicks(loan.timeLeft);
        loan.task = Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> loanTask(loan), delay);
    }

    private static void loanTask(BPLoan loan) {
        loan.timeLeft = System.currentTimeMillis() + BPUtils.ticksInMilliseconds(Values.CONFIG.getLoanDelay());
        loan.instalmentPoint++;

        BigDecimal amount = loan.getMoneyToReturn().divide(BigDecimal.valueOf(loan.getInstalments()));
        OfflinePlayer sender = loan.getSender(), target = loan.getTarget();
        BPEconomy economy = BankPlus.getBPEconomy();

        BigDecimal sBal = economy.getBankBalance(sender, loan.getFromBankName()), capacity = new BankReader(loan.getFromBankName()).getCapacity(sender);

        if (sBal.add(amount).doubleValue() >= capacity.doubleValue() && capacity.doubleValue() > 0d) {
            BigDecimal extra = amount.subtract(capacity.subtract(sBal));
            economy.setBankBalance(sender, capacity, loan.getFromBankName(), TransactionType.LOAN);

            if (sender.isOnline()) BPMessages.send(Bukkit.getPlayer(sender.getUniqueId()), "Loan-Payback-Full", BPUtils.placeValues(amount), BPUtils.placeValues(target, extra, "extra"));
            BankPlus.INSTANCE.getVaultEconomy().depositPlayer(sender, extra.doubleValue());
        } else {
            economy.addBankBalance(sender, amount, loan.getFromBankName(), TransactionType.LOAN);
            if (sender.isOnline()) BPMessages.send(Bukkit.getPlayer(sender.getUniqueId()), "Loan-Payback", BPUtils.placeValues(target, amount));
        }

        BigDecimal tBal = economy.getBankBalance(target, loan.getToBankName());
        if (tBal.doubleValue() < amount.doubleValue()) {
            BigDecimal debt = amount.subtract(tBal).add(DebtUtils.getDebt(target));
            economy.setBankBalance(target, BigDecimal.valueOf(0), loan.getToBankName(), TransactionType.LOAN);
            if (target.isOnline()) BPMessages.send(Bukkit.getPlayer(target.getUniqueId()), "Loan-Returned-Debt", BPUtils.placeValues(sender, debt));

            DebtUtils.setDebt(target, debt);
        } else {
            economy.removeBankBalance(target, amount, loan.getToBankName(), TransactionType.LOAN);
            if (target.isOnline()) BPMessages.send(Bukkit.getPlayer(target.getUniqueId()), "Loan-Returned", BPUtils.placeValues(sender, amount));
        }

        if (loan.instalmentPoint >= loan.getInstalments()) {
            BPPlayerFiles files = new BPPlayerFiles(loan.getTarget());
            File file = files.getPlayerFile();
            FileConfiguration config = files.getPlayerConfig();

            config.set("loans." + (Values.CONFIG.isStoringUUIDs() ? loan.getSender().getUniqueId() : loan.getSender().getName()), null);
            files.savePlayerFile(config, file, true);
            return;
        }
        loan.task = Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> loanTask(loan), Values.CONFIG.getLoanDelay());
    }

    public static void loadPlayerLoans(OfflinePlayer p) {
        if (p == null) return;

        BPPlayerFiles files = new BPPlayerFiles(p);
        FileConfiguration config = files.getPlayerConfig();

        ConfigurationSection loans = config.getConfigurationSection("loans");
        if (loans == null || loans.getKeys(false).isEmpty()) return;

        for (String loanSender : loans.getKeys(false)) {
            OfflinePlayer sender;
            try {
                sender = Values.CONFIG.isStoringUUIDs() ? Bukkit.getOfflinePlayer(UUID.fromString(loanSender)) : Bukkit.getOfflinePlayer(loanSender);
            } catch (IllegalArgumentException e) {
                continue;
            }

            ConfigurationSection values = config.getConfigurationSection("loans." + loanSender);

            BPLoan loan = new BPLoan(sender, p, new BigDecimal(values.getString("amount")), values.getString("from"), values.getString("to"));
            loan.timeLeft = System.currentTimeMillis() + values.getLong("time_left");

            BankPlus.INSTANCE.getLoanRegistry().getLoans().add(loan);
            startLoanTask(loan);
        }
    }

    public static void loadAllLoans() {
        saveLoans();
        for (BPLoan loan : new ArrayList<>(BankPlus.INSTANCE.getLoanRegistry().getLoans())) {
            BukkitTask task = loan.task;
            if (task != null) task.cancel();
        }
        BankPlus.INSTANCE.getLoanRegistry().getLoans().clear();

        File dataFolder = new File(BankPlus.INSTANCE.getDataFolder(), "playerdata");
        File[] files = dataFolder.listFiles();
        if (files == null) return;

        for (File file : files) {
            String name = file.getName().replace(".yml", "");

            OfflinePlayer p;
            UUID uuid;

            try {
                uuid = UUID.fromString(name);
            } catch (IllegalArgumentException e) {
                uuid = null;
            }

            if (Values.CONFIG.isStoringUUIDs()) {
                if (uuid == null) continue;
                p = Bukkit.getOfflinePlayer(uuid);
            } else {
                if (uuid != null) continue;
                p = Bukkit.getOfflinePlayer(name);
            }

            loadPlayerLoans(p);
        }
    }

    public static void saveLoan(BPLoan loan) {
        BPPlayerFiles files = new BPPlayerFiles(loan.getTarget());

        /*String path = "loans." + (Values.CONFIG.isStoringUUIDs() ? loan.getSender().getUniqueId() : loan.getSender().getName()) + ".";

        FileConfiguration config = files.getPlayerConfig();
        config.set(path + "amount", BPFormatter.formatBigDouble(loan.getMoneyGiven()));
        config.set(path + "from", loan.getFromBankName());
        config.set(path + "to", loan.getToBankName());
        config.set(path + "instalments", loan.getInstalments());
        config.set(path + "instalments_point", loan.instalmentPoint);
        config.set(path + "time_left", loan.timeLeft - System.currentTimeMillis());

        files.savePlayerFile(config, true);*/
    }

    public static void saveLoans() {
        for (BPLoan loan : BankPlus.INSTANCE.getLoanRegistry().getLoans())
            saveLoan(loan);
    }

    public static boolean hasRequest(Player p) {
        return BankPlus.INSTANCE.getLoanRegistry().getRequestsReceived().containsKey(p.getUniqueId());
    }

    public static boolean sentRequest(Player p) {
        return BankPlus.INSTANCE.getLoanRegistry().getRequestsSent().containsKey(p.getUniqueId());
    }
}