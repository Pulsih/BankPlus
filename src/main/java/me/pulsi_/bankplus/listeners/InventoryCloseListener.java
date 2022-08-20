package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitTask;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        BankPlusPlayer player = BankPlus.instance().getPlayers().get(p.getUniqueId());
        if (player == null) return;

        BukkitTask task = player.getInventoryUpdateTask();
        if (task != null) task.cancel();

        player.setInventoryUpdateTask(null);
        player.setOpenedBank(null);
    }
}