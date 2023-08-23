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
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

public class SingleEconomyManager {

    /**
     * The main HashMap that holds all player bank balances.
     */
    private static final Map<UUID, BigDecimal> totalPlayerMoney = new HashMap<>();
    private final Player p;
    private final OfflinePlayer op;

    public SingleEconomyManager(OfflinePlayer op) {
        this.op = op;
        this.p = op.isOnline() ? Bukkit.getPlayer(op.getUniqueId()) : null;
    }

    /**
     * Return a list with all player balances.
     *
     * @return List of balances.
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

            String bal = config.getString("Money");
            if (bal == null) continue;

            String name = config.getString("Account-Name");
            if (name == null) name = "null";

            BigDecimal balance;
            try {
                balance = new BigDecimal(bal);
            } catch (NumberFormatException e) {
                balance = new BigDecimal(0);
            }
            balances.add(balance);

            HashMap<BigDecimal, String> balanceName = new HashMap<>();
            balanceName.put(balance, name);
            BankPlus.INSTANCE.getBankTopManager().getLinkedBalanceName().add(balanceName);
        }
        return balances;
    }

    /**
     * Load the player bank balance. This cannot be an offline player!
     */
    public void loadBankBalance() {
        if (p == null || totalPlayerMoney.containsKey(p.getUniqueId())) return;
        String bal = new BPPlayerFiles(p).getPlayerConfig().getString("Money");
        totalPlayerMoney.put(p.getUniqueId(), bal == null ? new BigDecimal(0) : new BigDecimal(bal));
    }

    /**
     * Unload the player bank balance.
     */
    public BigDecimal unloadBankBalance() {
        return totalPlayerMoney.remove(p.getUniqueId());
    }

    /**
     * Save the player bank balance to his file.
     */
    public void saveBankBalance(boolean async) {
        BPPlayerFiles files = new BPPlayerFiles(p);
        files.getPlayerConfig().set("Money", BPFormatter.formatBigDouble(getBankBalance()));
        files.savePlayerFile(async);
    }

    /**
     * Get the player bank balance.
     *
     * @return player's balance.
     */
    public BigDecimal getBankBalance() {
        if (p == null) {
            String bal = new BPPlayerFiles(op).getPlayerConfig().getString("Money");
            return bal == null ? new BigDecimal(0) : new BigDecimal(bal);
        }
        loadBankBalance();
        return totalPlayerMoney.get(p.getUniqueId());
    }

    /**
     * Set the selected amount.
     *
     * @param amount The amount.
     */
    public void setBankBalance(BigDecimal amount) {
        setBankBalance(amount, false, TransactionType.SET);
    }

    /**
     * Set the selected amount.
     *
     * @param amount The amount.
     * @param type   Used to override the default transaction type.
     */
    public void setBankBalance(BigDecimal amount, TransactionType type) {
        setBankBalance(amount, false, type);
    }

    /**
     * Set the selected amount.
     *
     * @param amount       The amount.
     * @param ignoreEvents Choose if firing or not the events.
     */
    public void setBankBalance(BigDecimal amount, boolean ignoreEvents) {
        setBankBalance(amount, ignoreEvents, TransactionType.SET);
    }

    /**
     * Set the selected amount.
     *
     * @param amount       The amount.
     * @param ignoreEvents Choose if firing or not the events.
     * @param type         Used to override the default transaction type.
     */
    public void setBankBalance(BigDecimal amount, boolean ignoreEvents, TransactionType type) {
        Economy economy = BankPlus.INSTANCE.getEconomy();
        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(type, economy.getBalance(op), amount);
            if (event.isCancelled()) return;

            amount = event.getTransactionAmount();
        }
        set(amount);

