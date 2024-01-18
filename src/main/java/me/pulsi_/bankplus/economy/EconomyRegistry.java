package me.pulsi_.bankplus.economy;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
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
                pManager.setLevel(BankManager.getCurrentLevel(name, uuid), name);
                pManager.setMoney(economy.getBankBalance(uuid, name), name);
                pManager.setOfflineInterest(economy.getOfflineInterest(uuid, name), name);
            }
        } else {
            BPPlayerManager manager = new BPPlayerManager(uuid);

            File file = manager.getPlayerFile();
            FileConfiguration config = manager.getPlayerConfig(file);

            for (BPEconomy economy : BPEconomy.list()) {
                String name = economy.getBankName();
                config.set("banks." + name + ".debt", BPFormatter.formatBigDecimal(economy.getDebt(uuid)));
                config.set("banks." + name + ".level", BankManager.getCurrentLevel(name, uuid));
                config.set("banks." + name + ".money", BPFormatter.formatBigDecimal(economy.getBankBalance(uuid, name)));
                config.set("banks." + name + ".interest", BPFormatter.formatBigDecimal(economy.getOfflineInterest(uuid, name)));
            }
            manager.savePlayerFile(config, file, async);
        }
    }

    public void loadEveryone(boolean async) {
        for ()
        for (BPEconomy economy : BPEconomy.list()) {
            for (UUID uuid : economy.getLoadedPlayers()) {
                savePlayer(uuid, async);
                if (Bukkit.getPlayer(uuid) == null) economy.unloadPlayer(uuid);
            }
        }
    }

    public void saveEveryone(boolean async) {
        for (BPEconomy economy : BPEconomy.list()) {
            for (UUID uuid : economy.getLoadedPlayers()) {
                savePlayer(uuid, async);
                if (Bukkit.getPlayer(uuid) == null) economy.unloadPlayer(uuid);
            }
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