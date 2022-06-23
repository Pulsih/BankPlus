package me.pulsi_.bankplus.values.configs;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class BankValues {

    private boolean isGuiEnabled;
    private String guiFillerMaterial;
    private String guiFillerDisplayname;
    private boolean isGuiFillerGlowing;
    private boolean isGuiFillerEnabled;
    private String guiTitle;
    private int guiUpdateDelay;
    private int guiLines;
    private ConfigurationSection guiItems;

    public static BankValues getInstance() {
        return new BankValues();
    }

    public void setupValues() {
        FileConfiguration bank = BankPlus.getCm().getConfig("bank");

        isGuiEnabled = bank.getBoolean("Enabled");
        guiFillerMaterial = bank.getString("Filler.Material");
        guiFillerDisplayname = bank.getString("Filler.DisplayName");
        isGuiFillerGlowing = bank.getBoolean("Filler.Glowing");
        isGuiFillerEnabled = bank.getBoolean("Filler.Enabled");
        guiTitle = bank.getString("Title");
        guiUpdateDelay = bank.getInt("Update-Delay");
        guiLines = bank.getInt("Lines");
        guiItems = bank.getConfigurationSection("Items");
    }

    public boolean isGuiEnabled() {
        return isGuiEnabled;
    }

    public int getGuiLines() {
        return guiLines;
    }

    public String getGuiTitle() {
        return guiTitle;
    }

    public String getGuiFillerMaterial() {
        return guiFillerMaterial;
    }

    public String getGuiFillerDisplayname() {
        return guiFillerDisplayname;
    }

    public ConfigurationSection getGuiItems() {
        return guiItems;
    }

    public int getGuiUpdateDelay() {
        return guiUpdateDelay;
    }

    public boolean isGuiFillerGlowing() {
        return isGuiFillerGlowing;
    }

    public boolean isGuiFillerEnabled() {
        return isGuiFillerEnabled;
    }
}