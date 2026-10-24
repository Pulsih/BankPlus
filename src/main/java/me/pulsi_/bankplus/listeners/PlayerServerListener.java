package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.EconomyUtils;
import me.pulsi_.bankplus.sql.BPSQL;
import me.pulsi_.bankplus.utils.BPLogger;
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

    private static final int LOAD_ATTEMPTS = 12; // 5 seconds apart, so a short database outage doesn't lock players out.

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> loadPlayerData(p, 1));
    }

    private void loadPlayerData(Player p, int attempt) {
        try {
            if (!BankPlus.INSTANCE().isEnabled()) return;

            boolean wasRegistered = BPSQL.isRegistered(p, ConfigValues.getMainGuiName());
            if (!wasRegistered && ConfigValues.isNotifyingNewPlayer())
                    BPLogger.Console.info("Successfully registered " + p.getName() + "!");

            BPSQL.fillRecords(p);

            if (!BankPlus.INSTANCE().isEnabled()) return;

            int loadDelay = ConfigValues.getLoadDelay();
            if (loadDelay <= 0) PlayerRegistry.loadPlayer(p, wasRegistered);
            else Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> PlayerRegistry.loadPlayer(p, wasRegistered), loadDelay);

            if (!ConfigValues.isNotifyingOfflineInterest()) return;

            BigDecimal amount = BigDecimal.ZERO;
            for (BPEconomy economy : BPEconomy.list()) {
                BigDecimal offlineInterest = BPSQL.getInterest(p, economy.getOriginBank().getIdentifier());
                if (offlineInterest.compareTo(BigDecimal.ZERO) <= 0) continue;

                amount = amount.add(offlineInterest);
                BPSQL.setInterest(p, economy.getOriginBank().getIdentifier(), BigDecimal.ZERO);
            }

            BigDecimal finalAmount = amount;
            if (finalAmount.compareTo(BigDecimal.ZERO) > 0 && BankPlus.INSTANCE().isEnabled())
                Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () ->
                                BPMessages.sendMessage(p, ConfigValues.getOfflineInterestMessage(), BPUtils.placeValues(finalAmount)),
                        ConfigValues.getNotifyOfflineInterestDelay() * 20L);
        } catch (BPSQL.DataException ex) {
            // Never load the player with default values here, the real balance would be overwritten when saving.
            if (attempt >= LOAD_ATTEMPTS || !p.isOnline() || !BankPlus.INSTANCE().isEnabled()) {
                BPLogger.Console.error(ex, "Could not load the data of " + p.getName() + ", their bank will stay unavailable until they rejoin.");
                return;
            }

            if (attempt == 1) BPLogger.Console.warn("Could not load the data of " + p.getName() + ", retrying every 5 seconds for a minute.");
            Bukkit.getScheduler().runTaskLaterAsynchronously(BankPlus.INSTANCE(), () -> loadPlayerData(p, attempt + 1), 100L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        BPPlayer player = PlayerRegistry.get(p);
        if (player != null) {
            BukkitTask updating = player.getBankUpdatingTask();
            if (updating != null) updating.cancel();

            player.setDepositing(false);
            player.setWithdrawing(false);
        }

        if (ConfigValues.isSavingOnQuit()) EconomyUtils.savePlayer(p, true);
    }
}