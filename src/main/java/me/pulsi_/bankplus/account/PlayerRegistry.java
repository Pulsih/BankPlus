package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.economy.BPEconomy;
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
        for (BPEconomy economy : BPEconomy.list()) economy.loadPlayerHolder(p, wasRegistered);
        BPPlayer bpPlayer = new BPPlayer(p);
        players.putIfAbsent(p.getUniqueId(), bpPlayer);
        return bpPlayer;
    }

    public static BPPlayer unloadPlayer(UUID uuid) {
        for (BPEconomy economy : BPEconomy.list()) economy.unloadPlayerBalance(uuid);
        return players.remove(uuid);
    }

    public static BPPlayer get(OfflinePlayer p) {
        return players.get(p.getUniqueId());
    }
}