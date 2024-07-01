package me.pulsi_.bankplus.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.managers.BPTaskManager;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

public class EconomyUtils {

    /**
     * Save the selected player balance.
     *
     * @param uuid The player UUID.
     */
    public static void savePlayer(UUID uuid, boolean unload) {
        if (BankPlus.INSTANCE().getMySql().isConnected()) {
            SQLPlayerManager pManager = new SQLPlayerManager(uuid);
            for (BPEconomy economy : BPEconomy.list()) {
                pManager.updatePlayer(
                        economy.getOriginBank().getIdentifier(),
                        economy.getDebt(uuid),
                        economy.getBankBalance(uuid),
                        economy.getBankLevel(uuid),
                        economy.getOfflineInterest(uuid)
                );
            }
        } else {
            BPPlayerManager manager = new BPPlayerManager(uuid);

            File file = manager.getPlayerFile();
            FileConfiguration config = manager.getPlayerConfig(file);

            for (BPEconomy economy : BPEconomy.list()) {
                String name = economy.getOriginBank().getIdentifier();
                config.set("banks." + name + ".debt", BPFormatter.styleBigDecimal(economy.getDebt(uuid)));
                config.set("banks." + name + ".level", economy.getBankLevel(uuid));
                config.set("banks." + name + ".money", BPFormatter.styleBigDecimal(economy.getBankBalance(uuid)));
                config.set("banks." + name + ".interest", BPFormatter.styleBigDecimal(economy.getOfflineInterest(uuid)));
            }
            manager.savePlayerFile(config, file);
        }
        if (unload) PlayerRegistry.unloadPlayer(uuid);
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
     * @param async Choose if executing this action asynchronously to increase the server performance. (DO NOT USE ON SERVER SHOTDOWN)
     */
    public static void saveEveryone(boolean async) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
                for (UUID uuid : BPEconomy.getLoadedPlayers())
                    savePlayer(uuid, Bukkit.getPlayer(uuid) == null);
            });
            return;
        }

        for (UUID uuid : BPEconomy.getLoadedPlayers())
            savePlayer(uuid, Bukkit.getPlayer(uuid) == null);
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