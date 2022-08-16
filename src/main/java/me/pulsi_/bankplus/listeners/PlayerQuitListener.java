package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayer;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.utils.BPSets;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            MultiEconomyManager economyManager = new MultiEconomyManager(p);
            economyManager.saveBankBalance(true);
            economyManager.unloadBankBalance();
        } else {
            SingleEconomyManager economyManager = new SingleEconomyManager(p);
            economyManager.saveBankBalance(true);
            economyManager.unloadBankBalance();
        }
        BankPlusPlayer player = BankPlus.instance().getPlayers().remove(p.getUniqueId());
        BukkitTask task = player.getInventoryUpdateTask();
        if (task != null) task.cancel();
        BPSets.removePlayerFromDepositing(p);
        BPSets.removePlayerFromWithdrawing(p);
    }
}