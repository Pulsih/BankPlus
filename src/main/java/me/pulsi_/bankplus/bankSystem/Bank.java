package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.economy.BPEconomy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class Bank {

    private final String identifier;
    private final BPEconomy bankEconomy;
    private final HashMap<Integer, BankLevel> bankLevels = new HashMap<>();

    private String title = "&c&l * TITLE NOT FOUND *";
    private int size, updateDelay;
    private String fillerMaterial, accessPermission;
    private boolean giveInterestIfNotAvailable, fillerEnabled, fillerGlowing;
    private ItemStack[] content;
    private ConfigurationSection items, banksListGuiItems;

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

    public ItemStack[] getContent() {
        return content;
    }

    public String getAccessPermission() {
        return accessPermission;
    }

    public ConfigurationSection getItems() {
        return items;
    }

    public ConfigurationSection getBanksListGuiItems() {
        return banksListGuiItems;
    }

    public void setTitle(String title) {
        if (title != null) this.title = title;
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

    public void setContent(ItemStack[] content) {
        this.content = content;
    }

    public void setAccessPermission(String accessPermission) {
        this.accessPermission = accessPermission;
    }

    public void setItems(ConfigurationSection items) {
        this.items = items;
    }

    public void setBanksListGuiItems(ConfigurationSection banksListGuiItems) {
        this.banksListGuiItems = banksListGuiItems;
    }

    public BankLevel getBankLevel(int level) {
        return bankLevels.get(level);
    }

    public HashMap<Integer, BankLevel> getBankLevels() {
        return bankLevels;
    }

    public static class BankLevel {
        BigDecimal cost, capacity, interest, offlineInterest;
        List<ItemStack> requiredItems;
        List<String> interestLimiter;
        boolean removeRequiredItems;
    }
}