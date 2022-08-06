package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.guis.BanksHolder;
import me.pulsi_.bankplus.utils.SetUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            MultiEconomyManager.saveBankBalance(p);
            MultiEconomyManager.unloadBankBalance(p);
        } else {
            SingleEconomyManager.saveBankBalance(p);
            SingleEconomyManager.unloadBankBalance(p);
        }
        if (BanksHolder.tasks.containsKey(p)) BanksHolder.tasks.remove(p).cancel();
        BanksHolder.openedBank.remove(p);
        SetUtils.removePlayerFromDepositing(p);
        SetUtils.removePlayerFromWithdrawing(p);
    }
}