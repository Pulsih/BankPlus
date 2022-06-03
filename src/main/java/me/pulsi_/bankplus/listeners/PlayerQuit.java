package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.managers.EconomyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        EconomyManager.saveBankBalance(p);
        EconomyManager.playerMoney.remove(p.getUniqueId());
    }
}