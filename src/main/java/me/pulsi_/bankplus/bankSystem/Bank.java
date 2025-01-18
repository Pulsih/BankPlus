package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPLogger;
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

                BankGui.BankItem bankItem = new BankGui.BankItem().loadBankItem(itemSection);

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

        ConfigurationSection availableSection = config.getConfigurationSection("Settings.BanksGuiItem.Available");
        bankGui.setAvailableBankListItem(new BankGui.BankItem().loadBankItem(availableSection));

        ConfigurationSection unavailableSection = config.getConfigurationSection("Settings.BanksGuiItem.Unavailable");
        bankGui.setUnavailableBankListItem(new BankGui.BankItem().loadBankItem(unavailableSection));
    }

    public static class BankLevel {
        BigDecimal cost, capacity, interest, offlineInterest, afkInterest, maxInterestAmount;
        HashMap<String, ItemStack> requiredItems;
        List<String> interestLimiter;
        boolean removeRequiredItems;
    }
}