package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * BankPlus main core class to manage the entire server economy.
 * In this class, you'll find many useful methods that will help you get, modify and set player's balances.
 */
public class BankManager {

    public BankManager() throws Exception {
        throw new Exception("This class may not be initialized.");
    }

    public static Bank getBank(String bankName) {
        return BankPlus.INSTANCE().getBankGuiRegistry().getBanks().get(bankName);
    }

    public static Collection<Bank> getBanks() {
        return BankPlus.INSTANCE().getBankGuiRegistry().getBanks().values();
    }

    public static boolean exist(String bankName) {
        return getBank(bankName) != null;
    }

    /**
     * Get the bank capacity based on the bank level of the selected player.
     *
     * @param p The player.
     * @return The capacity amount.
     */
    public static BigDecimal getCapacity(String bankName, OfflinePlayer p) {
        return getCapacity(bankName, getCurrentLevel(bankName, p));
    }

    /**
     * Get the bank capacity of that specified level.
     *
     * @param bankName The bank name.
     * @param level    The level to check.
     * @return The capacity amount.
     */
    public static BigDecimal getCapacity(String bankName, int level) {
        ConfigurationSection upgrades = getBank(bankName).getUpgrades();
        if (upgrades == null) return Values.CONFIG.getMaxBankCapacity();

        String capacity = upgrades.getString((Math.max(level, 1)) + ".Capacity");
        return (capacity == null ? Values.CONFIG.getMaxBankCapacity() : new BigDecimal(capacity));
    }

    /**
     * Get the bank interest rate based on the bank level of the selected player.
     * This method requires specifying a player because it needs his money to make calculations.
     * It also already checks if the interest limiter is enabled and returns an amount based on that.
     *
     * @param bankName The bank name.
     * @param p        The player.
     * @return The interest amount.
     */
    public static BigDecimal getInterestRate(String bankName, OfflinePlayer p) {
        return getInterestRate(bankName, p, getCurrentLevel(bankName, p));
    }

    /**
     * Get the bank interest rate at the selected level.
     * This method requires specifying a player because it needs his money to make calculations.
     * It also already checks if the interest limiter is enabled and returns an amount based on that.
     *
     * @param bankName The bank name.
     * @param p        The player.
     * @param level    The level to check.
     * @return The interest amount.
     */
    public static BigDecimal getInterestRate(String bankName, OfflinePlayer p, int level) {
        if (Values.CONFIG.enableInterestLimiter())
            return getLimitedInterest(bankName, p, level, Values.CONFIG.getInterestMoneyGiven());

        Bank bank = getBank(bankName);
        ConfigurationSection upgrades = bank.getUpgrades();
        if (upgrades == null) return Values.CONFIG.getInterestMoneyGiven();

        level = Math.max(level, 1);
        String interest = upgrades.getString(level + ".Interest");
        if (BPUtils.isInvalidNumber(interest)) {
            // Check this before to avoid spamming warns if the user does not specify an interest in an upgrade.
            if (interest != null)
                BPLogger.warn("Invalid interest amount in the " + level + "* upgrades section, file: " + bankName + ".yml");
            return Values.CONFIG.getInterestMoneyGiven();
        }

        return new BigDecimal(interest.replace("%", ""));
    }

    /**
     * Get the bank offline interest rate based on the bank level of the selected player.
     * This method requires specifying a player because it needs his money to make calculations.
     * It also already checks if the interest limiter is enabled and returns an amount based on that.
     *
     * @param bankName The bank name.
     * @param p        The player.
     * @return The interest amount.
     */
    public static BigDecimal getOfflineInterestRate(String bankName, OfflinePlayer p) {
        return getOfflineInterestRate(bankName, p, getCurrentLevel(bankName, p));
    }

