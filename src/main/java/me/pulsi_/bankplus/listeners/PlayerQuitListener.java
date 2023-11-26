package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.utils.BPSets;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public class PlayerQuitListener implements Listener {

    private final PlayerRegistry registry;

    public PlayerQuitListener() {
        this.registry = BankPlus.INSTANCE().getPlayerRegistry();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        BukkitTask updating = registry.get(p).getBankUpdatingTask();
        if (updating != null) updating.cancel();

        BPSets.removePlayerFromDepositing(p);
        BPSets.removePlayerFromWithdrawing(p);
    }
}