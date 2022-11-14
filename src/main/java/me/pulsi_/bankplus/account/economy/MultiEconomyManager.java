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
    private final OfflinePlayer oP;

    public MultiEconomyManager(Player player) {
        this.p = player;
        this.oP = null;
    }

    public MultiEconomyManager(OfflinePlayer player) {
        this.p = null;
        this.oP = player;
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
     * Load the player bank balances.
     */
    public void loadBankBalance() {
        if (onNull("Cannot load player bank balance!")) return;
        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
            if (bankPlayerMoney.containsKey(p.getUniqueId() + "." + bankName)) return;
            String bal = new BankPlusPlayerFiles(p).getPlayerConfig().getString("Banks." + bankName + ".Money");
            bankPlayerMoney.put(p.getUniqueId() + "." + bankName, bal == null ? new BigDecimal(0) : new BigDecimal(bal));
        }
    }

    /**
     * Unload the player bank balances.
     */
    public void unloadBankBalance() {
        if (onNull("Cannot unload player bank balance!")) return;
        ConfigurationSection section = new BankPlusPlayerFiles(p).getPlayerConfig().getConfigurationSection("Banks");
        if (section != null) section.getKeys(false).forEach(key -> bankPlayerMoney.remove(p.getUniqueId() + "." + key));
    }

    /**
     * Save the selected bank balance to the player file.
     */
    public void saveBankBalance(String bankName, boolean async) {
        if (onNull("Cannot save player bank balance!")) return;
        BankPlusPlayerFiles files = new BankPlusPlayerFiles(p);
        files.getPlayerConfig().set("Banks." + bankName + ".Money", BPMethods.formatBigDouble(getBankBalance(bankName)));
        files.savePlayerFile(async);
    }

    /**
     * Save all bank balances to the player file.
     */
    public void saveBankBalance(boolean async) {
        if (onNull("Cannot save player bank balances!")) return;
        BankPlusPlayerFiles files = new BankPlusPlayerFiles(p);
        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
            files.getPlayerConfig().set("Banks." + bankName + ".Money", BPMethods.formatBigDouble(getBankBalance(bankName)));
        files.savePlayerFile(async);
    }

    /**
     * Get the player bank balance of the selected bank.
     *
     * @param bankName The bank.
     * @return BidDecimal amount.
     */
    public BigDecimal getBankBalance(String bankName) {
        if (onNull("Cannot get player bank balance!")) return null;
        loadBankBalance();
        return bankPlayerMoney.get(p.getUniqueId() + "." + bankName);
    }

    /**
     * Get the total player bank balance of all banks.
     *
     * @return BidDecimal amount.
     */
    public BigDecimal getBankBalance() {
        if (onNull("Cannot get player bank balances!")) return null;
        loadBankBalance();
        BigDecimal amount = new BigDecimal(0);
        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
            amount = amount.add(bankPlayerMoney.get(p.getUniqueId() + "." + bankName));
        return amount;
    }

    /**
     * Get the offline player bank balance.
     *
     * @param bankName The bank.
     * @return BidDecimal amount.
     */
    public BigDecimal getOfflineBankBalance(String bankName) {
        if (offNull("Cannot get player bank balance!")) return null;
        String bal = new BankPlusPlayerFiles(oP).getPlayerConfig().getString("Banks." + bankName + ".Money");
        return bal == null ? new BigDecimal(0) : new BigDecimal(bal);
    }

    /**
     * Get the total offline player bank balance of all banks.
     *
     * @return BidDecimal amount.
     */
    public BigDecimal getOfflineBankBalance() {
        if (offNull("Cannot get player bank balances!")) return null;
        BigDecimal amount = new BigDecimal(0);
        BankPlusPlayerFiles files = new BankPlusPlayerFiles(oP);
        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
            String bal = files.getPlayerConfig().getString("Banks." + bankName + ".Money");
            amount = amount.add(new BigDecimal(bal == null ? "0" : bal));
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
        if (onNull("Cannot set player bank balance!")) return;
        set(amount, bankName);
    }

    /**
     * Set the offline player bank balance to the selected amount in the selected bank.
     *
     * @param amount   The new bank balance amount.
     * @param bankName The bank.
     */
    public void setOfflineBankBalance(BigDecimal amount, String bankName) {
        if (offNull("Cannot set player bank balance!")) return;
        setOffline(amount, bankName);
    }

    /**
     * Add to the player bank balance the selected amount.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public void addBankBalance(BigDecimal amount, String bankName) {
        if (onNull("Cannot add player bank balance!")) return;
        set(getBankBalance(bankName).add(amount), bankName);
    }

    /**
     * Add to the offline player bank balance the selected amount.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public void addOfflineBankBalance(BigDecimal amount, String bankName) {
        addOfflineBankBalance(amount, bankName, false);
    }

    /**
     * Add to the offline player bank balance the selected amount.
     *
     * @param amount             The amount.
     * @param bankName           The bank.
     * @param addOfflineInterest Choose if adding both balance and offline interest.
     */
    public void addOfflineBankBalance(BigDecimal amount, String bankName, boolean addOfflineInterest) {
        if (offNull("Cannot add player bank balance!")) return;
        setOffline(getOfflineBankBalance(bankName).add(amount), bankName, addOfflineInterest);
    }

    /**
     * Remove from the player bank balance the selected amount.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public void removeBankBalance(BigDecimal amount, String bankName) {
        if (onNull("Cannot remove player bank balance!")) return;
        set(getBankBalance(bankName).subtract(amount), bankName);
    }

    /**
     * Remove from the offline player bank balance the selected amount.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     */
    public void removeOfflineBankBalance(BigDecimal amount, String bankName) {
        if (offNull("Cannot remove player bank balance!")) return;
        setOffline(getOfflineBankBalance(bankName).subtract(amount), bankName);
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     */
    private void set(BigDecimal amount, String bankName) {
        String amountFormatted = BPMethods.formatBigDouble(amount);
        BigDecimal finalAmount = new BigDecimal(amountFormatted).doubleValue() < 0 ? new BigDecimal(0) : new BigDecimal(amountFormatted);
        bankPlayerMoney.put(p.getUniqueId() + "." + bankName, finalAmount);
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param amount   The amount.
     * @param bankName The bank.
     */
    private void setOffline(BigDecimal amount, String bankName) {
        setOffline(amount, bankName, false);
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param amount             The amount.
     * @param bankName           The bank.
     * @param addOfflineInterest Choose if adding both balance and offline interest.
     */
    private void setOffline(BigDecimal amount, String bankName, boolean addOfflineInterest) {
        BankPlusPlayerFiles files = new BankPlusPlayerFiles(oP);
        FileConfiguration config = files.getOfflinePlayerConfig();
        config.set("Banks." + bankName + ".Money", BPMethods.formatBigDouble(amount));
        if (Values.CONFIG.isNotifyOfflineInterest()) {
            BigDecimal offlineInterest = new BigDecimal(BPMethods.formatBigDouble(amount)).subtract(getOfflineBankBalance(bankName));
            if (offlineInterest.doubleValue() > 0) config.set("Offline-Interest", offlineInterest.toString());
        }

        files.saveOfflinePlayerFile(config, true);
    }

    public void deposit(BigDecimal amount, String bankName) {
        if (onNull("Cannot deposit player bank balance!") || !BPMethods.hasPermission(p, "bankplus.deposit")) return;

        if (amount.doubleValue() < Values.CONFIG.getDepositMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getDepositMinimumAmount());
            return;
        }

        BigDecimal money = BigDecimal.valueOf(BankPlus.INSTANCE.getEconomy().getBalance(p));
        if (!BPMethods.checkPreRequisites(money, amount, p) || BPMethods.isBankFull(p)) return;

        BigDecimal maxDepositAmount = Values.CONFIG.getMaxDepositAmount();
        if (maxDepositAmount.doubleValue() != 0 && amount.doubleValue() >= maxDepositAmount.doubleValue())
            amount = maxDepositAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getDepositTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.deposit.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100)));

        BigDecimal capacity = new BanksManager(bankName).getCapacity(p);
        BigDecimal newBankBalance = getBankBalance().add(amount);

        /*
        Make it possible so when depositing all your money with taxes, the money will have the ability
        to FILL the bank instead of always depositing a bit less and never filling up the bank.
        */
        if (capacity.doubleValue() != 0 && newBankBalance.doubleValue() >= capacity.doubleValue()) {
            BigDecimal moneyToFull = capacity.subtract(getBankBalance());
            amount = moneyToFull.add(taxes);
            if (money.doubleValue() < amount.doubleValue()) amount = money;
        }

        EconomyResponse depositResponse = BankPlus.INSTANCE.getEconomy().withdrawPlayer(p, amount.doubleValue());
        BankPlus.DEBUGGER.debugTransactions(p, amount, depositResponse);
        if (BPMethods.hasFailed(p, depositResponse)) return;

        addBankBalance(amount.subtract(taxes), bankName);
        BPMessages.send(p, "Success-Deposit", BPMethods.placeValues(p, amount.subtract(taxes), taxes));
        BPMethods.playSound("DEPOSIT", p);
    }

    public void withdraw(BigDecimal amount, String bankName) {
        if (onNull("Cannot withdraw player bank balance!") || !BPMethods.hasPermission(p, "bankplus.withdraw")) return;

        if (amount.doubleValue() < Values.CONFIG.getWithdrawMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getWithdrawMinimumAmount());
            return;
        }

        BigDecimal bankBal = getBankBalance(bankName);
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

        removeBankBalance(amount.add(taxes), bankName);
        BPMessages.send(p, "Success-Withdraw", BPMethods.placeValues(p, amount.subtract(taxes), taxes));
        BPMethods.playSound("WITHDRAW", p);
    }

    /**
     * Method used to execute the pay transaction.
     *
     * @param fromBank The bank where the money will be taken.
     * @param target   The player that will receive your money.
     * @param amount   How much money you want to pay.
     * @param toBank   The bank where the money will be added.
     */
    public void pay(String fromBank, Player target, BigDecimal amount, String toBank) {
        if (onNull("Cannot initialize pay transaction!")) return;

        MultiEconomyManager targetManager = new MultiEconomyManager(target);
        BigDecimal maxBankCapacity = Values.CONFIG.getMaxBankCapacity();
        BigDecimal bankBalance = getBankBalance(fromBank);

        if (bankBalance.doubleValue() < amount.doubleValue()) {
            BPMessages.send(p, "Insufficient-Money");
            return;
        }

        if (maxBankCapacity.doubleValue() == 0) {
            removeBankBalance(amount, fromBank);
            BPMessages.send(p, "Payment-Sent", BPMethods.placeValues(target, amount));
            targetManager.addBankBalance(amount, toBank);
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
            removeBankBalance(moneyLeft, fromBank);
            BPMessages.send(p, "Payment-Sent", BPMethods.placeValues(target, moneyLeft));
            targetManager.addBankBalance(moneyLeft, toBank);
            BPMessages.send(target, "Payment-Received", BPMethods.placeValues(p, moneyLeft));
            return;
        }

        removeBankBalance(amount, fromBank);
        BPMessages.send(p, "Payment-Sent", BPMethods.placeValues(target, amount));
        targetManager.addBankBalance(amount, toBank);
        BPMessages.send(target, "Payment-Received", BPMethods.placeValues(p, amount));
    }

    private boolean onNull(String tried) {
        if (p == null) {
            BPLogger.error(tried + " Specified offline-player but called online-player method!");
            return true;
        }
        return false;
    }

    private boolean offNull(String tried) {
        if (oP == null) {
            BPLogger.error(tried + " Specified online-player but called offline-player method!");
            return true;
        }
        return false;
    }
}