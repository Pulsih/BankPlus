package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.ConfigValues;
import me.pulsi_.bankplus.values.MultipleBanksValues;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class Bank {

    private final String identifier;
    private final BPEconomy bankEconomy;
    private final BankGui bankGui;
    // HashMap to keep track of the bank levels.
    private final HashMap<Integer, BankLevel> bankLevels = new HashMap<>();

    private String accessPermission;
    private boolean giveInterestIfNotAvailable;

    public Bank(String identifier) {
        this.identifier = identifier;
        bankEconomy = new BPEconomy(this);
        bankGui = new BankGui(this);
    }

    public String getIdentifier() {
        return identifier;
    }

    public BPEconomy getBankEconomy() {
        return bankEconomy;
    }

    public BankGui getBankGui() {
        return bankGui;
    }

    public HashMap<Integer, BankLevel> getBankLevels() {
        return bankLevels;
    }

    public String getAccessPermission() {
        return accessPermission;
    }

    public boolean isGiveInterestIfNotAvailable() {
        return giveInterestIfNotAvailable;
    }

    public BankLevel getBankLevel(int level) {
        return bankLevels.get(level);
    }

    public void setAccessPermission(String accessPermission) {
        this.accessPermission = accessPermission;
    }

    public void setGiveInterestIfNotAvailable(boolean giveInterestIfNotAvailable) {
        this.giveInterestIfNotAvailable = giveInterestIfNotAvailable;
    }

    public void loadBankProperties() {
        loadBankProperties(new File(BankPlus.INSTANCE().getDataFolder(), "banks" + File.separator + identifier + ".yml"));
    }

    /**
     * Update the bank settings, such as access permission, levels, items and more.
     *
     * @param file The bank file.
     */
    public void loadBankProperties(File file) {
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            BPLogger.warn(e, "Could not load \"" + identifier + "\" bank properties because it contains an invalid configuration!");
            return;
        }

        setAccessPermission(config.getString("Settings.Permission"));
        setGiveInterestIfNotAvailable(config.getBoolean("Settings.Give-Interest-If-Not-Available"));

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

        if (!ConfigValues.isGuiModuleEnabled()) return;
        bankGui.getBankItems().clear();

        bankGui.setTitle(config.getString("Title"));
        bankGui.setSize(config.getInt("Lines"));
        bankGui.setUpdateDelay(config.getInt("Update-Delay"));
        bankGui.setFillerEnabled(config.getBoolean("Filler.Enabled"));
        bankGui.setFillerMaterial(config.getString("Filler.Material"));
        bankGui.setFillerGlowing(config.getBoolean("Filler.Glowing"));

        ConfigurationSection items = config.getConfigurationSection("Items");
        if (items != null) {
            for (String item : items.getKeys(false)) {
                ConfigurationSection itemSection = items.getConfigurationSection(item);
                if (itemSection == null) continue;

                ItemStack itemStack = BankUtils.getItemFromSection(itemSection);
                BankGui.BankItem bankItem = new BankGui.BankItem();
                bankItem.setItem(itemStack);
                bankItem.setMaterial(itemSection.getString("Material"));
                bankItem.setDisplayname(itemSection.getString("Displayname"));
                bankItem.setActions(itemSection.getStringList("Actions"));

                List<String> configLore = itemSection.getStringList("Lore");
                if (!configLore.isEmpty()) bankItem.getLore().put(0, configLore);
                else {
                    ConfigurationSection loreSection = itemSection.getConfigurationSection("Lore");
                    if (loreSection != null) {
                        List<String> defaultLore = loreSection.getStringList("Default");
                        bankItem.getLore().put(0, defaultLore);

                        for (String level : loreSection.getKeys(false)) {
                            if (level.equalsIgnoreCase("default") || BPUtils.isInvalidNumber(level)) continue;

                            List<String> levelLore = loreSection.getStringList(level);
                            bankItem.getLore().put(Integer.parseInt(level), levelLore);
                        }
                    }
                }

                List<Integer> slots = itemSection.getIntegerList("Slot");
                if (slots.isEmpty()) bankGui.setBankItem(itemSection.getInt("Slot") - 1, bankItem);
                else for (int slot : slots) bankGui.setBankItem(slot - 1, bankItem);
            }
        }

        if (!MultipleBanksValues.enableMultipleBanksModule()) {
            bankGui.setAvailableBankListItem(null);
            bankGui.setUnavailableBankListItem(null);
            return;
        }

        ConfigurationSection availableSection = config.getConfigurationSection("Settings.BanksGuiItem.Available"),
                unavailableSection = config.getConfigurationSection("Settings.BanksGuiItem.Unavailable");

        BankGui.BankItem availableItem = new BankGui.BankItem();
        availableItem.setItem(BankUtils.getItemFromSection(availableSection));
        if (availableSection != null) {
            availableItem.setMaterial(availableSection.getString("Material"));
            availableItem.setDisplayname(availableSection.getString("Displayname"));
            availableItem.getLore().put(0, availableSection.getStringList("Lore"));
        }
        bankGui.setAvailableBankListItem(availableItem);

        BankGui.BankItem unavailableItem = new BankGui.BankItem();
        availableItem.setItem(BankUtils.getItemFromSection(unavailableSection));
        if (unavailableSection != null) {
            unavailableItem.setMaterial(unavailableSection.getString("Material"));
            unavailableItem.setDisplayname(unavailableSection.getString("Displayname"));
            unavailableItem.getLore().put(0, unavailableSection.getStringList("Lore"));
        }
        bankGui.setUnavailableBankListItem(unavailableItem);
    }

    public static class BankLevel {
        BigDecimal cost, capacity, interest, offlineInterest, afkInterest, maxInterestAmount;
        List<ItemStack> requiredItems;
        List<String> interestLimiter;
        boolean removeRequiredItems;
    }
}