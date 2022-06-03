package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.managers.AFKManager;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AFKListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        long time = System.currentTimeMillis() + Methods.millisecondsInMinutes(Values.CONFIG.getAfkPlayersTime());
        AFKManager.afkCooldown.put(p.getUniqueId(), time);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        long time = System.currentTimeMillis() + Methods.millisecondsInMinutes(Values.CONFIG.getAfkPlayersTime());
        AFKManager.afkCooldown.put(p.getUniqueId(), time);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        long time = System.currentTimeMillis() + Methods.millisecondsInMinutes(Values.CONFIG.getAfkPlayersTime());
        AFKManager.afkCooldown.put(p.getUniqueId(), time);
    }
}