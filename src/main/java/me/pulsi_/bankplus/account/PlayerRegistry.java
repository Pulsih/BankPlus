package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerRegistry {

    private final HashMap<UUID, BPPlayer> players;
    private BukkitTask saveTask;

    public PlayerRegistry() {
        players = new HashMap<>();
        forceSave(true);
    }

    public void put(Player p, BPPlayer player) {
        players.put(p.getUniqueId(), player);
    }

    public BPPlayer get(OfflinePlayer p) {
        return players.get(p.getUniqueId());
    }

    public BPPlayer remove(UUID playerUUID) {
        return players.remove(playerUUID);
    }

    public BPPlayer remove(OfflinePlayer p) {
        return players.remove(p.getUniqueId());
    }

    public boolean contains(OfflinePlayer p) {
        return players.containsKey(p.getUniqueId());
    }

    public void savePlayer(UUID uuid, boolean async) {
        if (Values.CONFIG.isSqlEnabled() && BankPlus.INSTANCE().getSql().isConnected()) {
            SQLPlayerManager pManager = new SQLPlayerManager(uuid);
            for (String bankName : BankPlus.INSTANCE().getBankGuiRegistry().getBanks().keySet()) {
                pManager.setLevel(BankManager.getCurrentLevel(bankName, uuid), bankName);
                pManager.setMoney(BPEconomy.getBankBalance(uuid, bankName), bankName);
                pManager.setDebt(BPEconomy.getDebt(uuid, bankName), bankName);
                pManager.setOfflineInterest(BPEconomy.getOfflineInterest(uuid, bankName), bankName);
            }
        } else {
            BPPlayerManager files = new BPPlayerManager(uuid);

            File file = files.getPlayerFile();
            FileConfiguration config = files.getPlayerConfig(file);

            for (String bankName : BankPlus.INSTANCE().getBankGuiRegistry().getBanks().keySet()) {
                config.set("banks." + bankName + ".level", BankManager.getCurrentLevel(bankName, uuid));
                config.set("banks." + bankName + ".money", BPFormatter.formatBigDouble(BPEconomy.getBankBalance(uuid, bankName)));
                config.set("banks." + bankName + ".debt", BPFormatter.formatBigDouble(BPEconomy.getDebt(uuid, bankName)));
                config.set("banks." + bankName + ".interest", BPFormatter.formatBigDouble(BPEconomy.getOfflineInterest(uuid, bankName)));
            }
            files.savePlayerFile(config, file, async);
        }
    }

    public void forceSave(boolean async) {
        saveEveryone(async);

        if (saveTask != null) saveTask.cancel();

        long delay = Values.CONFIG.getSaveBalancedDelay();
        if (delay <= 0) return;

        long minutes = delay * 1200L;
        boolean saveBroadcast = Values.CONFIG.isSaveBalancesBroadcast();

        saveTask = Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE(), () -> {
            if (saveBroadcast) BPLogger.info("All player data have been saved!");
            saveEveryone(async);
        }, minutes, minutes);
    }

    public void saveEveryone(boolean async) {
        for (UUID uuid : new ArrayList<>(players.keySet())) {
            savePlayer(uuid, async);
            if (Bukkit.getPlayer(uuid) == null) {
                remove(uuid);
                OnlineInfoHolder.unloadInfo(uuid);
            }
        }
    }
}