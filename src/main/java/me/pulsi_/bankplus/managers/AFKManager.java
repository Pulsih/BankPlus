package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class AFKManager {

    public static Map<UUID, Long> afkCooldown = new HashMap<>();

    public static List<Player> afkPlayers = new ArrayList<>();

    public static boolean isPlayerCountdownActive = false;

    public static boolean isAFK(Player p) {
        return afkPlayers.contains(p);
    }

    public static void startCountdown() {
        if (!Values.CONFIG.isIgnoringAfkPlayers()) {
            isPlayerCountdownActive = false;
            return;
        }
        isPlayerCountdownActive = true;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!afkCooldown.containsKey(p.getUniqueId())) continue;
            if (afkCooldown.get(p.getUniqueId()) < System.currentTimeMillis())
                if (!afkPlayers.contains(p)) afkPlayers.add(p);
            else
                afkPlayers.remove(p);
        }

        Bukkit.getScheduler().runTaskLater(BankPlus.getInstance(), AFKManager::startCountdown, 20L);
    }
}