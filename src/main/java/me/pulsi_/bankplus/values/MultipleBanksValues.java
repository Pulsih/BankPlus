package me.pulsi_.bankplus.values;

import me.pulsi_.bankplus.BankPlus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class MultipleBanksValues {

    private static boolean enableMultipleBanksModule;
    private static boolean showNotAvailableBanks;
    private static boolean directlyOpenIf1IsAvailable;
    private static boolean previousItemGlowing, nextItemGlowing;
    private static Component banksGuiTitle;
    private static String previousItemMaterial, nextItemMaterial, previousItemDisplayname, nextItemDisplayname, fillerMaterial;
    private static List<String> previousItemLore, nextItemLore, autoBanksUnlocker;
    private static int bankListGuiLines, previousItemSlot, nextItemSlot, updateDelay;
    private static boolean fillerEnabled, fillerGlowing;

    public static void setupValues() {
        FileConfiguration multipleBanks = BankPlus.INSTANCE().getConfigs().getConfig("multiple_banks.yml");

        enableMultipleBanksModule = multipleBanks.getBoolean("Enabled");
        showNotAvailableBanks = multipleBanks.getBoolean("Shows-Not-Available-Banks");
        directlyOpenIf1IsAvailable = multipleBanks.getBoolean("Directly-Open-If-1-Is-Available");
        autoBanksUnlocker = multipleBanks.getStringList("Auto-Banks-Unlocker");
        previousItemGlowing = multipleBanks.getBoolean("Banks-Gui.Previous-Page.Glowing");
        nextItemGlowing = multipleBanks.getBoolean("Banks-Gui.Next-Page.Glowing");
        banksGuiTitle = multipleBanks.getComponent("Banks-Gui.Title", MiniMessage.miniMessage());
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

    public static boolean enableMultipleBanksModule() {
        return enableMultipleBanksModule;
    }

    public static boolean isShowNotAvailableBanks() {
        return showNotAvailableBanks;
    }

    public static boolean isDirectlyOpenIf1IsAvailable() {
        return directlyOpenIf1IsAvailable;
    }

    public static List<String> getAutoBanksUnlocker() {
        return autoBanksUnlocker;
    }

    public static boolean isPreviousItemGlowing() {
        return previousItemGlowing;
    }

    public static boolean isNextItemGlowing() {
        return nextItemGlowing;
    }

    public static Component getBanksGuiTitle() {
        return banksGuiTitle;
    }

    public static String getPreviousItemMaterial() {
        return previousItemMaterial;
    }

    public static String getNextItemMaterial() {
        return nextItemMaterial;
    }

    public static String getPreviousItemDisplayname() {
        return previousItemDisplayname;
    }

    public static String getNextItemDisplayname() {
        return nextItemDisplayname;
    }

    public static String getFillerMaterial() {
        return fillerMaterial;
    }

    public static List<String> getPreviousItemLore() {
        return previousItemLore;
    }

    public static List<String> getNextItemLore() {
        return nextItemLore;
    }

    public static int getBankListGuiLines() {
        return Math.max(9, Math.min(54, bankListGuiLines * 9));
    }

    public static int getPreviousItemSlot() {
        return previousItemSlot;
    }

    public static int getNextItemSlot() {
        return nextItemSlot;
    }

    public static boolean isFillerEnabled() {
        return fillerEnabled;
    }

    public static boolean isFillerGlowing() {
        return fillerGlowing;
    }

    public static int getUpdateDelay() {
        return updateDelay;
    }
}