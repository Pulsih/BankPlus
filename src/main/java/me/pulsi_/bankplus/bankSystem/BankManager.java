package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * BankPlus main core class to manage the entire server economy.
 * In this class, you'll find many useful methods that will help you get, modify and set player's balances.
 * <p>
 * To access this class, use the method {@link BankPlus#getBankManager()};
 */
public class BankManager {

    private final BPEconomy economy;
    private final BankGuiRegistry registry;

    public BankManager() {
        economy = BankPlus.getBPEconomy();
        registry = BankPlus.INSTANCE.getBankGuiRegistry();
    }

    public Bank getBank(String bankName) {
        return registry.getBanks().get(bankName);
    }

    public boolean exist(String bankName) {
        return registry.getBanks().containsKey(bankName);
    }

    /**
     * Get the bank capacity based on the bank level of the selected player.
     *
     * @param p The player.
     * @return The capacity amount.
     */
    public BigDecimal getCapacity(String bankName, OfflinePlayer p) {
        return getCapacity(bankName, getCurrentLevel(p));
    }

    /**
     * Get the bank capacity at level 1.
     *
     * @param bankName The bank name.
     * @return The capacity amount.
     */
    public BigDecimal getCapacity(String bankName) {
        return getCapacity(bankName, 1);
    }

    /**
     * Get the bank capacity of that specified level.
     *
     * @param bankName The bank name.
     * @param level The level to check.
     * @return The capacity amount.
     */
    public BigDecimal getCapacity(String bankName, int level) {
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
     * @param p The player.
     * @return The interest amount.
     */
    public BigDecimal getInterest(String bankName, OfflinePlayer p) {
        return getInterest(bankName, p, getCurrentLevel(p));
    }

    /**
     * Get the bank interest rate at the selected level.
     * This method requires specifying a player because it needs his money to make calculations.
     * It also already checks if the interest limiter is enabled and returns an amount based on that.
     *
     * @param bankName The bank name.
     * @param p The player.
     * @param level The level to check.
     * @return The interest amount.
     */
    public BigDecimal getInterest(String bankName, OfflinePlayer p, int level) {
        if (Values.CONFIG.enableInterestLimiter()) return getLimiterInterest(p, level, Values.CONFIG.getInterestMoneyGiven());

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
     * @param p The player.
     * @return The interest amount.
     */
    public BigDecimal getOfflineInterest(String bankName, OfflinePlayer p) {
        return getOfflineInterest(bankName, p, getCurrentLevel(p));
    }

    /**
     * Get the bank offline interest rate at the selected level.
     * This method requires specifying a player because it needs his money to make calculations.
     * It also already checks if the interest limiter is enabled and returns an amount based on that.
     *
     * @param bankName The bank name.
     * @param p The player.
     * @param level The level to check.
     * @return The interest amount.
     */
    public BigDecimal getOfflineInterest(String bankName, OfflinePlayer p, int level) {
        if (Values.CONFIG.enableInterestLimiter()) return getLimiterInterest(p, level, Values.CONFIG.getOfflineInterestMoneyGiven());

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
     * Get the list of interest limiter
     * @param level The bank level.
     */
    public List<String> getInterestLimiter(int level) {
        if (!hasUpgrades()) return Values.CONFIG.getInterestLimiter();

        List<String> limiter = bank.getUpgrades().getStringList(level + ".Interest-Limiter");
        return limiter.isEmpty() ? Values.CONFIG.getInterestLimiter() : limiter;
    }

    /**
     * Get the cost of this bank level.
     *
     * @param level The level to check.
     * @return The cost, zero if no cost is specified.
     */
    public BigDecimal getLevelCost(int level) {
        if (!hasUpgrades()) return new BigDecimal(0);

        String cost = bank.getUpgrades().getString(level + ".Cost");
        return new BigDecimal(cost == null ? "0" : cost);
    }

    /**
     * Get the items required to level up the bank to this level.
     *
     * @param level The level to check.
     * @return The itemstack representing the item needed with its amount set, null if not specified.
     */
    public List<ItemStack> getLevelRequiredItems(int level) {
        List<ItemStack> items = new ArrayList<>();
        if (!hasUpgrades()) return items;

        String requiredItemsString = bank.getUpgrades().getString(level + ".Required-Items");
        if (requiredItemsString == null || requiredItemsString.isEmpty()) return items;

        List<String> configItems = new ArrayList<>();
        if (!requiredItemsString.contains(",")) configItems.add(requiredItemsString);
        else configItems.addAll(Arrays.asList(requiredItemsString.split(",")));

        for (String splitItem : configItems) {
            if (!splitItem.contains("-")) {
                try {
                    items.add(new ItemStack(Material.valueOf(splitItem)));
                } catch (IllegalArgumentException e) {
                    BPLogger.warn("The bank \"" + bank.getIdentifier() + "\" contains an invalid item in the \"Required-Items\" path at level *" + level + ".");
                }
            } else {
                String[] split = splitItem.split("-");
                ItemStack item;
                try {
                    item = new ItemStack(Material.valueOf(split[0]));
                } catch (IllegalArgumentException e) {
                    BPLogger.warn("The bank \"" + bank.getIdentifier() + "\" contains an invalid item in the \"Required-Items\" path at level *" + level + ".");
                    continue;
                }
                int amount = 1;
                try {
                    amount = Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                    BPLogger.warn("The bank \"" + bank.getIdentifier() + "\" contains an invalid number in the \"Required-Items\" path at level *" + level + ".");
                }

                item.setAmount(amount);
                items.add(item);
            }
        }

        return items;
    }

    /**
     * Check if the plugin should take the required items from the player inventory.
     *
     * @param level The level to check.
     */
    public boolean removeRequiredItems(int level) {
        return hasUpgrades() && bank.getUpgrades().getBoolean(level + ".Remove-Required-Items");
    }

    /**
     * Get a list of all levels that this bank have, if it has none, it will return a list of 1.
     *
     * @return A string list with all levels.
     */
    public List<String> getLevels() {
        List<String> levels = new ArrayList<>();
        if (!hasUpgrades()) {
            levels.add("1");
            return levels;
        }

        levels.addAll(bank.getUpgrades().getKeys(false));
        return levels;
    }

    /**
     * Get the current bank level for that player.
     *
     * @param uuid The player UUID.
     * @return The current level.
     */
    public int getCurrentLevel(UUID uuid) {
        return getCurrentLevel(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Get the current bank level for that player.
     *
     * @param p The player.
     * @return The current level.
     */
    public int getCurrentLevel(OfflinePlayer p) {
        if (Values.CONFIG.isSqlEnabled() && BankPlus.INSTANCE.getSql().isConnected())
            return new SQLPlayerManager(p).getLevel(bank.getIdentifier());

        FileConfiguration config = new BPPlayerManager(p).getPlayerConfig();
        return Math.max(config.getInt("banks." + bank.getIdentifier() + ".level"), 1);
    }

    /**
     * Check if the bank of that player has a next level.
     *
     * @param p The player.
     * @return true if it has a next level, false otherwise.
     */
    public boolean hasNextLevel(Player p) {
        return hasNextLevel(getCurrentLevel(p));
    }

    /**
     * Check if the selected bank has another level next the one specified.
     *
     * @param currentLevel The current level of the bank.
     * @return true if it has a next level, false otherwise.
     */
    public boolean hasNextLevel(int currentLevel) {
        return hasUpgrades() && bank.getUpgrades().getConfigurationSection(String.valueOf(currentLevel + 1)) != null;
    }

    /**
     * This method does not require to specify a bank in the constructor.
     *
     * @param p The player.
     * @return A list with all names of available banks for this player. To get a list of ALL banks use the BanksGuiRegistry class through the main class.
     */
    public List<String> getAvailableBanks(OfflinePlayer p) {
        List<String> availableBanks = new ArrayList<>();
        if (p == null) return availableBanks;

        if (p.isOnline()) {
            for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                if (new BankManager(bankName).isAvailable(p.getPlayer()))
                    availableBanks.add(bankName);
        } else {
            for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                if (new BankManager(bankName).isAvailable(p))
                    availableBanks.add(bankName);
        }
        return availableBanks;
    }

    /**
     * Check if this bank is available for the player.
     *
     * @param p The player
     * @return true if available, false otherwise.
     */
    public boolean isAvailable(Player p) {
        return !hasPermission() || bank.getPermission().isEmpty() || p.hasPermission(bank.getPermission());
    }

    /**
     * Check if this bank is available for the player.
     *
     * @param p The player
     * @return true if available, false otherwise.
     */
    public boolean isAvailable(OfflinePlayer p) {
        if (!hasPermission()) return true;
        else {
            String wName = Bukkit.getWorlds().get(0).getName();
            return BankPlus.INSTANCE.getPermissions().playerHas(wName, p, bank.getPermission());
        }
    }

    public void setLevel(OfflinePlayer p, int level) {
        BPPlayerManager files = new BPPlayerManager(p);
        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig(file);
        config.set("banks." + bank.getIdentifier() + ".level", level);
        files.savePlayerFile(config, file, true);
    }

    /**
     * Method used to upgrade the selected bank for the specified player.
     *
     * @param p The player.
     */
    public void upgradeBank(Player p) {
        if (!hasNextLevel(p)) {
            BPMessages.send(p, "Bank-Max-Level");
            return;
        }

        int nextLevel = getCurrentLevel(p) + 1;

        List<ItemStack> requiredItems = getLevelRequiredItems(nextLevel);
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
        }

        BigDecimal cost = getLevelCost(nextLevel);
        if (Values.CONFIG.useBankBalanceToUpgrade()) {

            BigDecimal balance = economy.getBankBalance(p, bank.getIdentifier());
            if (balance.doubleValue() < cost.doubleValue()) {
                BPMessages.send(p, "Insufficient-Money");
                return;
            }

            if (removeRequiredItems(nextLevel) && !requiredItems.isEmpty()) for (ItemStack item : requiredItems) p.getInventory().removeItem(item);
            economy.removeBankBalance(p, cost, bank.getIdentifier());
        } else {

            Economy economy = BankPlus.INSTANCE.getVaultEconomy();
            double balance = economy.getBalance(p);

            if (balance < cost.doubleValue()) {
                BPMessages.send(p, "Insufficient-Money");
                return;
            }

            if (removeRequiredItems(nextLevel) && !requiredItems.isEmpty()) for (ItemStack item : requiredItems) p.getInventory().removeItem(item);
            economy.withdrawPlayer(p, cost.doubleValue());
        }

        setLevel(p, nextLevel);
        BPMessages.send(p, "Bank-Upgraded");

        if (!hasNextLevel(nextLevel)) {
            for (String line : Values.MULTIPLE_BANKS.getAutoBanksUnlocker()) {
                if (!line.contains(":")) continue;

                String[] parts = line.split(":");
                String bankName = parts[0];
                if (!bankName.equals(bank.getIdentifier())) continue;

                for (int i = 1; i < parts.length; i++) {
                    String cmd = parts[i].replace("%player%", p.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        }
    }

    private BigDecimal getLimiterInterest(OfflinePlayer p, int level, BigDecimal fallBack) {
        for (String limiter : getInterestLimiter(level)) {
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

            BigDecimal balance = economy.getBankBalance(p, bank.getIdentifier());
            if (fromNumber.doubleValue() <= balance.doubleValue() && toNumber.doubleValue() >= balance.doubleValue())
                return interestRate;
        }
        return fallBack;
    }
}