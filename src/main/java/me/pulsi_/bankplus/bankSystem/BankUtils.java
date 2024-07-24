package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPItems;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPChat;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import me.pulsi_.bankplus.values.MultipleBanksValues;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * BankPlus main core class to manage the entire server economy.
 * In this class, you'll find many useful methods that will help you get, modify and set player's balances.
 */
public class BankUtils {

    public BankUtils() throws Exception {
        throw new Exception("This class may not be initialized.");
    }
 
    public static Bank getBank(String bankName) {
        return BankPlus.INSTANCE().getBankRegistry().getBanks().get(bankName);
    }

    public static Collection<Bank> getBanks() {
        return BankPlus.INSTANCE().getBankRegistry().getBanks().values();
    }

    /**
     * Checks if the selected bank is registered.
     * @param bank The bank.
     * @return true if it is registered, false otherwise.
     */
    public static boolean exist(Bank bank) {
        return exist(bank, null);
    }

    /**
     * Checks if the selected bank is registered.
     * @param bank The bank.
     * @return true if it is registered, false otherwise.
     */
    public static boolean exist(Bank bank, CommandSender s) {
        boolean exist = bank != null;
        if (!exist) BPMessages.send(s, "Invalid-Bank");
        return exist;
    }

    /**
     * Checks if the selected bank name is registered.
     * @param bankName The bank name.
     * @return true if it is registered, false otherwise.
     */
    public static boolean exist(String bankName) {
        return exist(bankName, null);
    }

    /**
     * Checks if the selected bank name is registered, and automatically alert the command sender, if specified, its non-existence.
     * @param bankName The bank name.
     * @return true if it is registered, false otherwise.
     */
    public static boolean exist(String bankName, CommandSender s) {
        boolean exist = getBank(bankName) != null;
        if (!exist) BPMessages.send(s, "Invalid-Bank");
        return exist;
    }

    /**
     * Checks if the selected bank is the main bank.
     * @param bank The bank.
     * @return true if it is the main bank, false otherwise.
     */
    public static boolean isMainBank(Bank bank) {
        return bank.getIdentifier().equals(ConfigValues.getMainGuiName());
    }

    /**
     * Get the bank capacity based on the bank level of the selected player.
     *
     * @param p The player.
     * @return The capacity amount.
     */
    public static BigDecimal getCapacity(Bank bank, OfflinePlayer p) {
        return getCapacity(bank, getCurrentLevel(bank, p));
    }

    /**
     * Get the bank capacity of that specified level.
     *
     * @param bank  The bank.
     * @param level The level to check.
     * @return The capacity amount.
     */
    public static BigDecimal getCapacity(Bank bank, int level) {
        Bank.BankLevel bankLevel = bank.getBankLevel(level);
        return bankLevel == null ? ConfigValues.getMaxBankCapacity() : bankLevel.capacity;
    }

    /**
     * Get the bank max interest amount based on the bank level of the selected player.
     *
     * @param bank The bank name.
     * @param p    The player.
     * @return The interest amount.
     */
    public static BigDecimal getMaxInterestAmount(Bank bank, OfflinePlayer p) {
        return getMaxInterestAmount(bank, getCurrentLevel(bank, p));
    }

    /**
     * Get the bank max interest amount based on the selected bank level.
     *
     * @param bank  The bank name.
     * @param level The level.
     * @return The interest amount.
     */
    public static BigDecimal getMaxInterestAmount(Bank bank, int level) {
        Bank.BankLevel bankLevel = bank.getBankLevel(level);
        return bankLevel == null ? ConfigValues.getInterestMaxAmount() : bankLevel.maxInterestAmount;
    }

    /**
     * Get the bank interest rate based on the bank level of the selected player.
     * This method requires specifying a player because it needs his money to make calculations.
     * It also already checks if the interest limiter is enabled and returns an amount based on that.
     *
     * @param bank The bank name.
     * @param p    The player.
     * @return The interest amount.
     */
    public static BigDecimal getInterestRate(Bank bank, OfflinePlayer p) {
        return getInterestRate(bank, p, getCurrentLevel(bank, p));
    }

