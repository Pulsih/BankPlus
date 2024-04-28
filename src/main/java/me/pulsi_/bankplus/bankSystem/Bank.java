package me.pulsi_.bankplus.bankSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.*;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Bank {

    private final String identifier;
    private final BPEconomy bankEconomy;
    // HashMap to keep track of the bank levels.
    private final HashMap<Integer, BankLevel> bankLevels = new HashMap<>();
    // Pseudo inventory content to keep track of items, lore and actions. K = Inventory slot V = BankItem
    private final HashMap<Integer, BankItem> bankItems = new HashMap<>();

    private String title = "&c&l * TITLE NOT FOUND *";
    private int size, updateDelay;
    private String fillerMaterial, accessPermission;
    private boolean giveInterestIfNotAvailable, fillerEnabled, fillerGlowing;
    private ConfigurationSection banksListGuiItems;

    public Bank(String identifier) {
        this.identifier = identifier;
        bankEconomy = new BPEconomy(identifier);
    }

    public String getIdentifier() {
        return identifier;
    }

    public BPEconomy getBankEconomy() {
        return bankEconomy;
    }

    public HashMap<Integer, BankLevel> getBankLevels() {
        return bankLevels;
    }

    public HashMap<Integer, BankItem> getBankItems() {
        return bankItems;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return Math.max(9, Math.min(54, size * 9));
    }

    public int getUpdateDelay() {
        return updateDelay;
    }

    public String getFillerMaterial() {
        return fillerMaterial;
    }

    public boolean isGiveInterestIfNotAvailable() {
        return giveInterestIfNotAvailable;
    }

    public boolean isFillerEnabled() {
        return fillerEnabled;
    }

    public boolean isFillerGlowing() {
        return fillerGlowing;
    }

    public String getAccessPermission() {
        return accessPermission;
    }

    public ConfigurationSection getBanksListGuiItems() {
        return banksListGuiItems;
    }

    public void setTitle(String title) {
        if (title != null && !title.isEmpty()) this.title = title;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setUpdateDelay(int updateDelay) {
        this.updateDelay = updateDelay;
    }

    public void setFillerMaterial(String fillerMaterial) {
        this.fillerMaterial = fillerMaterial;
    }

    public void setGiveInterestIfNotAvailable(boolean giveInterestIfNotAvailable) {
        this.giveInterestIfNotAvailable = giveInterestIfNotAvailable;
    }

    public void setFillerEnabled(boolean fillerEnabled) {
        this.fillerEnabled = fillerEnabled;
    }

    public void setFillerGlowing(boolean fillerGlowing) {
        this.fillerGlowing = fillerGlowing;
    }

    public void setAccessPermission(String accessPermission) {
        this.accessPermission = accessPermission;
    }

    public void setBanksListGuiItems(ConfigurationSection banksListGuiItems) {
        this.banksListGuiItems = banksListGuiItems;
    }

    public BankLevel getBankLevel(int level) {
        return bankLevels.get(level);
    }

    public void setBankItem(int slot, BankItem item) {
        if (item != null) bankItems.put(slot, item);
    }

    public void openGuiBank(Player p) {
        openGuiBank(p, false);
    }

    public void openGuiBank(Player p, boolean bypass) {
        if (!bypass) {
            if (Values.CONFIG.isNeedOpenPermissionToOpen() && !BPUtils.hasPermission(p, "bankplus.open")) return;

            if (!BankUtils.isAvailable(identifier, p)) {
                BPMessages.send(p, "Cannot-Access-Bank");
                return;
            }
        }

        BPPlayer player = PlayerRegistry.get(p);
        if (player == null) {
            PlayerRegistry.loadPlayer(p);
            player = PlayerRegistry.get(p);
        }

        if (identifier.equals(BankListGui.multipleBanksGuiID)) {
            BankListGui.openMultipleBanksGui(p);
            return;
        }

        if (!BankUtils.exist(identifier)) {
            BPMessages.send(p, "Invalid-Bank");
            return;
        }

        BukkitTask updating = player.getBankUpdatingTask();
        if (updating != null) updating.cancel();

        Bank baseBank = BankUtils.getBank(identifier);
        String title = baseBank.getTitle();
        if (!BankPlus.INSTANCE().isPlaceholderApiHooked()) title = BPChat.color(title);
        else title = PlaceholderAPI.setPlaceholders(p, BPChat.color(title));

        Inventory bankInventory = Bukkit.createInventory(new BankHolder(), baseBank.getSize(), title);
        placeContent(bankItems, bankInventory, p);
        updateBankGuiMeta(bankInventory, p);

        long delay = baseBank.getUpdateDelay();
        if (delay >= 0) player.setBankUpdatingTask(Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE(), () -> updateBankGuiMeta(bankInventory, p), delay, delay));

        player.setOpenedBank(baseBank);
        BPUtils.playSound("PERSONAL", p);
        p.openInventory(bankInventory);
    }

    /**
     * Method to update every item meta in the bank inventory: lore, displayname and placeholders.
     * @param playerOpenedInventory The inventory opened by the player, should check if he has opened a bank.
     * @param p The player to update placeholders and lore.
     */
    public void updateBankGuiMeta(Inventory playerOpenedInventory, Player p) {
        for (int slot : bankItems.keySet()) updateBankGuiItemMeta(slot, playerOpenedInventory, p);
    }

    /**
     * Method to update a single item meta: lore, displayname and placeholders.
     * @param slot The slot of the item.
     */
    public void updateBankGuiItemMeta(int slot, Inventory playerOpenedInventory, Player p) {
        BankItem bankItem = bankItems.get(slot);

        ItemStack inventoryItem = playerOpenedInventory.getItem(slot);
        ItemMeta meta = inventoryItem.getItemMeta();

        List<String> actualLore = new ArrayList<>(), levelLore = bankItem.lore.get(BankUtils.getCurrentLevel(identifier, p));
        if (levelLore == null) levelLore = bankItem.lore.get(0);
        for (String line : levelLore) actualLore.add(BPChat.color(line));

        if (BankPlus.INSTANCE().isPlaceholderApiHooked()) {
            meta.setDisplayName(BPChat.color(PlaceholderAPI.setPlaceholders(p, bankItem.displayname)));
            meta.setLore(PlaceholderAPI.setPlaceholders(p, actualLore));
        } else {
            meta.setDisplayName(BPChat.color(bankItem.displayname));
            meta.setLore(actualLore);
        }
        inventoryItem.setItemMeta(meta);
    }

    public void updateBankSettings() {
        updateBankSettings(new File(BankPlus.INSTANCE().getDataFolder(), "banks" + File.separator + identifier + ".yml"));
    }

    /**
     * Update the bank settings, such as access permission, levels and other
     * @param file The bank file.
     */
    public void updateBankSettings(File file) {
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            BPLogger.warn(e, "Could not load \"" + identifier + "\" bank properties because it contains an invalid configuration!");
            return;
        }

        setAccessPermission(config.getString("Settings.Permission"));

        ConfigurationSection levels = config.getConfigurationSection("Levels");
        bankLevels.clear();

        if (levels != null) {
            for (String key : levels.getKeys(false)) {
                int level;
                try {
                    level = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    BPLogger.warn("The bank \"" + identifier + "\" contains an invalid level number! (" + key + ")");
                    continue;
                }

                ConfigurationSection levelSection = levels.getConfigurationSection(key);
                if (levelSection != null) bankLevels.put(level, BankUtils.buildBankLevel(levelSection, identifier));
            }
        }

        if (!Values.CONFIG.isGuiModuleEnabled()) return;

        setTitle(config.getString("Title"));
        setSize(config.getInt("Lines"));
        setUpdateDelay(config.getInt("Update-Delay"));
        setGiveInterestIfNotAvailable(config.getBoolean("Settings.Give-Interest-If-Not-Available"));
        setFillerEnabled(config.getBoolean("Filler.Enabled"));
        setFillerMaterial(config.getString("Filler.Material"));
        setFillerGlowing(config.getBoolean("Filler.Glowing"));

        setBanksListGuiItems(config.getConfigurationSection("Settings.BanksGuiItem"));

        ConfigurationSection items = config.getConfigurationSection("Items");
        if (items != null) {
            for (String item : items.getKeys(false)) {
                ConfigurationSection itemSection = items.getConfigurationSection(item);
                if (itemSection == null) continue;

                ItemStack itemStack = BankUtils.getItemFromSection(itemSection);
                Bank.BankItem bankItem = new Bank.BankItem();
                bankItem.item = itemStack;
                bankItem.material = itemSection.getString("Material");
                bankItem.displayname = itemSection.getString("Displayname");
                bankItem.actions = itemSection.getStringList("Actions");

                List<String> configLore = itemSection.getStringList("Lore");
                if (!configLore.isEmpty()) bankItem.lore.put(0, configLore);
                else {
                    ConfigurationSection loreSection = itemSection.getConfigurationSection("Lore");
                    if (loreSection != null) {
                        List<String> defaultLore = loreSection.getStringList("Default");
                        bankItem.lore.put(0, defaultLore);

                        for (String level : loreSection.getKeys(false)) {
                            if (level.equalsIgnoreCase("default") || BPUtils.isInvalidNumber(level)) continue;

                            List<String> levelLore = loreSection.getStringList(level);
                            bankItem.lore.put(Integer.parseInt(level), levelLore);
                        }
                    }
                }

                List<Integer> slots = itemSection.getIntegerList("Slot");
                if (slots.isEmpty()) setBankItem(itemSection.getInt("Slot") - 1, bankItem);
                else for (int slot : slots) setBankItem(slot - 1, bankItem);
            }
        }
    }

    /**
     * Method used to place all the bank items WITHOUT the lore.
     * @param items The bank items.
     * @param bankInventory The bank inventory.
     * @param p The player needed for possible skulls.
     */
    public void placeContent(HashMap<Integer, BankItem> items, Inventory bankInventory, Player p) {
        if (fillerEnabled) {
            ItemStack filler = BPItems.getFiller(this);
            int size = bankInventory.getSize();
            for (int i = 0; i < size; i++) bankInventory.setItem(i, filler);
        }

        for (int slot : items.keySet()) {
            BankItem item = items.get(slot);
            if (item.material.equalsIgnoreCase("head-%player%")) bankInventory.setItem(slot,  BPItems.getHead(p));
            else bankInventory.setItem(slot, items.get(slot).item);
        }
    }

    public static class BankLevel {
        BigDecimal cost, capacity, interest, offlineInterest;
        List<ItemStack> requiredItems;
        List<String> interestLimiter;
        boolean removeRequiredItems;
    }

    /**
     * Class used to keep track of item settings.
     * The ItemStack holds final settings such as material, item flags and enchantments.
     * The lore and displayname are the values that are going to be updated.
     */
    public static class BankItem {
        ItemStack item;
        String material, displayname;
        List<String> actions;

        final HashMap<Integer, List<String>> lore = new HashMap<>();
    }
}