package me.pulsi_.bankplus.account.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayerFiles;
import me.pulsi_.bankplus.bankGuis.BanksManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class SingleEconomyManager {

    /**
     * The main HashMap that holds all player bank balances.
     */
    public static Map<UUID, BigDecimal> totalPlayerMoney = new HashMap<>();
    private final Player p;
    private final OfflinePlayer op;

    public SingleEconomyManager(Player p) {
        this.p = p;
        this.op = null;
    }

    public SingleEconomyManager(OfflinePlayer op) {
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
        String bal = new BankPlusPlayerFiles(p).getPlayerConfig().getString("Money");
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
        BankPlusPlayerFiles files = new BankPlusPlayerFiles(p);
        files.getPlayerConfig().set("Money", BPMethods.formatBigDouble(getBankBalance()));
        files.savePlayerFile(async);
    }

    /**
     * Get the player bank balance.
     *
     * @return BidDecimal amount.
     */
    public BigDecimal getBankBalance() {
        if (p == null) {
            String bal = new BankPlusPlayerFiles(op).getPlayerConfig().getString("Money");
            return bal == null ? new BigDecimal(0) : new BigDecimal(bal);
        }
        loadBankBalance();
        return totalPlayerMoney.get(p.getUniqueId());
    }

    /**
     * Set the player bank balance to the selected amount.
     *
     * @param amount The new bank balance amount.
     */
    public void setBankBalance(BigDecimal amount) {
        set(amount);
    }

    /**
     * Add to the offline player bank balance the selected amount.
     *
     * @param amount The amount.
     */
    public void addBankBalance(BigDecimal amount) {
        addBankBalance(amount, false);
    }

    /**
     * Add to the offline player bank balance the selected amount.
     *
     * @param amount             The amount.
     * @param addOfflineInterest Choose if adding both balance and offline interest.
     */
    public void addBankBalance(BigDecimal amount, boolean addOfflineInterest) {
        set(getBankBalance().add(amount), addOfflineInterest);
    }

    /**
     * Remove from the player bank balance the selected amount.
     *
     * @param amount The amount.
     */
    public void removeBankBalance(BigDecimal amount) {
        set(getBankBalance().subtract(amount));
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param amount The amount.
     */
    private void set(BigDecimal amount) {
        set(amount, false);
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param amount             The amount.
     * @param addOfflineInterest Choose if adding both balance and offline interest.
     */
    private void set(BigDecimal amount, boolean addOfflineInterest) {
        if (p != null) {
            String amountFormatted = BPMethods.formatBigDouble(amount);
            BigDecimal finalAmount = new BigDecimal(amountFormatted).doubleValue() < 0 ? new BigDecimal(0) : new BigDecimal(amountFormatted);
            totalPlayerMoney.put(p.getUniqueId(), finalAmount);
            return;
        }

        BankPlusPlayerFiles files = new BankPlusPlayerFiles(op);
        FileConfiguration config = files.getPlayerConfig();
        config.set("Money", BPMethods.formatBigDouble(amount));
        if (addOfflineInterest && Values.CONFIG.isNotifyOfflineInterest()) {
            BigDecimal offlineInterest = new BigDecimal(BPMethods.formatBigDouble(amount)).subtract(getBankBalance());
            if (offlineInterest.doubleValue() > 0) config.set("Offline-Interest", offlineInterest.toString());
        }
        files.savePlayerFile(config, true);
    }

    public void deposit(BigDecimal amount) {
        if (amount.doubleValue() < Values.CONFIG.getDepositMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getDepositMinimumAmount());
            return;
        }

        BigDecimal money = BigDecimal.valueOf(BankPlus.INSTANCE.getEconomy().getBalance(p));
        if (!BPMethods.checkPreRequisites(money, amount, p) || BPMethods.isBankFull(p)) return;

        BigDecimal maxDepositAmount = Values.CONFIG.getMaxDepositAmount();
        if (maxDepositAmount.doubleValue() > 0 && amount.doubleValue() >= maxDepositAmount.doubleValue())
            amount = maxDepositAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getDepositTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.deposit.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100)));

        BigDecimal capacity = new BanksManager(Values.CONFIG.getMainGuiName()).getCapacity(p);
        BigDecimal newBankBalance = getBankBalance().add(amount);

        /*
        Make it possible so when depositing all your money with taxes, the money will have the ability
        to FILL the bank instead of always depositing a bit less and never filling up the bank.
        */
        if (capacity.doubleValue() > 0 && newBankBalance.doubleValue() >= capacity.doubleValue()) {
            BigDecimal moneyToFull = capacity.subtract(getBankBalance());
            amount = moneyToFull.add(taxes);
            if (money.doubleValue() < amount.doubleValue()) amount = money;
        }

        EconomyResponse depositResponse = BankPlus.INSTANCE.getEconomy().withdrawPlayer(p, amount.doubleValue());
        BankPlus.DEBUGGER.debugTransactions(p, amount, depositResponse);
        if (BPMethods.hasFailed(p, depositResponse)) return;

        addBankBalance(amount.subtract(taxes));
        BPMessages.send(p, "Success-Deposit", BPMethods.placeValues(p, amount.subtract(taxes), taxes));
        BPMethods.playSound("DEPOSIT", p);
    }

    public void withdraw(BigDecimal amount) {
        if (amount.doubleValue() < Values.CONFIG.getWithdrawMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getWithdrawMinimumAmount());
            return;
        }

        BigDecimal bankBal = getBankBalance();
        if (!BPMethods.checkPreRequisites(bankBal, amount, p)) return;
        if (amount.doubleValue() > bankBal.doubleValue()) amount = bankBal;

        BigDecimal maxWithdrawAmount = Values.CONFIG.getMaxWithdrawAmount();
        if (maxWithdrawAmount.doubleValue() != 0 && amount.doubleValue() >= maxWithdrawAmount.doubleValue())
            amount = maxWithdrawAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getWithdrawTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.withdraw.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100)));

        EconomyResponse withdrawResponse = BankPlus.INSTANCE.getEconomy().depositPlayer(p, amount.doubleValue());
        BankPlus.DEBUGGER.debugTransactions(p, amount, withdrawResponse);
        if (BPMethods.hasFailed(p, withdrawResponse)) return;

        removeBankBalance(amount.add(taxes));
        BPMessages.send(p, "Success-Withdraw", BPMethods.placeValues(p, amount.subtract(taxes), taxes));
        BPMethods.playSound("WITHDRAW", p);
    }

    /**
     * Method used to execute the pay transaction.
     *
     * @param target The player that will receive your money.
     * @param amount How much money you want to pay.
     */
    public void pay(Player target, BigDecimal amount) {
        SingleEconomyManager targetManager = new SingleEconomyManager(target);
        BigDecimal maxBankCapacity = Values.CONFIG.getMaxBankCapacity();
        BigDecimal bankBalance = getBankBalance();

        if (bankBalance.doubleValue() < amount.doubleValue()) {
            BPMessages.send(p, "Insufficient-Money");
            return;
        }

        if (maxBankCapacity.doubleValue() == 0) {
            removeBankBalance(amount);
            BPMessages.send(p, "Payment-Sent", BPMethods.placeValues(target, amount));
            targetManager.addBankBalance(amount);
            BPMessages.send(target, "Payment-Received", BPMethods.placeValues(p, amount));
            return;
        }

        BigDecimal targetMoney = targetManager.getBankBalance();
        if (targetMoney.doubleValue() >= maxBankCapacity.doubleValue()) {
            BPMessages.send(p, "Bank-Full", BPMethods.placeValues(target, BigDecimal.valueOf(0)));
            return;
        }

        BigDecimal moneyLeft = maxBankCapacity.subtract(targetMoney);
        if (amount.doubleValue() >= moneyLeft.doubleValue()) {
            removeBankBalance(moneyLeft);
            BPMessages.send(p, "Payment-Sent", BPMethods.placeValues(target, moneyLeft));
            targetManager.addBankBalance(moneyLeft);
            BPMessages.send(target, "Payment-Received", BPMethods.placeValues(p, moneyLeft));
            return;
        }

        removeBankBalance(amount);
        BPMessages.send(p, "Payment-Sent", BPMethods.placeValues(target, amount));
        targetManager.addBankBalance(amount);
        BPMessages.send(target, "Payment-Received", BPMethods.placeValues(p, amount));
    }
}