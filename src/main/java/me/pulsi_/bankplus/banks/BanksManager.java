package me.pulsi_.bankplus.banks;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayerFilesUtils;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class BanksManager {
    
    private final Bank bank;

    public BanksManager() {
        this.bank = null;
    }
    
    public BanksManager(String bankName) {
        this.bank = BankPlus.instance().getBanks().get(bankName);
    }
    
    public void loadBanks() {
        HashMap<String, Bank> banks = BankPlus.instance().getBanks();
        File file = new File(BankPlus.instance().getDataFolder(), "banks");
        File[] files = file.listFiles();

        banks.clear();
        List<File> bankFiles;

        if (files == null || files.length == 0) {
            File defaultBankFile = new File(BankPlus.instance().getDataFolder(), "banks" + File.separator + Values.CONFIG.getMainGuiName() + ".yml");
            if (!defaultBankFile.exists()) {
                BankPlus.instance().saveResource("banks" + File.separator + "bankplus_main_gui_base_file.yml", false);
                File newMainFile = new File(BankPlus.instance().getDataFolder(), "banks" + File.separator + "bankplus_main_gui_base_file.yml");
                newMainFile.renameTo(defaultBankFile);
            }
            bankFiles = new ArrayList<>(Collections.singletonList(defaultBankFile));
        } else {
            bankFiles = new ArrayList<>(Arrays.asList(files));

            boolean theresMainGui = false;
            for (File bankFile : bankFiles) {
                if (!bankFile.getName().replace(".yml", "").equals(Values.CONFIG.getMainGuiName())) continue;
                theresMainGui = true;
                break;
            }
            if (!theresMainGui) {
                BPLogger.warn("The main gui was missing, creating the file...");
                File defaultBankFile = new File(BankPlus.instance().getDataFolder(), "banks" + File.separator + Values.CONFIG.getMainGuiName() + ".yml");
                defaultBankFile.getParentFile().mkdir();

                BankPlus.instance().saveResource("banks" + File.separator + "bankplus_main_gui_base_file.yml", false);
                File newMainFile = new File(BankPlus.instance().getDataFolder(), "banks" + File.separator + "bankplus_main_gui_base_file.yml");
                newMainFile.renameTo(defaultBankFile);

                bankFiles.add(defaultBankFile);
            }
        }

        for (File bankFile : bankFiles) {
            FileConfiguration bankConfig = new YamlConfiguration();
            try {
                bankConfig.load(bankFile);
            } catch (IOException | InvalidConfigurationException e) {
                BPLogger.error("An error has occurred while loading a bank file: " + e.getMessage());
            }

            String identifier = bankFile.getName().replace(".yml", "");
            Bank bank = new Bank(identifier);

            ItemStack[] content = null;
            ConfigurationSection items = bankConfig.getConfigurationSection("Items");
            if (items != null) {
                Inventory inv = Bukkit.createInventory(null, bank.getSize(), "");
                for (String item : items.getKeys(false)) {
                    ConfigurationSection itemValues = items.getConfigurationSection(item);
                    if (itemValues == null) continue;

                    String material = itemValues.getString("Material");
                    if (material == null) continue;

                    ItemStack guiItem;
                    if (material.startsWith("HEAD")) guiItem = ItemCreator.getHead(itemValues);
                    else guiItem = ItemCreator.createItemStack(itemValues);
                    if (guiItem == null) continue;

                    ItemMeta meta = guiItem.getItemMeta();
                    String displayname = itemValues.getString("Displayname");
                    List<String> lore = new ArrayList<>();
                    for (String lines : itemValues.getStringList("Lore")) lore.add(BPChat.color(lines));

                    meta.setDisplayName(BPChat.color(displayname == null ? "&c&l*CANNOT FIND DISPLAYNAME*" : displayname));
                    meta.setLore(lore);

                    if (itemValues.getBoolean("Glowing")) {
                        meta.addEnchant(Enchantment.DURABILITY, 1, true);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }

                    int modelData = itemValues.getInt("CustomModelData");
                    if (modelData > 0) {
                        try {
                            meta.setCustomModelData(modelData);
                        } catch (NoSuchMethodError e) {
                            BPLogger.warn("Cannot set custom model data to the item: \"" + displayname + "\"&e. Custom model data is only available on 1.14.4+ servers!");
                        }
                    }
                    guiItem.setItemMeta(meta);

                    try {
                        int slot = itemValues.getInt("Slot") - 1;
                        inv.setItem(slot, guiItem);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        inv.addItem(guiItem);
                    }
                }
                if (bank.hasFiller())
                    for (int i = 0; i < inv.getSize(); i++)
                        if (inv.getItem(i) == null) inv.setItem(i, ItemCreator.getFiller(bank));
                content = inv.getContents();
            }

            bank.setContent(content);
            banks.put(identifier, bank);
        }
        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) new BanksListGui().loadMultipleBanksGui();
    }

    public boolean exist(String bankName) {
        return BankPlus.instance().getBanks().containsKey(bankName);
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

    public ConfigurationSection getItems() {
        return bank.getItems();
    }

    public boolean hasSettings() {
        return bank.getSettings() != null;
    }

    public boolean hasPermission() {
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

    public BigDecimal getCapacity(Player p) {
        if (!hasUpgrades()) return Values.CONFIG.getMaxBankCapacity();

        FileConfiguration config = BankPlusPlayerFilesUtils.getPlayerConfig(p);
        int level = Math.max(config.getInt("Banks." + bank.getIdentifier() + ".Level"), 1);
        String capacity = getUpgrades().getString(level + ".Capacity");
        return new BigDecimal(capacity == null ? Values.CONFIG.getMaxBankCapacity().toString() : capacity);
    }

    public BigDecimal getCapacity(OfflinePlayer p) {
        if (!hasUpgrades()) return Values.CONFIG.getMaxBankCapacity();

        FileConfiguration config = BankPlusPlayerFilesUtils.getPlayerConfig(p);
        int level = Math.max(config.getInt("Banks." + bank.getIdentifier() + ".Level"), 1);
        String capacity = getUpgrades().getString(level + ".Capacity");
        return new BigDecimal(capacity == null ? Values.CONFIG.getMaxBankCapacity().toString() : capacity);
    }

    public BigDecimal getLevelCost(int level) {
        if (!hasUpgrades()) return new BigDecimal(0);

        String cost = getUpgrades().getString(level + ".Cost");
        return new BigDecimal(cost == null ? "0" : cost);
    }

    public int getLevel(Player p) {
        FileConfiguration config = BankPlusPlayerFilesUtils.getPlayerConfig(p);
        return Math.max(config.getInt("Banks." + bank.getIdentifier() + ".Level"), 1);
    }

    public List<String> getLevels() {
        List<String> levels = new ArrayList<>();
        if (!hasUpgrades()) {
            levels.add("1");
            return levels;
        }

        levels.addAll(getUpgrades().getKeys(false));
        return levels;
    }

    public boolean hasNextLevel(Player p) {
        ConfigurationSection section = getUpgrades();
        if (section == null) return false;

        return section.getConfigurationSection(String.valueOf(getLevel(p) + 1)) != null;
    }

    public boolean hasNextLevel(int currentLevel) {
        ConfigurationSection section = getUpgrades();
        if (section == null) return false;

        return section.getConfigurationSection(String.valueOf(currentLevel + 1)) != null;
    }

    public List<String> getAvailableBanks(Player p) {
        List<String> availableBanks = new ArrayList<>();
        for (String bankName : BankPlus.instance().getBanks().keySet()) if (isAvailable(p)) availableBanks.add(bankName);
        return availableBanks;
    }

    public List<String> getAvailableBanks(OfflinePlayer p) {
        List<String> availableBanks = new ArrayList<>();
        for (String bankName : BankPlus.instance().getBanks().keySet()) if (isAvailable(p)) availableBanks.add(bankName);
        return availableBanks;
    }

    public boolean isAvailable(Player p) {
        if (!hasPermission()) return true;
        else return p.hasPermission(getPermission());
    }

    public boolean isAvailable(OfflinePlayer p) {
        if (!hasPermission()) return true;
        else {
            String wName = Bukkit.getWorlds().get(0).getName();
            return BankPlus.instance().getPermissions().playerHas(wName, p, getPermission());
        }
    }

    public void upgradeBank(Player p) {
        if (!hasNextLevel(p)) {
            MessageManager.send(p, "Bank-Max-Level");
            return;
        }
        BigDecimal balance;

        SingleEconomyManager singleEconomyManager = new SingleEconomyManager(p);
        MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) balance = multiEconomyManager.getBankBalance();
        else balance = singleEconomyManager.getBankBalance();

        int level = getLevel(p);
        BigDecimal cost = getLevelCost(level + 1);
        if (balance.doubleValue() < cost.doubleValue()) {
            MessageManager.send(p, "Insufficient-Money");
            return;
        }

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) multiEconomyManager.removeBankBalance(cost, bank.getIdentifier());
        else singleEconomyManager.removeBankBalance(cost);

        BankPlusPlayerFilesUtils.getPlayerConfig(p).set("Banks." + bank.getIdentifier() + ".Level", level + 1);
        BankPlusPlayerFilesUtils.savePlayerFile(p, true);
        MessageManager.send(p, "Bank-Upgraded");
    }

    public Bank getBank() {
        return bank;
    }
}