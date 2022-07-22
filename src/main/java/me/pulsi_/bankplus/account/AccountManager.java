package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.guis.BanksManager;
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
import java.util.HashMap;
import java.util.UUID;

public class AccountManager {

    private static final HashMap<UUID, File> playerFile = new HashMap<>();

    private static final HashMap<UUID, FileConfiguration> playerConfig = new HashMap<>();

    public static void checkForFileFixes(Player p) {
        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (!BankPlus.wasOnSingleEconomy) return;

            FileConfiguration config = getPlayerConfig(p);
            BigDecimal bal = SingleEconomyManager.getBankBalance(p);

            for (String bankName : BanksManager.getBankNames()) {
                String sBalance = config.getString("Banks." + bankName + ".Money");
                if (Values.CONFIG.getMainGuiName().equals(bankName)) {
                    config.set("Banks." + bankName + ".Money", BPMethods.formatBigDouble(bal));
                    continue;
                }
                if (sBalance == null) config.set("Banks." + bankName + ".Money", "0.00");
            }
            SingleEconomyManager.unloadBankBalance(p);
            MultiEconomyManager.loadBankBalance(p);
        } else {
            if (BankPlus.wasOnSingleEconomy) return;

            FileConfiguration config = getPlayerConfig(p);
            BigDecimal amount = new BigDecimal(0);

            for (String bankName : BanksManager.getBankNames()) {
                String sBalance = config.getString("Banks." + bankName + ".Money");
                if (sBalance != null) amount = amount.add(new BigDecimal(sBalance));
            }
            config.set("Money", BPMethods.formatBigDouble(amount));
            MultiEconomyManager.unloadBankBalance(p);
            SingleEconomyManager.loadBankBalance(p);
        }
        savePlayerFile(p, true);
    }

    public static void registerPlayer(Player p) {
        String identifier;
        if (!Values.CONFIG.isStoringUUIDs()) identifier = p.getName();
        else identifier = p.getUniqueId().toString();
        File file = new File(BankPlus.getInstance().getDataFolder(), "playerdata" + File.separator + identifier + ".yml");
        if (!file.exists()) BPLogger.info("Successfully registered " + p.getName() + "!");
        try {
            file.getParentFile().mkdir();
            file.createNewFile();
        } catch (IOException e) {
            BPLogger.warn("Something went wrong when registering " + p.getName() + ": " + e.getMessage());
        }
    }

    public static File getPlayerFile(Player p) {
        if (!playerFile.containsKey(p.getUniqueId())) {
            String identifier;
            if (!Values.CONFIG.isStoringUUIDs()) identifier = p.getName();
            else identifier = p.getUniqueId().toString();
            File file = new File(BankPlus.getInstance().getDataFolder(), "playerdata" + File.separator + identifier + ".yml");
            playerFile.put(p.getUniqueId(), file);
        }
        return playerFile.get(p.getUniqueId());
    }

    public static File getPlayerFile(OfflinePlayer p) {
        String identifier;
        if (!Values.CONFIG.isStoringUUIDs()) identifier = p.getName();
        else identifier = p.getUniqueId().toString();
        return new File(BankPlus.getInstance().getDataFolder(), "playerdata" + File.separator + identifier + ".yml");
    }

    public static FileConfiguration getPlayerConfig(Player p) {
        if (!playerConfig.containsKey(p.getUniqueId())) {
            File file = getPlayerFile(p);
            FileConfiguration config = new YamlConfiguration();
            try {
                config.load(file);
            } catch (IOException | InvalidConfigurationException ignored) {
            }
            playerConfig.put(p.getUniqueId(), config);
        }
        return playerConfig.get(p.getUniqueId());
    }

    public static FileConfiguration getPlayerConfig(OfflinePlayer p) {
        File file = getPlayerFile(p);
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException ignored) {
        }
        return config;
    }

    public static void savePlayerFile(Player p, boolean async) {
        File file = getPlayerFile(p);
        FileConfiguration config = getPlayerConfig(p);

        if (!async) {
            save(config, file);
            return;
        }
        try {
            Bukkit.getScheduler().runTaskAsynchronously(BankPlus.getInstance(), () -> {
                try {
                    config.save(file);
                } catch (Exception e) {
                    Bukkit.getScheduler().runTask(BankPlus.getInstance(), () -> save(config, file));
                }
            });
        } catch (Exception e) {
            save(config, file);
        }
    }

    public static void savePlayerFile(OfflinePlayer p, boolean async) {
        File file = getPlayerFile(p);
        FileConfiguration config = getPlayerConfig(p);

        if (!async) {
            save(config, file);
            return;
        }
        try {
            Bukkit.getScheduler().runTaskAsynchronously(BankPlus.getInstance(), () -> {
                try {
                    config.save(file);
                } catch (Exception e) {
                    Bukkit.getScheduler().runTask(BankPlus.getInstance(), () -> save(config, file));
                }
            });
        } catch (Exception e) {
            save(config, file);
        }
    }

    private static void save(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            BPLogger.error(e.getMessage());
        }
    }
}