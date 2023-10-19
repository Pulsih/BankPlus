package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class BPPlayerFiles {

    private final OfflinePlayer p;

    public BPPlayerFiles(OfflinePlayer p) {
        this.p = p;
    }

    public BPPlayerFiles(UUID uuid) {
        this.p = Bukkit.getOfflinePlayer(uuid);
    }

    public boolean isPlayerRegistered() {
        File file = getPlayerFile();
        if (file.exists()) return true;

        try {
            file.getParentFile().mkdir();
            file.createNewFile();
        } catch (IOException e) {
            BPLogger.warn("Something went wrong while registering " + p.getName() + ": " + e.getMessage());
        }
        return false;
    }

    public File getPlayerFile() {
        String identifier = (Values.CONFIG.isStoringUUIDs() ? p.getUniqueId().toString() : p.getName());
        return new File(BankPlus.INSTANCE.getDataFolder(), "playerdata" + File.separator + identifier + ".yml");
    }

    public FileConfiguration getPlayerConfig() {
        return YamlConfiguration.loadConfiguration(getPlayerFile());
    }

    public FileConfiguration getPlayerConfig(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    public void savePlayerFile(FileConfiguration config, File file, boolean async) {
        if (!async) {
            save(config, file);
            return;
        }
        try {
            Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE, () -> {
                try {
                    config.save(file);
                } catch (Exception e) {
                    Bukkit.getScheduler().runTask(BankPlus.INSTANCE, () -> save(config, file));
                }
            });
        } catch (Exception e) {
            save(config, file);
        }
    }

    private void save(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            BPLogger.error(e.getMessage());
        }
    }
}