package me.pulsi_.bankplus.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerFiles;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.events.BPAfterTransactionEvent;
import me.pulsi_.bankplus.events.BPPreTransactionEvent;
import me.pulsi_.bankplus.utils.*;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
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
    private static final Map<String, BigDecimal> bankPlayerMoney = new HashMap<>();
    private final Player p;
    private final OfflinePlayer op;

    public MultiEconomyManager(OfflinePlayer op) {
        this.op = op;
        this.p = op.isOnline() ? Bukkit.getPlayer(op.getUniqueId()) : null;
    }

    public MultiEconomyManager(Player p) {
        this.op = Bukkit.getOfflinePlayer(p.getUniqueId());
        this.p = p;
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
                BPLogger.error(e, "An error has occurred while loading a user file (File name: " + file.getName() + "):");
                continue;
            }

            ConfigurationSection section = config.getConfigurationSection("Banks");
            if (section == null) return balances;

            BigDecimal balance = new BigDecimal(0);
            String name = config.getString("Account-Name") == null ? "null" : config.getString("Account-Name");

            for (String bankName : section.getKeys(false)) {
                String bal = config.getString("Banks." + bankName + ".Money");
                if (bal != null && !BPUtils.isInvalidNumber(bal))
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
            if (bankPlayerMoney.containsKey(p.getUniqueId() + "." + bankName)) continue;
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
     * Get the player bank balance of all banks.
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
     * Set the selected amount in the selected bank.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public void setBankBalance(BigDecimal amount, String bankName) {
        setBankBalance(amount, bankName, false, TransactionType.SET);
    }

    /**
     * Set the selected amount in the selected bank.
     *
     * @param amount       The amount.
     * @param bankName     The bank.
     * @param ignoreEvents Choose if firing or not the events.
     */
    public void setBankBalance(BigDecimal amount, String bankName, boolean ignoreEvents) {
        setBankBalance(amount, bankName, ignoreEvents, TransactionType.SET);
    }

    /**
     * Set the amount in the selected bank.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     * @param type     Used to override the default transaction type.
     */
    public void setBankBalance(BigDecimal amount, String bankName, TransactionType type) {
        setBankBalance(amount, bankName, false, type);
    }

    /**
     * Set the selected amount in the selected bank.
     *
     * @param amount   The new bank balance amount.
     * @param bankName The bank.
     */
    public void setBankBalance(BigDecimal amount, String bankName, boolean ignoreEvents, TransactionType type) {
        Economy economy = BankPlus.INSTANCE.getEconomy();
        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(type, economy.getBalance(op), amount, bankName);
            if (event.isCancelled()) return;

            amount = event.getTransactionAmount();
        }
        set(amount, bankName);

        if (!ignoreEvents) endEvent(type, economy.getBalance(op), amount, bankName);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public void addBankBalance(BigDecimal amount, String bankName) {
        addBankBalance(amount, bankName, false, false, TransactionType.ADD);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param amount             The amount.
     * @param bankName           The bank.
     * @param addOfflineInterest Choose if also adding the offline interest (used, for example, if a player is offline)
     */
    public void addBankBalance(BigDecimal amount, String bankName, boolean addOfflineInterest) {
        addBankBalance(amount, bankName, addOfflineInterest, false, TransactionType.ADD);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     * @param type     Used to override the default transaction type.
     */
    public void addBankBalance(BigDecimal amount, String bankName, TransactionType type) {
        addBankBalance(amount, bankName, false, false, type);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param amount             The amount.
     * @param bankName           The bank.
     * @param addOfflineInterest Choose if also adding the offline interest (used, for example, if a player is offline)
     * @param ignoreEvents       Choose if firing or not the events.
     */
    public void addBankBalance(BigDecimal amount, String bankName, boolean addOfflineInterest, boolean ignoreEvents) {
        addBankBalance(amount, bankName, addOfflineInterest, ignoreEvents, TransactionType.ADD);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param amount             The amount.
     * @param bankName           The bank.
     * @param addOfflineInterest Choose if also adding the offline interest (used, for example, if a player is offline)
     * @param ignoreEvents       Choose if firing or not the events.
     * @param type               Used to override the default transaction type.
     */
    public void addBankBalance(BigDecimal amount, String bankName, boolean addOfflineInterest, boolean ignoreEvents, TransactionType type) {
        Economy economy = BankPlus.INSTANCE.getEconomy();
        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(type, economy.getBalance(op), amount, bankName);
            if (event.isCancelled()) return;

            amount = event.getTransactionAmount();
        }
        set(getBankBalance(bankName).add(amount), bankName, addOfflineInterest);

        if (!ignoreEvents) endEvent(type, economy.getBalance(op), amount, bankName);
    }

    /**
     * Remove the selected amount.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public void removeBankBalance(BigDecimal amount, String bankName) {
        removeBankBalance(amount, bankName, false, TransactionType.REMOVE);
    }

    /**
     * Remove the selected amount.
     *
     * @param amount       The amount.
     * @param bankName     The bank.
     * @param ignoreEvents Choose if firing or not the events.
     */
    public void removeBankBalance(BigDecimal amount, String bankName, boolean ignoreEvents) {
        removeBankBalance(amount, bankName, ignoreEvents, TransactionType.REMOVE);
    }

    /**
     * Remove the selected amount.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     * @param type     Used to override the default transaction type.
     */
    public void removeBankBalance(BigDecimal amount, String bankName, TransactionType type) {
        removeBankBalance(amount, bankName, false, type);
    }

    /**
     * Remove the selected amount.
     *
     * @param amount       The amount.
     * @param bankName     The bank.
     * @param ignoreEvents Choose if firing or not the events.
     * @param type         Used to override the default transaction type.
     */
    public void removeBankBalance(BigDecimal amount, String bankName, boolean ignoreEvents, TransactionType type) {
        Economy economy = BankPlus.INSTANCE.getEconomy();
        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(type, economy.getBalance(op), amount, bankName);
            if (event.isCancelled()) return;

            amount = event.getTransactionAmount();
        }
        set(getBankBalance(bankName).subtract(amount), bankName);

        if (!ignoreEvents) endEvent(type, economy.getBalance(op), amount, bankName);
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
        Economy economy = BankPlus.INSTANCE.getEconomy();
        BPPreTransactionEvent event = startEvent(TransactionType.DEPOSIT, economy.getBalance(p), amount, bankName);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();

        if (amount.doubleValue() < Values.CONFIG.getDepositMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getDepositMinimumAmount());
            return;
        }

        BigDecimal money = BigDecimal.valueOf(BankPlus.INSTANCE.getEconomy().getBalance(p));
        if (!BPUtils.checkPreRequisites(money, amount, p) || BPUtils.isBankFull(p, bankName)) return;

        if (money.doubleValue() < amount.doubleValue()) amount = money;

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
        }

        EconomyResponse depositResponse = BankPlus.INSTANCE.getEconomy().withdrawPlayer(p, amount.doubleValue());
        if (BPUtils.hasFailed(p, depositResponse)) return;

        addBankBalance(amount.subtract(taxes), bankName, false, true);
        BPMessages.send(p, "Success-Deposit", BPUtils.placeValues(p, amount.subtract(taxes)), BPUtils.placeValues(taxes, "taxes"));
        BPUtils.playSound("DEPOSIT", p);

        endEvent(TransactionType.DEPOSIT, economy.getBalance(p), amount, bankName);
    }

    public void withdraw(BigDecimal amount, String bankName) {
        Economy economy = BankPlus.INSTANCE.getEconomy();
        BPPreTransactionEvent event = startEvent(TransactionType.WITHDRAW, economy.getBalance(p), amount, bankName);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();

        if (amount.doubleValue() < Values.CONFIG.getWithdrawMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getWithdrawMinimumAmount());
            return;
        }

        BigDecimal bankBal = getBankBalance(bankName);
        if (!BPUtils.checkPreRequisites(bankBal, amount, p)) return;

        if (bankBal.doubleValue() < amount.doubleValue()) amount = bankBal;

        BigDecimal maxWithdrawAmount = Values.CONFIG.getMaxWithdrawAmount();
        if (maxWithdrawAmount.doubleValue() > 0 && amount.doubleValue() >= maxWithdrawAmount.doubleValue())
            amount = maxWithdrawAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getWithdrawTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.withdraw.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getWithdrawTaxes().divide(BigDecimal.valueOf(100)));

        EconomyResponse withdrawResponse = economy.depositPlayer(p, amount.subtract(taxes).doubleValue());
        if (BPUtils.hasFailed(p, withdrawResponse)) return;

        removeBankBalance(amount, bankName, true);
        BPMessages.send(p, "Success-Withdraw", BPUtils.placeValues(p, amount.subtract(taxes)), BPUtils.placeValues(taxes, "taxes"));
        BPUtils.playSound("WITHDRAW", p);

        endEvent(TransactionType.WITHDRAW, economy.getBalance(p), amount, bankName);
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
            removeBankBalance(targetCapacity.subtract(targetBalance), fromBank, TransactionType.PAY);
            targetEM.setBankBalance(targetCapacity, toBank, TransactionType.PAY);

            BPMessages.send(p, "Payment-Sent", BPUtils.placeValues(target, amount));
            BPMessages.send(target, "Payment-Received", BPUtils.placeValues(p, amount));
            return;
        }

        removeBankBalance(amount, fromBank, TransactionType.PAY);
        targetEM.addBankBalance(amount, toBank, TransactionType.PAY);
        BPMessages.send(p, "Payment-Sent", BPUtils.placeValues(target, amount));
        BPMessages.send(target, "Payment-Received", BPUtils.placeValues(p, amount));
    }

    private BPPreTransactionEvent startEvent(TransactionType type, double vaultBalance, BigDecimal amount, String bankName) {
        BPPreTransactionEvent event = new BPPreTransactionEvent(
                op, type, getBankBalance(bankName), vaultBalance, amount, false, bankName
        );
        BPUtils.callEvent(event);
        return event;
    }

    private void endEvent(TransactionType type, double vaultBalance, BigDecimal amount, String bankName) {
        BPAfterTransactionEvent event = new BPAfterTransactionEvent(
                op, type, getBankBalance(bankName), vaultBalance, amount, false, bankName
        );
        BPUtils.callEvent(event);
    }
}