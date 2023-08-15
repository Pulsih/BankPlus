package me.pulsi_.bankplus.account.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerFiles;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.events.BPTransactionEvent;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiEconomyManager {

    /**
     * The main HashMap that holds all player bank balances.
     */
    public static Map<String, BigDecimal> bankPlayerMoney = new HashMap<>();
    private final Player p;
    private final OfflinePlayer op;

    public MultiEconomyManager(Player p) {
        this.p = p;
        this.op = null;
    }

    public MultiEconomyManager(OfflinePlayer op) {
        this.p = null;
        this.op = op;
    }
    /**
     * Return a list with all player balances.
     *
     * @return List of BigDecimal amounts.
     */
    public static List<BigDecimal> getAllBankBalances() {
        List<BigDecimal> balances = new ArrayList<>();

        File dataFolder = new File(BankPlus.INSTANCE.getDataFolder(), "playerdata");
        File[] files = dataFolder.listFiles();
        if (files == null) return balances;

        for (File file : files) {
            FileConfiguration config = new YamlConfiguration();

            try {
                config.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                BPLogger.error("An error has occurred while loading a bank file: " + e.getMessage());
            }

            ConfigurationSection section = config.getConfigurationSection("Banks");
            if (section == null) return balances;

            BigDecimal balance = new BigDecimal(0);
            String name = config.getString("Account-Name") == null ? "null" : config.getString("Account-Name");

            for (String bankName : section.getKeys(false)) {
                String bal = config.getString("Banks." + bankName + ".Money");
                if (bal != null && !BPMethods.isInvalidNumber(bal))
                    balance = balance.add(new BigDecimal(bal));
            }
            balances.add(balance);

            HashMap<BigDecimal, String> balanceName = new HashMap<>();
            balanceName.put(balance, name);
            BankPlus.INSTANCE.getBankTopManager().getLinkedBalanceName().add(balanceName);
        }
        return balances;
    }

    /**
     * Load the player bank balances. This cannot be an offline player!
     */
    public void loadBankBalance() {
        if (p == null) return;
        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
            if (bankPlayerMoney.containsKey(p.getUniqueId() + "." + bankName)) return;
            String bal = new BPPlayerFiles(p).getPlayerConfig().getString("Banks." + bankName + ".Money");
            bankPlayerMoney.putIfAbsent(p.getUniqueId() + "." + bankName, new BigDecimal(bal == null ? "0" : bal));
        }
    }

    /**
     * Unload the player bank balances.
     */
    public void unloadBankBalance() {
        ConfigurationSection section = new BPPlayerFiles(p).getPlayerConfig().getConfigurationSection("Banks");
        if (section != null) section.getKeys(false).forEach(key -> bankPlayerMoney.remove(p.getUniqueId() + "." + key));
    }

    /**
     * Save the selected bank balance to the player file.
     */
    public void saveBankBalance(String bankName, boolean async) {
        BPPlayerFiles files = new BPPlayerFiles(p);
        files.getPlayerConfig().set("Banks." + bankName + ".Money", BPFormatter.formatBigDouble(getBankBalance(bankName)));
        files.savePlayerFile(async);
    }

    /**
     * Save all bank balances to the player file.
     */
    public void saveBankBalance(boolean async) {
        BPPlayerFiles files = new BPPlayerFiles(p);
        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
            files.getPlayerConfig().set("Banks." + bankName + ".Money", BPFormatter.formatBigDouble(getBankBalance(bankName)));
        files.savePlayerFile(async);
    }

    /**
     * Get the player bank balance of the selected bank.
     *
     * @param bankName The bank.
     * @return BidDecimal amount.
     */
    public BigDecimal getBankBalance(String bankName) {
        if (p == null) {
            String bal = new BPPlayerFiles(op).getPlayerConfig().getString("Banks." + bankName + ".Money");
            return new BigDecimal(bal == null ? "0" : bal);
        }
        loadBankBalance();
        return bankPlayerMoney.get(p.getUniqueId() + "." + bankName);
    }

    /**
     * Get the total player bank balance of all banks.
     *
     * @return BidDecimal amount.
     */
    public BigDecimal getBankBalance() {
        BigDecimal amount = new BigDecimal(0);
        if (p != null) {
            loadBankBalance();
            for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                amount = amount.add(bankPlayerMoney.get(p.getUniqueId() + "." + bankName));
        } else {
            BPPlayerFiles files = new BPPlayerFiles(op);
            for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
                String bal = files.getPlayerConfig().getString("Banks." + bankName + ".Money");
                amount = amount.add(new BigDecimal(bal == null ? "0" : bal));
            }
        }
        return amount;
    }

    /**
     * Set the player bank balance to the selected amount in the selected bank.
     *
     * @param amount   The new bank balance amount.
     * @param bankName The bank.
     */
    public void setBankBalance(BigDecimal amount, String bankName) {
        BPTransactionEvent event = new BPTransactionEvent(
                op, BPTransactionEvent.TransactionType.SET, getBankBalance(bankName), amount, false, bankName
        );
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        set(event.getTransactionAmount(), bankName);
    }

    /**
     * Add to the player bank balance the selected amount.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public void addBankBalance(BigDecimal amount, String bankName) {
        addBankBalance(amount, bankName, false);
    }

    /**
     * Add to the offline player bank balance the selected amount.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public void addBankBalance(BigDecimal amount, String bankName, boolean addOfflineInterest) {
        BPTransactionEvent event = new BPTransactionEvent(
                op, BPTransactionEvent.TransactionType.ADD, getBankBalance(bankName), amount, false, bankName
        );
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        set(getBankBalance(bankName).add(event.getTransactionAmount()), bankName, addOfflineInterest);
    }

    /**
     * Remove from the player bank balance the selected amount.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public void removeBankBalance(BigDecimal amount, String bankName) {
        BPTransactionEvent event = new BPTransactionEvent(
                op, BPTransactionEvent.TransactionType.REMOVE, getBankBalance(bankName), amount, false, bankName
        );
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        set(getBankBalance(bankName).subtract(event.getTransactionAmount()), bankName);
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     */
    private void set(BigDecimal amount, String bankName) {
        set(amount, bankName, false);
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param amount             The amount.
     * @param bankName           The bank.
     * @param addOfflineInterest Choose if adding both balance and offline interest.
     */
    private void set(BigDecimal amount, String bankName, boolean addOfflineInterest) {
        if (p != null) {
            String amountFormatted = BPFormatter.formatBigDouble(amount);
            BigDecimal finalAmount = new BigDecimal(amountFormatted).doubleValue() < 0 ? new BigDecimal(0) : new BigDecimal(amountFormatted);
            bankPlayerMoney.put(p.getUniqueId() + "." + bankName, finalAmount);
            return;
        }

        BPPlayerFiles files = new BPPlayerFiles(op);
        FileConfiguration config = files.getPlayerConfig();
        config.set("Banks." + bankName + ".Money", BPFormatter.formatBigDouble(amount));
        if (addOfflineInterest && Values.CONFIG.isNotifyOfflineInterest()) {
            BigDecimal offlineInterest = new BigDecimal(BPFormatter.formatBigDouble(amount)).subtract(getBankBalance(bankName));
            if (offlineInterest.doubleValue() > 0) config.set("Offline-Interest", offlineInterest.toString());
        }

        files.savePlayerFile(config, true);
    }

    public void deposit(BigDecimal amount, String bankName) {
        BPTransactionEvent event = new BPTransactionEvent(
                op, BPTransactionEvent.TransactionType.DEPOSIT, getBankBalance(bankName), amount, false, bankName
        );
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();

        if (amount.doubleValue() < Values.CONFIG.getDepositMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getDepositMinimumAmount());
            return;
        }

        BigDecimal money = BigDecimal.valueOf(BankPlus.INSTANCE.getEconomy().getBalance(p));
        if (!BPMethods.checkPreRequisites(money, amount, p) || BPMethods.isBankFull(p, bankName)) return;

        BigDecimal maxDepositAmount = Values.CONFIG.getMaxDepositAmount();
        if (maxDepositAmount.doubleValue() != 0 && amount.doubleValue() >= maxDepositAmount.doubleValue())
            amount = maxDepositAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getDepositTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.deposit.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100)));

        BigDecimal capacity = new BankReader(bankName).getCapacity(p);
        BigDecimal newBankBalance = getBankBalance().add(amount);

        /*
        Make it possible so when depositing all your money with taxes, the money will have the ability
        to FILL the bank instead of always depositing a bit less and never filling up the bank.
        */
        if (capacity.doubleValue() > 0d && newBankBalance.doubleValue() >= capacity.doubleValue()) {
            BigDecimal moneyToFull = capacity.subtract(getBankBalance());
            amount = moneyToFull.add(taxes);
            if (money.doubleValue() < amount.doubleValue()) amount = money;
        }

        EconomyResponse depositResponse = BankPlus.INSTANCE.getEconomy().withdrawPlayer(p, amount.doubleValue());
        if (BPMethods.hasFailed(p, depositResponse)) return;

        addBankBalance(amount.subtract(taxes), bankName);
        BPMessages.send(p, "Success-Deposit", BPMethods.placeValues(p, amount.subtract(taxes)), BPMethods.placeValues(taxes, "taxes"));
        BPMethods.playSound("DEPOSIT", p);
    }

    public void withdraw(BigDecimal amount, String bankName) {
        BPTransactionEvent event = new BPTransactionEvent(
                op, BPTransactionEvent.TransactionType.WITHDRAW, getBankBalance(bankName), amount, false, bankName
        );
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();

        if (amount.doubleValue() < Values.CONFIG.getWithdrawMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getWithdrawMinimumAmount());
            return;
        }

        BigDecimal bankBal = getBankBalance(bankName);
        if (!BPMethods.checkPreRequisites(bankBal, amount, p)) return;
        if (amount.doubleValue() > bankBal.doubleValue()) amount = bankBal;

        BigDecimal maxWithdrawAmount = Values.CONFIG.getMaxWithdrawAmount();
        if (maxWithdrawAmount.doubleValue() > 0 && amount.doubleValue() >= maxWithdrawAmount.doubleValue())
            amount = maxWithdrawAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getWithdrawTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.withdraw.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100)));

        EconomyResponse withdrawResponse = BankPlus.INSTANCE.getEconomy().depositPlayer(p, amount.doubleValue());
        if (BPMethods.hasFailed(p, withdrawResponse)) return;

        removeBankBalance(amount.add(taxes), bankName);
        BPMessages.send(p, "Success-Withdraw", BPMethods.placeValues(p, amount.subtract(taxes)), BPMethods.placeValues(taxes, "taxes"));
        BPMethods.playSound("WITHDRAW", p);
    }

    /**
     * Method used to execute the pay transaction.
     *
     * @param target   The player that will receive your money.
     * @param amount   How much money you want to pay.
     * @param fromBank The bank where the money will be taken.
     * @param toBank   The bank where the money will be added.
     */
    public void pay(Player target, BigDecimal amount, String fromBank, String toBank) {
        BigDecimal senderBalance = getBankBalance(fromBank);

        if (senderBalance.doubleValue() < amount.doubleValue()) {
            BPMessages.send(p, "Insufficient-Money");
            return;
        }

        MultiEconomyManager targetEM = new MultiEconomyManager(target);
        BigDecimal targetCapacity = new BankReader(toBank).getCapacity(target), targetBalance = targetEM.getBankBalance(toBank), newBalance = amount.add(targetBalance);

        if (targetBalance.doubleValue() >= targetCapacity.doubleValue()) {
            BPMessages.send(p, "Bank-Full", "%player%$" + target.getName());
            return;
        }

        if (newBalance.doubleValue() >= targetCapacity.doubleValue() && targetCapacity.doubleValue() > 0d) {
            removeBankBalance(targetCapacity.subtract(targetBalance), fromBank);
            targetEM.setBankBalance(targetCapacity, toBank);

            BPMessages.send(p, "Payment-Sent", BPMethods.placeValues(target, amount));
            BPMessages.send(target, "Payment-Received", BPMethods.placeValues(p, amount));
            return;
        }

        removeBankBalance(amount, fromBank);
        targetEM.addBankBalance(amount, toBank);
        BPMessages.send(p, "Payment-Sent", BPMethods.placeValues(target, amount));
        BPMessages.send(target, "Payment-Received", BPMethods.placeValues(p, amount));
    }
}