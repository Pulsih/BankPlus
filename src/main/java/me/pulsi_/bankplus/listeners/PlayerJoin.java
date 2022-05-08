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

        String uuid = p.getUniqueId().toString();
        String pName = p.getName();

        if (Values.CONFIG.isStoringUUIDs()) {
            String name = plugin.players().getString("Players." + uuid + ".Name");
            String sBalance = plugin.players().getString("Players." + uuid + ".Money");
            String sOfflineInterest = plugin.players().getString("Players." + uuid + ".Offline-Interest");

            if (sBalance == null) {
                ChatUtils.log("&a&lBank&9&lPlus &2Successfully registered &f" + pName + "&a's account!");
                plugin.players().set("Players." + uuid + ".Money", startAmount);
            }
            if (isSendingOfflineInterestMessage && sOfflineInterest == null)
                plugin.players().set("Players." + uuid + ".Offline-Interest", 0);
            if (name == null) plugin.players().set("Players." + uuid + ".Name", pName);

            plugin.savePlayers();
        } else {
            String sBalance = plugin.players().getString("Players." + pName + ".Money");
            String sOfflineInterest = plugin.players().getString("Players." + pName + ".Offline-Interest");

            if (sBalance == null) {
                ChatUtils.log("&a&lBank&9&lPlus &2Successfully registered &f" + pName + "&a's account!");
                plugin.players().set("Players." + pName + ".Money", startAmount);
            }
            if (isSendingOfflineInterestMessage && sOfflineInterest == null)
                plugin.players().set("Players." + pName + ".Offline-Interest", 0);

            plugin.savePlayers();

        }

        if (!isSendingOfflineInterestMessage) return;
        long offlineInterest = EconomyManager.getInstance().getOfflineInterest(p);
        if (offlineInterest <= 0) return;

        long delay = Values.CONFIG.getNotifyOfflineInterestDelay();
        String message = ChatUtils.color(Values.CONFIG.getNotifyOfflineInterestMessage());

        if (delay != 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () ->
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
        EconomyManager.getInstance().setOfflineInterest(p, 0);
    }
}