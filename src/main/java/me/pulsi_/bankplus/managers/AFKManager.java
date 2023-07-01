package me.pulsi_.bankplus.managers;

import com.earth2me.essentials.Essentials;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class AFKManager {

    private final Map<UUID, Long> afkCooldown = new HashMap<>();
    private final List<Player> afkPlayers = new ArrayList<>();
    private boolean isPlayerCountdownActive = false;

    private final BankPlus plugin;

    public AFKManager(BankPlus plugin) {
        this.plugin = plugin;
    }

    public boolean isAFK(Player p) {
        return Values.CONFIG.useEssentialsXAFK() ? Essentials.getPlugin(Essentials.class).getUser(p).isAfk() : afkPlayers.contains(p);
    }

    public void startCountdown() {
        if (!Values.CONFIG.isIgnoringAfkPlayers() || Values.CONFIG.useEssentialsXAFK()) {
            isPlayerCountdownActive = false;
            return;
        }
        isPlayerCountdownActive = true;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!afkCooldown.containsKey(p.getUniqueId())) continue;
            if (afkCooldown.get(p.getUniqueId()) > System.currentTimeMillis()) afkPlayers.remove(p);
            else {
                if (!afkPlayers.contains(p)) afkPlayers.add(p);
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, this::startCountdown, 20L);
    }

    public Map<UUID, Long> getAfkCooldown() {
        return afkCooldown;
    }

    public boolean isPlayerCountdownActive() {
        return isPlayerCountdownActive;
    }
}