    /**
     * Get the bank offline interest rate at the selected level.
     * This method requires specifying a player because it needs his money to make calculations.
     * It also already checks if the interest limiter is enabled and returns an amount based on that.
     *
     * @param bankName The bank name.
     * @param p        The player.
     * @param level    The level to check.
     * @return The interest amount.
     */
    public static BigDecimal getOfflineInterestRate(String bankName, OfflinePlayer p, int level) {
        if (Values.CONFIG.enableInterestLimiter())
            return getLimitedInterest(bankName, p, level, Values.CONFIG.getOfflineInterestMoneyGiven());

        Bank bank = getBank(bankName);
        ConfigurationSection upgrades = bank.getUpgrades();
        if (upgrades == null) return Values.CONFIG.getOfflineInterestMoneyGiven();

        level = Math.max(level, 1);
        String interest = upgrades.getString(level + ".Offline-Interest");
        if (BPUtils.isInvalidNumber(interest)) {
            // Check this before to avoid spamming warns if the user does not specify an interest in an upgrade.
            if (interest != null)
                BPLogger.warn("Invalid offline interest amount in the " + level + "* upgrades section, file: " + bankName + ".yml");
            return Values.CONFIG.getOfflineInterestMoneyGiven();
        }

        return new BigDecimal(interest.replace("%", ""));
    }

    /**
     * Get the bank interest limiter at the selected level.
     *
     * @param bankName The bank name.
     * @param level    The level to check.
     * @return A list representing the interest limiter.
     */
    public static List<String> getInterestLimiter(String bankName, int level) {
        Bank bank = getBank(bankName);
        ConfigurationSection upgrades = bank.getUpgrades();
        if (upgrades == null) return Values.CONFIG.getInterestLimiter();

        List<String> limiter = upgrades.getStringList(Math.max(level, 1) + ".Interest-Limiter");
        return limiter.isEmpty() ? Values.CONFIG.getInterestLimiter() : limiter;
    }

    /**
     * Get the bank cost at the selected level.
     *
     * @param bankName The bank name.
     * @param level    The level to check.
     * @return The cost amount.
     */
    public static BigDecimal getLevelCost(String bankName, int level) {
        Bank bank = getBank(bankName);
        ConfigurationSection upgrades = bank.getUpgrades();
        if (upgrades == null) return new BigDecimal(0);

        String cost = upgrades.getString(Math.max(level, 1) + ".Cost");
        return new BigDecimal(cost == null ? "0" : cost);
    }

