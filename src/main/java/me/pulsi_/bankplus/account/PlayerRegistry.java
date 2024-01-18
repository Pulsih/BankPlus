package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.UUID;

public class PlayerRegistry {

    private final HashMap<UUID, BPPlayer> players = new HashMap<>();

    public HashMap<UUID, BPPlayer> getPlayers() {
        return players;
    }

    public static BPPlayer get(OfflinePlayer p) {
        return BankPlus.INSTANCE().getPlayerRegistry().getPlayers().get(p.getUniqueId());
    }
}