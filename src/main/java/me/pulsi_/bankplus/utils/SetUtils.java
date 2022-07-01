package me.pulsi_.bankplus.utils;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SetUtils {

    public static Set<UUID> playerDepositing = new HashSet<>();

    public static Set<UUID> playerWithdrawing = new HashSet<>();

    public static void addFromDepositingPlayers(Player p) {
        playerDepositing.add(p.getUniqueId());
    }

    public static void addFromWithdrawingPlayers(Player p) {
        playerWithdrawing.add(p.getUniqueId());
    }

    public static void removeFromDepositingPlayers(Player p) {
        playerDepositing.remove(p.getUniqueId());
    }

    public static void removeFromWithdrawingPlayers(Player p) {
        playerWithdrawing.remove(p.getUniqueId());
    }
}