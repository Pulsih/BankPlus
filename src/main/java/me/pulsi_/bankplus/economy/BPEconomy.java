package me.pulsi_.bankplus.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.bankSystem.Bank;
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
import java.util.*;

public class BPEconomy {

    private final String bankName;
    private final HashMap<UUID, MoneyHolder> balances = new HashMap<>();

    public BPEconomy(String bankName) {
        this.bankName = bankName;
    }

    public static BPEconomy get(String bankName) {
        BankPlus pl = BankPlus.INSTANCE();
        if (pl == null) return null;

        return pl.getBankGuiRegistry().getBanks().get(bankName).getBankEconomy();
    }

    public static List<BPEconomy> list() {
        List<BPEconomy> economies = new ArrayList<>();
        BankPlus pl = BankPlus.INSTANCE();
        if (pl == null) return economies;

        for (Bank bank : pl.getBankGuiRegistry().getBanks().values()) economies.add(bank.getBankEconomy());
        return economies;
    }

    public static List<String> nameList() {
        List<String> economies = new ArrayList<>();
        BankPlus pl = BankPlus.INSTANCE();
        if (pl == null) return economies;

        economies.addAll(pl.getBankGuiRegistry().getBanks().keySet());
        return economies;
    }

    /**
     * Return a list with all player balances of all banks.
     *
     * @return A hashmap with the player name as KEY and the sum of all the player bank balances as VALUE.
     */
    public static LinkedHashMap<String, BigDecimal> getAllEconomiesBankBalances() {
        LinkedHashMap<String, BigDecimal> balances = new LinkedHashMap<>();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            BigDecimal balance = new BigDecimal(0);
            for (String bankName : BankManager.getAvailableBanks(p)) balance = balance.add(get(bankName).getBankBalance(p));
            balances.put(p.getName(), balance);
        }
        return balances;
    }

    public boolean isPlayerLoaded(OfflinePlayer p) {
        return balances.containsKey(p.getUniqueId());
    }

    public Set<UUID> getLoadedPlayers() {
        return balances.keySet();
    }

    public void loadPlayer(OfflinePlayer p) {
        if (isPlayerLoaded(p)) return;

        MoneyHolder holder = new MoneyHolder();
        holder.debt = getDebt(p);
        holder.money = getBankBalance(p);
        holder.offlineInterest = getOfflineInterest(p);

        balances.put(p.getUniqueId(), holder);
    }

    public void unloadPlayer(UUID uuid) {
        balances.remove(uuid);
    }

    /**
     * Return a list with all player balances of that bank.
     *
     * @return A hashmap with the player name as KEY and the sum of all the player bank balances as VALUE.
     */
    public LinkedHashMap<String, BigDecimal> getAllBankBalances() {
        LinkedHashMap<String, BigDecimal> balances = new LinkedHashMap<>();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) balances.put(p.getName(), getBankBalance(p));
        return balances;
    }

    /**
     * Get the sum of player bank balances of all banks.
     *
     * @param p The player.
     */
    public static BigDecimal getBankBalancesSum(OfflinePlayer p) {
        BigDecimal amount = BigDecimal.valueOf(0);
        for (BPEconomy economy : list())
            amount = amount.add(economy.getBankBalance(p));
        return amount;
    }

    /**
     * Get the player bank balance of the selected bank.
     *
     * @param uuid The UUID of the player.
     */
    public BigDecimal getBankBalance(UUID uuid) {
        return getBankBalance(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Get the player bank balance of the selected bank.
     *
     * @param p The player.
     */
    public BigDecimal getBankBalance(OfflinePlayer p) {
        if (balances.containsKey(p.getUniqueId())) return balances.get(p.getUniqueId()).money;
        if (BankPlus.INSTANCE().getMySql().isConnected()) return new SQLPlayerManager(p).getMoney(bankName);
        String bal = new BPPlayerManager(p).getPlayerConfig().getString("banks." + bankName + ".money");
        return new BigDecimal(bal == null ? "0" : bal);
    }

    /**
     * Set the selected amount in the selected bank.
     *
     * @return Number representing the actual amount set.
     */
    public BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount) {
        return setBankBalance(p, amount, TransactionType.SET);
    }

    /**
     * Set the selected amount in the selected bank.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     * @return Number representing the actual amount set.
     */
    public BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents) {
        return setBankBalance(p, amount, ignoreEvents, TransactionType.SET);
    }

    /**
     * Set the selected amount in the selected bank.
     *
     * @param type Override the transaction type with the one you choose.
     * @return Number representing the actual amount set.
     */
    public BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount, TransactionType type) {
        return setBankBalance(p, amount, false, type);
    }

    private BigDecimal setBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents, TransactionType type) {
        BigDecimal result = new BigDecimal(0);

        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(p, type, amount, bankName);
            if (event.isCancelled()) return result;

            amount = event.getTransactionAmount();
        }

        result = result.max(amount.min(BankManager.getCapacity(bankName, p)));
        set(p, result);

        if (!ignoreEvents) endEvent(p, type, amount, bankName);
        return result;
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount) {
        return addBankBalance(p, amount, false, TransactionType.ADD, false);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents) {
        return addBankBalance(p, amount, ignoreEvents, TransactionType.ADD, false);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param type Override the transaction type with the one you choose.
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, TransactionType type) {
        return addBankBalance(p, amount, false, type, false);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param type               Override the transaction type with the one you choose.
     * @param addOfflineInterest Choose if updating the offline interest with this transaction.
     * @return Number representing the actual amount added.
     */
    public BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, TransactionType type, boolean addOfflineInterest) {
        return addBankBalance(p, amount, false, type, addOfflineInterest);
    }

    private BigDecimal addBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents, TransactionType type, boolean addOfflineInterest) {
        BigDecimal result = new BigDecimal(0);

        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(p, type, amount, bankName);
            if (event.isCancelled()) return result;

            amount = event.getTransactionAmount();
        }

        BigDecimal capacity = BankManager.getCapacity(bankName, p), balance = getBankBalance(p);
        if (capacity.doubleValue() <= 0D || balance.add(amount).doubleValue() < capacity.doubleValue()) {
            result = amount;
            BigDecimal moneyToAdd = balance.add(result);
            if (addOfflineInterest) set(p, moneyToAdd, result);
            else set(p, moneyToAdd);
        } else {
            result = capacity.subtract(balance);

            if (addOfflineInterest) set(p, capacity, result);
            else set(p, capacity);
        }

        if (!ignoreEvents) endEvent(p, type, amount, bankName);
        return result;
    }

    /**
     * Remove the selected amount.
     *
     * @return Number representing the actual amount removed.
     */
    public BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount) {
        return removeBankBalance(p, amount, false, TransactionType.REMOVE);
    }

    /**
     * Remove the selected amount.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     * @return Number representing the actual amount removed.
     */
    public BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents) {
        return removeBankBalance(p, amount, ignoreEvents, TransactionType.REMOVE);
    }

    /**
     * Remove the selected amount.
     *
     * @param type Override the transaction type with the one you choose.
     * @return Number representing the actual amount removed.
     */
    public BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount, TransactionType type) {
        return removeBankBalance(p, amount, false, type);
    }

    private BigDecimal removeBankBalance(OfflinePlayer p, BigDecimal amount, boolean ignoreEvents, TransactionType type) {
        BigDecimal result = new BigDecimal(0);
        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(p, type, amount, bankName);
            if (event.isCancelled()) return result;

            amount = event.getTransactionAmount();
        }

        BigDecimal balance = getBankBalance(p);
        if (balance.subtract(amount).doubleValue() < 0D) {
            result = balance;
            set(p, new BigDecimal(0));
        } else {
            result = amount;
            set(p, balance.subtract(result));
        }

        if (!ignoreEvents) endEvent(p, type, result, bankName);
        return result;
    }

    /**
     * Get the offline interest earned from the selected player in the selected bank.
     *
     * @param uuid The player UUID.
     * @return Offline interest.
     */
    public BigDecimal getOfflineInterest(UUID uuid) {
        return getOfflineInterest(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Get the offline interest earned from the selected player in the selected bank.
     *
     * @param p The player.
     * @return Offline interest.
     */
    public BigDecimal getOfflineInterest(OfflinePlayer p) {
        if (balances.containsKey(p.getUniqueId())) return balances.get(p.getUniqueId()).offlineInterest;

        if (BankPlus.INSTANCE().getMySql().isConnected())
            return new SQLPlayerManager(p).getOfflineInterest(bankName);

        String interest = new BPPlayerManager(p).getPlayerConfig().getString("banks." + bankName + ".interest");
        return new BigDecimal(interest == null ? "0" : interest);
    }

    /**
     * Set the offline interest to the selected amount in the selected bank.
     *
     * @param p      The player.
     * @param amount The new amount.
     */
    public void setOfflineInterest(OfflinePlayer p, BigDecimal amount) {
        amount = new BigDecimal(BPFormatter.formatBigDecimal(amount)).max(BigDecimal.valueOf(0));

        if (balances.containsKey(p.getUniqueId())) {
            balances.get(p.getUniqueId()).offlineInterest = amount;
            return;
        }

        if (BankPlus.INSTANCE().getMySql().isConnected()) {
            new SQLPlayerManager(p).setOfflineInterest(amount, bankName);
            return;
        }

        BPPlayerManager files = new BPPlayerManager(p);
        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig(file);
        config.set("banks." + bankName + ".interest", BPFormatter.formatBigDecimal(amount));
        files.savePlayerFile(config, file, true);
    }

    /**
     * Set the player bank debt to the selected amount.
     *
     * @param p      The player.
     * @param amount The new debt amount.
     */
    public void setDebt(OfflinePlayer p, BigDecimal amount) {
        amount = BPFormatter.getBigDoubleFormatted(amount).max(BigDecimal.valueOf(0));

        if (balances.containsKey(p.getUniqueId())) {
            balances.get(p.getUniqueId()).debt = amount;
            return;
        }

        if (Values.CONFIG.isSqlEnabled()) {
            BPSQL sql = BankPlus.INSTANCE().getMySql();
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
     *
     * @param uuid The player UUID.
     */
    public BigDecimal getDebt(UUID uuid) {
        return getDebt(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Get the player bank debt of the selected bank.
     *
     * @param p The player.
     */
    public BigDecimal getDebt(OfflinePlayer p) {
        if (balances.containsKey(p.getUniqueId())) return balances.get(p.getUniqueId()).debt;

        if (BankPlus.INSTANCE().getMySql().isConnected())
            return new SQLPlayerManager(p).getDebt(bankName);

        String bal = new BPPlayerManager(p).getPlayerConfig().getString("banks." + bankName + ".debt");
        return new BigDecimal(bal == null ? "0" : bal);
    }

    /**
     * Method internally used to simplify the transactions.
     */
    private void set(OfflinePlayer p, BigDecimal amount) {
        set(p, amount, new BigDecimal(0));
    }

    /**
     * Method internally used to simplify the transactions.
     */
    private void set(OfflinePlayer p, BigDecimal amount, BigDecimal offlineInterest) {
        amount = BPFormatter.getBigDoubleFormatted(amount).max(BigDecimal.valueOf(0));
        offlineInterest = BPFormatter.getBigDoubleFormatted(offlineInterest).max(BigDecimal.valueOf(0));
        boolean changeOfflineInterest = offlineInterest.doubleValue() > 0d;

        if (balances.containsKey(p.getUniqueId())) {
            balances.get(p.getUniqueId()).money = amount;
            if (changeOfflineInterest) balances.get(p.getUniqueId()).offlineInterest = offlineInterest;
            return;
        }

        if (Values.CONFIG.isSqlEnabled()) {
            BPSQL sql = BankPlus.INSTANCE().getMySql();
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
        config.set("banks." + bankName + ".money", BPFormatter.formatBigDecimal(amount));
        if (changeOfflineInterest) config.set("banks." + bankName + ".interest", offlineInterest);
        files.savePlayerFile(config, file, true);
    }

    public void deposit(Player p, BigDecimal amount) {
        Economy economy = BankPlus.INSTANCE().getVaultEconomy();
        BPPreTransactionEvent event = startEvent(p, TransactionType.DEPOSIT, amount, bankName);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();

        if (amount.doubleValue() < Values.CONFIG.getDepositMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getDepositMinimumAmount());
            return;
        }

        BigDecimal money = BigDecimal.valueOf(economy.getBalance(p));
        if (!BPUtils.checkPreRequisites(money, amount, p) || BPUtils.isBankFull(p, bankName)) return;

        if (money.doubleValue() < amount.doubleValue()) amount = money;

        BigDecimal maxDepositAmount = Values.CONFIG.getMaxDepositAmount();
        if (maxDepositAmount.doubleValue() != 0 && amount.doubleValue() >= maxDepositAmount.doubleValue())
            amount = maxDepositAmount;

        BigDecimal taxes = new BigDecimal(0);
        if (Values.CONFIG.getDepositTaxes().doubleValue() > 0 && !p.hasPermission("bankplus.deposit.bypass-taxes"))
            taxes = amount.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100)));

        BigDecimal capacity = BankManager.getCapacity(bankName, p);
        BigDecimal newBankBalance = getBankBalance(p).add(amount);

        /*
        Make it possible so when depositing all your money with taxes, the money will have the ability
        to FILL the bank instead of always depositing a bit less and never filling up the bank.
        */
        if (capacity.doubleValue() > 0d && newBankBalance.doubleValue() >= capacity.doubleValue()) {
            BigDecimal moneyToFull = capacity.subtract(getBankBalance(p));
            amount = moneyToFull.add(taxes);
        }

        EconomyResponse depositResponse = economy.withdrawPlayer(p, amount.doubleValue());
        if (BPUtils.hasFailed(p, depositResponse)) return;

        addBankBalance(p, amount.subtract(taxes), true);
        BPMessages.send(p, "Success-Deposit", BPUtils.placeValues(p, amount.subtract(taxes)), BPUtils.placeValues(taxes, "taxes"));
        BPUtils.playSound("DEPOSIT", p);

        endEvent(p, TransactionType.DEPOSIT, amount, bankName);
    }

    public void withdraw(Player p, BigDecimal amount) {
        Economy economy = BankPlus.INSTANCE().getVaultEconomy();
        BPPreTransactionEvent event = startEvent(p, TransactionType.WITHDRAW, amount, bankName);
        if (event.isCancelled()) return;

        amount = event.getTransactionAmount();

        if (amount.doubleValue() < Values.CONFIG.getWithdrawMinimumAmount().doubleValue()) {
            BPMessages.send(p, "Minimum-Number", "%min%$" + Values.CONFIG.getWithdrawMinimumAmount());
            return;
        }

        BigDecimal bankBal = getBankBalance(p);
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

        removeBankBalance(p, amount, true);
        BPMessages.send(p, "Success-Withdraw", BPUtils.placeValues(p, amount.subtract(taxes)), BPUtils.placeValues(taxes, "taxes"));
        BPUtils.playSound("WITHDRAW", p);

        endEvent(p, TransactionType.WITHDRAW, amount, bankName);
    }

    /**
     * Method used to execute the pay transaction.
     *
     * @param from     The player that will give the money.
     * @param to       The player that will receive your money.
     * @param amount   How much money you want to pay.
     * @param toBank   The bank where the money will be added.
     */
    public void pay(Player from, Player to, BigDecimal amount, String toBank) {
        BigDecimal senderBalance = getBankBalance(from);
        BPEconomy toEconomy = get(toBank);

        // Check if the sender has at least more than 0 money
        if (senderBalance.compareTo(amount) < 0) {
            BPMessages.send(from, "Insufficient-Money");
            return;
        }

        // Check if the receiver of the payment has the bank full
        if (toEconomy.getBankBalance(to).compareTo(BankManager.getCapacity(toBank, to)) >= 0) {
            BPMessages.send(from, "Bank-Full", "%player%$" + to.getName());
            return;
        }

        BigDecimal added = toEconomy.addBankBalance(to, amount, TransactionType.PAY), extra = amount.subtract(added);
        BPMessages.send(to, "Payment-Received", BPUtils.placeValues(from, added));

        BigDecimal removed = removeBankBalance(from, amount.subtract(extra), TransactionType.PAY);
        BPMessages.send(from, "Payment-Sent", BPUtils.placeValues(to, removed));
    }

    /**
     * Returns the name of the bank that own this economy.
     * @return A string representing the bank name.
     */
    public String getBankName() {
        return bankName;
    }

    public BPPreTransactionEvent startEvent(OfflinePlayer p, TransactionType type, BigDecimal amount, String bankName) {
        BPPreTransactionEvent event = new BPPreTransactionEvent(
                p, type, getBankBalance(p), BankPlus.INSTANCE().getVaultEconomy().getBalance(p), amount, bankName
        );
        BPUtils.callEvent(event);
        return event;
    }

    public void endEvent(OfflinePlayer p, TransactionType type, BigDecimal amount, String bankName) {
        BPAfterTransactionEvent event = new BPAfterTransactionEvent(
                p, type, getBankBalance(p), BankPlus.INSTANCE().getVaultEconomy().getBalance(p), amount, bankName
        );
        BPUtils.callEvent(event);
    }

    private static class MoneyHolder {
        private BigDecimal money = new BigDecimal(0), offlineInterest = new BigDecimal(0), debt = new BigDecimal(0);
    }
}