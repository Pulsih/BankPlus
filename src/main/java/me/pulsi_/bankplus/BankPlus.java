package me.pulsi_.bankplus;

import me.pulsi_.bankplus.external.bStats;
import me.pulsi_.bankplus.gui.GuiHolder;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.ConfigManager;
import me.pulsi_.bankplus.managers.DataManager;
import me.pulsi_.bankplus.placeholders.Placeholders;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class BankPlus extends JavaPlugin {

    private static BankPlus instance;
    private ConfigManager configManager;

    private static Economy econ = null;
    private static Permission perms = null;

    private boolean isPlaceholderAPIHooked = false;

    @Override
    public void onEnable() {

        instance = this;
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            ChatUtils.log("");
            ChatUtils.log("&cCannot load &a&lBank&9&lPlus&c, Vault is not installed!");
            ChatUtils.log("&cPlease download it in order to use this plugin!");
            ChatUtils.log("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomy()) {
            ChatUtils.log("");
            ChatUtils.log("&cCannot load &a&lBank&9&lPlus&c, No economy plugin found!");
            ChatUtils.log("&cPlease download an economy plugin to use BankPlus!");
            ChatUtils.log("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();

        this.configManager = new ConfigManager(this);
        configManager.createConfigs();

        DataManager.setupPlugin();

        new bStats(this, 11612);
        if (Values.CONFIG.isInterestEnabled()) Interest.startsInterest();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders().register();
            isPlaceholderAPIHooked = true;
            BPLogger.info("Hooked into PlaceholderAPI!");
        }
    }

    @Override
    public void onDisable() {
        instance = this;

        DataManager.shutdownPlugin();
        if (Values.CONFIG.isInterestEnabled()) Interest.saveInterest();
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
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
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return true;
    }

    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
    }

    public static BankPlus getInstance() {
        return instance;
    }
}