package me.pulsi_.bankplus;

import me.pulsi_.bankplus.external.bStats;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.AFKManager;
import me.pulsi_.bankplus.managers.ConfigManager;
import me.pulsi_.bankplus.managers.DataManager;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.placeholders.Placeholders;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class BankPlus extends JavaPlugin {

    private static BankPlus instance;
    private static ConfigManager cm;

    private static Economy econ = null;
    private static Permission perms = null;

    private boolean isPlaceholderAPIHooked = false;
    private String serverVersion;

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
        serverVersion = getServer().getVersion();

        setupPermissions();

        cm = new ConfigManager(this);
        cm.createConfigs();

        DataManager.setupPlugin();

        new bStats(this, 11612);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            BPLogger.info("Hooked into PlaceholderAPI!");
            new Placeholders().register();
            isPlaceholderAPIHooked = true;
        }

        if (Values.CONFIG.isInterestEnabled()) Interest.startsInterest();
        if (Values.CONFIG.isIgnoringAfkPlayers()) AFKManager.startCountdown();
    }

    @Override
    public void onDisable() {
        instance = this;

        for (Player p : Bukkit.getOnlinePlayers())
            EconomyManager.saveBankBalance(p);

        DataManager.shutdownPlugin();
        if (Values.CONFIG.isInterestEnabled()) Interest.saveInterest();
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