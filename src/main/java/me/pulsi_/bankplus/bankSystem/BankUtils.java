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

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.*;

/**
 * Class containing tons of useful methods to manage banks and retrieve useful information.
 */
public class BankUtils {

    public static final String MATERIAL_FIELD = "Material";
    public static final String AMOUNT_FIELD = "Amount";
    public static final String DISPLAYNAME_FIELD = "Displayname";
    public static final String LORE_FIELD = "Lore";
    public static final String ITEM_FLAGS_FIELD = "ItemFlags";
    public static final String GLOWING_FIELD = "Glowing";
    public static final String CUSTOM_MODEL_DATA_FIELD = "CustomModelData";
    public static final String COST_FIELD = "Cost";
    public static final String CAPACITY_FIELD = "Capacity";
    public static final String INTEREST_FIELD = "Interest";
    public static final String OFFLINE_INTEREST_FIELD = "Offline-Interest";
    public static final String AFK_INTEREST_FIELD = "AFK-Interest";
    public static final String MAX_INTEREST_AMOUNT_FIELD = "Max-Interest-Amount";
    public static final String REQUIRED_ITEMS_FIELD = "Required-Items";
    public static final String REMOVE_REQUIRED_ITEMS_FIELD = "Remove-Required-Items";
    public static final String INTEREST_LIMITER_FIELD = "Interest-Limiter";

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
     *
     * @param bank The bank.
     * @return true if it is registered, false otherwise.
     */
    public static boolean exist(Bank bank) {
        return exist(bank, null);
    }

    /**
     * Checks if the selected bank is registered.
     *
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
     *
     * @param bankName The bank name.
     * @return true if it is registered, false otherwise.
     */
    public static boolean exist(String bankName) {
        return exist(bankName, null);
    }

