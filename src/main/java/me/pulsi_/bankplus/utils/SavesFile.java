package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Class to simplify the access and to the saves.yml file.
 */
public class SavesFile {

    public static final String SAVES_FILE_NAME = "saves.yml";

    public static File getFile() {
        return BankPlus.INSTANCE().getConfigs().getFile(SAVES_FILE_NAME);
    }

    public static FileConfiguration getConfig() {
        return BankPlus.INSTANCE().getConfigs().getConfig(SAVES_FILE_NAME);
    }

    public static String getString(String path) {
        FileConfiguration config = getConfig();
        return config == null ? null : config.getString(path);
    }

    public static long getLong(String path) {
        FileConfiguration config = getConfig();
        return config == null ? 0 : config.getLong(path);
    }

    public static void set(String path, Object object) {
        File file = getFile();
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e) {
                BPLogger.Console.warn("Could not create saves.yml file: " + e.getMessage());
            }

        FileConfiguration config = getConfig();

        config.set(path, object);
        try {
            config.save(file);
        } catch (IOException e) {
            BPLogger.Console.warn("Could not save saves.yml file: " + e.getMessage());
        }
    }
}
