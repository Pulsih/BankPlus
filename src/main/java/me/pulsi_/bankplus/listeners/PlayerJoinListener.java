package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.math.BigDecimal;

public class PlayerJoinListener implements Listener {

    private final BPEconomy economy;

    public PlayerJoinListener() {
        economy = BankPlus.getBPEconomy();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BPPlayerManager pManager = new BPPlayerManager(p);
        if (!pManager.isPlayerRegistered() && Values.CONFIG.notifyRegisteredPlayer()) BPLogger.info("Successfully registered " + p.getName() + "!");

        BPPlayer player = new BPPlayer(p);
        BankPlus.INSTANCE.getPlayerRegistry().put(p, player);

        File file = pManager.getPlayerFile();
        FileConfiguration config = pManager.getPlayerConfig(file);
        String sOfflineInterest = config.getString("interest");
        String sName = config.getString("name");
        String debt = config.getString("debt");

        boolean hasChanges = false;

        if (Values.CONFIG.notifyOfflineInterest() && sOfflineInterest == null) {
            config.set("interest", "0");
            hasChanges = true;
        }
        if (sName == null) {
            config.set("name", p.getName());
            hasChanges = true;
        }
        if (debt == null) {
            config.set("debt", "0");
            hasChanges = true;
        }

        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
            String sBalance = config.getString("banks." + bankName + ".money");
            String sLevel = config.getString("banks." + bankName + ".level");
            if (sBalance == null) {
                BigDecimal amount = Values.CONFIG.getMainGuiName().equals(bankName) ? Values.CONFIG.getStartAmount() : BigDecimal.valueOf(0);
                config.set("banks." + bankName + ".money", BPFormatter.formatBigDouble(amount));
                hasChanges = true;
            }
            if (sLevel == null) {
                config.set("banks." + bankName + ".level", 1);
                hasChanges = true;
            }
        }

        if (Values.CONFIG.notifyOfflineInterest() && sOfflineInterest != null) {
            BigDecimal offlineInterest = new BigDecimal(sOfflineInterest);
            if (offlineInterest.doubleValue() > 0) {
                Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () ->
                        BPMessages.send(p, Values.CONFIG.getNotifyOfflineInterestMessage(), BPUtils.placeValues(offlineInterest), true),
                        Values.CONFIG.getNotifyOfflineInterestDelay() * 20L);

                config.set("interest", "0");
                hasChanges = true;
            }
        }

        economy.loadBankBalance(p, config);
        if (hasChanges) pManager.savePlayerFile(config, file, true);
    }
}