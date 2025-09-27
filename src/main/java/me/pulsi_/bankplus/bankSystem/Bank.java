package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.ConfigValues;
import me.pulsi_.bankplus.values.MultipleBanksValues;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

/**
 * Bank class, that contains an economy, a gui (if enabled) and bank levels.
 */
public class Bank {

    // If the item name is "Filler", it will be placed in the whole gui.
    public static final String FILLER_IDENTIFIER = "Filler";

    private final String identifier;
    private final BPEconomy bankEconomy;
    private final BankGui bankGui;
    // HashMap to keep track of the bank levels.
    private final HashMap<Integer, BankLevel> bankLevels = new HashMap<>();

    private String accessPermission;
    private boolean giveInterestIfNotAvailable;

    public Bank(String identifier) {
        this.identifier = identifier;
        this.bankEconomy = new BPEconomy(this);
        this.bankGui = new BankGui(this);
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
            BPLogger.Console.warn(e, "Could not load \"" + identifier + "\" bank properties because it contains an invalid configuration!");
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
                    BPLogger.Console.warn("The bank \"" + identifier + "\" contains an invalid level number! (" + key + ")");
                    continue;
                }

                ConfigurationSection levelSection = levels.getConfigurationSection(key);
                if (levelSection != null) bankLevels.put(level, BankUtils.buildBankLevel(levelSection, identifier));
            }
        }

        bankGui.getBankItems().clear();
        if (!ConfigValues.isGuiModuleEnabled()) {
            bankGui.setTitle(BPItems.DISPLAYNAME_NOT_FOUND);
            bankGui.setSize(0);
            bankGui.setUpdateDelay(0);
            bankGui.setAvailableBankListItem(null);
            bankGui.setAvailableBankListItem(null);
            return;
        }
        bankGui.setTitle(config.getComponent("Title", MiniMessage.miniMessage()));
        bankGui.setSize(config.getInt("Lines"));
        bankGui.setUpdateDelay(config.getInt("Update-Delay"));

        ConfigurationSection items = config.getConfigurationSection("Items");
        if (items != null) {
            for (String itemName : items.getKeys(false)) {
                ConfigurationSection itemSection = items.getConfigurationSection(itemName);
                if (itemSection == null) continue;

                BankGui.BPGuiItem bankItem = BankGui.BPGuiItem.loadBankItem(itemSection);

                if (itemName.equals(FILLER_IDENTIFIER)) { // If it's the Filler item, update it in the bank.
                    bankGui.setFiller(bankItem);
                    continue;
                }

                List<Integer> slots = itemSection.getIntegerList("Slot");
                if (slots.isEmpty()) bankGui.setBankItem(itemSection.getInt("Slot") - 1, bankItem);
                else for (int slot : slots) bankGui.setBankItem(slot - 1, bankItem);
            }
        }

        // If the multiple banks gui is not enabled, is not necessary to
        // load the available / unavailable items, saving a bit of space.
        if (!MultipleBanksValues.enableMultipleBanksModule()) return;
        ConfigurationSection availableSection = config.getConfigurationSection("Settings.BanksGuiItem.Available");
        bankGui.setAvailableBankListItem(BankGui.BPGuiItem.loadBankItem(availableSection));

        ConfigurationSection unavailableSection = config.getConfigurationSection("Settings.BanksGuiItem.Unavailable");
        bankGui.setUnavailableBankListItem(BankGui.BPGuiItem.loadBankItem(unavailableSection));
    }

    public static class BankLevel {
        BigDecimal cost, capacity, interest, offlineInterest, afkInterest, maxInterestAmount;
        HashMap<String, RequiredItem> requiredItems;
        List<String> interestLimiter;
        boolean removeRequiredItems;
    }

    public static class RequiredItem {

        public RequiredItem(ItemStack item) {
            this.item = item;
        }

        public ItemStack item;
        public int amount = 1;
    }
}