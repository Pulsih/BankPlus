package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.guis.BanksManager;
import me.pulsi_.bankplus.managers.DataManager;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will be changed between version, it will be used to add the compatibility
 * for older versions to the newest versions. ( Ex: Automatically moving the player
 * balances from the players.yml to the per-player file )
 */
public class VersionUtils {

    public static void moveBankFileToBanksFolder() {
        File oldBankFile = new File(BankPlus.getInstance().getDataFolder(), "bank.yml");
        if (!oldBankFile.exists()) return;

        File mainBankFile = new File(BankPlus.getInstance().getDataFolder(), "banks" + File.separator + Values.CONFIG.getMainGuiName() + ".yml");

        FileConfiguration oldBankConfig = new YamlConfiguration(), mainBankConfig = new YamlConfiguration();
        try {
            oldBankConfig.load(oldBankFile);
            mainBankConfig.load(mainBankFile);
        } catch (IOException | InvalidConfigurationException e) {
            BPLogger.error(e.getMessage());
            return;
        }
        oldBankFile.delete();

        mainBankConfig.set("Items", null);
        mainBankConfig.set("Title", oldBankConfig.getString("Title"));
        mainBankConfig.set("Lines", oldBankConfig.getInt("Lines"));

        ConfigurationSection itemValues = oldBankConfig.getConfigurationSection("Items");
        if (itemValues == null) return;

        for (String item : itemValues.getKeys(false)) {
            ConfigurationSection getter = oldBankConfig.getConfigurationSection("Items." + item);
            if (getter == null) continue;

            String a = getter.getString("DisplayName");
            String b = getter.getString("Material");
            int c = getter.getInt("Slot");
            List<String> d = getter.getStringList("Lore");
            boolean e = getter.getBoolean("Glowing");
            String f = getter.getString("Action.Action-Type");
            String g = getter.getString("Action.Amount");
            int h = getter.getInt("Amount");

            mainBankConfig.set("Items." + item + ".Material", b);
            mainBankConfig.set("Items." + item + ".Amount", h);
            mainBankConfig.set("Items." + item + ".Slot", c);
            mainBankConfig.set("Items." + item + ".Displayname", a);
            mainBankConfig.set("Items." + item + ".Lore", d);
            mainBankConfig.set("Items." + item + ".Glowing", e);
            mainBankConfig.set("Items." + item + ".Action.Action-Type", f);
            mainBankConfig.set("Items." + item + ".Action.Amount", g);
        }
        try {
            mainBankConfig.save(mainBankFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataManager.reloadPlugin();
    }

    public static void changePlayerStoragePosition(List<String> playersIdentifiers, int point) {
        File playersFile = new File(BankPlus.getInstance().getDataFolder(), "players.yml");
        if (!playersFile.exists()) return;

        File oldPlayersFile = new File(BankPlus.getInstance().getDataFolder(), "old_players.yml");
        playersFile.renameTo(oldPlayersFile);

        FileConfiguration playersConfig = new YamlConfiguration();
        try {
            playersConfig.load(oldPlayersFile);
        } catch (IOException | InvalidConfigurationException e) {
            BPLogger.error(e.getMessage());
            return;
        }

        ConfigurationSection players = playersConfig.getConfigurationSection("Players");
        if (players == null) return;

        if (playersIdentifiers.isEmpty()) playersIdentifiers = new ArrayList<>(players.getKeys(false));

        for (int i = point; i < playersIdentifiers.size(); i++) {
            String id = playersIdentifiers.get(i);

            File file = new File(BankPlus.getInstance().getDataFolder(), "playerdata" + File.separator + id + ".yml");
            if (file.exists()) continue;

            try {
                file.getParentFile().mkdir();
                file.createNewFile();
            } catch (IOException ignored) {
            }

            FileConfiguration config = new YamlConfiguration();
            try {
                config.load(file);
            } catch (IOException | InvalidConfigurationException ignored) {
            }

            String sOfflineInterest = players.getString(id + ".Offline-Interest");
            String sName = players.getString(id + ".Account-Name");
            String sBalance = players.getString(id + ".Money");

            if (sOfflineInterest == null) config.set("Offline-Interest", BPMethods.formatBigDouble(BigDecimal.valueOf(0)));
            else config.set("Offline-Interest", sOfflineInterest);
            if (sName != null) config.set("Account-Name", sName);

            if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
                if (sBalance == null) config.set("Money", BPMethods.formatBigDouble(Values.CONFIG.getStartAmount()));
                else config.set("Money", BPMethods.formatBigDouble(new BigDecimal(sBalance)));

                for (String bankName : BanksManager.getBankNames()) {
                    String sLevel = config.getString("Banks." + bankName + ".Level");
                    if (sLevel == null) config.set("Banks." + bankName + ".Level", 1);
                }
            } else {
                for (String bankName : BanksManager.getBankNames()) {
                    if (!Values.CONFIG.getMainGuiName().equals(bankName)) config.set("Banks." + bankName + ".Money", "0.00");
                    else {
                        if (sBalance != null) config.set("Banks." + bankName + ".Money", BPMethods.formatBigDouble(new BigDecimal(sBalance)));
                        else config.set("Banks." + bankName + ".Money", BPMethods.formatBigDouble(Values.CONFIG.getStartAmount()));
                    }
                    config.set("Banks." + bankName + ".Level", 1);
                }
            }

            try {
                Bukkit.getScheduler().runTaskAsynchronously(BankPlus.getInstance(), () -> {
                    try {
                        config.save(file);
                    } catch (Exception e) {
                        Bukkit.getScheduler().runTask(BankPlus.getInstance(), () -> {
                            try {
                                config.save(file);
                            } catch (IOException ex) {
                                BPLogger.error(ex.getMessage());
                            }
                        });
                    }
                });
            } catch (Exception e) {
                try {
                    config.save(file);
                } catch (IOException ex) {
                    BPLogger.error(ex.getMessage());
                }
            }

            point++;
            if (point >= 60) {
                int finalPoint = point;
                List<String> finalPlayersIdentifiers = playersIdentifiers;
                Bukkit.getScheduler().runTaskLater(BankPlus.getInstance(), () -> changePlayerStoragePosition(finalPlayersIdentifiers, finalPoint), 10L);
                BPLogger.info("Moved the first " + point + " players to the playedata folder.");
                break;
            }
        }
    }
}