package me.pulsi_.bankplus;

import me.pulsi_.bankplus.external.bStats;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.ConfigManager;
import me.pulsi_.bankplus.managers.ConfigValues;
import me.pulsi_.bankplus.managers.DataManager;
import me.pulsi_.bankplus.placeholders.Placeholders;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.ListUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class BankPlus extends JavaPlugin {

    private static Economy econ;
    private ConfigManager configManager;
    private Interest interest;

    private boolean isPlaceholderAPIHooked = false;

    @Override
    public void onEnable() {

        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            ChatUtils.consoleMessage("");
            ChatUtils.consoleMessage("&cCannot load &a&lBank&9&lPlus&c, Vault is not installed!");
            ChatUtils.consoleMessage("&cPlease download it in order to use this plugin!");
            ChatUtils.consoleMessage("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomy()) {
            ChatUtils.consoleMessage("");
            ChatUtils.consoleMessage("&cCannot load &a&lBank&9&lPlus&c, No economy plugin found!");
            ChatUtils.consoleMessage("&cPlease download an economy plugin to use BankPlus!");
            ChatUtils.consoleMessage("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            ChatUtils.consoleMessage("&a&lBank&9&lPlus &fDetected PlaceholderAPI!");
            new Placeholders(this).register();
            isPlaceholderAPIHooked = true;
        }

        this.configManager = new ConfigManager(this);
        configManager.createConfigs();

        DataManager.registerEvents(this);
        DataManager.setupCommands(this);
        DataManager.startupMessage(this);

        ConfigValues.setupValues();

        new bStats(this, 11612);

        this.interest = new Interest(this);
        if (ConfigValues.isInterestEnabled())
            interest.startsInterest();

        ListUtils.PLAYERCHAT_DEBUG.add("DISABLED");
        ListUtils.GUIBANK_DEBUG.add("DISABLED");
        ListUtils.INTEREST_DEBUG.add("DISABLED");
    }

    @Override
    public void onDisable() {
        DataManager.shutdownMessage(this);
        if (ConfigValues.isInterestEnabled())
            interest.saveInterest();
    }

    public Economy getEconomy() {
        return econ;
    }

    public FileConfiguration config() {
        return configManager.getConfiguration();
    }
    public FileConfiguration messages() {
        return configManager.getMessages();
    }
    public FileConfiguration players() {
        return configManager.getPlayers();
    }

    public void reloadConfigs() {
        configManager.reloadConfigs();
    }
    public void savePlayers() {
        configManager.savePlayers();
    }

    public boolean isPlaceholderAPIHooked() {
        return isPlaceholderAPIHooked;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }
}