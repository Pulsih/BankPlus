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

    public static void loadPlayer(Player p) {
        for (BPEconomy economy : BPEconomy.list()) economy.loadPlayerBalance(p);
        players.putIfAbsent(p.getUniqueId(), new BPPlayer(p));
    }

    public static void unloadPlayer(UUID uuid) {
        players.remove(uuid);
        for (BPEconomy economy : BPEconomy.list()) economy.unloadPlayerBalance(uuid);
    }

    public static BPPlayer get(OfflinePlayer p) {
        return players.get(p.getUniqueId());
    }
}