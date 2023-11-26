package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public class Bank {

    private final String identifier;
    private final String title;
    private final int size, updateDelay;
    private final String fillerMaterial;
    private final boolean hasFiller, fillerGlowing;
    private ItemStack[] content;
    private String permission;
    private ConfigurationSection items, upgrades, banksListGuiItems, settings;

    public Bank(String identifier, String title, int size, int updateDelay, ItemStack[] content) {
        this.identifier = identifier;
        this.title = title;
        this.size = size;
        this.updateDelay = updateDelay;
        this.hasFiller = false;
        this.fillerMaterial = null;
        this.fillerGlowing = false;
        this.content = content;
        this.permission = null;
        this.items = null;
        this.upgrades = null;
        this.banksListGuiItems = null;
        this.settings = null;
    }

    public Bank(String identifier) {
        this.identifier = identifier;
        File file = new File(BankPlus.INSTANCE().getDataFolder(), "banks" + File.separator + identifier + ".yml");
        if (!file.exists()) {
            BPLogger.error("The bank named \"" + identifier + "\" does not exist!");
            this.title = "&c&l* TITLE NOT FOUND *";
            this.size = 0;
            this.updateDelay = 0;
            this.hasFiller = false;
            this.fillerMaterial = null;
            this.fillerGlowing = false;
            this.content = null;
            return;
        }

        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            BPLogger.error("An error has occurred while loading a bank file: " + e.getMessage());
        }

        String title = config.getString("Title");
        this.title = title == null ? "&c&l* TITLE NOT FOUND *" : title;
        this.size = config.getInt("Lines");
        this.updateDelay = config.getInt("Update-Delay");
        this.hasFiller = config.getBoolean("Filler.Enabled");
        this.fillerMaterial = config.getString("Filler.Material");
        this.fillerGlowing = config.getBoolean("Filler.Glowing");

        this.content = null;
        this.permission = config.getString("Settings.Permission");
        this.items = config.getConfigurationSection("Items");
        this.upgrades = config.getConfigurationSection("Upgrades");
        this.banksListGuiItems = config.getConfigurationSection("Settings.BanksGuiItem");
        this.settings = config.getConfigurationSection("Settings");
    }

    public String getIdentifier() {
        return identifier;
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

    public boolean hasFiller() {
        return hasFiller;
    }

    public boolean isFillerGlowing() {
        return fillerGlowing;
    }

    public ItemStack[] getContent() {
        return content;
    }

    public void setContent(ItemStack[] content) {
        this.content = content;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public ConfigurationSection getItems() {
        return items;
    }

    public void setItems(ConfigurationSection items) {
        this.items = items;
    }

    public ConfigurationSection getUpgrades() {
        return upgrades;
    }

    public void setUpgrades(ConfigurationSection upgrades) {
        this.upgrades = upgrades;
    }

    public ConfigurationSection getBanksGuiItemSection() {
        return banksListGuiItems;
    }

    public void setBanksListGuiItems(ConfigurationSection items) {
        this.banksListGuiItems = items;
    }

    public ConfigurationSection getSettings() {
        return settings;
    }

    public void setSettings(ConfigurationSection settings) {
        this.settings = settings;
    }
}