        if (!ignoreEvents) endEvent(type, economy.getBalance(op), amount);
    }

    /**
     * Add the selected amount.
     *
     * @param amount The amount.
     */
    public void addBankBalance(BigDecimal amount) {
        addBankBalance(amount, false, false, TransactionType.ADD);
    }

    /**
     * Add the selected amount.
     *
     * @param amount             The amount.
     * @param addOfflineInterest Choose if also adding the offline interest (used, for example, if a player is offline)
     */
    public void addBankBalance(BigDecimal amount, boolean addOfflineInterest) {
        addBankBalance(amount, addOfflineInterest, false, TransactionType.ADD);
    }

    /**
     * Add the selected amount.
     *
     * @param amount             The amount.
     * @param addOfflineInterest Choose if also adding the offline interest (used, for example, if a player is offline)
     * @param ignoreEvents       Choose if firing or not the events.
     */
    public void addBankBalance(BigDecimal amount, boolean addOfflineInterest, boolean ignoreEvents) {
        addBankBalance(amount, addOfflineInterest, ignoreEvents, TransactionType.ADD);
    }

    /**
     * Add the selected amount.
     *
     * @param amount The amount.
     * @param type   Used to override the default transaction type.
     */
    public void addBankBalance(BigDecimal amount, TransactionType type) {
        addBankBalance(amount, false, false, type);
    }

    /**
     * Add the selected amount.
     *
     * @param amount             The amount.
     * @param addOfflineInterest Choose if also adding the offline interest (used, for example, if a player is offline)
     * @param ignoreEvents       Choose if firing or not the events.
     * @param type               Used to override the default transaction type.
     */
    public void addBankBalance(BigDecimal amount, boolean addOfflineInterest, boolean ignoreEvents, TransactionType type) {
        Economy economy = BankPlus.INSTANCE.getEconomy();
        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(type, economy.getBalance(op), amount);
            if (event.isCancelled()) return;

            amount = event.getTransactionAmount();
        }
        set(getBankBalance().add(amount), addOfflineInterest);

        if (!ignoreEvents) endEvent(type, economy.getBalance(op), amount);
    }

    /**
     * Remove the selected amount.
     *
     * @param amount The amount.
     */
    public void removeBankBalance(BigDecimal amount) {
        removeBankBalance(amount, false, TransactionType.REMOVE);
    }

    /**
     * Remove the selected amount.
     *
     * @param amount       The amount.
     * @param ignoreEvents Choose if firing or not the events.
     */
    public void removeBankBalance(BigDecimal amount, boolean ignoreEvents) {
        removeBankBalance(amount, ignoreEvents, TransactionType.REMOVE);
    }

    /**
     * Remove the selected amount.
     *
     * @param amount The amount.
     * @param type   Used to override the default transaction type.
     */
    public void removeBankBalance(BigDecimal amount, TransactionType type) {
        removeBankBalance(amount, false, type);
    }

    /**
     * Remove the selected amount.
     *
     * @param amount       The amount.
     * @param ignoreEvents Choose if firing or not the events.
     */
    public void removeBankBalance(BigDecimal amount, boolean ignoreEvents, TransactionType type) {
        Economy economy = BankPlus.INSTANCE.getEconomy();
        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(type, economy.getBalance(op), amount);
            if (event.isCancelled()) return;

            amount = event.getTransactionAmount();
        }
        set(getBankBalance().subtract(amount));

        if (!ignoreEvents) endEvent(type, economy.getBalance(op), amount);
    }

    private void set(BigDecimal amount) {
        set(amount, false);
    }

    private void set(BigDecimal amount, boolean addOfflineInterest) {
        if (p != null) {
            String amountFormatted = BPFormatter.formatBigDouble(amount);
            BigDecimal finalAmount = new BigDecimal(amountFormatted).doubleValue() < 0 ? new BigDecimal(0) : new BigDecimal(amountFormatted);
            totalPlayerMoney.put(p.getUniqueId(), finalAmount);
            return;
        }

        BPPlayerFiles files = new BPPlayerFiles(op);
        FileConfiguration config = files.getPlayerConfig();
        config.set("Money", BPFormatter.formatBigDouble(amount));
        if (addOfflineInterest && Values.CONFIG.isNotifyOfflineInterest()) {
            BigDecimal offlineInterest = new BigDecimal(BPFormatter.formatBigDouble(amount)).subtract(getBankBalance());
            if (offlineInterest.doubleValue() > 0) config.set("Offline-Interest", offlineInterest.toString());
        }
        files.savePlayerFile(config, true);
    }

    public void deposit(BigDecimal amount) {
        Economy economy = BankPlus.INSTANCE.getEconomy();
        BPPreTransactionEvent event = startEvent(TransactionType.DEPOSIT, economy.getBalance(p), amount);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();

        if (amount.doubleValue() < Values.CONFIG.getDepositMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getDepositMinimumAmount());
            return;
        }

        BigDecimal money = BigDecimal.valueOf(BankPlus.INSTANCE.getEconomy().getBalance(p));
        if (!BPMethods.checkPreRequisites(money, amount, p) || BPMethods.isBankFull(p)) return;

        if (money.doubleValue() < amount.doubleValue()) amount = money;

        BigDecimal maxDepositAmount = Values.CONFIG.getMaxDepositAmount();
        if (maxDepositAmount.doubleValue() > 0 && amount.doubleValue() >= maxDepositAmount.doubleValue())
            amount = maxDepositAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getDepositTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.deposit.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100)));

        BigDecimal capacity = new BankReader(Values.CONFIG.getMainGuiName()).getCapacity(p);
        BigDecimal newBankBalance = getBankBalance().add(amount);

        /*
        When depositing all your money with taxes, the money will have the ability
        to FILL the bank instead of always depositing a bit less and never filling up the bank.
        */
        if (capacity.doubleValue() > 0d && newBankBalance.doubleValue() >= capacity.doubleValue()) {
            BigDecimal moneyToFull = capacity.subtract(getBankBalance());
            amount = moneyToFull.add(taxes);
        }

        EconomyResponse depositResponse = BankPlus.INSTANCE.getEconomy().withdrawPlayer(p, amount.doubleValue());
        if (BPMethods.hasFailed(p, depositResponse)) return;

        addBankBalance(amount.subtract(taxes), false, true);
        BPMessages.send(p, "Success-Deposit", BPMethods.placeValues(p, amount.subtract(taxes)), BPMethods.placeValues(taxes, "taxes"));
        BPMethods.playSound("DEPOSIT", p);

        endEvent(TransactionType.DEPOSIT, economy.getBalance(p), amount);
    }

    public void withdraw(BigDecimal amount) {
        Economy economy = BankPlus.INSTANCE.getEconomy();
        BPPreTransactionEvent event = startEvent(TransactionType.WITHDRAW, economy.getBalance(p), amount);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();

        if (amount.doubleValue() < Values.CONFIG.getWithdrawMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getWithdrawMinimumAmount());
            return;
        }

        BigDecimal bankBal = getBankBalance();
        if (!BPMethods.checkPreRequisites(bankBal, amount, p)) return;

        if (bankBal.doubleValue() < amount.doubleValue()) amount = bankBal;

        BigDecimal maxWithdrawAmount = Values.CONFIG.getMaxWithdrawAmount();
        if (maxWithdrawAmount.doubleValue() > 0 && amount.doubleValue() >= maxWithdrawAmount.doubleValue())
            amount = maxWithdrawAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getWithdrawTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.withdraw.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getWithdrawTaxes().divide(BigDecimal.valueOf(100)));

        EconomyResponse withdrawResponse = economy.depositPlayer(p, amount.subtract(taxes).doubleValue());
        if (BPMethods.hasFailed(p, withdrawResponse)) return;

        removeBankBalance(amount, true);
        BPMessages.send(p, "Success-Withdraw", BPMethods.placeValues(p, amount.subtract(taxes)), BPMethods.placeValues(taxes, "taxes"));
        BPMethods.playSound("WITHDRAW", p);

        endEvent(TransactionType.WITHDRAW, economy.getBalance(p), amount);
    }

    /**
     * Method used to execute the pay transaction.
     *
     * @param target The player that will receive your money.
     * @param amount How much money you want to pay.
     */
    public void pay(Player target, BigDecimal amount) {
        BigDecimal senderBalance = getBankBalance();

        if (senderBalance.doubleValue() < amount.doubleValue()) {
            BPMessages.send(p, "Insufficient-Money");
            return;
        }

        SingleEconomyManager targetEM = new SingleEconomyManager(target);
        BigDecimal targetCapacity = new BankReader(Values.CONFIG.getMainGuiName()).getCapacity(target), targetBalance = targetEM.getBankBalance(), newBalance = amount.add(targetBalance);

        if (targetBalance.doubleValue() >= targetCapacity.doubleValue()) {
            BPMessages.send(p, "Bank-Full", "%player%$" + target.getName());
            return;
        }

        if (newBalance.doubleValue() >= targetCapacity.doubleValue() && targetCapacity.doubleValue() > 0d) {
            removeBankBalance(targetCapacity.subtract(targetBalance), TransactionType.PAY);
            targetEM.setBankBalance(targetCapacity, TransactionType.PAY);

            BPMessages.send(p, "Payment-Sent", BPMethods.placeValues(target, amount));
            BPMessages.send(target, "Payment-Received", BPMethods.placeValues(p, amount));
            return;
        }

        removeBankBalance(amount, TransactionType.PAY);
        targetEM.addBankBalance(amount, TransactionType.PAY);
        BPMessages.send(p, "Payment-Sent", BPMethods.placeValues(target, amount));
        BPMessages.send(target, "Payment-Received", BPMethods.placeValues(p, amount));
    }

    private BPPreTransactionEvent startEvent(TransactionType type, double vaultBalance, BigDecimal amount) {
        BPPreTransactionEvent event = new BPPreTransactionEvent(
                op, type, getBankBalance(), vaultBalance, amount, true, Values.CONFIG.getMainGuiName()
        );
        BPMethods.callEvent(event);
        return event;
    }

    private void endEvent(TransactionType type, double vaultBalance, BigDecimal amount) {
        BPAfterTransactionEvent event = new BPAfterTransactionEvent(
                op, type, getBankBalance(), vaultBalance, amount, true, Values.CONFIG.getMainGuiName()
        );
        BPMethods.callEvent(event);
    }
}