    /**
     * Get the bank interest rate at the selected level.
     * This method requires specifying a player because it needs his money to make calculations.
     * It also already checks if the interest limiter is enabled and returns an amount based on that.
     *
     * @param bank  The bank.
     * @param p     The player.
     * @param level The level to check.
     * @return The interest amount.
     */
    public static BigDecimal getInterestRate(Bank bank, OfflinePlayer p, int level) {
        if (ConfigValues.isInterestLimiterEnabled())
            return getLimitedInterest(bank, p, level, ConfigValues.getInterestRate());

        if (bank == null) return ConfigValues.getInterestRate();

        Bank.BankLevel bankLevel = bank.getBankLevel(level);
        return bankLevel == null ? ConfigValues.getInterestRate() : bankLevel.interest;
    }

    /**
     * Get the bank offline interest rate based on the bank level of the selected player.
     * This method requires specifying a player because it needs his money to make calculations.
     * It also already checks if the interest limiter is enabled and returns an amount based on that.
     *
     * @param bank The bank.
     * @param p    The player.
     * @return The interest amount.
     */
    public static BigDecimal getOfflineInterestRate(Bank bank, OfflinePlayer p) {
        return getOfflineInterestRate(bank, p, getCurrentLevel(bank, p));
    }

    /**
     * Get the bank offline interest rate at the selected level.
     * This method requires specifying a player because it needs his money to make calculations.
     * It also already checks if the interest limiter is enabled and returns an amount based on that.
     *
     * @param bank  The bank.
     * @param p     The player.
     * @param level The level to check.
     * @return The interest amount.
     */
    public static BigDecimal getOfflineInterestRate(Bank bank, OfflinePlayer p, int level) {
        if (ConfigValues.isInterestLimiterEnabled())
            return getLimitedInterest(bank, p, level, ConfigValues.getOfflineInterestRate());

        Bank.BankLevel bankLevel = bank.getBankLevel(level);
        return bankLevel == null ? ConfigValues.getOfflineInterestRate() : bankLevel.offlineInterest;
    }

    /**
     * Get the bank afk interest rate based on the bank level of the selected player.
     * This method requires specifying a player because it needs his money to make calculations.
     * It also already checks if the interest limiter is enabled and returns an amount based on that.
     *
     * @param bank The bank.
     * @param p    The player.
     * @return The AFK interest amount.
     */
    public static BigDecimal getAfkInterestRate(Bank bank, OfflinePlayer p) {
        return getAfkInterestRate(bank, p, getCurrentLevel(bank, p));
    }

    /**
     * Get the bank afk interest rate at the selected level.
     * This method requires specifying a player because it needs his money to make calculations.
     * It also already checks if the interest limiter is enabled and returns an amount based on that.
     *
     * @param bank  The bank.
     * @param p     The player.
     * @param level The level to check.
     * @return The AFK interest amount.
     */
    public static BigDecimal getAfkInterestRate(Bank bank, OfflinePlayer p, int level) {
        if (ConfigValues.isInterestLimiterEnabled())
            return getLimitedInterest(bank, p, level, ConfigValues.getAfkInterestRate());

        Bank.BankLevel bankLevel = bank.getBankLevel(level);
        return bankLevel == null ? ConfigValues.getAfkInterestRate() : bankLevel.afkInterest;
    }

    /**
     * Get the bank interest limiter at the selected level.
     *
     * @param bank  The bank.
     * @param level The level to check.
     * @return A list representing the interest limiter.
     */
    public static List<String> getInterestLimiter(Bank bank, int level) {
        Bank.BankLevel bankLevel = bank.getBankLevel(level);
        return bankLevel == null ? ConfigValues.getInterestLimiter() : bankLevel.interestLimiter;
    }

