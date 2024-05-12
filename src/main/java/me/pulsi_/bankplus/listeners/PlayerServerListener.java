package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.EconomyUtils;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPMessages;
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
        boolean wasRegistered = true;
        if (!pManager.isPlayerRegistered()) {
            pManager.registerPlayer();
            if (Values.CONFIG.notifyRegisteredPlayer()) BPLogger.info("Successfully registered " + p.getName() + "!");
            wasRegistered = false;
        }
        pManager.checkForFileFixes(p, pManager);
        PlayerRegistry.loadPlayer(p, wasRegistered);

        if (!Values.CONFIG.notifyOfflineInterest()) return;

        BigDecimal amount = BigDecimal.ZERO;
        for (BPEconomy economy : BPEconomy.list()) {
            BigDecimal offlineInterest = economy.getOfflineInterest(p);
            amount = amount.add(offlineInterest);
            if (offlineInterest.compareTo(BigDecimal.ZERO) > 0) economy.setOfflineInterest(p, BigDecimal.ZERO);
        }

        BigDecimal finalAmount = amount;
        if (finalAmount.compareTo(BigDecimal.ZERO) > 0)
            Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () ->
                            BPMessages.send(p, Values.CONFIG.getNotifyOfflineInterestMessage(), BPUtils.placeValues(finalAmount), true),
                    Values.CONFIG.getNotifyOfflineInterestDelay() * 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        BPPlayer player = PlayerRegistry.get(p);
        if (player != null) {
            BukkitTask updating = player.getBankUpdatingTask();
            if (updating != null) updating.cancel();
        }

        BPSets.removePlayerFromDepositing(p);
        BPSets.removePlayerFromWithdrawing(p);

        if (!Values.CONFIG.isSaveOnQuit()) return;

        UUID uuid = p.getUniqueId();
        EconomyUtils.savePlayer(uuid, true);
        PlayerRegistry.unloadPlayer(uuid);
    }
}