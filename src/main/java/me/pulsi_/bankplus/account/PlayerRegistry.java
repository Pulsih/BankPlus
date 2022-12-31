package me.pulsi_.bankplus.account;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerRegistry {

    private final HashMap<UUID, BankPlusPlayer> players = new HashMap<>();

    public void put(Player p, BankPlusPlayer player) {
        players.put(p.getUniqueId(), player);
    }

    public BankPlusPlayer get(Player p) {
        if (!contains(p)) {
            BankPlusPlayerFiles files = new BankPlusPlayerFiles(p);
            files.registerPlayer();
            BankPlusPlayer player = new BankPlusPlayer(p, files.getPlayerFile(), files.getPlayerConfig());
            put(p, player);
            return player;
        }
        return players.get(p.getUniqueId());
    }

    public BankPlusPlayer remove(Player p) {
        return players.remove(p.getUniqueId());
    }

    public boolean contains(Player p) {
        return players.containsKey(p.getUniqueId());
    }

    public HashMap<UUID, BankPlusPlayer> getPlayers() {
        return players;
    }
}
