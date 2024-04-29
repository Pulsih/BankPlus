package me.pulsi_.bankplus.values.configs;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.BPConfigs;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class MultipleBanksValues {

    private boolean enableMultipleBanksModule, showNotAvailableBanks, directlyOpenIf1IsAvailable, previousItemGlowing, nextItemGlowing;
    private String banksGuiTitle, previousItemMaterial, nextItemMaterial, previousItemDisplayname, nextItemDisplayname, fillerMaterial;
    private List<String> previousItemLore, nextItemLore, autoBanksUnlocker;
    private int bankListGuiLines, previousItemSlot, nextItemSlot, updateDelay;
    private boolean fillerEnabled, fillerGlowing;

    public static MultipleBanksValues getInstance() {
        return new MultipleBanksValues();
    }

    public void setupValues() {
        FileConfiguration multipleBanks = BankPlus.INSTANCE().getConfigs().getConfig(BPConfigs.Type.MULTIPLE_BANKS.name);

        enableMultipleBanksModule = multipleBanks.getBoolean("Enabled");
        showNotAvailableBanks = multipleBanks.getBoolean("Shows-Not-Available-Banks");
        directlyOpenIf1IsAvailable = multipleBanks.getBoolean("Directly-Open-If-1-Is-Available");
        autoBanksUnlocker = multipleBanks.getStringList("Auto-Banks-Unlocker");
        previousItemGlowing = multipleBanks.getBoolean("Banks-Gui.Previous-Page.Glowing");
        nextItemGlowing = multipleBanks.getBoolean("Banks-Gui.Next-Page.Glowing");
        banksGuiTitle = multipleBanks.getString("Banks-Gui.Title");
        previousItemMaterial = multipleBanks.getString("Banks-Gui.Previous-Page.Material");
        nextItemMaterial = multipleBanks.getString("Banks-Gui.Next-Page.Material");
        previousItemDisplayname = multipleBanks.getString("Banks-Gui.Previous-Page.DisplayName");
        nextItemDisplayname = multipleBanks.getString("Banks-Gui.Next-Page.DisplayName");
        fillerMaterial = multipleBanks.getString("Banks-Gui.Filler.Material");
        previousItemLore = multipleBanks.getStringList("Banks-Gui.Previous-Page.Lore");
        nextItemLore = multipleBanks.getStringList("Banks-Gui.Next-Page.Lore");
        bankListGuiLines = multipleBanks.getInt("Banks-Gui.Lines");
        previousItemSlot = multipleBanks.getInt("Banks-Gui.Previous-Page.Slot");
        nextItemSlot = multipleBanks.getInt("Banks-Gui.Next-Page.Slot");
        updateDelay = multipleBanks.getInt("Banks-Gui.Update-Delay");
        fillerEnabled = multipleBanks.getBoolean("Banks-Gui.Filler.Enabled");
        fillerGlowing = multipleBanks.getBoolean("Banks-Gui.Filler.Glowing");
    }

    public boolean enableMultipleBanksModule() {
        return enableMultipleBanksModule;
    }

    public boolean isShowNotAvailableBanks() {
        return showNotAvailableBanks;
    }

    public boolean isDirectlyOpenIf1IsAvailable() {
        return directlyOpenIf1IsAvailable;
    }

    public List<String> getAutoBanksUnlocker() {
        return autoBanksUnlocker;
    }

    public boolean isPreviousItemGlowing() {
        return previousItemGlowing;
    }

    public boolean isNextItemGlowing() {
        return nextItemGlowing;
    }

    public String getBanksGuiTitle() {
        return banksGuiTitle;
    }

    public String getPreviousItemMaterial() {
        return previousItemMaterial;
    }

    public String getNextItemMaterial() {
        return nextItemMaterial;
    }

    public String getPreviousItemDisplayname() {
        return previousItemDisplayname;
    }

    public String getNextItemDisplayname() {
        return nextItemDisplayname;
    }

    public String getFillerMaterial() {
        return fillerMaterial;
    }

    public List<String> getPreviousItemLore() {
        return previousItemLore;
    }

    public List<String> getNextItemLore() {
        return nextItemLore;
    }

    public int getBankListGuiLines() {
        return Math.max(9, Math.min(54, bankListGuiLines * 9));
    }

    public int getPreviousItemSlot() {
        return previousItemSlot;
    }

    public int getNextItemSlot() {
        return nextItemSlot;
    }

    public boolean isFillerEnabled() {
        return fillerEnabled;
    }

    public boolean isFillerGlowing() {
        return fillerGlowing;
    }

    public int getUpdateDelay() {
        return updateDelay;
    }
}