    /**
     * Get the bank cost at the selected level.
     *
     * @param bank  The bank.
     * @param level The level to check.
     * @return The cost amount.
     */
    public static BigDecimal getLevelCost(Bank bank, int level) {
        Bank.BankLevel bankLevel = bank.getBankLevel(level);
        return bankLevel == null ? BigDecimal.ZERO : bankLevel.cost;
    }

    /**
     * Get a list of required items to level up the bank to the selected level.
     *
     * @param bank  The bank.
     * @param level The level to check.
     * @return A list of itemstack representing the required items with its amount set, null if not specified.
     */
    public static List<ItemStack> getRequiredItems(Bank bank, int level) {
        Bank.BankLevel bankLevel = bank.getBankLevel(level);
        return bankLevel == null ? new ArrayList<>() : bankLevel.requiredItems;
    }

    /**
     * Check if the bank at the selected level takes from the player inventory the required items when upgrading.
     *
     * @param bank  The bank.
     * @param level The level to check.
     * @return true if in the selected bank level is specified to remove the items, false otherwise.
     */
    public static boolean isRemovingRequiredItems(Bank bank, int level) {
        Bank.BankLevel bankLevel = bank.getBankLevel(level);
        return bankLevel != null && bankLevel.removeRequiredItems;
    }

    /**
     * Get a list of all registered levels in the selected bank.
     *
     * @param bank The bank.
     * @return A list of available levels.
     */
    public static List<String> getLevels(Bank bank) {
        List<String> levels = new ArrayList<>();
        for (int level : bank.getBankLevels().keySet()) levels.add(level + "");
        return levels;
    }

    /**
     * Get the current bank level of the selected player.
     *
     * @param bank The bank.
     * @param p    The player.
     * @return The player bank level of the selected bank.
     */
    public static int getCurrentLevel(Bank bank, OfflinePlayer p) {
        if (bank == null) return 1;

        BPEconomy economy = bank.getBankEconomy();
        return economy == null ? 1 : economy.getBankLevel(p);
    }

    /**
     * Check if the selected bank has a level next the player current bank level.
     *
     * @param bank The bank.
     * @param p    The player.
     * @return true if the bank has another level, false otherwise.
     */
    public static boolean hasNextLevel(Bank bank, OfflinePlayer p) {
        return bank != null && hasNextLevel(bank, getCurrentLevel(bank, p));
    }

    /**
     * Check if the selected bank has a level next the one specified.
     *
     * @param bank         The bank.
     * @param currentLevel The level.
     * @return true if the bank has another level, false otherwise.
     */
    public static boolean hasNextLevel(Bank bank, int currentLevel) {
        return bank != null && getLevels(bank).contains(String.valueOf(currentLevel + 1));
    }

    /**
     * Get a list of all available banks for that player.
     *
     * @param p The player.
     * @return A list of available bank names.
     */
    public static List<Bank> getAvailableBanks(OfflinePlayer p) {
        List<Bank> availableBanks = new ArrayList<>();
        if (p == null) return availableBanks;

        for (Bank bank : getBanks())
            if (isAvailable(bank, p)) availableBanks.add(bank);

        return availableBanks;
    }

    /**
     * Get a list of all available banks for that player.
     *
     * @param p The player.
     * @return A list of available bank names.
     */
    public static List<String> getAvailableBankNames(OfflinePlayer p) {
        List<String> availableBanks = new ArrayList<>();
        if (p == null) return availableBanks;

        for (String bankName : BankPlus.INSTANCE().getBankRegistry().getBanks().keySet())
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
        return isAvailable(getBank(bankName), p);
    }

    /**
     * Check if the selected bank is available for the selected player.
     *
     * @param bank The bank.
     * @param p    The player.
     * @return true if available, false otherwise.
     */
    public static boolean isAvailable(Bank bank, OfflinePlayer p) {
        if (bank == null) return false;

        String permission = bank.getAccessPermission();
        if (permission == null || permission.isEmpty()) return true;

        Player oP = p.getPlayer();
        return oP != null ? oP.hasPermission(permission) : BPUtils.hasOfflinePermission(p, permission);
    }

