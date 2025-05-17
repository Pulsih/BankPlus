package me.pulsi_.bankplus.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.managers.BPTaskManager;
import me.pulsi_.bankplus.mySQL.BPSQL;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EconomyUtils {

    /**
     * Save the selected player information of all bank economies.
     *
     * @param p The player.
     */
    public static void savePlayer(OfflinePlayer p, boolean unload) {
        BPPlayerManager manager = new BPPlayerManager(p);

        File file = manager.getPlayerFile();
        FileConfiguration config = manager.getPlayerConfig(file);

        for (BPEconomy economy : BPEconomy.list()) {
            String name = economy.getOriginBank().getIdentifier();
            config.set("banks." + name + ".debt", BPFormatter.styleBigDecimal(economy.getDebt(p)));
            config.set("banks." + name + ".level", economy.getBankLevel(p));
            config.set("banks." + name + ".money", BPFormatter.styleBigDecimal(economy.getBankBalance(p)));
            config.set("banks." + name + ".interest", BPFormatter.styleBigDecimal(economy.getOfflineInterest(p)));
        }
        manager.savePlayerFile(config, file);

        if (BPSQL.isConnected()) { // If MySQL is enabled, save the data also in the database.
            SQLPlayerManager pManager = new SQLPlayerManager(p);
            for (BPEconomy economy : BPEconomy.list())
                pManager.updatePlayer(
                        economy.getOriginBank().getIdentifier(),
                        economy.getDebt(p),
                        economy.getBankBalance(p),
                        economy.getBankLevel(p),
                        economy.getOfflineInterest(p)
                );
        }

        if (unload) PlayerRegistry.unloadPlayer(p);
    }

    /**
     * Load all the online players balance.
     */
    public static void loadEveryone() {
        for (Player p : Bukkit.getOnlinePlayers())
            PlayerRegistry.loadPlayer(p);
    }

    /**
     * Save everyone's balance.
     *
     * @param async Choose if saving asynchronously to have less impact on the server performance. DON'T USE ON SERVER SHUTDOWN
     */
    public static void saveEveryone(boolean async) {
        Set<UUID> loadedUUIDs = new HashSet<>();
        for (BPEconomy economy : BPEconomy.list()) // It's safe to call #addAll since Sets don't allow duplicated entries.
            loadedUUIDs.addAll(economy.getLoadedPlayers());

        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
                for (UUID uuid : loadedUUIDs) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                    savePlayer(p, !p.isOnline());
                }
            });
            return;
        }

        for (UUID uuid : loadedUUIDs) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
            savePlayer(p, !p.isOnline());
        }
    }

    /**
     * Restart the saving cooldown.
     */
    public static void restartSavingInterval() {
        long delay = ConfigValues.getSaveDelay();
        if (delay <= 0) return;

        long minutes = delay * 1200L;
        BPTaskManager.setTask(BPTaskManager.MONEY_SAVING_TASK, Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE(), () -> saveEveryone(true), minutes, minutes));
    }
}