package me.pulsi_.bankplus;

import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.*;
import me.pulsi_.bankplus.placeholders.Placeholders;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class BankPlus extends JavaPlugin {

    private static BankPlus instance;
    private static ConfigManager cm;

    private static Economy econ = null;
    private static Permission perms = null;

    private boolean isPlaceholderAPIHooked = false, isEssentialsXHooked = false;
    private String serverVersion;

    @Override
    public void onEnable() {

        instance = this;
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            BPLogger.log("");
            BPLogger.log("&cCannot load &a&lBank&9&lPlus&c, Vault is not installed!");
            BPLogger.log("&cPlease download it in order to use this plugin!");
            BPLogger.log("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomy()) {
            BPLogger.log("");
            BPLogger.log("&cCannot load &a&lBank&9&lPlus&c, No economy plugin found!");
            BPLogger.log("&cPlease download an economy plugin to use BankPlus!");
            BPLogger.log("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        serverVersion = getServer().getVersion();
        cm = new ConfigManager(this);
        setupPermissions();

        DataManager.setupPlugin();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            BPLogger.info("Hooked into PlaceholderAPI!");
            new Placeholders().register();
            isPlaceholderAPIHooked = true;
        }
        if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
            BPLogger.info("Hooked into Essentials!");
            isEssentialsXHooked = true;
        }

        if (Values.CONFIG.isInterestEnabled()) Interest.startInterest();
        if (Values.CONFIG.isIgnoringAfkPlayers()) AFKManager.startCountdown();
        if (Values.CONFIG.isBanktopEnabled()) {
            BankTopManager.updateBankTop();
            BankTopManager.startUpdateTask();
        }
        Methods.startSavingBalancesTask();
    }

    @Override
    public void onDisable() {

        instance = this;
        EconomyManager.saveBankBalance(new ArrayList<>(Bukkit.getOnlinePlayers()));
        if (Values.CONFIG.isInterestEnabled()) Interest.saveInterest();
        DataManager.shutdownPlugin();
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public boolean isPlaceholderAPIHooked() {
        return isPlaceholderAPIHooked;
    }

    public boolean isEssentialsXHooked() {
        return isEssentialsXHooked;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public static ConfigManager getCm() {
        return cm;
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