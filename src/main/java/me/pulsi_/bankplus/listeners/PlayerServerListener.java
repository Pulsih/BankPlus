package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.EconomyUtils;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPSets;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;

public class PlayerServerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        boolean wasRegistered;
        BPPlayerManager pManager = new BPPlayerManager(p);
        if (pManager.isPlayerRegistered()) wasRegistered = true;
        else {
            pManager.registerPlayer();
            if (ConfigValues.isNotifyingNewPlayer()) BPLogger.Console.info("Successfully registered " + p.getName() + "!");
            wasRegistered = false;
        }

        pManager.checkForFileFixes(p, pManager);

        int loadDelay = ConfigValues.getLoadDelay();
        if (loadDelay <= 0) PlayerRegistry.loadPlayer(p, wasRegistered);
        else Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> PlayerRegistry.loadPlayer(p, wasRegistered), loadDelay);

        if (!ConfigValues.isNotifyingOfflineInterest()) return;

        BigDecimal amount = BigDecimal.ZERO;
        for (BPEconomy economy : BPEconomy.list()) {
            BigDecimal offlineInterest = economy.getOfflineInterest(p);
            amount = amount.add(offlineInterest);
            if (offlineInterest.compareTo(BigDecimal.ZERO) > 0) economy.setOfflineInterest(p, BigDecimal.ZERO);
        }

        BigDecimal finalAmount = amount;
        String mess = BPMessages.applyMessagesPrefix(ConfigValues.getOfflineInterestMessage());
        if (finalAmount.compareTo(BigDecimal.ZERO) > 0)
            Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () ->
                            BPMessages.send(p, mess, BPUtils.placeValues(finalAmount), false),
                    ConfigValues.getNotifyOfflineInterestDelay() * 20L);
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

        if (ConfigValues.isSavingOnQuit()) EconomyUtils.savePlayer(p.getUniqueId(), true);
    }
}