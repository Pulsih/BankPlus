package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.math.BigDecimal;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BPPlayerManager pManager = new BPPlayerManager(p);
        if (!pManager.isPlayerRegistered()) {
            pManager.registerPlayer();
            if (Values.CONFIG.notifyRegisteredPlayer()) BPLogger.info("Successfully registered " + p.getName() + "!");
        }
        pManager.loadPlayer();

        if (!Values.CONFIG.notifyOfflineInterest()) return;

        BigDecimal amount = new BigDecimal(0);
        BPEconomy economy = BankPlus.getBPEconomy();
        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
            BigDecimal offlineInterest = economy.getOfflineInterest(p, bankName);
            amount = amount.add(offlineInterest);
            if (offlineInterest.doubleValue() > 0d) economy.setOfflineInterest(p, BigDecimal.valueOf(0), bankName);
        }

        BigDecimal finalAmount = amount;
        if (finalAmount.doubleValue() > 0d)
            Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () ->
                            BPMessages.send(p, Values.CONFIG.getNotifyOfflineInterestMessage(), BPUtils.placeValues(finalAmount), true),
                    Values.CONFIG.getNotifyOfflineInterestDelay() * 20L);
    }
}