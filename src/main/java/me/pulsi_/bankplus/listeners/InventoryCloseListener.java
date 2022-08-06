package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.guis.BanksHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        BanksHolder.openedBank.remove(p);
        if (BanksHolder.tasks.containsKey(p)) BanksHolder.tasks.remove(p).cancel();
    }
}