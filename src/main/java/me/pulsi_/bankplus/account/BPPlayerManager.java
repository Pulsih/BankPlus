package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.mySQL.BPSQL;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

public class BPPlayerManager {

    private final OfflinePlayer p;

    public BPPlayerManager(OfflinePlayer p) {
        this.p = p;
    }

    public BPPlayerManager(UUID uuid) {
        this.p = Bukkit.getOfflinePlayer(uuid);
    }

    public void loadPlayer() {
        Player oP = p.getPlayer();
        if (oP == null) return;

        BPPlayer player = new BPPlayer(oP);

        boolean changes = false;
        HashMap<String, BPPlayer.PlayerBank> information = new HashMap<>();
        // If the player is already loaded, put the loaded map in the "balances" map.
        HashMap<String, BPPlayer.PlayerBank> bankInformation = getBankInformation();
        if (bankInformation != null) information = bankInformation;

        if (Values.CONFIG.isSqlEnabled() && BankPlus.INSTANCE.getSql().isConnected()) {
            SQLPlayerManager pManager = new SQLPlayerManager(p);
            for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
                if (information.containsKey(bankName)) continue;

                information.put(bankName, new BPPlayer.PlayerBank(
                        pManager.getLevel(bankName),
                        pManager.getMoney(bankName),
                        pManager.getDebt(bankName),
                        pManager.getOfflineInterest(bankName)
                ));
                changes = true;
            }

        } else {
            FileConfiguration config = getPlayerConfig();
            for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
                // If the "balances" map already contains the bank values, skip.
                if (information.containsKey(bankName)) continue;

                String sLevel = config.getString("banks." + bankName + ".level"),
                        bal = config.getString("banks." + bankName + ".money"),
                        debt = config.getString("banks." + bankName + ".debt"),
                        interest = config.getString("banks." + bankName + ".interest");

                int level = 1;
                BigDecimal balAmount = new BigDecimal(0), debtAmount = new BigDecimal(0), interestAmount = new BigDecimal(0);

                if (sLevel != null) {
                    try {
                        level = Integer.parseInt(sLevel);
                    } catch (NumberFormatException e) {
                        BPLogger.warn("Could not get \"" + bankName + "\" bank level for " + p.getName() + " because it contains an invalid number! (Using 0 as default)");
                    }
                }
                if (bal != null) {
                    try {
                        balAmount = new BigDecimal(bal);
                    } catch (NumberFormatException e) {
                        BPLogger.warn("Could not get \"" + bankName + "\" bank balance for " + p.getName() + " because it contains an invalid number! (Using 0 as default)");
                    }
                }
                if (debt != null) {
                    try {
                        debtAmount = new BigDecimal(debt);
                    } catch (NumberFormatException e) {
                        BPLogger.warn("Could not get \"" + bankName + "\" bank debt for " + p.getName() + " because it contains an invalid number! (Using 0 as default)");
                    }
                }
                if (interest != null) {
                    try {
                        interestAmount = new BigDecimal(interest);
                    } catch (NumberFormatException e) {
                        BPLogger.warn("Could not get \"" + bankName + "\" bank interest for " + p.getName() + " because it contains an invalid number! (Using 0 as default)");
                    }
                }
                information.put(bankName, new BPPlayer.PlayerBank(
                        level,
                        balAmount,
                        debtAmount,
                        interestAmount
                ));
                changes = true;
            }
        }
        if (changes) player.setBankInformation(information);

        BankPlus.INSTANCE.getPlayerRegistry().put(oP, new BPPlayer(oP));
    }

    /**
     * Check if the player file has any errors and fix them.
     * @param p The player to check.
     */
    public FileConfiguration checkForFileFixes(OfflinePlayer p, BPPlayerManager pManager) {
        File file = pManager.getPlayerFile();
        FileConfiguration config = pManager.getPlayerConfig(file);
        boolean hasChanges = false;

        String sName = config.getString("name");
        if (sName == null) {
            config.set("name", p.getName());
            hasChanges = true;
        }

        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
            String sBalance = config.getString("banks." + bankName + ".money");
            String sLevel = config.getString("banks." + bankName + ".level");
            String sDebt = config.getString("banks." + bankName + ".debt");

            if (sLevel == null) {
                config.set("banks." + bankName + ".level", 1);
                hasChanges = true;
            }
            if (sBalance == null) {
                BigDecimal amount = Values.CONFIG.getMainGuiName().equals(bankName) ? Values.CONFIG.getStartAmount() : BigDecimal.valueOf(0);
                config.set("banks." + bankName + ".money", BPFormatter.formatBigDouble(amount));
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
        return config;
    }

    public boolean isPlayerRegistered() {
        if (Values.CONFIG.isSqlEnabled()) {
            BPSQL sql = BankPlus.INSTANCE.getSql();
            if (sql.isConnected()) return sql.isPlayerRegistered(p);
        }

        File file = getPlayerFile();
        return file.exists();
    }

    public void registerPlayer() {
        if (Values.CONFIG.isSqlEnabled()) {
            BPSQL sql = BankPlus.INSTANCE.getSql();
            if (sql.isConnected()) sql.registerPlayer(p);
        }

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

    public HashMap<String, BPPlayer.PlayerBank> getBankInformation() {
        BPPlayer player = BankPlus.INSTANCE.getPlayerRegistry().get(p);
        return (player == null ? null : player.getBankInformation());
    }

    private void save(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            BPLogger.error(e.getMessage());
        }
    }
}