    /**
     * Checks if the player bank is full.
     *
     * @param bank The bank.
     * @param p    The player to check.
     * @return true if it's full, false otherwise.
     */
    public static boolean isFull(Bank bank, OfflinePlayer p) {
        BigDecimal capacity = getCapacity(bank, p), pMoney = bank.getBankEconomy().getBankBalance(p);
        return pMoney.compareTo(capacity) >= 0;
    }

    /**
     * Set a new level to the selected bank for the selected player.
     *
     * @param bank  The bank.
     * @param p     The player
     * @param level The new level.
     */
    public static void setLevel(Bank bank, OfflinePlayer p, int level) {
        bank.getBankEconomy().setBankLevel(p, level);
    }

    /**
     * Method to upgrade the selected bank for the selected player,
     * the player must be online because of the possible required
     * items and values that are required to upgrade, to set the
     * level of the bank to an offline player use {@link BankUtils#setLevel(Bank, OfflinePlayer, int)}
     *
     * @param bank The bank.
     * @param p    The player.
     */
    public static void upgradeBank(Bank bank, Player p) {
        if (!hasNextLevel(bank, p)) {
            BPMessages.send(p, "Bank-Max-Level");
            return;
        }

        int nextLevel = getCurrentLevel(bank, p) + 1;

        boolean remove = false;
        List<ItemStack> requiredItems = getRequiredItems(bank, nextLevel);
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

        BigDecimal cost = getLevelCost(bank, nextLevel);
        BPEconomy economy = bank.getBankEconomy();

        if (ConfigValues.isUsingBankBalanceToUpgrade()) {

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

        if (isRemovingRequiredItems(bank, nextLevel) && remove)
            for (ItemStack item : requiredItems) p.getInventory().removeItem(item);

        setLevel(bank, p, nextLevel);
        BPMessages.send(p, "Bank-Upgraded");

        if (!hasNextLevel(bank, nextLevel)) {
            for (String line : MultipleBanksValues.getAutoBanksUnlocker()) {
                if (!line.contains(":")) continue;

                String[] parts = line.split(":");
                String name = parts[0];
                if (!name.equals(bank.getIdentifier())) continue;

                for (int i = 1; i < parts.length; i++) {
                    String cmd = parts[i].replace("%player%", p.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        }
    }

    /**
     * Create a BankLevel object from a level section.
     *
     * @param levelSection The level section.
     * @param bankName     The name shown in the console if something goes wrong while getting the level.
     * @return A bank level.
     */
    public static Bank.BankLevel buildBankLevel(ConfigurationSection levelSection, String bankName) {
        Bank.BankLevel bankLevel = new Bank.BankLevel();

        bankLevel.cost = BPFormatter.getStyledBigDecimal(levelSection.getString("Cost"));

        String capacity = levelSection.getString("Capacity");
        bankLevel.capacity = capacity == null ? ConfigValues.getMaxBankCapacity() : BPFormatter.getStyledBigDecimal(capacity);

        String interest = levelSection.getString("Interest");
        bankLevel.interest = interest == null ? ConfigValues.getInterestRate() : BPFormatter.getStyledBigDecimal(interest.replace("%", ""));

        String offlineInterest = levelSection.getString("Offline-Interest");
        bankLevel.offlineInterest = offlineInterest == null ? ConfigValues.getOfflineInterestRate() : BPFormatter.getStyledBigDecimal(offlineInterest.replace("%", ""));

        String afkInterest = levelSection.getString("Afk-Interest") == null ? levelSection.getString("AFK-Interest") : levelSection.getString("Afk-Interest");
        bankLevel.afkInterest = afkInterest == null ? ConfigValues.getAfkInterestRate() : BPFormatter.getStyledBigDecimal(afkInterest.replace("%", ""));

        String maxInterestAmount = levelSection.getString("Max-Interest-Amount");
        bankLevel.maxInterestAmount = maxInterestAmount == null ? ConfigValues.getInterestMaxAmount() : BPFormatter.getStyledBigDecimal(maxInterestAmount);

        List<ItemStack> requiredItems = new ArrayList<>();
        String requiredItemsString = levelSection.getString("Required-Items");
        if (requiredItemsString != null && !requiredItemsString.isEmpty()) {

            List<String> configItems = new ArrayList<>();
            if (!requiredItemsString.contains(",")) configItems.add(requiredItemsString);
            else configItems.addAll(Arrays.asList(requiredItemsString.split(",")));

            for (String splitItem : configItems) {
                if (!splitItem.contains("-")) {
                    try {
                        requiredItems.add(new ItemStack(Material.valueOf(splitItem)));
                    } catch (IllegalArgumentException e) {
                        BPLogger.warn("The bank \"" + bankName + "\" contains an invalid item in the \"Required-Items\" path at level *" + levelSection.getName() + ".");
                    }
                } else {
                    String[] split = splitItem.split("-");
                    ItemStack item;
                    try {
                        item = new ItemStack(Material.valueOf(split[0]));
                    } catch (IllegalArgumentException e) {
                        BPLogger.warn("The bank \"" + bankName + "\" contains an invalid item in the \"Required-Items\" path at level *" + levelSection.getName() + ".");
                        continue;
                    }
                    int amount = 1;
                    try {
                        amount = Integer.parseInt(split[1]);
                    } catch (NumberFormatException e) {
                        BPLogger.warn("The bank \"" + bankName + "\" contains an invalid number in the \"Required-Items\" path at level *" + levelSection.getName() + ".");
                    }

                    item.setAmount(amount);
                    requiredItems.add(item);
                }
            }
        }

        bankLevel.requiredItems = requiredItems;
        bankLevel.removeRequiredItems = levelSection.getBoolean("Remove-Required-Items");

        List<String> limiter = levelSection.getStringList("Interest-Limiter");
        bankLevel.interestLimiter = limiter.isEmpty() ? ConfigValues.getInterestLimiter() : limiter;

        return bankLevel;
    }

    /**
     * Automatically get the itemstack with all attributes from the item section.
     *
     * @param itemSection The item section in the config.
     * @return An ItemStack.
     */
    public static ItemStack getItemFromSection(ConfigurationSection itemSection) {
        if (itemSection == null) return BPItems.UNKNOWN_ITEM.clone();

        ItemStack guiItem = BPItems.createItemStack(itemSection.getString("Material"));

        int amount = itemSection.getInt("amount");
        if (amount > 1) guiItem.setAmount(amount);

        ItemMeta meta = guiItem.getItemMeta();

        String displayname = itemSection.getString("Displayname");
        meta.setDisplayName(BPChat.color(displayname == null ? "&c&l*CANNOT FIND DISPLAYNAME*" : displayname));

        List<String> lore = new ArrayList<>();
        for (String lines : itemSection.getStringList("Lore")) lore.add(BPChat.color(lines));
        meta.setLore(lore);

        if (itemSection.getBoolean("Glowing")) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        int modelData = itemSection.getInt("CustomModelData");
        if (modelData > 0) {
            try {
                meta.setCustomModelData(modelData);
            } catch (NoSuchMethodError e) {
                BPLogger.warn("Cannot set custom model data to the item: \"" + displayname + "\"&e. Custom model data is only available on 1.14.4+ servers!");
            }
        }

        guiItem.setItemMeta(meta);
        return guiItem;
    }

    private static BigDecimal getLimitedInterest(Bank bank, OfflinePlayer p, int level, BigDecimal fallBack) {
        for (String limiter : getInterestLimiter(bank, level)) {
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

            BigDecimal balance = bank.getBankEconomy().getBankBalance(p);
            if (fromNumber.doubleValue() <= balance.doubleValue() && toNumber.doubleValue() >= balance.doubleValue())
                return interestRate;
        }
        return fallBack;
    }
}