package me.pulsi_.bankplus.utils;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SetUtils {

    public static Set<UUID> playerDepositing = new HashSet<>();

    public static Set<UUID> playerWithdrawing = new HashSet<>();

    public static void addPlayerToDeposit(Player p) {
        playerDepositing.add(p.getUniqueId());
    }

    public static void addPlayerToWithdraw(Player p) {
        playerWithdrawing.add(p.getUniqueId());
    }

    public static void removePlayerFromDepositing(Player p) {
        playerDepositing.remove(p.getUniqueId());
    }

    public static void removePlayerFromWithdrawing(Player p) {
        playerWithdrawing.remove(p.getUniqueId());
    }
}