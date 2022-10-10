package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

public class BankPlusPlayerFiles {

    private final Player player;
    private final OfflinePlayer offlinePlayer;

    public BankPlusPlayerFiles(Player player) {
        this.player = player;
        this.offlinePlayer = null;
    }

    public BankPlusPlayerFiles(OfflinePlayer offlinePlayer) {
        this.player = null;
        this.offlinePlayer = offlinePlayer;
    }

    public void checkForFileFixes() {
        SingleEconomyManager singleEconomyManager = new SingleEconomyManager(player);
        MultiEconomyManager multiEconomyManager = new MultiEconomyManager(player);
        FileConfiguration config = getPlayerConfig();

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (!BankPlus.wasOnSingleEconomy) return;

            BigDecimal bal = singleEconomyManager.getBankBalance();
            for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
                String sBalance = config.getString("Banks." + bankName + ".Money");
                if (Values.CONFIG.getMainGuiName().equals(bankName)) {
                    config.set("Banks." + bankName + ".Money", BPMethods.formatBigDouble(bal));
                    continue;
                }
                if (sBalance == null) config.set("Banks." + bankName + ".Money", "0.00");
            }
            singleEconomyManager.unloadBankBalance();
            multiEconomyManager.loadBankBalance();
        } else {
            if (BankPlus.wasOnSingleEconomy) return;

            BigDecimal amount = new BigDecimal(0);
            for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
                String sBalance = config.getString("Banks." + bankName + ".Money");
                if (sBalance != null) amount = amount.add(new BigDecimal(sBalance));
            }
            config.set("Money", BPMethods.formatBigDouble(amount));
            multiEconomyManager.unloadBankBalance();
            singleEconomyManager.loadBankBalance();
        }
        savePlayerFile(true);
    }

    public void registerPlayer() {
        if (onNull(player, "Cannot register player!")) return;
        String identifier = Values.CONFIG.isStoringUUIDs() ? player.getUniqueId().toString() : player.getName();
        File file = new File(BankPlus.INSTANCE.getDataFolder(), "playerdata" + File.separator + identifier + ".yml");
        if (!file.exists()) BPLogger.info("Successfully registered " + player.getName() + "!");
        try {
            file.getParentFile().mkdir();
            file.createNewFile();
        } catch (IOException e) {
            BPLogger.warn("Something went wrong when registering " + player.getName() + ": " + e.getMessage());
        }
    }

    public File getPlayerFile() {
        if (onNull(player, "Cannot get player file!")) return null;

        BankPlusPlayer bankPlusPlayer = BankPlus.INSTANCE.getPlayerRegistry().get(player);
        if (bankPlusPlayer != null && bankPlusPlayer.getPlayerFile() != null) return bankPlusPlayer.getPlayerFile();

        String identifier = Values.CONFIG.isStoringUUIDs() ? player.getUniqueId().toString() : player.getName();
        return new File(BankPlus.INSTANCE.getDataFolder(), "playerdata" + File.separator + identifier + ".yml");
    }

    public File getOfflinePlayerFile() {
        if (offNull(offlinePlayer, "Cannot get player file!")) return null;
        String identifier = Values.CONFIG.isStoringUUIDs() ? offlinePlayer.getUniqueId().toString() : offlinePlayer.getName();
        return new File(BankPlus.INSTANCE.getDataFolder(), "playerdata" + File.separator + identifier + ".yml");
    }

    public FileConfiguration getPlayerConfig() {
        if (onNull(player, "Cannot get player config!")) return null;

        BankPlusPlayer bankPlusPlayer = BankPlus.INSTANCE.getPlayerRegistry().get(player);
        if (bankPlusPlayer != null && bankPlusPlayer.getPlayerConfig() != null) return bankPlusPlayer.getPlayerConfig();

        File file = getPlayerFile();
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            BPLogger.warn("Something went wrong while trying to get the " + player.getName() + "'s file configuration: " + e.getMessage());
        }
        return config;
    }

    public FileConfiguration getOfflinePlayerConfig() {
        if (offNull(offlinePlayer, "Cannot get player config!")) return null;
        File file = getOfflinePlayerFile();
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            BPLogger.warn("Something went wrong while trying to get the " + offlinePlayer.getName() + "'s file configuration: " + e.getMessage());
        }
        return config;
    }

    public void savePlayerFile(boolean async) {
        File file = getPlayerFile();
        FileConfiguration config = getPlayerConfig();

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

    public void saveOfflinePlayerFile(boolean async) {
        File file = getOfflinePlayerFile();
        FileConfiguration config = getOfflinePlayerConfig();

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

    private boolean onNull(Player p, String tried) {
        if (p == null) {
            BPLogger.error(tried + " Called online-player method but the player was null!");
            return true;
        }
        return false;
    }

    private boolean offNull(OfflinePlayer p, String tried) {
        if (p == null) {
            BPLogger.error(tried + " Called offline-player method but the player was null!");
            return true;
        }
        return false;
    }
}