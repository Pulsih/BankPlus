package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.mySQL.BPSQL;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

public class BPPlayerManager {

    private final OfflinePlayer p;

    public BPPlayerManager(OfflinePlayer p) {
        this.p = p;
    }

    public BPPlayerManager(UUID uuid) {
        this.p = Bukkit.getOfflinePlayer(uuid);
    }

    /**
     * Check if the player file has any errors and fix them.
     *
     * @param p The player to check.
     */
    public void checkForFileFixes(OfflinePlayer p, BPPlayerManager pManager) {
        File file = pManager.getPlayerFile();
        FileConfiguration config = pManager.getPlayerConfig(file);
        boolean hasChanges = false;

        String sName = config.getString("name");
        if (sName == null) {
            config.set("name", p.getName());
            hasChanges = true;
        }

        for (Bank bank : BankUtils.getBanks()) {
            String bankName = bank.getIdentifier();
            String sBalance = config.getString("banks." + bankName + ".money");
            String sLevel = config.getString("banks." + bankName + ".level");
            String sDebt = config.getString("banks." + bankName + ".debt");

            if (sLevel == null) {
                config.set("banks." + bankName + ".level", 1);
                hasChanges = true;
            }
            if (sBalance == null) {
                BigDecimal amount = Values.CONFIG.getMainGuiName().equals(bankName) ? Values.CONFIG.getStartAmount() : BigDecimal.valueOf(0);
                config.set("banks." + bankName + ".money", BPFormatter.styleBigDecimal(amount));
                hasChanges = true;
            }
            if (sDebt == null) {
                config.set("banks." + bankName + ".debt", "0");
                hasChanges = true;
            }
            if (Values.CONFIG.notifyOfflineInterest()) {
                String sInterest = config.getString("banks." + bankName + ".interest");
                if (sInterest == null) {
                    config.set("banks." + bankName + ".interest", "0");
                    hasChanges = true;
                }
            }
        }

        if (hasChanges) pManager.savePlayerFile(config, file, true);
    }

    public boolean isPlayerRegistered() {
        BPSQL sql = BankPlus.INSTANCE().getMySql();
        if (sql.isConnected()) return sql.isPlayerRegistered(p);
        return getPlayerFile().exists();
    }

    public void registerPlayer() {
        if (BankPlus.INSTANCE().getMySql().isConnected()) return; // The database already get updated when we check for player registration.

        File file = getPlayerFile();
        if (file.exists()) return;

        try {
            file.getParentFile().mkdir();
            file.createNewFile();
        } catch (IOException e) {
            BPLogger.warn("Something went wrong while registering " + p.getName() + ": " + e.getMessage());
        }
    }

    public File getPlayerFile() {
        String identifier = (Values.CONFIG.isStoringUUIDs() ? p.getUniqueId().toString() : p.getName());
        return new File(BankPlus.INSTANCE().getDataFolder(), "playerdata" + File.separator + identifier + ".yml");
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
            Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
                try {
                    config.save(file);
                } catch (Exception e) {
                    Bukkit.getScheduler().runTask(BankPlus.INSTANCE(), () -> save(config, file));
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