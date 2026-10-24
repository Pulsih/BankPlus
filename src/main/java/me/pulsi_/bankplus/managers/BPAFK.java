package me.pulsi_.bankplus.managers;

import com.Zrips.CMI.CMI;
import com.earth2me.essentials.Essentials;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BPAFK {

    // The interest runs asynchronously and reads these, while the main thread updates them.
    private final Map<UUID, Long> afkCooldown = new ConcurrentHashMap<>();
    private final Set<UUID> afkPlayers = ConcurrentHashMap.newKeySet();
    private BukkitTask countdownTask;
    private boolean isPlayerCountdownActive = false;

    private final BankPlus plugin;

    public BPAFK(BankPlus plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if the afk state is handled by EssentialsX or CMI. Having the option
     * enabled without the plugin installed would throw when loading its classes.
     */
    public boolean isUsingExternalAfk() {
        return (ConfigValues.isUsingEssentialsXAFK() && plugin.isEssentialsXHooked()) || (ConfigValues.isUsingCmiAfk() && plugin.isCmiHooked());
    }

    public boolean isAFK(Player p) {
        if (ConfigValues.isUsingEssentialsXAFK() && plugin.isEssentialsXHooked()) return Essentials.getPlugin(Essentials.class).getUser(p).isAfk();
        if (ConfigValues.isUsingCmiAfk() && plugin.isCmiHooked()) return CMI.getInstance().getPlayerManager().getUser(p).isAfk();
        return afkPlayers.contains(p.getUniqueId());
    }

    public void startCountdown() {
        // Reloading calls this again, make sure there is only one countdown running.
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }

        if (!ConfigValues.isIgnoringAfkPlayers() || isUsingExternalAfk()) {
            isPlayerCountdownActive = false;
            return;
        }
        isPlayerCountdownActive = true;

        // Forget the players that left.
        afkPlayers.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        afkCooldown.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);

        for (Player p : Bukkit.getOnlinePlayers()) {
            Long cooldown = afkCooldown.get(p.getUniqueId());
            if (cooldown == null) continue;

            if (cooldown > System.currentTimeMillis()) afkPlayers.remove(p.getUniqueId());
            else afkPlayers.add(p.getUniqueId());
        }

        countdownTask = Bukkit.getScheduler().runTaskLater(plugin, this::startCountdown, 20L);
    }

    public Map<UUID, Long> getAfkCooldown() {
        return afkCooldown;
    }

    public boolean isPlayerCountdownActive() {
        return isPlayerCountdownActive;
    }
}