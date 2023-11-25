package me.pulsi_.bankplus.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.bankSystem.BankGuiRegistry;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.events.BPAfterTransactionEvent;
import me.pulsi_.bankplus.events.BPPreTransactionEvent;
import me.pulsi_.bankplus.mySQL.BPSQL;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * BankPlus main core class to manage the entire server economy.
 * In this class, you'll find many useful methods that will help you get, modify and set player's balances.
 * <p>
 * To access this class, use the method {@link BankPlus#getBPEconomy()};
 */
public class BPEconomy {

    private final BankGuiRegistry banksRegistry;

    public BPEconomy() {
        banksRegistry = BankPlus.INSTANCE.getBankGuiRegistry();
    }

    /**
     * Return a list with all player balances.
     *
     * @return A hashmap with the player name as key and the sum of all the player bank balances as value.
     */
    public LinkedHashMap<String, BigDecimal> getAllBankBalances() {
        LinkedHashMap<String, BigDecimal> balances = new LinkedHashMap<>();

        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            BigDecimal balance = new BigDecimal(0);
            for (String bankName : new BankManager().getAvailableBanks(p))
                balance = balance.add(getBankBalance(p, bankName));
            balances.put(p.getName(), balance);
        }
        return balances;
    }

    /**
     * Get the player bank balance of the selected bank.
     * @param uuid The UUID of the player.
     * @param bankName The bank where to get the balance.
     */
    public BigDecimal getBankBalance(UUID uuid, String bankName) {
        return getBankBalance(Bukkit.getOfflinePlayer(uuid), bankName);
    }

    /**
     * Get the player bank balance of the selected bank.
     * @param p The player.
     * @param bankName The bank where to get the balance.
     */
    public BigDecimal getBankBalance(OfflinePlayer p, String bankName) {
        HashMap<String, BPPlayer.PlayerBank> bankInformation = new BPPlayerManager(p).getBankInformation();
        if (bankInformation != null) {
            BPPlayer.PlayerBank info = bankInformation.get(bankName);
            if (info != null) return info.getBalance();
        }

        if (Values.CONFIG.isSqlEnabled() && BankPlus.INSTANCE.getSql().isConnected())
            return new SQLPlayerManager(p).getMoney(bankName);

        String bal = new BPPlayerManager(p).getPlayerConfig().getString("banks." + bankName + ".money");
        return new BigDecimal(bal == null ? "0" : bal);
    }

    /**
     * Get the sum of the player bank balance of all banks.
     * @param p The player.
     */
    public BigDecimal getBankBalance(OfflinePlayer p) {
        BigDecimal amount = new BigDecimal(0);
        for (String bankName : banksRegistry.getBanks().keySet()) amount = amount.add(getBankBalance(p, bankName));
        return amount;
    }

    /**
     * Set the selected amount in the selected bank.
     *
     * @return Number representing the actual amount set.
     */
    public BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount, String bankName) {
        return setBankBalance(p, amount, bankName, TransactionType.SET);
    }

    /**
     * Set the selected amount in the selected bank.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     * @return Number representing the actual amount set.
     */
    public BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, boolean ignoreEvents) {
        return setBankBalance(p, amount, bankName, ignoreEvents, TransactionType.SET);
    }

    /**
     * Set the selected amount in the selected bank.
     *
     * @param type Override the transaction type with the one you choose.
     * @return Number representing the actual amount set.
     */
    public BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, TransactionType type) {
        return setBankBalance(p, amount, bankName, false, type);
    }

    private BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, boolean ignoreEvents, TransactionType type) {
        Economy economy = BankPlus.INSTANCE.getVaultEconomy();
        BigDecimal result = new BigDecimal(0);

        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(p, type, economy.getBalance(p), amount, bankName);
            if (event.isCancelled()) return result;

            amount = event.getTransactionAmount();
        }

        result = result.max(amount.min(new BankManager(bankName).getCapacity(p)));
        set(p, result, bankName);

        if (!ignoreEvents) endEvent(p, type, economy.getBalance(p), amount, bankName);
        return result;
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, String bankName) {
        return addBankBalance(p, amount, bankName, false, TransactionType.ADD, false);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, boolean ignoreEvents) {
        return addBankBalance(p, amount, bankName, ignoreEvents, TransactionType.ADD, false);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param type Override the transaction type with the one you choose.
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, TransactionType type) {
        return addBankBalance(p, amount, bankName, false, type, false);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param type Override the transaction type with the one you choose.
     * @param addOfflineInterest Choose if updating the offline interest with this transaction.
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, TransactionType type, boolean addOfflineInterest) {
        return addBankBalance(p, amount, bankName, false, type, addOfflineInterest);
    }

    private BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, boolean ignoreEvents, TransactionType type, boolean addOfflineInterest) {
        Economy economy = BankPlus.INSTANCE.getVaultEconomy();
        BigDecimal result = new BigDecimal(0);

        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(p, type, economy.getBalance(p), amount, bankName);
            if (event.isCancelled()) return result;

            amount = event.getTransactionAmount();
        }

        BigDecimal capacity = new BankManager(bankName).getCapacity(p), balance = getBankBalance(p, bankName);
        if (capacity.doubleValue() <= 0D || balance.add(amount).doubleValue() < capacity.doubleValue()) {
            result = amount;
            BigDecimal moneyToAdd = balance.add(result);
            if (addOfflineInterest) set(p, moneyToAdd, result, bankName);
            else set(p, balance.add(result), bankName);
        } else {
            result = capacity.subtract(balance);

            if (addOfflineInterest) set(p, capacity, result, bankName);
            else set(p, capacity, bankName);
        }

        if (!ignoreEvents) endEvent(p, type, economy.getBalance(p), amount, bankName);
        return result;
    }

    /**
     * Remove the selected amount.
     *
     * @return Number representing the actual amount removed.
     */
    public BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount, String bankName) {
        return removeBankBalance(p, amount, bankName, TransactionType.REMOVE);
    }

    /**
     * Remove the selected amount.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     * @return Number representing the actual amount removed.
     */
    public BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, boolean ignoreEvents) {
        return removeBankBalance(p, amount, bankName, ignoreEvents, TransactionType.REMOVE);
    }

    /**
     * Remove the selected amount.
     *
     * @param type Override the transaction type with the one you choose.
     * @return Number representing the actual amount removed.
     */
    public BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, TransactionType type) {
        return removeBankBalance(p, amount, bankName, false, type);
    }

    private BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, boolean ignoreEvents, TransactionType type) {
        Economy economy = BankPlus.INSTANCE.getVaultEconomy();
        BigDecimal result = new BigDecimal(0);
        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(p, type, economy.getBalance(p), amount, bankName);
            if (event.isCancelled()) return result;

            amount = event.getTransactionAmount();
        }

        BigDecimal balance = getBankBalance(p, bankName);
        if (balance.subtract(amount).doubleValue() < 0D) {
            result = balance;
            set(p, new BigDecimal(0), bankName);
        } else {
            result = amount;
            set(p, balance.subtract(result), bankName);
        }

        if (!ignoreEvents) endEvent(p, type, economy.getBalance(p), amount, bankName);
        return result;
    }

    /**
     * Get the total offline interest earned from the selected player from all banks.
     * @param p The player.
     * @return Total offline interest.
     */
    public BigDecimal getOfflineInterest(OfflinePlayer p) {
        BigDecimal amount = new BigDecimal(0);
        for (String bankName : banksRegistry.getBanks().keySet()) amount = amount.add(getOfflineInterest(p, bankName));
        return amount;
    }

    /**
     * Get the offline interest earned from the selected player in the selected bank.
     * @param uuid The player UUID.
     * @param bankName The bank name.
     * @return Offline interest.
     */
    public BigDecimal getOfflineInterest(UUID uuid, String bankName) {
        return getOfflineInterest(Bukkit.getOfflinePlayer(uuid), bankName);
    }

    /**
     * Get the offline interest earned from the selected player in the selected bank.
     * @param p The player.
     * @param bankName The bank name.
     * @return Offline interest.
     */
    public BigDecimal getOfflineInterest(OfflinePlayer p, String bankName) {
        HashMap<String, BPPlayer.PlayerBank> bankInformation = new BPPlayerManager(p).getBankInformation();
        if (bankInformation != null) {
            BPPlayer.PlayerBank info = bankInformation.get(bankName);
            if (info != null) return info.getInterest();
        }

        if (Values.CONFIG.isSqlEnabled() && BankPlus.INSTANCE.getSql().isConnected())
            return new SQLPlayerManager(p).getOfflineInterest(bankName);

        String interest = new BPPlayerManager(p).getPlayerConfig().getString("banks." + bankName + ".interest");
        return new BigDecimal(interest == null ? "0" : interest);
    }

    /**
     * Set the offline interest to the selected amount in the selected bank.
     * @param p The player.
     * @param amount The new amount.
     * @param bankName The bank name.
     */
    public void setOfflineInterest(OfflinePlayer p, BigDecimal amount, String bankName) {
        amount = new BigDecimal(BPFormatter.formatBigDouble(amount)).max(BigDecimal.valueOf(0));
        HashMap<String, BPPlayer.PlayerBank> bankInformation = new BPPlayerManager(p).getBankInformation();
        if (bankInformation != null) {
            BPPlayer.PlayerBank info = bankInformation.get(bankName);
            if (info != null) {
                info.setInterest(amount);
                return;
            }
        }

        if (Values.CONFIG.isSqlEnabled() && BankPlus.INSTANCE.getSql().isConnected()) {
            new SQLPlayerManager(p).setOfflineInterest(amount, bankName);
            return;
        }

        BPPlayerManager files = new BPPlayerManager(p);
        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig(file);
        config.set("banks." + bankName + ".interest", BPFormatter.formatBigDouble(amount));
        files.savePlayerFile(config, file, true);
    }

    /**
     * Set the player bank debt to the selected amount.
     * @param p The player.
     * @param amount The new debt amount.
     * @param bankName The bank where to set the debt.
     */
    public void setDebt(OfflinePlayer p, BigDecimal amount, String bankName) {
        amount = new BigDecimal(BPFormatter.formatBigDouble(amount)).max(BigDecimal.valueOf(0));
        HashMap<String, BPPlayer.PlayerBank> bankInformation = new BPPlayerManager(p).getBankInformation();
        if (bankInformation != null) {
            BPPlayer.PlayerBank info = bankInformation.get(bankName);
            if (info != null) {
                info.setDebt(amount);
                return;
            }
        }

        if (Values.CONFIG.isSqlEnabled()) {
            BPSQL sql = BankPlus.INSTANCE.getSql();
            if (sql.isConnected()) {
                new SQLPlayerManager(p).setDebt(amount, bankName);
                return;
            }
        }

        BPPlayerManager files = new BPPlayerManager(p);
        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig(file);
        config.set("banks." + bankName + ".debt", amount);
        files.savePlayerFile(config, file, true);
    }

    /**
     * Get the player bank debt of the selected bank.
     * @param uuid The player UUID.
     * @param bankName The bank where to get the balance.
     */
    public BigDecimal getDebt(UUID uuid, String bankName) {
        return getDebt(Bukkit.getOfflinePlayer(uuid), bankName);
    }

    /**
     * Get the player bank debt of the selected bank.
     * @param p The player.
     * @param bankName The bank where to get the balance.
     */
    public BigDecimal getDebt(OfflinePlayer p, String bankName) {
        HashMap<String, BPPlayer.PlayerBank> bankInformation = new BPPlayerManager(p).getBankInformation();
        if (bankInformation != null) {
            BPPlayer.PlayerBank info = bankInformation.get(bankName);
            if (info != null) return info.getDebt();
        }

        if (Values.CONFIG.isSqlEnabled() && BankPlus.INSTANCE.getSql().isConnected())
            return new SQLPlayerManager(p).getDebt(bankName);

        String bal = new BPPlayerManager(p).getPlayerConfig().getString("banks." + bankName + ".debt");
        return new BigDecimal(bal == null ? "0" : bal);
    }

    /**
     * Get the sum of the player bank debt of all banks.
     * @param uuid The UUID of the player.
     */
    public BigDecimal getDebts(UUID uuid) {
        return getDebts(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Get the sum of the player bank debt of all banks.
     * @param p The player.
     */
    public BigDecimal getDebts(OfflinePlayer p) {
        BigDecimal amount = new BigDecimal(0);
        for (String bankName : banksRegistry.getBanks().keySet()) amount = amount.add(getDebt(p, bankName));
        return amount;
    }

    /**
     * Method internally used to simplify the transactions.
     */
    private void set(OfflinePlayer p, BigDecimal amount, String bankName) {
        set(p, amount, new BigDecimal(0), bankName);
    }

    /**
     * Method internally used to simplify the transactions.
     */
    private void set(OfflinePlayer p, BigDecimal amount, BigDecimal offlineInterest, String bankName) {
        HashMap<String, BPPlayer.PlayerBank> bankInformation = new BPPlayerManager(p).getBankInformation();
        amount = new BigDecimal(BPFormatter.formatBigDouble(amount)).max(BigDecimal.valueOf(0));
        offlineInterest = new BigDecimal(BPFormatter.formatBigDouble(offlineInterest)).max(BigDecimal.valueOf(0));

        boolean changeOfflineInterest = offlineInterest.doubleValue() != 0d;
        if (bankInformation != null) {
            BPPlayer.PlayerBank info = bankInformation.get(bankName);
            if (info != null) {
                info.setBalance(amount);
                if (changeOfflineInterest) info.setInterest(offlineInterest);
                return;
            }
        }

        if (Values.CONFIG.isSqlEnabled()) {
            BPSQL sql = BankPlus.INSTANCE.getSql();
            if (sql.isConnected()) {
                SQLPlayerManager pManager = new SQLPlayerManager(p);
                pManager.setMoney(amount, bankName);
                if (changeOfflineInterest) pManager.setOfflineInterest(offlineInterest, bankName);
                return;
            }
        }

        BPPlayerManager files = new BPPlayerManager(p);
        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig();
        config.set("banks." + bankName + ".money", amount);
        if (changeOfflineInterest) config.set("banks." + bankName + ".interest", offlineInterest);
        files.savePlayerFile(config, file, true);
    }

    public void deposit(Player p, BigDecimal amount, String bankName) {
        Economy economy = BankPlus.INSTANCE.getVaultEconomy();
        BPPreTransactionEvent event = startEvent(p, TransactionType.DEPOSIT, economy.getBalance(p), amount, bankName);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();

        if (amount.doubleValue() < Values.CONFIG.getDepositMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getDepositMinimumAmount());
            return;
        }

        BigDecimal money = BigDecimal.valueOf(BankPlus.INSTANCE.getVaultEconomy().getBalance(p));
        if (!BPUtils.checkPreRequisites(money, amount, p) || BPUtils.isBankFull(p, bankName)) return;

        if (money.doubleValue() < amount.doubleValue()) amount = money;

        BigDecimal maxDepositAmount = Values.CONFIG.getMaxDepositAmount();
        if (maxDepositAmount.doubleValue() != 0 && amount.doubleValue() >= maxDepositAmount.doubleValue())
            amount = maxDepositAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getDepositTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.deposit.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100)));

        BigDecimal capacity = new BankManager(bankName).getCapacity(p);
        BigDecimal newBankBalance = getBankBalance(p, bankName).add(amount);

        /*
        Make it possible so when depositing all your money with taxes, the money will have the ability
        to FILL the bank instead of always depositing a bit less and never filling up the bank.
        */
        if (capacity.doubleValue() > 0d && newBankBalance.doubleValue() >= capacity.doubleValue()) {
            BigDecimal moneyToFull = capacity.subtract(getBankBalance(p, bankName));
            amount = moneyToFull.add(taxes);
        }

        EconomyResponse depositResponse = BankPlus.INSTANCE.getVaultEconomy().withdrawPlayer(p, amount.doubleValue());
        if (BPUtils.hasFailed(p, depositResponse)) return;

        addBankBalance(p, amount.subtract(taxes), bankName, true);
        BPMessages.send(p, "Success-Deposit", BPUtils.placeValues(p, amount.subtract(taxes)), BPUtils.placeValues(taxes, "taxes"));
        BPUtils.playSound("DEPOSIT", p);

        endEvent(p, TransactionType.DEPOSIT, economy.getBalance(p), amount, bankName);
    }

    public void withdraw(Player p, BigDecimal amount, String bankName) {
        Economy economy = BankPlus.INSTANCE.getVaultEconomy();
        BPPreTransactionEvent event = startEvent(p, TransactionType.WITHDRAW, economy.getBalance(p), amount, bankName);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();

        if (amount.doubleValue() < Values.CONFIG.getWithdrawMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getWithdrawMinimumAmount());
            return;
        }

        BigDecimal bankBal = getBankBalance(p, bankName);
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

        removeBankBalance(p, amount, bankName, true);
        BPMessages.send(p, "Success-Withdraw", BPUtils.placeValues(p, amount.subtract(taxes)), BPUtils.placeValues(taxes, "taxes"));
        BPUtils.playSound("WITHDRAW", p);

        endEvent(p, TransactionType.WITHDRAW, economy.getBalance(p), amount, bankName);
    }

    /**
     * Method used to execute the pay transaction.
     *
     * @param from     The player that will give the money.
     * @param to       The player that will receive your money.
     * @param amount   How much money you want to pay.
     * @param fromBank The bank where the money will be taken.
     * @param toBank   The bank where the money will be added.
     */
    public void pay(Player from, Player to, BigDecimal amount, String fromBank, String toBank) {
        BigDecimal senderBalance = getBankBalance(from, fromBank);

        // Check if the sender has at least more than 0 money
        if (senderBalance.doubleValue() < amount.doubleValue()) {
            BPMessages.send(from, "Insufficient-Money");
            return;
        }

        // Check if the receiver of the payment has the bank full
        if (getBankBalance(to, toBank).doubleValue() >= new BankManager(toBank).getCapacity(to).doubleValue()) {
            BPMessages.send(from, "Bank-Full", "%player%$" + to.getName());
            return;
        }

        BigDecimal added = addBankBalance(to, amount, toBank, TransactionType.PAY), extra = amount.subtract(added);
        BPMessages.send(to, "Payment-Received", BPUtils.placeValues(from, added));

        BigDecimal removed = removeBankBalance(from, amount.subtract(extra), fromBank, TransactionType.PAY);
        BPMessages.send(from, "Payment-Sent", BPUtils.placeValues(to, removed));
    }

    private BPPreTransactionEvent startEvent(OfflinePlayer p, TransactionType type, double vaultBalance, BigDecimal amount, String bankName) {
        BPPreTransactionEvent event = new BPPreTransactionEvent(
                p, type, getBankBalance(p, bankName), vaultBalance, amount, bankName
        );
        BPUtils.callEvent(event);
        return event;
    }

    private void endEvent(OfflinePlayer p, TransactionType type, double vaultBalance, BigDecimal amount, String bankName) {
        BPAfterTransactionEvent event = new BPAfterTransactionEvent(
                p, type, getBankBalance(p, bankName), vaultBalance, amount, bankName
        );
        BPUtils.callEvent(event);
    }
}