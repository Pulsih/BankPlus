package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerFiles;
import me.pulsi_.bankplus.economy.MultiEconomyManager;
import me.pulsi_.bankplus.economy.SingleEconomyManager;
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
import java.util.List;

/**
 * This class is used to receive information from the selected bank and many more usefully methods to manage the bank and the player.
 */
public class BankReader {

    private final Bank bank;

    /**
     * This constructor is used for methods that doesn't require to select a bank, for example #getAvailableBanks();
     * If you try to access methods that requires the bank object with this constructor you'll get a lot of errors.
     */
    public BankReader() {
        this.bank = null;
    }

    public BankReader(String bankName) {
        this.bank = BankPlus.INSTANCE.getBankGuiRegistry().getBanks().get(bankName);
    }

    public boolean exist() {
        return bank != null;
    }

    public File getFile() {
        return bank.getBankFile();
    }

    public FileConfiguration getConfig() {
        return bank.getBankConfig();
    }

    public String getTitle() {
        return bank.getTitle();
    }

    public int getLines() {
        return bank.getSize();
    }

    public long getUpdateDelay() {
        return bank.getUpdateDelay();
    }

    public boolean isFillerEnabled() {
        return bank.hasFiller();
    }

    public String getFillerMaterial() {
        return bank.getFillerMaterial();
    }

    public boolean isFillerGlowing() {
        return bank.isFillerGlowing();
    }

    public boolean hasPermissionSection() {
        return bank.getPermission() != null;
    }

    public String getPermission() {
        return bank.getPermission();
    }

    public ConfigurationSection getBanksGuiItemSection() {
        return bank.getBanksListGuiItems();
    }

    public boolean hasUpgrades() {
        return bank.getUpgrades() != null;
    }

    public ConfigurationSection getUpgrades() {
        return bank.getUpgrades();
    }

    public boolean hasItems() {
        return bank.getItems() != null;
    }

    public ConfigurationSection getItems() {
        return bank.getItems();
    }

    public boolean hasSettings() {
        return bank.getSettings() != null;
    }

    public ConfigurationSection getSettings() {
        return bank.getSettings();
    }

    /**
     * Get the current bank capacity based on the bank level of this player.
     *
     * @param p The player.
     * @return The capacity amount.
     */
    public BigDecimal getCapacity(Player p) {
        return getCapacity(getCurrentLevel(p));
    }

    /**
     * Get the current bank capacity based on the bank level of this player.
     *
     * @param p The player.
     * @return The capacity amount.
     */
    public BigDecimal getCapacity(OfflinePlayer p) {
        return getCapacity(getCurrentLevel(p));
    }

    /**
     * Get the bank capacity of that specified level.
     *
     * @param level The bank level.
     * @return The capacity amount.
     */
    public BigDecimal getCapacity(int level) {
        if (!hasUpgrades()) return Values.CONFIG.getMaxBankCapacity();

        String capacity = getUpgrades().getString(level + ".Capacity");
        return new BigDecimal(capacity == null ? Values.CONFIG.getMaxBankCapacity().toString() : capacity);
    }

    /**
     * Get the interest rate of the player's bank level.
     *
     * @param p The player.
     * @return The interest amount.
     */
    public BigDecimal getInterest(Player p) {
        return getInterest(getCurrentLevel(p));
    }

    /**
     * Get the interest rate of the player's bank level.
     *
     * @param p The player.
     * @return The interest amount.
     */
    public BigDecimal getInterest(OfflinePlayer p) {
        return getInterest(getCurrentLevel(p));
    }

    /**
     * Get the interest rate of that bank level.
     *
     * @param level The bank level.
     * @return The interest amount.
     */
    public BigDecimal getInterest(int level) {
        if (!hasUpgrades()) return Values.CONFIG.getInterestMoneyGiven();

        String interest = getUpgrades().getString(level + ".Interest");

        if (BPUtils.isInvalidNumber(interest)) {
            if (interest != null)
                BPLogger.warn("Invalid interest amount in the " + level + "* upgrades section, file: " + bank.getIdentifier() + ".yml");
            return Values.CONFIG.getInterestMoneyGiven();
        }

        return new BigDecimal(interest.replace("%", ""));
    }

