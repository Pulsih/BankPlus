package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.BPConfigs;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * This class will be changed between versions, it will be used to add the compatibility for older versions to the newest versions.
 */
public class BPVersions {

    public static void convertPlayerFilesToNewStyle() {
        if (BPConfigs.isUpdated()) return;

        File dataFolder = new File(BankPlus.INSTANCE().getDataFolder(), "playerdata");
        File[] files = dataFolder.listFiles();
        if (files == null || files.length == 0) return;

        BPLogger.info("Found playerdata folder, starting to convert the files to the new format.");
        for (File file : files) {
            FileConfiguration oldConfig = new YamlConfiguration(), newConfig = new YamlConfiguration();

            try {
                oldConfig.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                BPLogger.error(e, "An error has occurred while loading a user file (File name: " + file.getName() + "):");
                continue;
            }

            String name = oldConfig.getString("Account-Name"), interest = oldConfig.getString("Offline-Interest"), debt = oldConfig.getString("Debt");
            if (name == null && interest == null && debt == null) continue;

            newConfig.set("name", name);
            if (Values.CONFIG.notifyOfflineInterest()) newConfig.set("interest", interest);
            newConfig.set("debt", debt);

            for (String bankName : BankPlus.INSTANCE().getBankGuiRegistry().getBanks().keySet()) {
                if (Values.CONFIG.getMainGuiName().equals(bankName)) newConfig.set("banks." + bankName + ".money", oldConfig.get("Money") == null ? Values.CONFIG.getStartAmount() : oldConfig.get("Money"));
                else newConfig.set("banks." + bankName + ".money", oldConfig.get("Banks." + bankName + ".Money") == null ? "0" : oldConfig.get("Banks." + bankName + ".Money"));
                newConfig.set("banks." + bankName + ".level", oldConfig.get("Banks." + bankName + ".Level") == null ? "1" : oldConfig.get("Banks." + bankName + ".Level"));
            }

            try {
                newConfig.save(file);
            } catch (IOException e) {
                BPLogger.error(e, "Could not apply changes while converting the player file \"" + file.getName() + "\".");
            }
        }
        BPLogger.info("Conversion finished!");
    }
}