package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.BPPlayerFiles;
import me.pulsi_.bankplus.economy.MultiEconomyManager;
import me.pulsi_.bankplus.economy.OfflineInterestManager;
import me.pulsi_.bankplus.economy.SingleEconomyManager;
import me.pulsi_.bankplus.utils.*;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.math.BigDecimal;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BPPlayerFiles files = new BPPlayerFiles(p);
        if (!files.isPlayerRegistered()) BPLogger.info("Successfully registered " + p.getName() + "!");

        BPPlayer player = new BPPlayer(p, files.getPlayerFile(), files.getPlayerConfig());
        BankPlus.INSTANCE.getPlayerRegistry().put(p, player);

        FileConfiguration config = player.getPlayerConfig();
        String sOfflineInterest = config.getString("Offline-Interest");
        String sName = config.getString("Account-Name");
        String debt = config.getString("Debt");
        boolean hasChanges = false;

        if (Values.CONFIG.isNotifyOfflineInterest() && sOfflineInterest == null) {
            config.set("Offline-Interest", BPFormatter.formatBigDouble(BigDecimal.valueOf(0)));
            hasChanges = true;
        }
        if (sName == null) {
            config.set("Account-Name", p.getName());
            hasChanges = true;
        }
        if (debt == null) {
            config.set("Debt", BPFormatter.formatBigDouble(BigDecimal.valueOf(0)));
            hasChanges = true;
        }

        if (Values.MULTIPLE_BANKS.isMultipleBanksEnabled()) {
            for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
                String sBalance = config.getString("Banks." + bankName + ".Money");
                String sLevel = config.getString("Banks." + bankName + ".Level");
                if (sBalance == null) {
                    if (!Values.CONFIG.getMainGuiName().equals(bankName)) config.set("Banks." + bankName + ".Money", BPFormatter.formatBigDouble(BigDecimal.valueOf(0)));
                    else config.set("Banks." + bankName + ".Money", BPFormatter.formatBigDouble(Values.CONFIG.getStartAmount()));
                    hasChanges = true;
                }
                if (sLevel == null) {
                    config.set("Banks." + bankName + ".Level", 1);
                    hasChanges = true;
                }
            }
            new MultiEconomyManager(p).loadBankBalance();
        } else {
            String sBalance = config.getString("Money");
            if (sBalance == null) {
                config.set("Money", BPFormatter.formatBigDouble(Values.CONFIG.getStartAmount()));
                hasChanges = true;
            }
            for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
                String sLevel = config.getString("Banks." + bankName + ".Level");
                if (sLevel == null) {
                    config.set("Banks." + bankName + ".Level", 1);
                    hasChanges = true;
                }
            }
            new SingleEconomyManager(p).loadBankBalance();
        }
        if (hasChanges) files.savePlayerFile(config, true);

        offlineInterestMessage(p);
    }

    private void offlineInterestMessage(Player p) {
        if (!Values.CONFIG.isNotifyOfflineInterest()) return;

        OfflineInterestManager interestManager = new OfflineInterestManager(p);
        BigDecimal offlineInterest = interestManager.getOfflineInterest();
        if (offlineInterest.doubleValue() <= 0) return;

        long delay = Values.CONFIG.getNotifyOfflineInterestDelay();

        if (delay == 0) BPMessages.send(p, Values.CONFIG.getNotifyOfflineInterestMessage(), BPUtils.placeValues(offlineInterest), true);
        else Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () ->
                BPMessages.send(p, Values.CONFIG.getNotifyOfflineInterestMessage(), BPUtils.placeValues(offlineInterest), true), delay * 20L);
        interestManager.setOfflineInterest(new BigDecimal(0), true);
    }
}