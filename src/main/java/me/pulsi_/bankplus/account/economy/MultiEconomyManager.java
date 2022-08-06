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

    /**
     * Return a list with all player balances.
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

            ConfigurationSection section = config.getConfigurationSection("Banks");
            if (section == null) return balances;

            BigDecimal balance = new BigDecimal(0);
            String name = config.getString("Account-Name") == null ? "null" : config.getString("Account-Name");

            for (String bankName : section.getKeys(false)) {
                String bal = config.getString("Banks." + bankName + ".Money");
                if (bal != null && BPMethods.isValidNumber(bal))
                    balance = balance.add(new BigDecimal(bal));
            }
            balances.add(balance);

            HashMap<BigDecimal, String> balanceName = new HashMap<>();
            balanceName.put(balance, name);
            BankTopManager.linkedBalanceName.add(balanceName);
        }
        return balances;
    }

    /**
     * Load the player bank balances.
     *
     * @param p The player.
     */
    public static void loadBankBalance(Player p) {
        for (String bankName : BanksManager.getBankNames()) {
            if (bankPlayerMoney.containsKey(p.getUniqueId() + "." + bankName)) return;
            String bal = AccountManager.getPlayerConfig(p).getString("Banks." + bankName + ".Money");
            bankPlayerMoney.put(p.getUniqueId() + "." + bankName, bal == null ? new BigDecimal(0) : new BigDecimal(bal));
        }
    }

    /**
     * Unload the player bank balances.
     *
     * @param p The player.
     */
    public static void unloadBankBalance(Player p) {
        FileConfiguration config = AccountManager.getPlayerConfig(p);
        ConfigurationSection section = config.getConfigurationSection("Banks");
        if (section != null) section.getKeys(false).forEach(key -> bankPlayerMoney.remove(p.getUniqueId() + "." + key));
    }

    /**
     * Save the selected bank balance to the player file.
     *
     * @param p The player.
     */
    public static void saveBankBalance(Player p, String bankName) {
        FileConfiguration players = AccountManager.getPlayerConfig(p);
        players.set("Banks." + bankName + ".Money", BPMethods.formatBigDouble(getBankBalance(p, bankName)));
        AccountManager.savePlayerFile(p, true);
    }

    /**
     * Save all bank balances to the player file.
     *
     * @param p The player.
     */
    public static void saveBankBalance(Player p) {
        for (String bankName : BanksManager.getBankNames()) {
            FileConfiguration players = AccountManager.getPlayerConfig(p);
            players.set("Banks." + bankName + ".Money", BPMethods.formatBigDouble(getBankBalance(p, bankName)));
        }
        AccountManager.savePlayerFile(p, true);
    }

    /**
     * Get the player bank balance of the selected bank.
     *
     * @param p        The player.
     * @param bankName The bank.
     * @return BidDecimal amount.
     */
    public static BigDecimal getBankBalance(Player p, String bankName) {
        loadBankBalance(p);
        return bankPlayerMoney.get(p.getUniqueId() + "." + bankName);
    }

    /**
     * Get the total player bank balance of all banks.
     *
     * @param p The player.
     * @return BidDecimal amount.
     */
    public static BigDecimal getBankBalance(Player p) {
        loadBankBalance(p);
        BigDecimal amount = new BigDecimal(0);
        for (String bankName : BanksManager.getBankNames())
            amount = amount.add(bankPlayerMoney.get(p.getUniqueId() + "." + bankName));
        return amount;
    }

    /**
     * Get the offline player bank balance.
     *
     * @param p        The player.
     * @param bankName The bank.
     * @return BidDecimal amount.
     */
    public static BigDecimal getBankBalance(OfflinePlayer p, String bankName) {
        String bal = AccountManager.getPlayerConfig(p).getString("Banks." + bankName + ".Money");
        return bal == null ? new BigDecimal(0) : new BigDecimal(bal);
    }

    /**
     * Get the total offline player bank balance of all banks.
     *
     * @param p The player.
     * @return BidDecimal amount.
     */
    public static BigDecimal getBankBalance(OfflinePlayer p) {
        BigDecimal amount = new BigDecimal(0);
        for (String bankName : BanksManager.getBankNames()) {
            String bal = AccountManager.getPlayerConfig(p).getString("Banks." + bankName + ".Money");
            amount = amount.add(new BigDecimal(bal == null ? "0" : bal));
        }
        return amount;
    }

    /**
     * Set the player bank balance to the selected amount in the selected bank.
     *
     * @param p        The player.
     * @param amount   The new bank balance amount.
     * @param bankName The bank.
     */
    public static void setBankBalance(Player p, BigDecimal amount, String bankName) {
        set(p, amount, bankName);
    }

    /**
     * Set the offline player bank balance to the selected amount in the selected bank.
     *
     * @param p        The player.
     * @param amount   The new bank balance amount.
     * @param bankName The bank.
     */
    public static void setBankBalance(OfflinePlayer p, BigDecimal amount, String bankName) {
        set(p, amount, bankName);
    }

    /**
     * Add to the player bank balance the selected amount.
     *
     * @param p        The player.
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public static void addBankBalance(Player p, BigDecimal amount, String bankName) {
        set(p, getBankBalance(p, bankName).add(amount), bankName);
    }

    /**
     * Add to the offline player bank balance the selected amount.
     *
     * @param p        The player.
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public static void addBankBalance(OfflinePlayer p, BigDecimal amount, String bankName) {
        set(p, getBankBalance(p, bankName).add(amount), bankName);
    }

    /**
     * Remove from the player bank balance the selected amount.
     *
     * @param p        The player.
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public static void removeBankBalance(Player p, BigDecimal amount, String bankName) {
        set(p, getBankBalance(p, bankName).subtract(amount), bankName);
    }

    /**
     * Remove from the offline player bank balance the selected amount.
     *
     * @param p        The player.
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public static void removeBankBalance(OfflinePlayer p, BigDecimal amount, String bankName) {
        set(p, getBankBalance(p, bankName).subtract(amount), bankName);
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param p        The player.
     * @param amount   The amount.
     * @param bankName The bank.
     */
    private static void set(Player p, BigDecimal amount, String bankName) {
        String amountFormatted = BPMethods.formatBigDouble(amount);
        bankPlayerMoney.put(p.getUniqueId() + "." + bankName, new BigDecimal(amountFormatted));
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param p        The player.
     * @param amount   The amount.
     * @param bankName The bank.
     */
    private static void set(OfflinePlayer p, BigDecimal amount, String bankName) {
        AccountManager.getPlayerConfig(p).set("Banks." + bankName + ".Money", BPMethods.formatBigDouble(amount));
        AccountManager.savePlayerFile(p, true);
    }

    public static void deposit(Player p, BigDecimal amount, String bankName) {
        if (!BPMethods.hasPermission(p, "bankplus.deposit")) return;

        BigDecimal money = BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p));
        if (!BPMethods.hasMoney(money, amount, p) || BPMethods.isBankFull(p, bankName)) return;

        BigDecimal maxDepositAmount = Values.CONFIG.getMaxDepositAmount();
        if (maxDepositAmount.doubleValue() != 0 && amount.doubleValue() >= maxDepositAmount.doubleValue()) amount = maxDepositAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getDepositTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.deposit.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100)));

        BigDecimal capacity = BanksManager.getCapacity(p, bankName);
        BigDecimal newBankBalance = getBankBalance(p, bankName).add(amount);
        if (capacity.doubleValue() != 0 && newBankBalance.doubleValue() >= capacity.doubleValue()) {
            BigDecimal moneyToFull = capacity.subtract(getBankBalance(p, bankName));
            amount = moneyToFull.add(taxes);
            if (money.doubleValue() < amount.doubleValue()) amount = money;
        }

        EconomyResponse depositResponse = BankPlus.getEconomy().withdrawPlayer(p, amount.doubleValue());
        BPDebugger.debugDeposit(p, amount, depositResponse);
        if (BPMethods.hasFailed(p, depositResponse)) return;

        addBankBalance(p, amount.subtract(taxes), bankName);
        MessageManager.send(p, "Success-Deposit", BPMethods.placeValues(p, amount.subtract(taxes), taxes));
        BPMethods.playSound("DEPOSIT", p);
    }

    public static void withdraw(Player p, BigDecimal amount, String bankName) {
        if (!BPMethods.hasPermission(p, "bankplus.withdraw")) return;

        BigDecimal bankBalance = getBankBalance(p, bankName);
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

        removeBankBalance(p, amount, bankName);
        MessageManager.send(p, "Success-Withdraw", BPMethods.placeValues(p, amount.subtract(taxes), taxes));
        BPMethods.playSound("WITHDRAW", p);
    }

    public static void pay(Player p1, Player p2, BigDecimal amount, String bankName1, String bankName2) {
        BigDecimal bankBalance = getBankBalance(p1, bankName1);
        BigDecimal maxBankCapacity = Values.CONFIG.getMaxBankCapacity();

        if (bankBalance.doubleValue() < amount.doubleValue()) {
            MessageManager.send(p1, "Insufficient-Money");
            return;
        }

        if (maxBankCapacity.doubleValue() == 0) {
            removeBankBalance(p1, amount, bankName1);
            MessageManager.send(p1, "Payment-Sent", BPMethods.placeValues(p2, amount));
            addBankBalance(p2, amount, bankName2);
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
            removeBankBalance(p1, moneyLeft, bankName1);
            MessageManager.send(p1, "Payment-Sent", BPMethods.placeValues(p2, moneyLeft));
            addBankBalance(p2, moneyLeft, bankName2);
            MessageManager.send(p2, "Payment-Received", BPMethods.placeValues(p1, moneyLeft));
            return;
        }

        removeBankBalance(p1, amount, bankName1);
        MessageManager.send(p1, "Payment-Sent", BPMethods.placeValues(p2, amount));
        addBankBalance(p2, amount, bankName2);
        MessageManager.send(p2, "Payment-Received", BPMethods.placeValues(p1, amount));
    }
}