    /**
     * Get the offline interest rate of the player's bank level.
     *
     * @param p The player
     * @return The offline interest amount.
     */
    public BigDecimal getOfflineInterest(Player p) {
        return getOfflineInterest(getCurrentLevel(p));
    }

    /**
     * Get the offline interest rate of the player's bank level.
     *
     * @param p The player
     * @return The offline interest amount.
     */
    public BigDecimal getOfflineInterest(OfflinePlayer p) {
        return getOfflineInterest(getCurrentLevel(p));
    }

    /**
     * Get the offline interest rate of that bank level.
     *
     * @param level The bank level.
     * @return The offline interest amount.
     */
    public BigDecimal getOfflineInterest(int level) {
        if (!hasUpgrades()) return Values.CONFIG.getOfflineInterestMoneyGiven();

        String interest = getUpgrades().getString(level + ".Offline-Interest");

        if (BPUtils.isInvalidNumber(interest)) {
            if (interest != null)
                BPLogger.warn("Invalid offline interest amount in the " + level + "* upgrades section, file: " + bank.getIdentifier() + ".yml");
            return Values.CONFIG.getOfflineInterestMoneyGiven();
        }

        return new BigDecimal(interest.replace("%", ""));
    }

    /**
     * Get the cost of this bank level.
     *
     * @param level The level to check.
     * @return The cost, zero if no cost is specified.
     */
    public BigDecimal getLevelCost(int level) {
        if (!hasUpgrades()) return new BigDecimal(0);

        String cost = getUpgrades().getString(level + ".Cost");
        return new BigDecimal(cost == null ? "0" : cost);
    }

    /**
     * Get the items required to level up the bank to this level.
     *
     * @param level The level to check.
     * @return The itemstack representing the item needed with its amount set, null if not specified.
     */
    public ItemStack getLevelRequiredItems(int level) {
        if (!hasUpgrades()) return null;

        String items = getUpgrades().getString(level + ".Required-Items");
        if (items == null || items.isEmpty()) return null;

        ItemStack itemStack = null;
        int amount = 1;
        String item;

        if (!items.contains("-")) item = items;
        else {
            String[] split = items.split("-");
            item = split[0];
            try {
                amount = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                BPLogger.warn("Invalid required items amount in the " + level + "* upgrades section, file: " + bank.getIdentifier() + ".yml");
            }
        }

        try {
            itemStack = new ItemStack(Material.valueOf(item), Math.max(amount, 1));
        } catch (NumberFormatException e) {
            BPLogger.warn("Invalid required items material in the " + level + "* upgrades section, file: " + bank.getIdentifier() + ".yml");
        }

        return itemStack;
    }

    /**
     * Check if the plugin should take the required items from the player inventory.
     *
     * @param level The level to check.
     */
    public boolean removeRequiredItems(int level) {
        return hasUpgrades() && getUpgrades().getBoolean(level + ".Remove-Required-Items");
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

        levels.addAll(getUpgrades().getKeys(false));
        return levels;
    }

    /**
     * Get the current bank level for that player.
     *
     * @param p The player.
     * @return The current level.
     */
    public int getCurrentLevel(Player p) {
        FileConfiguration config = new BPPlayerFiles(p).getPlayerConfig();
        return Math.max(config.getInt("Banks." + bank.getIdentifier() + ".Level"), 1);
    }

    /**
     * Get the current bank level for that player.
     *
     * @param p The player.
     * @return The current level.
     */
    public int getCurrentLevel(OfflinePlayer p) {
        FileConfiguration config = new BPPlayerFiles(p).getPlayerConfig();
        return Math.max(config.getInt("Banks." + bank.getIdentifier() + ".Level"), 1);
    }

    /**
     * Check if the bank of that player has a next level.
     *
     * @param p The player.
     * @return true if it has a next level, false otherwise.
     */
    public boolean hasNextLevel(Player p) {
        ConfigurationSection section = getUpgrades();
        return section == null ? false : section.getConfigurationSection(String.valueOf(getCurrentLevel(p) + 1)) != null;
    }

    /**
     * Check if the selected bank has another level next the one specified.
     *
     * @param currentLevel The current level of the bank.
     * @return true if it has a next level, false otherwise.
     */
    public boolean hasNextLevel(int currentLevel) {
        ConfigurationSection section = getUpgrades();
        return section == null ? false : section.getConfigurationSection(String.valueOf(currentLevel + 1)) != null;
    }

