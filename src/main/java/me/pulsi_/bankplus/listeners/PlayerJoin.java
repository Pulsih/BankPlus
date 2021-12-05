package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final BankPlus plugin;

    public PlayerJoin(BankPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player p = e.getPlayer();
        long startAmount = Values.CONFIG.getStartAmount();
        boolean isSendingOfflineInterestMessage = Values.CONFIG.isNotifyOfflineInterest();

        if (Values.CONFIG.isStoringUUIDs()) {
            String name = plugin.players().getString("Players." + p.getUniqueId() + ".Name");
            String sBalance = plugin.players().getString("Players." + p.getUniqueId() + ".Money");
            String sOfflineInterest = plugin.players().getString("Players." + p.getUniqueId() + ".Offline-Interest");

            if (name == null) {
                plugin.players().set("Players." + p.getUniqueId() + ".Name", p.getName());
                plugin.savePlayers();
            }
            if (sBalance == null) {
                ChatUtils.consoleMessage("&a&lBank&9&lPlus &2Successfully registered &f" + p.getName() + "&a's account!");
                plugin.players().set("Players." + p.getUniqueId() + ".Money", startAmount);
                plugin.savePlayers();
            }
            if (isSendingOfflineInterestMessage)
                if (sOfflineInterest == null) {
                    plugin.players().set("Players." + p.getUniqueId() + ".Offline-Interest", 0);
                    plugin.savePlayers();
                }
        } else {
            String sBalance = plugin.players().getString("Players." + p.getName() + ".Money");
            String sOfflineInterest = plugin.players().getString("Players." + p.getName() + ".Offline-Interest");

            if (sBalance == null) {
                ChatUtils.consoleMessage("&a&lBank&9&lPlus &2Successfully registered &f" + p.getName() + "&a's account!");
                plugin.players().set("Players." + p.getName() + ".Money", startAmount);
                plugin.savePlayers();
            }
            if (isSendingOfflineInterestMessage)
                if (sOfflineInterest == null) {
                    plugin.players().set("Players." + p.getName() + ".Offline-Interest", 0);
                    plugin.savePlayers();
                }
        }

        if (isSendingOfflineInterestMessage) {
            long delay = Values.CONFIG.getNotifyOfflineInterestDelay();
            long offlineInterest = EconomyManager.getInstance().getOfflineInterest(p);
            String message = ChatUtils.color(Values.CONFIG.getNotifyOfflineInterestMessage());

            if (delay != 0) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> p.sendMessage(message
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
            if (offlineInterest != 0) EconomyManager.getInstance().setOfflineInterest(p, 0);
        }
    }
}