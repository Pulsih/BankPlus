package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ConfigManager {

    private final BankPlus plugin;
    private File configFile, messagesFile, playersFile;
    private FileConfiguration config, messages, players;

    public ConfigManager(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void createConfigs() {
        configFile = new File(plugin.getDataFolder(), "Config.yml");
        messagesFile = new File(plugin.getDataFolder(), "Messages.yml");
        playersFile = new File(plugin.getDataFolder(), "Players.yml");

        if (!configFile.exists()) {
            configFile.getParentFile().mkdir();
            plugin.saveResource("Config.yml", false);
        }
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdir();
            plugin.saveResource("Messages.yml", false);
        }
        if (!playersFile.exists()) {
            playersFile.getParentFile().mkdir();
            plugin.saveResource("Players.yml", false);
        }

        config = new YamlConfiguration();
        messages = new YamlConfiguration();
        players = new YamlConfiguration();

        try {
            config.load(configFile);
            messages.load(messagesFile);
            players.load(playersFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
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

    public void savePlayers() throws IOException {
        players.save(playersFile);
    }
}