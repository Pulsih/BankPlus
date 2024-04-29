package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.*;
import me.pulsi_.bankplus.values.Values;
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

public class Bank extends BankGui {

    private final String identifier;
    private final BPEconomy bankEconomy;
    // HashMap to keep track of the bank levels.
    private final HashMap<Integer, BankLevel> bankLevels = new HashMap<>();

    public Bank(String identifier) {
        super(identifier);

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

    public BankLevel getBankLevel(int level) {
        return bankLevels.get(level);
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
        getBankItems().clear();

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

        ConfigurationSection items = config.getConfigurationSection("Items");
        if (items != null) {
            for (String item : items.getKeys(false)) {
                ConfigurationSection itemSection = items.getConfigurationSection(item);
                if (itemSection == null) continue;

                ItemStack itemStack = BankUtils.getItemFromSection(itemSection);
                Bank.BankItem bankItem = new Bank.BankItem();
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
                if (slots.isEmpty()) setBankItem(itemSection.getInt("Slot") - 1, bankItem);
                else for (int slot : slots) setBankItem(slot - 1, bankItem);
            }
        }

        if (!Values.MULTIPLE_BANKS.enableMultipleBanksModule()) {
            setAvailableBankListItem(null);
            setUnavailableBankListItem(null);
            return;
        }

        ConfigurationSection availableSection = config.getConfigurationSection("Settings.BanksGuiItem.Available"),
                unavailableSection = config.getConfigurationSection("Settings.BanksGuiItem.Unavailable");

        Bank.BankItem availableItem = new Bank.BankItem();
        availableItem.setItem(BankUtils.getItemFromSection(availableSection));
        if (availableSection != null) {
            availableItem.setMaterial(availableSection.getString("Material"));
            availableItem.setDisplayname(availableSection.getString("Displayname"));
            availableItem.getLore().put(0, availableSection.getStringList("Lore"));
        }
        setAvailableBankListItem(availableItem);

        Bank.BankItem unavailableItem = new Bank.BankItem();
        availableItem.setItem(BankUtils.getItemFromSection(unavailableSection));
        if (unavailableSection != null) {
            unavailableItem.setMaterial(unavailableSection.getString("Material"));
            unavailableItem.setDisplayname(unavailableSection.getString("Displayname"));
            unavailableItem.getLore().put(0, unavailableSection.getStringList("Lore"));
        }
        setUnavailableBankListItem(unavailableItem);
    }

    public static class BankLevel {
        BigDecimal cost, capacity, interest, offlineInterest;
        List<ItemStack> requiredItems;
        List<String> interestLimiter;
        boolean removeRequiredItems;
    }
}