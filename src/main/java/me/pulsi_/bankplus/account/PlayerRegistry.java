package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.mySQL.BPSQL;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerRegistry {

    private static final HashMap<UUID, BPPlayer> players = new HashMap<>();

    public static boolean isPlayerLoaded(OfflinePlayer p) {
        return players.containsKey(p.getUniqueId());
    }

    public static BPPlayer loadPlayer(Player p) {
        return loadPlayer(p, true);
    }

    public static BPPlayer loadPlayer(Player p, boolean wasRegistered) {
        BPPlayer bpPlayer = new BPPlayer(p);
        players.putIfAbsent(p.getUniqueId(), bpPlayer);

        if (BPSQL.isConnected()) {
            // If MySQL is enabled, load the player asynchronously, it may take few more
            // milliseconds but this will ensure to not affect server performance
            // and the player values will be visible after the player has been loaded.
            Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
                for (BPEconomy economy : BPEconomy.list()) economy.loadPlayerHolder(p, wasRegistered);
                bpPlayer.setLoaded(true); // Set the player as loaded, only once loaded, to allow him to make operations.
            });
            return bpPlayer;
        }

        for (BPEconomy economy : BPEconomy.list()) economy.loadPlayerHolder(p, wasRegistered);
        bpPlayer.setLoaded(true); // Set the player as loaded, only once loaded, to allow him to make operations.
        return bpPlayer;
    }

    public static BPPlayer unloadPlayer(UUID uuid) {
        for (BPEconomy economy : BPEconomy.list()) economy.unloadPlayerBalance(uuid);
        return players.remove(uuid);
    }

    public static BPPlayer get(OfflinePlayer p) {
        return get(p.getUniqueId());
    }

    public static BPPlayer get(UUID uuid) {
        return players.get(uuid);
    }
}