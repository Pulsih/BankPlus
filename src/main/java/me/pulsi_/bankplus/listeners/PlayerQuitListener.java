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

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (BankPlus.isShuttingDown) return;
        Player p = e.getPlayer();

        PlayerRegistry registry = BankPlus.INSTANCE.getPlayerRegistry();
        registry.savePlayer(p);

        BukkitTask updating = registry.remove(p).getBankUpdatingTask();
        if (updating != null) updating.cancel();

        BPSets.removePlayerFromDepositing(p);
        BPSets.removePlayerFromWithdrawing(p);
        BankPlus.getBPEconomy().unloadBankBalance(p);
    }
}