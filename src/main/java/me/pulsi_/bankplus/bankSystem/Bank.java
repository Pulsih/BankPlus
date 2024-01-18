package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.economy.BPEconomy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class Bank {

    private final String identifier;
    private final BPEconomy bankEconomy;
    private String title;
    private int size, updateDelay;
    private String fillerMaterial;
    private boolean hasFiller, fillerGlowing;
    private ItemStack[] content;
    private String accessPermission;
    private ConfigurationSection items, upgrades, banksListGuiItems;

    public Bank(String identifier) {
        this.identifier = identifier;
        bankEconomy = new BPEconomy();
    }

    public String getIdentifier() {
        return identifier;
    }

    public BPEconomy getBankEconomy() {
        return bankEconomy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "&c&l* TITLE NOT FOUND *" : title;
    }

    public int getSize() {
        return Math.max(9, Math.min(54, size * 9));
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getUpdateDelay() {
        return updateDelay;
    }

    public void setUpdateDelay(int updateDelay) {
        this.updateDelay = updateDelay;
    }

    public String getFillerMaterial() {
        return fillerMaterial;
    }

    public void setFillerMaterial(String fillerMaterial) {
        this.fillerMaterial = fillerMaterial;
    }

    public boolean hasFiller() {
        return hasFiller;
    }

    public void setHasFiller(boolean hasFiller) {
        this.hasFiller = hasFiller;
    }

    public boolean isFillerGlowing() {
        return fillerGlowing;
    }

    public void setFillerGlowing(boolean fillerGlowing) {
        this.fillerGlowing = fillerGlowing;
    }

    public ItemStack[] getContent() {
        return content;
    }

    public void setContent(ItemStack[] content) {
        this.content = content;
    }

    public String getAccessPermission() {
        return accessPermission;
    }

    public void setAccessPermission(String accessPermission) {
        this.accessPermission = accessPermission;
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
}