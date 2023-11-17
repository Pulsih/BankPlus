package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
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

    public BPPlayer get(Player p) {
        return players.get(p.getUniqueId());
    }

    public BPPlayer remove(UUID playerUUID) {
        return players.remove(playerUUID);
    }

    public BPPlayer remove(Player p) {
        return players.remove(p.getUniqueId());
    }

    public boolean contains(Player p) {
        return players.containsKey(p.getUniqueId());
    }

    public void savePlayer(UUID uuid, boolean async) {
        BPEconomy economy = BankPlus.getBPEconomy();
        BPPlayerManager files = new BPPlayerManager(uuid);

        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig(file);

        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
            config.set("banks." + bankName + ".money", BPFormatter.formatBigDouble(economy.getBankBalance(uuid, bankName)));

        config.set("debt", BPFormatter.formatBigDouble(BankPlus.getBPEconomy().getDebt(uuid)));

        files.savePlayerFile(config, file, async);

        BankPlus.getBPEconomy().unloadBankBalance(uuid);
        remove(uuid);
    }

    public void forceSave(boolean async) {
        saveEveryone(async);

        if (saveTask != null) saveTask.cancel();

        long delay = Values.CONFIG.getSaveBalancedDelay();
        if (delay <= 0) return;

        long minutes = delay * 1200L;
        boolean saveBroadcast = Values.CONFIG.isSaveBalancesBroadcast();

        saveTask = Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE, () -> {
            if (saveBroadcast) BPLogger.info("All player data have been saved!");
            saveEveryone(async);
        }, minutes, minutes);
    }

    public void saveEveryone(boolean async) {
        BPEconomy economy = BankPlus.getBPEconomy();
        for (UUID uuid : new ArrayList<>(players.keySet())) {
            Player p = Bukkit.getPlayer(uuid);

            if (p == null) savePlayer(uuid, async);
            else economy.saveBankBalances(p, async);

            players.remove(uuid);
        }
    }
}