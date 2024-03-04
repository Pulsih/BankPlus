package me.pulsi_.bankplus.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.UUID;

public class EconomyRegistry {

    private BukkitTask saveTask;

    public EconomyRegistry() {
        forceSave(true);
    }

    public void savePlayer(UUID uuid, boolean async) {
        if (BankPlus.INSTANCE().getMySql().isConnected()) {
            SQLPlayerManager pManager = new SQLPlayerManager(uuid);
            for (BPEconomy economy : BPEconomy.list()) {
                String name = economy.getBankName();

                pManager.setDebt(economy.getDebt(uuid), name);
                pManager.setLevel(economy.getBankLevel(uuid), name);
                pManager.setMoney(economy.getBankBalance(uuid), name);
                pManager.setOfflineInterest(economy.getOfflineInterest(uuid), name);
            }
        } else {
            BPPlayerManager manager = new BPPlayerManager(uuid);

            File file = manager.getPlayerFile();
            FileConfiguration config = manager.getPlayerConfig(file);

            for (BPEconomy economy : BPEconomy.list()) {
                String name = economy.getBankName();
                config.set("banks." + name + ".debt", BPFormatter.formatBigDecimal(economy.getDebt(uuid)));
                config.set("banks." + name + ".level", economy.getBankLevel(uuid));
                config.set("banks." + name + ".money", BPFormatter.formatBigDecimal(economy.getBankBalance(uuid)));
                config.set("banks." + name + ".interest", BPFormatter.formatBigDecimal(economy.getOfflineInterest(uuid)));
            }
            manager.savePlayerFile(config, file, async);
        }
    }

    public void loadEveryone() {
        for (Player p : Bukkit.getOnlinePlayers())
            PlayerRegistry.loadPlayer(p);
    }

    public void saveEveryone(boolean async) {
        for (UUID uuid : BPEconomy.getLoadedPlayers()) {
            savePlayer(uuid, async);
            if (Bukkit.getPlayer(uuid) == null) PlayerRegistry.unloadPlayer(uuid);
        }
    }

    public void forceSave(boolean async) {
        saveEveryone(async);

        if (saveTask != null) saveTask.cancel();

        long delay = Values.CONFIG.getSaveDelay();
        if (delay <= 0) return;

        long minutes = delay * 1200L;
        saveTask = Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE(), () -> saveEveryone(async), minutes, minutes);
    }
}