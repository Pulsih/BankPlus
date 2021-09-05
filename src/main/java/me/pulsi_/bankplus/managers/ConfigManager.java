package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.IllegalPluginAccessException;

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

        if (!configFile.exists())
            plugin.saveResource("config.yml", false);
        if (!messagesFile.exists())
            plugin.saveResource("messages.yml", false);
        if (!playersFile.exists())
            plugin.saveResource("players.yml", false);

        config = new YamlConfiguration();
        messages = new YamlConfiguration();
        players = new YamlConfiguration();

        try {
            config.load(configFile);
            messages.load(messagesFile);
            players.load(playersFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfiguration() {
        return config;
    }
    public FileConfiguration getMessages() {
        return messages;
    }
    public FileConfiguration getPlayers() {
        return players;
    }

    public void reloadConfigs() {
        config = YamlConfiguration.loadConfiguration(configFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        players = YamlConfiguration.loadConfiguration(playersFile);
    }

    public void savePlayers() {
        try {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    players.save(playersFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IllegalPluginAccessException e) {
            try {
                players.save(playersFile);
            } catch (IOException ex) {
                e.printStackTrace();
            }
        }
    }
}