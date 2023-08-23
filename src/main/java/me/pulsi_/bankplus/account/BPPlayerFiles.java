package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.MultiEconomyManager;
import me.pulsi_.bankplus.economy.SingleEconomyManager;
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

public class BPPlayerFiles {

    private final Player p;
    private final OfflinePlayer op;

    public BPPlayerFiles(OfflinePlayer op) {
        this.op = op;
        this.p = op.isOnline() ? Bukkit.getPlayer(op.getUniqueId()) : null;
    }

    public void checkForFileFixes() {
        if (p == null) {
            BPLogger.error("Cannot check for file fixes for " + op.getName() + "! The player must be online!");
            return;
        }

        SingleEconomyManager singleEconomyManager = new SingleEconomyManager(p);
        MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);
        FileConfiguration config = getPlayerConfig();

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (!BankPlus.INSTANCE.wasOnSingleEconomy()) return;

            BigDecimal bal = singleEconomyManager.getBankBalance();
            for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
                String sBalance = config.getString("Banks." + bankName + ".Money");
                if (Values.CONFIG.getMainGuiName().equals(bankName)) {
                    config.set("Banks." + bankName + ".Money", BPFormatter.formatBigDouble(bal));
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
            config.set("Money", BPFormatter.formatBigDouble(amount));
            multiEconomyManager.unloadBankBalance();
            singleEconomyManager.loadBankBalance();
        }
        savePlayerFile(true);
    }

    public boolean isPlayerRegistered() {
        File file = getPlayerFile();
        if (file.exists()) return true;

        try {
            file.getParentFile().mkdir();
            file.createNewFile();
        } catch (IOException e) {
            BPLogger.warn("Something went wrong while registering " + op.getName() + ": " + e.getMessage());
        }
        return false;
    }

    public File getPlayerFile() {
        if (p != null) {
            BPPlayer bpPlayer = BankPlus.INSTANCE.getPlayerRegistry().get(p);
            if (bpPlayer != null && bpPlayer.getPlayerFile() != null) return bpPlayer.getPlayerFile();
        }

        String identifier = (Values.CONFIG.isStoringUUIDs() ? op.getUniqueId().toString() : op.getName());
        return new File(BankPlus.INSTANCE.getDataFolder(), "playerdata" + File.separator + identifier + ".yml");
    }

    public FileConfiguration getPlayerConfig() {
        if (p != null) {
            BPPlayer bpPlayer = BankPlus.INSTANCE.getPlayerRegistry().get(p);
            if (bpPlayer != null && bpPlayer.getPlayerConfig() != null) return bpPlayer.getPlayerConfig();
        }

        return YamlConfiguration.loadConfiguration(getPlayerFile());
    }

    public void savePlayerFile(boolean async) {
        FileConfiguration config = getPlayerConfig();
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

    private void save(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            BPLogger.error(e.getMessage());
        }
    }
}