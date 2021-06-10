package me.pulsi_.bankplus;

import me.pulsi_.bankplus.external.bStats;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.ConfigManager;
import me.pulsi_.bankplus.managers.DataManager;
import me.pulsi_.bankplus.placeholders.Placeholders;
import me.pulsi_.bankplus.utils.ChatUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class BankPlus extends JavaPlugin {

    private static Economy econ = null;
    private ConfigManager configManager;
    private Interest interest;

    @Override
    public void onEnable() {

        if (!setupEconomy()) {
            getServer().getConsoleSender().sendMessage("");
            getServer().getConsoleSender().sendMessage(ChatUtils.c("&cCannot load &a&lBank&9&lPlus&c, Vault is not installed!"));
            getServer().getConsoleSender().sendMessage(ChatUtils.c("&cPlease download it if you want to use this plugin!"));
            getServer().getConsoleSender().sendMessage(ChatUtils.c("&f&nhttps://www.spigotmc.org/resources/vault.34315/"));
            getServer().getConsoleSender().sendMessage("");
            getServer().getPluginManager().disablePlugin(this);

        } else {

            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new Placeholders(this).register();
            } else {
                getServer().getConsoleSender().sendMessage("");
                getServer().getConsoleSender().sendMessage(ChatUtils.c("&cCannot setup Placeholders, PlaceholderAPI is not installed!"));
                getServer().getConsoleSender().sendMessage("");
            }

            this.configManager = new ConfigManager(this);
            configManager.createConfigs();
            DataManager.registerEvents(this);
            DataManager.setupCommands(this);

            new bStats(this, 11612);

            DataManager.startupMessage(this);

            this.interest = new Interest(this);
            interest.giveInterest();
        }
    }

    @Override
    public void onDisable() {
        DataManager.shutdownMessage(this);
    }

    public Economy getEconomy() {
        return econ;
    }
    public FileConfiguration getConfiguration() {
        return configManager.getConfiguration();
    }
    public FileConfiguration getMessages() {
        return configManager.getMessages();
    }
    public FileConfiguration getPlayers() {
        return configManager.getPlayers();
    }
    public void reloadConfigs() {
        configManager.reloadConfigs();
    }
    public void savePlayers() throws IOException {
        configManager.savePlayers();
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
        return econ != null;
    }
}