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

    private final Player p;
    private final OfflinePlayer op;

    public BankPlusPlayerFiles(Player p) {
        this.p = p;
        this.op = null;
    }

    public BankPlusPlayerFiles(OfflinePlayer op) {
        this.p = null;
        this.op = op;
    }

    public void checkForFileFixes() {
        SingleEconomyManager singleEconomyManager = new SingleEconomyManager(p);
        MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);
        FileConfiguration config = getPlayerConfig();

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (!BankPlus.INSTANCE.wasOnSingleEconomy()) return;

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
            if (BankPlus.INSTANCE.wasOnSingleEconomy()) return;

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

    public boolean isPlayerRegistered() {
        String identifier = Values.CONFIG.isStoringUUIDs() ? (p == null ? op.getUniqueId() : p.getUniqueId()).toString() : (p == null ? op.getName() : p.getName());

        File file = new File(BankPlus.INSTANCE.getDataFolder(), "playerdata" + File.separator + identifier + ".yml");
        if (file.exists()) return false;

        String name = (p != null ? p.getName() : op.getName());
        BPLogger.info("Successfully registered " + name + "!");
        try {
            file.getParentFile().mkdir();
            file.createNewFile();
        } catch (IOException e) {
            BPLogger.warn("Something went wrong when registering " + name + ": " + e.getMessage());
        }
        return true;
    }

    public File getPlayerFile() {
        if (p != null) {
            BankPlusPlayer bankPlusPlayer = BankPlus.INSTANCE.getPlayerRegistry().get(p);
            if (bankPlusPlayer != null && bankPlusPlayer.getPlayerFile() != null) return bankPlusPlayer.getPlayerFile();
        }

        String identifier = Values.CONFIG.isStoringUUIDs() ? (p == null ? op.getUniqueId() : p.getUniqueId()).toString() : (p == null ? op.getName() : p.getName());
        return new File(BankPlus.INSTANCE.getDataFolder(), "playerdata" + File.separator + identifier + ".yml");
    }

    public FileConfiguration getPlayerConfig() {
        if (p != null) {
            BankPlusPlayer bankPlusPlayer = BankPlus.INSTANCE.getPlayerRegistry().get(p);
            if (bankPlusPlayer != null && bankPlusPlayer.getPlayerConfig() != null) return bankPlusPlayer.getPlayerConfig();
        }

        File file = getPlayerFile();
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            BPLogger.warn("Something went wrong while trying to get " + op.getName() + "'s file configuration: " + e.getMessage());
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

    public void savePlayerFile(FileConfiguration config, boolean async) {
        File file = getPlayerFile();

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

    public void savePlayerFile(File file, FileConfiguration config, boolean async) {
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