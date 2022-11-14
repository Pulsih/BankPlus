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
    private final OfflinePlayer oP;

    public SingleEconomyManager(Player player) {
        this.p = player;
        this.oP = null;
    }

    public SingleEconomyManager(OfflinePlayer offlinePlayer) {
        this.p = null;
        this.oP = offlinePlayer;
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
     * Load the player bank balance.
     */
    public void loadBankBalance() {
        if (onNull("Cannot load player bank balance!")) return;
        if (totalPlayerMoney.containsKey(p.getUniqueId())) return;
        String bal = new BankPlusPlayerFiles(p).getPlayerConfig().getString("Money");
        totalPlayerMoney.put(p.getUniqueId(), bal == null ? new BigDecimal(0) : new BigDecimal(bal));
    }

    /**
     * Unload the player bank balance.
     */
    public void unloadBankBalance() {
        if (onNull("Cannot unload player bank balance!")) return;
        totalPlayerMoney.remove(p.getUniqueId());
    }

    /**
     * Save the player bank balance to his file.
     */
    public void saveBankBalance(boolean async) {
        if (onNull("Cannot save player bank balance!")) return;
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
        if (onNull("Cannot get player bank balance!")) return null;
        loadBankBalance();
        return totalPlayerMoney.get(p.getUniqueId());
    }

    /**
     * Get the offline player bank balance.
     *
     * @return BidDecimal amount.
     */
    public BigDecimal getOfflineBankBalance() {
        if (offNull("Cannot get player bank balance!")) return null;
        String bal = new BankPlusPlayerFiles(oP).getOfflinePlayerConfig().getString("Money");
        return bal == null ? new BigDecimal(0) : new BigDecimal(bal);
    }

    /**
     * Set the player bank balance to the selected amount.
     *
     * @param amount The new bank balance amount.
     */
    public void setBankBalance(BigDecimal amount) {
        if (onNull("Cannot set player bank balance!")) return;
        set(amount);
    }

    /**
     * Set the offline player bank balance to the selected amount.
     *
     * @param amount The new bank balance amount.
     */
    public void setOfflineBankBalance(BigDecimal amount) {
        if (offNull( "Cannot set player bank balance!")) return;
        setOffline(amount);
    }

    /**
     * Add to the player bank balance the selected amount.
     *
     * @param amount The amount.
     */
    public void addBankBalance(BigDecimal amount) {
        if (onNull( "Cannot add player bank balance!")) return;
        set(getBankBalance().add(amount));
    }

    /**
     * Add to the offline player bank balance the selected amount.
     *
     * @param amount The amount.
     */
    public void addOfflineBankBalance(BigDecimal amount) {
        addOfflineBankBalance(amount, false);
    }

    /**
     * Add to the offline player bank balance the selected amount.
     *
     * @param amount             The amount.
     * @param addOfflineInterest Choose if adding both balance and offline interest.
     */
    public void addOfflineBankBalance(BigDecimal amount, boolean addOfflineInterest) {
        if (offNull("Cannot add player bank balance!")) return;
        setOffline(getOfflineBankBalance().add(amount), addOfflineInterest);
    }

    /**
     * Remove from the player bank balance the selected amount.
     *
     * @param amount The amount.
     */
    public void removeBankBalance(BigDecimal amount) {
        if (onNull("Cannot remove player bank balance!")) return;
        set(getBankBalance().subtract(amount));
    }

    /**
     * Remove from the offline player bank balance the selected amount.
     *
     * @param amount The amount.
     */
    public void removeOfflineBankBalance(BigDecimal amount) {
        if (offNull("Cannot remove player bank balance!")) return;
        setOffline(getOfflineBankBalance().subtract(amount));
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param amount The amount.
     */
    private void set(BigDecimal amount) {
        String amountFormatted = BPMethods.formatBigDouble(amount);
        BigDecimal finalAmount = new BigDecimal(amountFormatted).doubleValue() < 0 ? new BigDecimal(0) : new BigDecimal(amountFormatted);
        totalPlayerMoney.put(p.getUniqueId(), finalAmount);
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param amount The amount.
     */
    private void setOffline(BigDecimal amount) {
        setOffline(amount, false);
    }

    /**
     * Method used to simplify the transaction.
     *
     * @param amount             The amount.
     * @param addOfflineInterest Choose if adding both balance and offline interest.
     */
    private void setOffline(BigDecimal amount, boolean addOfflineInterest) {
        BankPlusPlayerFiles files = new BankPlusPlayerFiles(oP);
        FileConfiguration config = files.getOfflinePlayerConfig();
        config.set("Money", BPMethods.formatBigDouble(amount));
        if (addOfflineInterest && Values.CONFIG.isNotifyOfflineInterest()) {
            BigDecimal offlineInterest = new BigDecimal(BPMethods.formatBigDouble(amount)).subtract(getOfflineBankBalance());
            if (offlineInterest.doubleValue() > 0) config.set("Offline-Interest", offlineInterest.toString());
        }
        files.saveOfflinePlayerFile(config, true);
    }

    public void deposit(BigDecimal amount) {
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

        BigDecimal capacity = new BanksManager(Values.CONFIG.getMainGuiName()).getCapacity(p);
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

        addBankBalance(amount.subtract(taxes));
        BPMessages.send(p, "Success-Deposit", BPMethods.placeValues(p, amount.subtract(taxes), taxes));
        BPMethods.playSound("DEPOSIT", p);
    }

    public void withdraw(BigDecimal amount) {
        if (onNull("Cannot withdraw player bank balance!") || !BPMethods.hasPermission(p, "bankplus.withdraw")) return;

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
        if (onNull("Cannot initialize pay transaction!")) return;

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