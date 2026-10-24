package me.pulsi_.bankplus.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.managers.BPTaskManager;
import me.pulsi_.bankplus.sql.BPSQL;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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
        boolean saved = true;
        for (BPEconomy economy : BPEconomy.list()) {
            // Skip unloaded holders so a failed/partial load cannot overwrite SQL with defaults.
            if (!economy.isPlayerLoaded(p)) continue;
            if (!BPSQL.savePlayer(p, economy)) saved = false;
        }

        // If something failed keep the player loaded, the next saving cycle will try again instead of losing the data.
        if (unload && saved) PlayerRegistry.unloadPlayer(p);
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

        // Only used on shutdown, just log a bit in case saving gets stuck.
        int total = loadedUUIDs.size(), saved = 0;
        long start = System.currentTimeMillis();
        BPLogger.Console.info("Saving " + total + " loaded players...");
        for (UUID uuid : loadedUUIDs) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
            savePlayer(p, !p.isOnline());

            saved++;
            if (saved % 50 == 0) BPLogger.Console.info("Saved " + saved + "/" + total + "...");
        }
        BPLogger.Console.info("Saved everyone in " + (System.currentTimeMillis() - start) + "ms.");
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