    /**
     * Get a list of required items to level up the bank to the selected level.
     *
     * @param bankName The bank name.
     * @param level    The level to check.
     * @return A list of itemstack representing the required items with its amount set, null if not specified.
     */
    public static List<ItemStack> getRequiredItems(String bankName, int level) {
        List<ItemStack> items = new ArrayList<>();

        Bank bank = getBank(bankName);
        ConfigurationSection upgrades = bank.getUpgrades();
        if (upgrades == null) return items;

        level = Math.max(level, 1);
        String requiredItemsString = upgrades.getString(level + ".Required-Items");
        if (requiredItemsString == null || requiredItemsString.isEmpty()) return items;

        List<String> configItems = new ArrayList<>();
        if (!requiredItemsString.contains(",")) configItems.add(requiredItemsString);
        else configItems.addAll(Arrays.asList(requiredItemsString.split(",")));

        for (String splitItem : configItems) {
            if (!splitItem.contains("-")) {
                try {
                    items.add(new ItemStack(Material.valueOf(splitItem)));
                } catch (IllegalArgumentException e) {
                    BPLogger.warn("The bank \"" + bankName + "\" contains an invalid item in the \"Required-Items\" path at level *" + level + ".");
                }
            } else {
                String[] split = splitItem.split("-");
                ItemStack item;
                try {
                    item = new ItemStack(Material.valueOf(split[0]));
                } catch (IllegalArgumentException e) {
                    BPLogger.warn("The bank \"" + bankName + "\" contains an invalid item in the \"Required-Items\" path at level *" + level + ".");
                    continue;
                }
                int amount = 1;
                try {
                    amount = Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                    BPLogger.warn("The bank \"" + bankName + "\" contains an invalid number in the \"Required-Items\" path at level *" + level + ".");
                }

                item.setAmount(amount);
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Check if the bank at the selected level takes from the player inventory the required items when upgrading.
     *
     * @param bankName The bank name.
     * @param level    The level to check.
     * @return true if in the selected bank level is specified to remove the items, false otherwise.
     */
    public static boolean isRemovingRequiredItems(String bankName, int level) {
        ConfigurationSection upgrades = getBank(bankName).getUpgrades();
        return upgrades != null && upgrades.getBoolean(Math.max(level, 1) + ".Remove-Required-Items");
    }

    /**
     * Get a list of all registered levels in the selected bank.
     *
     * @param bankName The bank name.
     * @return A list of available levels.
     */
    public static List<String> getLevels(String bankName) {
        List<String> levels = new ArrayList<>();

        ConfigurationSection upgrades = getBank(bankName).getUpgrades();
        if (upgrades == null) {
            levels.add("1");
            return levels;
        }

        levels.addAll(upgrades.getKeys(false));
        return levels;
    }

    /**
     * Get the current bank level of the selected player.
     *
     * @param bankName The bank name.
     * @param p        The player.
     * @return The player bank level of the selected bank.
     */
    public static int getCurrentLevel(String bankName, OfflinePlayer p) {
        BPEconomy economy = BPEconomy.get(bankName);
        return economy == null ? 1 :economy.getBankLevel(p);
    }

    /**
     * Check if the selected bank has a level next the player current bank level.
     *
     * @param bankName The bank name.
     * @param p        The player.
     * @return true if the bank has another level, false otherwise.
     */
    public static boolean hasNextLevel(String bankName, OfflinePlayer p) {
        return hasNextLevel(bankName, getCurrentLevel(bankName, p));
    }

    /**
     * Check if the selected bank has a level next the one specified.
     *
     * @param bankName     The bank name.
     * @param currentLevel The level.
     * @return true if the bank has another level, false otherwise.
     */
    public static boolean hasNextLevel(String bankName, int currentLevel) {
        ConfigurationSection upgrades = getBank(bankName).getUpgrades();
        return upgrades != null && upgrades.getConfigurationSection(String.valueOf(currentLevel + 1)) != null;
    }

    /**
     * Get a list of all available banks for that player.
     *
     * @param p The player.
     * @return A list of available bank names.
     */
    public static List<String> getAvailableBanks(OfflinePlayer p) {
        List<String> availableBanks = new ArrayList<>();
        if (p == null) return availableBanks;

        for (String bankName : BPEconomy.nameList())
            if (isAvailable(bankName, p)) availableBanks.add(bankName);

        return availableBanks;
    }

    /**
     * Check if the selected bank is available for the selected player.
     *
     * @param bankName The bank name.
     * @param p        The player.
     * @return true if available, false otherwise.
     */
    public static boolean isAvailable(String bankName, OfflinePlayer p) {
        Bank bank = getBank(bankName);
        if (bank == null) return false;

        String permission = bank.getAccessPermission();
        if (permission == null || permission.isEmpty()) return true;

        Player oP = p.getPlayer();
        return oP != null ? oP.hasPermission(permission) : BPUtils.hasOfflinePermission(p, permission);
    }

    /**
     * Set a new level to the selected bank for the selected player.
     *
     * @param bankName The bank name.
     * @param p        The player
     * @param level    The new level.
     */
    public static void setLevel(String bankName, OfflinePlayer p, int level) {
        BPEconomy economy = BPEconomy.get(bankName);
        if (economy != null) economy.setBankLevel(p, level);
        else BPLogger.warn("Could not set bank level to " + p.getName() + " because the specified bank \"" + bankName + "\" does not exist.");
    }

    /**
     * Method to upgrade the selected bank for the selected player,
     * the player must be online because of the possible required
     * items and values that are required to upgrade, to set the
     * level of the bank to an offline player use {@link BankManager#setLevel(String, OfflinePlayer, int)}
     *
     * @param bankName The bank name.
     * @param p        The player.
     */
    public static void upgradeBank(String bankName, Player p) {
        if (!hasNextLevel(bankName, p)) {
            BPMessages.send(p, "Bank-Max-Level");
            return;
        }

        int nextLevel = getCurrentLevel(bankName, p) + 1;

        boolean remove = false;
        List<ItemStack> requiredItems = getRequiredItems(bankName, nextLevel);
        if (!requiredItems.isEmpty()) {
            boolean hasItems = false;

            for (ItemStack item : requiredItems) {
                int amount = item.getAmount();
                int playerAmount = 0;

                boolean hasItem = false;
                for (ItemStack content : p.getInventory().getContents()) {
                    if (content == null || content.getType() != item.getType()) continue;
                    playerAmount += content.getAmount();

                    if (playerAmount < amount) continue;
                    hasItem = true;
                    break;
                }
                if (!hasItem) {
                    hasItems = false;
                    break;
                }
                hasItems = true;
            }
            if (!hasItems) {
                BPMessages.send(p, "Insufficient-Items", "%items%$" + BPUtils.getRequiredItems(requiredItems));
                return;
            }
            remove = true;
        }

        BigDecimal cost = getLevelCost(bankName, nextLevel);
        BPEconomy economy = BPEconomy.get(bankName);

        if (Values.CONFIG.useBankBalanceToUpgrade()) {

            BigDecimal balance = economy.getBankBalance(p);
            if (balance.doubleValue() < cost.doubleValue()) {
                BPMessages.send(p, "Insufficient-Money");
                return;
            }

            economy.removeBankBalance(p, cost);
        } else {

            Economy vaultEconomy = BankPlus.INSTANCE().getVaultEconomy();
            double balance = vaultEconomy.getBalance(p);

            if (balance < cost.doubleValue()) {
                BPMessages.send(p, "Insufficient-Money");
                return;
            }

            vaultEconomy.withdrawPlayer(p, cost.doubleValue());
        }

        if (isRemovingRequiredItems(bankName, nextLevel) && remove) for (ItemStack item : requiredItems) p.getInventory().removeItem(item);

        setLevel(bankName, p, nextLevel);
        BPMessages.send(p, "Bank-Upgraded");

        if (!hasNextLevel(bankName, nextLevel)) {
            for (String line : Values.MULTIPLE_BANKS.getAutoBanksUnlocker()) {
                if (!line.contains(":")) continue;

                String[] parts = line.split(":");
                String name = parts[0];
                if (!name.equals(bankName)) continue;

                for (int i = 1; i < parts.length; i++) {
                    String cmd = parts[i].replace("%player%", p.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        }
    }

    private static BigDecimal getLimitedInterest(String bankName, OfflinePlayer p, int level, BigDecimal fallBack) {
        for (String limiter : getInterestLimiter(bankName, level)) {
            if (!limiter.contains(":")) continue;

            String[] split1 = limiter.split(":");
            if (BPUtils.isInvalidNumber(split1[1])) continue;

            String[] split2 = split1[0].split("-");
            if (BPUtils.isInvalidNumber(split2[0]) || BPUtils.isInvalidNumber(split2[1])) continue;

            String interest = split1[1].replace("%", ""), from = split2[0], to = split2[1];
            BigDecimal interestRate = new BigDecimal(interest), fromNumber = new BigDecimal(from), toNumber = new BigDecimal(to);

            if (fromNumber.doubleValue() > toNumber.doubleValue()) {
                BigDecimal temp = toNumber;
                fromNumber = toNumber;
                toNumber = temp;
            }

            BigDecimal balance = BPEconomy.get(bankName).getBankBalance(p);
            if (fromNumber.doubleValue() <= balance.doubleValue() && toNumber.doubleValue() >= balance.doubleValue())
                return interestRate;
        }
        return fallBack;
    }
}