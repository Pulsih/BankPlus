package me.pulsi_.bankplus.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerFiles;
import me.pulsi_.bankplus.bankSystem.BankGuiRegistry;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.events.BPAfterTransactionEvent;
import me.pulsi_.bankplus.events.BPPreTransactionEvent;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class BPEconomy {

    private final Map<UUID, HashMap<String, BigDecimal>> playerBalances = new HashMap<>();

    private final BankGuiRegistry registry;

    public BPEconomy() {
        registry = BankPlus.INSTANCE.getBankGuiRegistry();
    }

    /**
     * Return a list with all player balances.
     *
     * @return A hashmap with the player name as key and the sum of all the player bank balances as value.
     */
    public LinkedHashMap<String, BigDecimal> getAllBankBalances() {
        LinkedHashMap<String, BigDecimal> balances = new LinkedHashMap<>();

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

            String name = config.getString("name");
            if (name == null) continue;

            ConfigurationSection section = config.getConfigurationSection("banks");
            BigDecimal balance = new BigDecimal(0);

            if (section != null) {
                for (String bankName : section.getKeys(false)) {
                    String bal = config.getString("banks." + bankName + ".money");
                    if (bal != null && !BPUtils.isInvalidNumber(bal))
                        balance = balance.add(new BigDecimal(bal));
                }
            }
            balances.put(name, balance);
        }
        return balances;
    }

    /**
     * Load the player bank balances. This cannot be an offline player!
     */
    public void loadBankBalance(Player p) {
        loadBankBalance(p, new BPPlayerFiles(p).getPlayerConfig());
    }

    /**
     * Load the player bank balances. This cannot be an offline player!
     */
    public void loadBankBalance(Player p, FileConfiguration config) {
        if (p == null) return;

        HashMap<String, BigDecimal> balances = new HashMap<>();
        if (playerBalances.containsKey(p.getUniqueId())) balances = playerBalances.get(p.getUniqueId());

        boolean changes = false;
        for (String bankName : registry.getBanks().keySet()) {
            if (balances.containsKey(bankName)) continue;

            String bal = config.getString("banks." + bankName + ".money");
            BigDecimal amount = new BigDecimal(0);

            if (bal != null) {
                try {
                    amount = new BigDecimal(bal);
                } catch (NumberFormatException e) {
                    BPLogger.warn("Could not get \"" + bankName + "\" bank balance for " + p.getName() + " because it contains an invalid number! (Using 0 as default)");
                }
            }
            balances.put(bankName, amount);
            changes = true;
        }
        if (changes) playerBalances.put(p.getUniqueId(), balances);
    }

    /**
     * Unload the player bank balances.
     */
    public void unloadBankBalance(Player p) {
        playerBalances.remove(p.getUniqueId());
    }

    /**
     * Save all bank balances to the player file.
     */
    public void saveBankBalances(Player p) {
        saveBankBalances(p, true);
    }

    /**
     * Save all bank balances to the player file.
     */
    public void saveBankBalances(Player p, boolean async) {
        BPPlayerFiles files = new BPPlayerFiles(p);
        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig(file);
        for (String bankName : registry.getBanks().keySet())
            config.set("banks." + bankName + ".money", BPFormatter.formatBigDouble(getBankBalance(p, bankName)));
        files.savePlayerFile(config, file, async);
    }

    /**
     * Get the player bank balance of the selected bank.
     */
    public BigDecimal getBankBalance(OfflinePlayer p, String bankName) {
        if (!p.isOnline()) {
            String bal = new BPPlayerFiles(p).getPlayerConfig().getString("banks." + bankName + ".money");
            return new BigDecimal(bal == null ? "0" : bal);
        }
        loadBankBalance(p.getPlayer());
        return playerBalances.get(p.getUniqueId()).get(bankName);
    }

    /**
     * Get the player bank balance of all the existing bank.
     */
    public BigDecimal getBankBalance(OfflinePlayer p) {
        if (!p.isOnline()) {
            BigDecimal amount = new BigDecimal(0);
            FileConfiguration config = new BPPlayerFiles(p).getPlayerConfig();
            for (String bankName : registry.getBanks().keySet()) {
                String num = config.getString("banks." + bankName + ".money");
                amount = amount.add(new BigDecimal(num == null ? "0" : num));
            }
            return amount;
        }

        loadBankBalance(p.getPlayer());
        BigDecimal amount = new BigDecimal(0);
        for (String bankName : registry.getBanks().keySet())
            amount = amount.add(playerBalances.get(p.getUniqueId()).get(bankName));
        return amount;
    }

    /**
     * Set the selected amount in the selected bank.
     */
    public void setBankBalance(OfflinePlayer p, BigDecimal amount, String bankName) {
        setBankBalance(p, amount, bankName, TransactionType.SET);
    }

    /**
     * Set the selected amount in the selected bank.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     */
    public void setBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, boolean ignoreEvents) {
        Economy economy = BankPlus.INSTANCE.getVaultEconomy();
        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(p, TransactionType.SET, economy.getBalance(p), amount, bankName);
            if (event.isCancelled()) return;

            amount = event.getTransactionAmount();
        }
        set(p, amount, bankName);

        if (!ignoreEvents) endEvent(p, TransactionType.SET, economy.getBalance(p), amount, bankName);
    }

    /**
     * Set the selected amount in the selected bank.
     *
     * @param type Override the transaction type with the one you choose.
     */
    public void setBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, TransactionType type) {
        Economy economy = BankPlus.INSTANCE.getVaultEconomy();
        BPPreTransactionEvent event = startEvent(p, type, economy.getBalance(p), amount, bankName);
        if (event.isCancelled()) return;
        amount = event.getTransactionAmount();

        set(p, amount, bankName);

        endEvent(p, type, economy.getBalance(p), amount, bankName);
    }

    /**
     * Add the selected amount to the selected bank.
     */
    public void addBankBalance(OfflinePlayer p, BigDecimal amount, String bankName) {
        addBankBalance(p, amount, bankName, TransactionType.ADD);
    }

    public void addBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, boolean ignoreEvents) {
        Economy economy = BankPlus.INSTANCE.getVaultEconomy();
        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(p, TransactionType.ADD, economy.getBalance(p), amount, bankName);
            if (event.isCancelled()) return;

            amount = event.getTransactionAmount();
        }
        set(p, getBankBalance(p, bankName).add(amount), bankName);

        if (!ignoreEvents) endEvent(p, TransactionType.ADD, economy.getBalance(p), amount, bankName);
    }

    /**
     * Add the selected amount to the selected bank.
     *
     * @param type Override the transaction type with the one you choose.
     */
    public void addBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, TransactionType type) {
        Economy economy = BankPlus.INSTANCE.getVaultEconomy();

        BPPreTransactionEvent event = startEvent(p, type, economy.getBalance(p), amount, bankName);
        if (event.isCancelled()) return;
        amount = event.getTransactionAmount();

        set(p, getBankBalance(p, bankName).add(amount), bankName);

        endEvent(p, type, economy.getBalance(p), amount, bankName);
    }

    /**
     * Remove the selected amount.
     */
    public void removeBankBalance(OfflinePlayer p, BigDecimal amount, String bankName) {
        removeBankBalance(p, amount, bankName, TransactionType.REMOVE);
    }

    /**
     * Remove the selected amount.
     *
     * @param ignoreEvents Choose if ignoring or not the bankplus transaction event.
     */
    public void removeBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, boolean ignoreEvents) {
        Economy economy = BankPlus.INSTANCE.getVaultEconomy();
        if (!ignoreEvents) {
            BPPreTransactionEvent event = startEvent(p, TransactionType.REMOVE, economy.getBalance(p), amount, bankName);
            if (event.isCancelled()) return;

            amount = event.getTransactionAmount();
        }
        set(p, getBankBalance(p, bankName).subtract(amount), bankName);

        if (!ignoreEvents) endEvent(p, TransactionType.REMOVE, economy.getBalance(p), amount, bankName);
    }

    /**
     * Remove the selected amount.
     *
     * @param type Override the transaction type with the one you choose.
     */
    public void removeBankBalance(OfflinePlayer p, BigDecimal amount, String bankName, TransactionType type) {
        Economy economy = BankPlus.INSTANCE.getVaultEconomy();

        BPPreTransactionEvent event = startEvent(p, type, economy.getBalance(p), amount, bankName);
        if (event.isCancelled()) return;
        amount = event.getTransactionAmount();

        set(p, getBankBalance(p, bankName).subtract(amount), bankName);

        endEvent(p, type, economy.getBalance(p), amount, bankName);
    }

    /**
     * Get the received offline interest of a player while being offline.
     */
    public BigDecimal getOfflineInterest(OfflinePlayer p) {
        String interest = new BPPlayerFiles(p).getPlayerConfig().getString("interest");
        return new BigDecimal(interest == null ? "0" : interest);
    }

    /**
     * Set the player's offline interest to the selected amount.
     */
    public void setOfflineInterest(OfflinePlayer p, BigDecimal amount, boolean async) {
        BPPlayerFiles files = new BPPlayerFiles(p);
        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig();
        config.set("interest", BPFormatter.formatBigDouble(amount));
        files.savePlayerFile(config, file, async);
    }

    /**
     * Method internally used to simplify the transactions.
     */
    private void set(OfflinePlayer p, BigDecimal amount, String bankName) {
        if (p.isOnline()) {
            BigDecimal amountFormatted = new BigDecimal(BPFormatter.formatBigDouble(amount));

            HashMap<String, BigDecimal> balances = playerBalances.get(p.getUniqueId());
            balances.put(bankName, amountFormatted.max(BigDecimal.valueOf(0)));

            playerBalances.put(p.getUniqueId(), balances);
            return;
        }

        BPPlayerFiles files = new BPPlayerFiles(p);
        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig();
        config.set("banks." + bankName + ".money", BPFormatter.formatBigDouble(amount));
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

        BigDecimal capacity = new BankReader(bankName).getCapacity(p);
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
     * @param from The player that will give the money.
     * @param to   The player that will receive your money.
     * @param amount   How much money you want to pay.
     * @param fromBank The bank where the money will be taken.
     * @param toBank   The bank where the money will be added.
     */
    public void pay(Player from, Player to, BigDecimal amount, String fromBank, String toBank) {
        BigDecimal senderBalance = getBankBalance(from, fromBank);

        if (senderBalance.doubleValue() < amount.doubleValue()) {
            BPMessages.send(from, "Insufficient-Money");
            return;
        }

        BigDecimal targetCapacity = new BankReader(toBank).getCapacity(to), targetBalance = getBankBalance(to, toBank), newBalance = amount.add(targetBalance);

        if (targetBalance.doubleValue() >= targetCapacity.doubleValue()) {
            BPMessages.send(from, "Bank-Full", "%player%$" + to.getName());
            return;
        }

        if (newBalance.doubleValue() >= targetCapacity.doubleValue() && targetCapacity.doubleValue() > 0d) {
            removeBankBalance(from, targetCapacity.subtract(targetBalance), fromBank, TransactionType.PAY);
            setBankBalance(to, targetCapacity, toBank, TransactionType.PAY);

            BPMessages.send(from, "Payment-Sent", BPUtils.placeValues(to, amount));
            BPMessages.send(to, "Payment-Received", BPUtils.placeValues(from, amount));
            return;
        }

        removeBankBalance(from, amount, fromBank, TransactionType.PAY);
        addBankBalance(to, amount, toBank, TransactionType.PAY);
        BPMessages.send(from, "Payment-Sent", BPUtils.placeValues(to, amount));
        BPMessages.send(to, "Payment-Received", BPUtils.placeValues(from, amount));
    }

    private BPPreTransactionEvent startEvent(OfflinePlayer p, TransactionType type, double vaultBalance, BigDecimal amount, String bankName) {
        BPPreTransactionEvent event = new BPPreTransactionEvent(
                p, type, getBankBalance(p, bankName), vaultBalance, amount, false, bankName
        );
        BPUtils.callEvent(event);
        return event;
    }

    private void endEvent(OfflinePlayer p, TransactionType type, double vaultBalance, BigDecimal amount, String bankName) {
        BPAfterTransactionEvent event = new BPAfterTransactionEvent(
                p, type, getBankBalance(p, bankName), vaultBalance, amount, false, bankName
        );
        BPUtils.callEvent(event);
    }
}