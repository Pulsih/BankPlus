package me.pulsi_.bankplus.guis;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.AccountManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BanksManager {

    private static final HashMap<String, Object> guiValues = new HashMap<>();
    private static final List<String> bankNames = new ArrayList<>();

    public static void loadValues(String bankName) {
        FileConfiguration bankConfig = getConfig(bankName);
        if (bankConfig == null) {
            BPLogger.error("Cannot load the values for the bank \"" + bankName + "\"! (FileConfiguration null)");
            return;
        }

        guiValues.put(bankName + ".Title", bankConfig.getString("Title"));
        guiValues.put(bankName + ".Lines", bankConfig.getInt("Lines"));
        guiValues.put(bankName + ".Update-Delay", bankConfig.getLong("Update-Delay"));
        guiValues.put(bankName + ".Filler.Enabled", bankConfig.getBoolean("Filler.Enabled"));
        guiValues.put(bankName + ".Filler.Material", bankConfig.getString("Filler.Material"));
        guiValues.put(bankName + ".Filler.Glowing", bankConfig.getBoolean("Filler.Glowing"));
        guiValues.put(bankName + ".Settings", bankConfig.getConfigurationSection("Settings"));
        guiValues.put(bankName + ".Permission", bankConfig.getString("Settings.Permission"));
        guiValues.put(bankName + ".BanksGuiItem", bankConfig.getConfigurationSection("Settings.BanksGuiItem"));
        guiValues.put(bankName + ".Upgrades", bankConfig.getConfigurationSection("Upgrades"));
        guiValues.put(bankName + ".Items", bankConfig.getConfigurationSection("Items"));
    }

    public static File getFile(String bankName) {
        return new File(BankPlus.getInstance().getDataFolder(), "banks" + File.separator + bankName + ".yml");
    }

    public static FileConfiguration getConfig(String bankName) {
        File file = getFile(bankName);
        if (!file.exists()) return null;
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            BPLogger.error("An error has occurred while loading a bank file: " + e.getMessage());
        }
        return config;
    }

    public static String getTitle(String bankName) {
        return (String) guiValues.getOrDefault(bankName + ".Title", "&c&l* TITLE NOT FOUND *");
    }

    public static int getLines(String bankName) {
        int lines = (int) guiValues.get(bankName + ".Lines");
        if (lines < 1) return 9;
        switch (lines) {
            case 1:
                return 9;
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

    public static long getUpdateDelay(String bankName) {
        return (long) guiValues.get(bankName + ".Update-Delay");
    }

    public static boolean isFillerEnabled(String bankName) {
        return (boolean) guiValues.get(bankName + ".Filler.Enabled");
    }

    public static String getFillerMaterial(String bankName) {
        return (String) guiValues.get(bankName + ".Filler.Material");
    }

    public static boolean isFillerGlowing(String bankName) {
        return (boolean) guiValues.get(bankName + ".Filler.Glowing");
    }

    public static ConfigurationSection getItems(String bankName) {
        return (ConfigurationSection) guiValues.get(bankName + ".Items");
    }

    public static boolean hasSettings(String bankName) {
        return guiValues.get(bankName + ".Settings") != null;
    }

    public static boolean hasPermission(String bankName) {
        return guiValues.get(bankName + ".Permission") != null;
    }

    public static String getPermission(String bankName) {
        return (String) guiValues.get(bankName + ".Permission");
    }

    public static ConfigurationSection getBanksGuiItemSection(String bankName) {
        return (ConfigurationSection) guiValues.get(bankName + ".BanksGuiItem");
    }

    public static boolean hasUpgrades(String bankName) {
        return guiValues.get(bankName + ".Upgrades") != null;
    }

    public static ConfigurationSection getUpgrades(String bankName) {
        return (ConfigurationSection) guiValues.get(bankName + ".Upgrades");
    }

    public static BigDecimal getCapacityBasedOnLevel(Player p, String bankName) {
        if (!hasUpgrades(bankName)) return Values.CONFIG.getMaxBankCapacity();

        FileConfiguration config = AccountManager.getPlayerConfig(p);
        ConfigurationSection section = getUpgrades(bankName);
        if (section == null) return Values.CONFIG.getMaxBankCapacity();

        int level = Math.max(config.getInt("Banks." + bankName + ".Level"), 1);
        String capacity = section.getString(level + ".Capacity");
        return new BigDecimal(capacity == null ? Values.CONFIG.getMaxBankCapacity().toString() : capacity);
    }

    public static BigDecimal getLevelCost(int level, String bankName) {
        if (!hasUpgrades(bankName)) return new BigDecimal(0);

        ConfigurationSection section = getUpgrades(bankName);
        if (section == null) return new BigDecimal(0);

        String cost = section.getString(level + ".Cost");
        return new BigDecimal(cost == null ? "0" : cost);
    }

    public static int getBankLevel(Player p, String bankName) {
        FileConfiguration config = AccountManager.getPlayerConfig(p);
        return Math.max(config.getInt("Banks." + bankName + ".Level"), 1);
    }

    public static boolean exist(String bankName) {
        return bankNames.contains(bankName);
    }

    public static List<String> getBankNames() {
        return bankNames;
    }
}