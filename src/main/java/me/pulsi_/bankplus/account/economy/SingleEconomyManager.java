package me.pulsi_.bankplus.account.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.AccountManager;
import me.pulsi_.bankplus.guis.BanksManager;
import me.pulsi_.bankplus.managers.BankTopManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.BPDebugger;
import me.pulsi_.bankplus.utils.BPLogger;
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

    /**
     * Return a list with all player balances.
     * Do not call this method many times, it has to make
     * various calculations to get the final list of balances.
     *
     * @return List of BigDecimal amounts.
     */
    public static List<BigDecimal> getAllBankBalances() {
        List<BigDecimal> balances = new ArrayList<>();

        File dataFolder = new File(BankPlus.getInstance().getDataFolder(), "playerdata");
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
            BankTopManager.linkedBalanceName.add(balanceName);
        }
        return balances;
    }

    /**
     * Load the player bank balance.
     *
     * @param p The player.
     */
    public static void loadBankBalance(Player p) {
        if (totalPlayerMoney.containsKey(p.getUniqueId())) return;
        String bal = AccountManager.getPlayerConfig(p).getString("Money");
        totalPlayerMoney.put(p.getUniqueId(), bal == null ? new BigDecimal(0) : new BigDecimal(bal));
    }

    /**
     * Unload the player bank balance.
     *
     * @param p The player.
     */
    public static void unloadBankBalance(Player p) {
        totalPlayerMoney.remove(p.getUniqueId());
    }

    /**
     * Save the player bank balance to his file.
     *
     * @param p The player.
     */
    public static void saveBankBalance(Player p) {
        FileConfiguration players = AccountManager.getPlayerConfig(p);
        players.set("Money", BPMethods.formatBigDouble(getBankBalance(p)));
        AccountManager.savePlayerFile(p, true);
    }

    /**
     * Get the player bank balance.
     *
     * @param p The player.
     * @return BidDecimal amount.
     */
    public static BigDecimal getBankBalance(Player p) {
        loadBankBalance(p);
        return totalPlayerMoney.get(p.getUniqueId());
    }

    /**
     * Get the offline player bank balance.
     *
     * @param p The player.
     * @return BidDecimal amount.
     */
    public static BigDecimal getBankBalance(OfflinePlayer p) {
        String bal = AccountManager.getPlayerConfig(p).getString("Money");
        return bal == null ? new BigDecimal(0) : new BigDecimal(bal);
    }

    /**
     * Set the player bank balance to the selected amount.
     *
     * @param p      The player.
     * @param amount The new bank balance amount.
     */
    public static void setBankBalance(Player p, BigDecimal amount) {
        set(p, amount);
    }

    /**
     * Set the offline player bank balance to the selected amount.
     *
     * @param p      The player.
     * @param amount The new bank balance amount.
     */
    public static void setBankBalance(OfflinePlayer p, BigDecimal amount) {
        set(p, amount);
    }

    /**
     * Add to the player bank balance the selected amount.
     *
     * @param p      The player.
     * @param amount The amount.
     */
    public static void addBankBalance(Player p, BigDecimal amount) {
        set(p, getBankBalance(p).add(amount));
    }

    /**
     * Add to the offline player bank balance the selected amount.
     *
     * @param p      The player.
     * @param amount The amount.
     */
    public static void addBankBalance(OfflinePlayer p, BigDecimal amount) {
        set(p, getBankBalance(p).add(amount));
    }

    /**
     * Remove from the player bank balance the selected amount.
     *
     * @param p      The player.
     * @param amount The amount.
     */
    public static void removeBankBalance(Player p, BigDecimal amount) {
        set(p, getBankBalance(p).subtract(amount));
    }

    /**
     * Remove from the offline player bank balance the selected amount.
     *
     * @param p      The player.
     * @param amount The amount.
     */
    public static void removeBankBalance(OfflinePlayer p, BigDecimal amount) {
        set(p, getBankBalance(p).subtract(amount));
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param p      The player.
     * @param amount The amount.
     */
    private static void set(Player p, BigDecimal amount) {
        String amountFormatted = BPMethods.formatBigDouble(amount);
        totalPlayerMoney.put(p.getUniqueId(), new BigDecimal(amountFormatted));
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param p      The player.
     * @param amount The amount.
     */
    private static void set(OfflinePlayer p, BigDecimal amount) {
        AccountManager.getPlayerConfig(p).set("Money", BPMethods.formatBigDouble(amount));
        AccountManager.savePlayerFile(p, true);
    }

    public static void deposit(Player p, BigDecimal amount) {
        if (!BPMethods.hasPermission(p, "bankplus.deposit")) return;

        BigDecimal money = BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p));
        if (!BPMethods.hasMoney(money, amount, p) || BPMethods.isBankFull(p)) return;

        BigDecimal maxDepositAmount = Values.CONFIG.getMaxDepositAmount();
        if (maxDepositAmount.doubleValue() != 0 && amount.doubleValue() >= maxDepositAmount.doubleValue()) amount = maxDepositAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getDepositTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.deposit.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100)));

        BigDecimal capacity = BanksManager.getCapacity(p, Values.CONFIG.getMainGuiName());
        BigDecimal newBankBalance = getBankBalance(p).add(amount);
        if (capacity.doubleValue() != 0 && newBankBalance.doubleValue() >= capacity.doubleValue()) {
            BigDecimal moneyToFull = capacity.subtract(getBankBalance(p));
            amount = moneyToFull.add(taxes);
            if (money.doubleValue() < amount.doubleValue()) amount = money;
        }

        EconomyResponse depositResponse = BankPlus.getEconomy().withdrawPlayer(p, amount.doubleValue());
        BPDebugger.debugDeposit(p, amount, depositResponse);
        if (BPMethods.hasFailed(p, depositResponse)) return;

        addBankBalance(p, amount.subtract(taxes));
        MessageManager.send(p, "Success-Deposit", BPMethods.placeValues(p, amount.subtract(taxes), taxes));
        BPMethods.playSound("DEPOSIT", p);
    }

    public static void withdraw(Player p, BigDecimal amount) {
        if (!BPMethods.hasPermission(p, "bankplus.withdraw")) return;

        BigDecimal bankBalance = getBankBalance(p);
        if (!BPMethods.hasMoney(bankBalance, amount, p)) return;

        BigDecimal maxWithdrawAmount = Values.CONFIG.getMaxWithdrawAmount();
        if (maxWithdrawAmount.doubleValue() != 0 && amount.doubleValue() >= maxWithdrawAmount.doubleValue())
            amount = maxWithdrawAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getWithdrawTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.withdraw.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getWithdrawTaxes().divide(BigDecimal.valueOf(100)));

        BigDecimal newBalance = bankBalance.subtract(amount);
        if (newBalance.doubleValue() <= 0) amount = bankBalance;

        EconomyResponse withdrawResponse = BankPlus.getEconomy().depositPlayer(p, amount.subtract(taxes).doubleValue());
        BPDebugger.debugWithdraw(p, amount, withdrawResponse);
        if (BPMethods.hasFailed(p, withdrawResponse)) return;

        removeBankBalance(p, amount);
        MessageManager.send(p, "Success-Withdraw", BPMethods.placeValues(p, amount.subtract(taxes), taxes));
        BPMethods.playSound("WITHDRAW", p);
    }

    public static void pay(Player p1, Player p2, BigDecimal amount) {
        BigDecimal bankBalance = getBankBalance(p1);
        BigDecimal maxBankCapacity = Values.CONFIG.getMaxBankCapacity();

        if (bankBalance.doubleValue() < amount.doubleValue()) {
            MessageManager.send(p1, "Insufficient-Money");
            return;
        }

        if (maxBankCapacity.doubleValue() == 0) {
            removeBankBalance(p1, amount);
            MessageManager.send(p1, "Payment-Sent", BPMethods.placeValues(p2, amount));
            addBankBalance(p2, amount);
            MessageManager.send(p2, "Payment-Received", BPMethods.placeValues(p1, amount));
            return;
        }

        BigDecimal targetMoney = getBankBalance(p2);
        if (targetMoney.doubleValue() >= maxBankCapacity.doubleValue()) {
            MessageManager.send(p1, "Bank-Full", BPMethods.placeValues(p2, BigDecimal.valueOf(0)));
            return;
        }

        BigDecimal moneyLeft = maxBankCapacity.subtract(targetMoney);
        if (amount.doubleValue() >= moneyLeft.doubleValue()) {
            removeBankBalance(p1, moneyLeft);
            MessageManager.send(p1, "Payment-Sent", BPMethods.placeValues(p2, moneyLeft));
            addBankBalance(p2, moneyLeft);
            MessageManager.send(p2, "Payment-Received", BPMethods.placeValues(p1, moneyLeft));
            return;
        }

        removeBankBalance(p1, amount);
        MessageManager.send(p1, "Payment-Sent", BPMethods.placeValues(p2, amount));
        addBankBalance(p2, amount);
        MessageManager.send(p2, "Payment-Received", BPMethods.placeValues(p1, amount));
    }
}