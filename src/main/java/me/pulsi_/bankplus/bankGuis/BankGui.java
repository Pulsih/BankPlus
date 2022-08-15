package me.pulsi_.bankplus.bankGuis;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

/**
 * Class that represents a bank.
 */
public class BankGui {

    private final String identifier;
    private final File bankFile;
    private final FileConfiguration bankConfig;
    private final String title;
    private final int size, updateDelay;
    private final String fillerMaterial;
    private final boolean hasFiller, fillerGlowing;
    private ItemStack[] content;
    private String permission;
    private ConfigurationSection items, upgrades, banksListGuiItems, settings;

    public BankGui(String identifier) {
        this(identifier, null);
    }

    public BankGui(String identifier, String title, int size, int updateDelay, ItemStack[] content) {
        this.identifier = identifier;
        this.bankFile = null;
        this.bankConfig = null;
        this.title = title;
        this.size = size;
        this.updateDelay = updateDelay;
        this.hasFiller = false;
        this.fillerMaterial = null;
        this.fillerGlowing = false;
        this.content = content;
    }

    public BankGui(String identifier, ItemStack[] content) {
        this.identifier = identifier;
        File file = new File(BankPlus.instance().getDataFolder(), "banks" + File.separator + identifier + ".yml");
        if (!file.exists()) {
            BPLogger.error("The bank named \"" + identifier + "\" does not exist!");
            this.bankFile = null;
            this.bankConfig = null;
            this.title = "&c&l* TITLE NOT FOUND *";
            this.size = 0;
            this.updateDelay = 0;
            this.hasFiller = false;
            this.fillerMaterial = null;
            this.fillerGlowing = false;
            this.content = null;
            return;
        }
        this.bankFile = file;

        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            BPLogger.error("An error has occurred while loading a bank file: " + e.getMessage());
        }
        this.bankConfig = config;

        String title = bankConfig.getString("Title");
        this.title = title == null ? "&c&l* TITLE NOT FOUND *" : title;
        this.size = bankConfig.getInt("Lines");
        this.updateDelay = bankConfig.getInt("Update-Delay");
        this.hasFiller = bankConfig.getBoolean("Filler.Enabled");
        this.fillerMaterial = bankConfig.getString("Filler.Material");
        this.fillerGlowing = bankConfig.getBoolean("Filler.Glowing");

        this.content = content;
        this.permission = bankConfig.getString("Settings.Permission");
        this.items = bankConfig.getConfigurationSection("Items");
        this.upgrades = bankConfig.getConfigurationSection("Upgrades");
        this.banksListGuiItems = bankConfig.getConfigurationSection("Settings.BanksGuiItem");
        this.settings = bankConfig.getConfigurationSection("Settings");
    }

    public String getIdentifier() {
        return identifier;
    }

    public File getBankFile() {
        return bankFile;
    }

    public FileConfiguration getBankConfig() {
        return bankConfig;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        if (size < 2) return 9;
        switch (size) {
            case 2:
                return 18;
            case 3:
                return 27;
            case 4:
                return 36;
            case 5:
                return 45;
            default:
                return 54;
        }
    }

    public int getUpdateDelay() {
        return updateDelay;
    }

    public String getFillerMaterial() {
        return fillerMaterial;
    }

    public boolean hasFiller() {
        return hasFiller;
    }

    public boolean isFillerGlowing() {
        return fillerGlowing;
    }

    public ItemStack[] getContent() {
        return content;
    }

    public String getPermission() {
        return permission;
    }
    public ConfigurationSection getItems() {
        return items;
    }

    public ConfigurationSection getUpgrades() {
        return upgrades;
    }

    public ConfigurationSection getBanksListGuiItems() {
        return banksListGuiItems;
    }

    public ConfigurationSection getSettings() {
        return settings;
    }

    public void setContent(ItemStack[] content) {
        this.content = content;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setItems(ConfigurationSection items) {
        this.items = items;
    }

    public void setUpgrades(ConfigurationSection upgrades) {
        this.upgrades = upgrades;
    }

    public void setBanksListGuiItems(ConfigurationSection banksListGuiItems) {
        this.banksListGuiItems = banksListGuiItems;
    }

    public void setSettings(ConfigurationSection settings) {
        this.settings = settings;
    }
}