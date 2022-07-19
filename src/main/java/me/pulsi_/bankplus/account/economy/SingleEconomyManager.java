package me.pulsi_.bankplus.account.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.AccountManager;
import me.pulsi_.bankplus.managers.BankTopManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMethods;
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

            String sBalance = config.getString("Money");
            if (sBalance == null) continue;

            String sName = config.getString("Account-Name");
            if (sName == null) sName = "null";

            BigDecimal balance;
            try {
                balance = new BigDecimal(sBalance);
            } catch (NumberFormatException e) {
                balance = new BigDecimal(0);
            }
            balances.add(balance);
            BankTopManager.nameGetter.put(balance, sName);
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
}