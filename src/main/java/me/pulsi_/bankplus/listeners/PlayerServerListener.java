package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPSets;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.UUID;

public class PlayerServerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BPPlayerManager pManager = new BPPlayerManager(p);
        if (!pManager.isPlayerRegistered()) {
            pManager.registerPlayer();
            if (Values.CONFIG.notifyRegisteredPlayer()) BPLogger.info("Successfully registered " + p.getName() + "!");
        }
        pManager.checkForFileFixes(p, pManager);
        PlayerRegistry.loadPlayer(p);

        if (!Values.CONFIG.notifyOfflineInterest()) return;

        BigDecimal amount = new BigDecimal(0);
        for (BPEconomy economy : BPEconomy.list()) {
            BigDecimal offlineInterest = economy.getOfflineInterest(p);
            amount = amount.add(offlineInterest);
            if (offlineInterest.doubleValue() > 0d) economy.setOfflineInterest(p, BigDecimal.valueOf(0));
        }

        BigDecimal finalAmount = amount;
        if (finalAmount.doubleValue() > 0d)
            Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () ->
                            BPMessages.send(p, Values.CONFIG.getNotifyOfflineInterestMessage(), BPUtils.placeValues(finalAmount), true),
                    Values.CONFIG.getNotifyOfflineInterestDelay() * 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        BukkitTask updating = PlayerRegistry.get(p).getBankUpdatingTask();
        if (updating != null) updating.cancel();

        BPSets.removePlayerFromDepositing(p);
        BPSets.removePlayerFromWithdrawing(p);

        if (!Values.CONFIG.isSaveOnQuit()) return;

        UUID uuid = p.getUniqueId();
        BankPlus.INSTANCE().getEconomyRegistry().savePlayer(uuid, true);
        PlayerRegistry.unloadPlayer(uuid);
    }
}