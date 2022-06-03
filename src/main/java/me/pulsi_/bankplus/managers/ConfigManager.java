package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final BankPlus plugin;
    private File configFile, messagesFile, playersFile;
    private FileConfiguration config, messages, players;

    public ConfigManager(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void createConfigs() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        playersFile = new File(plugin.getDataFolder(), "players.yml");

        if (!configFile.exists()) plugin.saveResource("config.yml", false);
        if (!messagesFile.exists()) plugin.saveResource("messages.yml", false);
        if (!playersFile.exists()) plugin.saveResource("players.yml", false);

        config = new YamlConfiguration();
        messages = new YamlConfiguration();
        players = new YamlConfiguration();

        try {
            config.load(configFile);
            messages.load(messagesFile);
            players.load(playersFile);
        } catch (IOException | InvalidConfigurationException e) {
            BPLogger.error(e.getMessage());
        }
    }

    public FileConfiguration getConfig(String type) {
        switch (type) {
            case "config":
                return config;
            case "messages":
                return messages;
            case "players":
                return players;
            default:
                return null;
        }
    }

    public void reloadConfig(String type) {
        switch (type) {
            case "config":
                try {
                    config.load(configFile);
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error(e.getMessage());
                }
                break;

            case "messages":
                try {
                    messages.load(messagesFile);
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error(e.getMessage());
                }
                break;

            case "players":
                try {
                    players.load(playersFile);
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error(e.getMessage());
                }
                break;
        }
    }

    public void savePlayers() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                players.save(playersFile);
            } catch (IOException e) {
                BPLogger.error(e.getMessage());
            }
        });
    }
}