    /**
     * This method does not require to specify a bank in the constructor.
     *
     * @param p The player.
     * @return A list with all names of available banks for this player. To get a list of ALL banks use the BanksGuiRegistry class through the main class.
     */
    public List<String> getAvailableBanks(Player p) {
        List<String> availableBanks = new ArrayList<>();
        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
            if (new BankReader(bankName).isAvailable(p)) availableBanks.add(bankName);
        }
        return availableBanks;
    }

    /**
     * This method does not require to specify a bank in the constructor.
     *
     * @param p The player.
     * @return A list with all names of available banks for this player. To get a list of ALL banks use the BanksGuiRegistry class through the main class.
     */
    public List<String> getAvailableBanks(OfflinePlayer p) {
        List<String> availableBanks = new ArrayList<>();
        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
            if (new BankReader(bankName).isAvailable(p)) availableBanks.add(bankName);
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
        if (!hasPermissionSection()) return true;
        return getPermission().isEmpty() || p.hasPermission(getPermission());
    }

    /**
     * Check if this bank is available for the player.
     *
     * @param p The player
     * @return true if available, false otherwise.
     */
    public boolean isAvailable(OfflinePlayer p) {
        if (!hasPermissionSection()) return true;
        else {
            String wName = Bukkit.getWorlds().get(0).getName();
            return BankPlus.INSTANCE.getPermissions().playerHas(wName, p, getPermission());
        }
    }

    /**
     * Method used to upgrade the selected bank for the specified player.
     *
     * @param p The player.
     */
    public void upgradeBank(Player p) {
        if (!isAvailable(p)) {
            BPMessages.send(p, "Cannot-Access-Bank");
            return;
        }
        if (!hasNextLevel(p)) {
            BPMessages.send(p, "Bank-Max-Level");
            return;
        }

        int nextLevel = getCurrentLevel(p) + 1;

        ItemStack requiredItems = getLevelRequiredItems(nextLevel);
        if (requiredItems != null) {
            int amount = requiredItems.getAmount();

            int playerAmount = 0;
            boolean hasItems = false;
            for (ItemStack content : p.getInventory().getContents()) {
                if (content == null || content.getType() != requiredItems.getType()) continue;
                playerAmount += content.getAmount();

                if (playerAmount < amount) continue;
                hasItems = true;
                break;
            }
            if (!hasItems) {
                String item = (requiredItems.getType() + (amount > 1 ? "s" : "")).toLowerCase();
                BPMessages.send(p, "Insufficient-Items", "%amount%$" + amount, "%item%$" + item);
                return;
            }
        }

        BigDecimal cost = getLevelCost(nextLevel);
        if (Values.CONFIG.useBankBalanceToUpgrade()) {

            BigDecimal balance;
            SingleEconomyManager singleEconomyManager = new SingleEconomyManager(p);
            MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);

            if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) balance = multiEconomyManager.getBankBalance();
            else balance = singleEconomyManager.getBankBalance();

            if (balance.doubleValue() < cost.doubleValue()) {
                BPMessages.send(p, "Insufficient-Money");
                return;
            }

            if (removeRequiredItems(nextLevel) && requiredItems!= null) p.getInventory().removeItem(requiredItems);
            if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) multiEconomyManager.removeBankBalance(cost, bank.getIdentifier());
            else singleEconomyManager.removeBankBalance(cost);
        } else {

            Economy economy = BankPlus.INSTANCE.getEconomy();
            double balance = economy.getBalance(p);

            if (balance < cost.doubleValue()) {
                BPMessages.send(p, "Insufficient-Money");
                return;
            }

            if (removeRequiredItems(nextLevel) && requiredItems!= null) p.getInventory().removeItem(requiredItems);
            economy.withdrawPlayer(p, cost.doubleValue());
        }

        BPPlayerFiles files = new BPPlayerFiles(p);
        FileConfiguration config = files.getPlayerConfig();
        config.set("Banks." + bank.getIdentifier() + ".Level", nextLevel);
        files.savePlayerFile(config, true);

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

    /**
     * Get the bank specified in the constructor.
     *
     * @return The bank object.
     */
    public Bank getBank() {
        return bank;
    }
}