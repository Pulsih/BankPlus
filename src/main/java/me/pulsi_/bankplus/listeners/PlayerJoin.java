package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        String name = p.getName();

        if (Values.CONFIG.isStoringUUIDs())
            registerPlayer(p, uuid.toString());
        else
            registerPlayer(p, name);

        EconomyManager.playerMoney.put(uuid, EconomyManager.getBankBalance(p));
        offlineInterestMessage(p);
    }

    private void registerPlayer(Player p, String identifier) {
        boolean hasChanges = false;

        FileConfiguration players = BankPlus.getCm().getConfig("players");
        String sBalance = players.getString("Players." + identifier + ".Money");
        String sOfflineInterest = players.getString("Players." + identifier + ".Offline-Interest");

        if (sBalance == null) {
            ChatUtils.log("&a&lBank&9&lPlus &2Successfully registered &f" + p.getName() + "&a's account!");
            players.set("Players." + identifier + ".Money", Values.CONFIG.getStartAmount());
            hasChanges = true;
        }
        if (Values.CONFIG.isNotifyOfflineInterest() && sOfflineInterest == null) {
            players.set("Players." + identifier + ".Offline-Interest", 0);
            hasChanges = true;
        }

        if (hasChanges) BankPlus.getCm().savePlayers();
    }

    private void offlineInterestMessage(Player p) {
        if (!Values.CONFIG.isNotifyOfflineInterest()) return;
        long offlineInterest = EconomyManager.getOfflineInterest(p);
        if (offlineInterest <= 0) return;

        long delay = Values.CONFIG.getNotifyOfflineInterestDelay();
        String message = ChatUtils.color(Values.CONFIG.getNotifyOfflineInterestMessage());

        if (delay != 0) {
            Bukkit.getScheduler().runTaskLater(BankPlus.getInstance(), () ->
                    p.sendMessage(message
                            .replace("%amount%", Methods.formatCommas(offlineInterest))
                            .replace("%amount_formatted%", Methods.format(offlineInterest))
                            .replace("%amount_formatted_long%", Methods.formatLong(offlineInterest))
                    ), delay * 20L);
        } else {
            p.sendMessage(message
                    .replace("%amount%", Methods.formatCommas(offlineInterest))
                    .replace("%amount_formatted%", Methods.format(offlineInterest))
                    .replace("%amount_formatted_long%", Methods.formatLong(offlineInterest))
            );
        }
        EconomyManager.setOfflineInterest(p, 0);
    }
}