    /**
     * Checks if the selected bank name is registered, and automatically alert the command sender, if specified, its non-existence.
     *
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
     *
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
     * @return An HashMap with K = Required Item Name. V = ItemStack.
     */
    public static HashMap<String, ItemStack> getRequiredItems(Bank bank, int level) {
        Bank.BankLevel bankLevel = bank.getBankLevel(level);
        return bankLevel == null ? new HashMap<>() : bankLevel.requiredItems;
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
        if (bank == null) return levels;

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
     * Check if the selected bank has the specified level.
     *
     * @param bank  The bank.
     * @param level The level to check.
     * @return true if the bank has that level, false otherwise.
     */
    public static boolean hasLevel(Bank bank, String level) {
        return bank != null && getLevels(bank).contains(level);
    }

    /**
     * Check if the selected bank has the specified level.
     *
     * @param bank  The bank.
     * @param level The level to check.
     * @return true if the bank has that level, false otherwise.
     */
    public static boolean hasLevel(Bank bank, int level) {
        return hasLevel(bank, level + "");
    }

    /**
     * Check if the selected bank has a level next the one specified.
     *
     * @param bank         The bank.
     * @param currentLevel The level.
     * @return true if the bank has another level, false otherwise.
     */
    public static boolean hasNextLevel(Bank bank, int currentLevel) {
        return hasLevel(bank, currentLevel + 1);
    }

    /**
     * Check if the selected bank has a level next the player current bank level.
     *
     * @param bank The bank.
     * @param p    The player.
     * @return true if the bank has another level, false otherwise.
     */
    public static boolean hasNextLevel(Bank bank, OfflinePlayer p) {
        return hasNextLevel(bank, getCurrentLevel(bank, p));
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

        // variable to make sure to remove the items only when there are
        // some and not removing possible items from the player's inventory.
        boolean canRemoveSafely = false;
        Collection<ItemStack> requiredItems = getRequiredItems(bank, nextLevel).values();
        if (!requiredItems.isEmpty()) {
            for (ItemStack requiredItem : requiredItems) {
                int amount = requiredItem.getAmount();
                int playerAmount = 0;

                boolean hasItem = false;
                for (ItemStack item : p.getInventory().getContents()) {
                    if (item == null || !isRequiredItem(requiredItem, item)) continue;
                    playerAmount += item.getAmount();

                    if (playerAmount < amount) continue;
                    hasItem = true;
                    break;
                }
                if (!hasItem) {
                    BPMessages.send(p, "Insufficient-Items", "%items%$" + BPUtils.getRequiredItemsFormatted(requiredItems));
                    return;
                }
            }
            canRemoveSafely = true;
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

        if (isRemovingRequiredItems(bank, nextLevel) && canRemoveSafely)
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

        bankLevel.cost = BPFormatter.getStyledBigDecimal(levelSection.getString(COST_FIELD));

        String capacity = levelSection.getString(CAPACITY_FIELD);
        bankLevel.capacity = capacity == null ? ConfigValues.getMaxBankCapacity() : BPFormatter.getStyledBigDecimal(capacity);

        String interest = levelSection.getString(INTEREST_FIELD);
        bankLevel.interest = interest == null ? ConfigValues.getInterestRate() : BPFormatter.getStyledBigDecimal(interest.replace("%", ""));

        String offlineInterest = levelSection.getString(OFFLINE_INTEREST_FIELD);
        bankLevel.offlineInterest = offlineInterest == null ? ConfigValues.getOfflineInterestRate() : BPFormatter.getStyledBigDecimal(offlineInterest.replace("%", ""));

        String afkInterest = levelSection.getString(AFK_INTEREST_FIELD);
        bankLevel.afkInterest = afkInterest == null ? ConfigValues.getAfkInterestRate() : BPFormatter.getStyledBigDecimal(afkInterest.replace("%", ""));

        String maxInterestAmount = levelSection.getString(MAX_INTEREST_AMOUNT_FIELD);
        bankLevel.maxInterestAmount = maxInterestAmount == null ? ConfigValues.getInterestMaxAmount() : BPFormatter.getStyledBigDecimal(maxInterestAmount);

        bankLevel.requiredItems = retrieveRequiredItems(levelSection, bankName);

        bankLevel.removeRequiredItems = levelSection.getBoolean(REMOVE_REQUIRED_ITEMS_FIELD);

        List<String> limiter = levelSection.getStringList(INTEREST_LIMITER_FIELD);
        bankLevel.interestLimiter = limiter.isEmpty() ? ConfigValues.getInterestLimiter() : limiter;

        return bankLevel;
    }

    /**
     * Get the different level lores of the specified item section.
     *
     * @param itemSection The item section.
     * @return An HashMap containing K = Lore Level. V = Lore List.
     */
    public static HashMap<Integer, List<String>> getLevelLore(ConfigurationSection itemSection) {
        HashMap<Integer, List<String>> lore = new HashMap<>();

        List<String> configLore = itemSection.getStringList(LORE_FIELD);
        if (!configLore.isEmpty()) lore.put(0, configLore);
        else { // If its empty and the path exist, it contains level lore.
            ConfigurationSection loreSection = itemSection.getConfigurationSection(LORE_FIELD);
            if (loreSection != null) {
                List<String> defaultLore = loreSection.getStringList("Default");
                lore.put(0, defaultLore);

                for (String level : loreSection.getKeys(false)) {
                    if (level.equalsIgnoreCase("default") || BPUtils.isInvalidNumber(level)) continue;

                    List<String> levelLore = loreSection.getStringList(level);
                    lore.put(Integer.parseInt(level), levelLore);
                }
            }
        }
        return lore;
    }

    /**
     * Automatically get the item stack with all attributes from the item section.
     *
     * @param itemSection The item section in the config.
     * @return An ItemStack.
     */
    public static ItemStack getItemStackFromSection(ConfigurationSection itemSection) {
        if (itemSection == null) return BPItems.UNKNOWN_ITEM.clone();

        ItemStack guiItem = BPItems.createItemStack(itemSection.getString(MATERIAL_FIELD));

        int amount = itemSection.getInt(AMOUNT_FIELD);
        if (amount > 1) guiItem.setAmount(amount);

        ItemMeta meta = guiItem.getItemMeta();
        if (meta == null) return guiItem;

        String displayname = itemSection.getString(DISPLAYNAME_FIELD);
        meta.setDisplayName(BPChat.color(displayname == null ? "&c&l*CANNOT FIND DISPLAYNAME*" : displayname));

        List<String> lore = new ArrayList<>();
        for (String lines : itemSection.getStringList(LORE_FIELD)) lore.add(BPChat.color(lines));
        meta.setLore(lore);

        if (itemSection.getBoolean(GLOWING_FIELD)) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        for (String flag : itemSection.getStringList(ITEM_FLAGS_FIELD)) {
            try {
                meta.addItemFlags(ItemFlag.valueOf(flag));
            } catch (IllegalArgumentException e) {
                BPLogger.warn("Could not set item flag \"" + flag + "\" to item \"" + itemSection + "\" because it's not a valid item flag.");
            }
        }

        int modelData = itemSection.getInt(CUSTOM_MODEL_DATA_FIELD);
        if (modelData > 0) {
            try {
                meta.setCustomModelData(modelData);
            } catch (NoSuchMethodError e) { // Do that to add support for older minecraft versions.
                BPLogger.warn("Cannot set custom model data to the item: \"" + displayname + "\"&e. Custom model data is only available on 1.14.4+ servers!");
            }
        }

        guiItem.setItemMeta(meta);
        return guiItem;
    }

    /**
     * Method to retrieve required items.
     * Both from short (ItemType-Amount) and advanced method (ConfigurationSection).
     *
     * @param levelSection The section of the bank level where to get the required items.
     * @param bankName     The name of the bank where to retrieve the items (used for debugging).
     * @return A list of required items.
     */
    public static @Nonnull HashMap<String, ItemStack> retrieveRequiredItems(ConfigurationSection levelSection, String bankName) {
        HashMap<String, ItemStack> requiredItems = new HashMap<>();

        ConfigurationSection requiredItemsSection = levelSection.getConfigurationSection(REQUIRED_ITEMS_FIELD);
        if (requiredItemsSection != null) {
            for (String itemName : requiredItemsSection.getKeys(false))
                requiredItems.put(itemName, getItemStackFromSection(requiredItemsSection.getConfigurationSection(itemName)));
        } else {
            String requiredItemsString = levelSection.getString(REQUIRED_ITEMS_FIELD);
            if (requiredItemsString == null || requiredItemsString.isEmpty()) return requiredItems;

            List<String> items = new ArrayList<>();
            if (!requiredItemsString.contains(",")) items.add(requiredItemsString);
            else items.addAll(Arrays.asList(requiredItemsString.split(",")));

            for (String itemID : items) {
                if (!itemID.contains("-")) {
                    try {
                        requiredItems.put(itemID, new ItemStack(Material.valueOf(itemID)));
                    } catch (IllegalArgumentException e) {
                        BPLogger.warn("The bank \"" + bankName + "\" contains an invalid item in the \"Required-Items\" path at level *" + levelSection.getName() + ".");
                    }
                } else {
                    String[] split = itemID.split("-");
                    ItemStack item;
                    String id;
                    try {
                        Material material = Material.valueOf(split[0]);
                        id = String.valueOf(material);
                        item = new ItemStack(material);
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
                    requiredItems.put(id, item);
                }
            }
        }
        return requiredItems;
    }

    /**
     * Check if the item to check is equals to the required item.
     * The items must not be swapped due to its particular calculations.
     *
     * @param requiredItem The base required item.
     * @param itemToCheck  The item to check.
     * @return true if the checked item is a required item.
     */
    public static boolean isRequiredItem(ItemStack requiredItem, ItemStack itemToCheck) {
        Material requiredMaterial = requiredItem.getType(), givenMaterial = itemToCheck.getType();
        if (!requiredMaterial.equals(givenMaterial)) return false;

        ItemMeta requiredMeta = requiredItem.getItemMeta(), givenMeta = itemToCheck.getItemMeta();
        if (requiredMeta != null) {
            if (requiredMeta.hasDisplayName()) {
                String requiredDisplayname = requiredMeta.getDisplayName(), givenDisplayname = givenMeta.getDisplayName();
                if (!requiredDisplayname.equals(givenDisplayname)) return false;
            }

            if (requiredMeta.hasLore()) {
                List<String> requiredLore = requiredMeta.getLore(), givenLore = givenMeta.getLore();
                if (!requiredLore.equals(givenLore)) return false;
            }

            try {
                int requiredData = requiredMeta.getCustomModelData(), givenData = givenMeta.getCustomModelData();
                if (requiredData != givenData) return false;
            } catch (NoSuchMethodError e) {
            }
        }
        return true;
    }

    /**
     * Method to simplify the calculation of the limited interest.
     *
     * @param bank     The bank where the interest is calculated.
     * @param p        The player.
     * @param level    The level to check.
     * @param fallBack The default interest in case something goes wrong.
     * @return An interest rate limited.
     */
    private static BigDecimal getLimitedInterest(Bank bank, OfflinePlayer p, int level, BigDecimal fallBack) {
        BigDecimal balance = bank.getBankEconomy().getBankBalance(p);
        for (String limiter : getInterestLimiter(bank, level)) {
            if (!limiter.contains(":")) continue;

            String[] split1 = limiter.split(":");
            if (BPUtils.isInvalidNumber(split1[1])) continue;

            String[] split2 = split1[0].split("-");
            if (BPUtils.isInvalidNumber(split2[0]) || BPUtils.isInvalidNumber(split2[1])) continue;

            String interest = split1[1].replace("%", ""), from = split2[0], to = split2[1];
            BigDecimal interestRate = new BigDecimal(interest), fromNumber = new BigDecimal(from), toNumber = new BigDecimal(to);

            if (fromNumber.compareTo(toNumber) > 0) {
                BigDecimal temp = fromNumber;
                fromNumber = toNumber;
                toNumber = temp;
            }

            if (fromNumber.compareTo(balance) <= 0 && toNumber.compareTo(balance) >= 0) return interestRate;
        }
        return fallBack;
    }
}