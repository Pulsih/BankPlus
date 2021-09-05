package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.MapUtils;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private BankPlus plugin;
    public PlayerJoin(BankPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler ( priority = EventPriority.LOWEST )
    public void onJoin(PlayerJoinEvent e) {

        final Player p = e.getPlayer();
        final long startAmount = plugin.config().getLong("General.Join-Start-Amount");
        final String name = plugin.players().getString("Players." + p.getUniqueId() + ".Name");
        final String sBalance = plugin.players().getString("Players." + p.getUniqueId() + ".Money");
        final String sOfflineInterest = plugin.players().getString("Players." + p.getUniqueId() + ".Offline-Interest");
        final boolean isSendingOfflineInterestMessage = plugin.config().getBoolean("General.Offline-Interest-Earned-Message.Enabled");

        if (name == null) {
            plugin.players().set("Players." + p.getUniqueId() + ".Name", p.getName());
            plugin.savePlayers();
        }
        if (sBalance == null) {
            plugin.players().set("Players." + p.getUniqueId() + ".Money", startAmount);
            plugin.savePlayers();
        }
        if (isSendingOfflineInterestMessage)
            if (sOfflineInterest == null) {
                plugin.players().set("Players." + p.getUniqueId() + ".Offline-Interest", 0);
                plugin.savePlayers();
            }

        if (isSendingOfflineInterestMessage) {
            final EconomyManager economy = new EconomyManager(plugin);
            final long delay = plugin.config().getLong("General.Offline-Interest-Earned-Message.Delay");
            final long offlineInterest = economy.getOfflineInterest(p);
            final String message = ChatUtils.color(plugin.config().getString("General.Offline-Interest-Earned-Message.Message"));
            if (delay != 0) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> p.sendMessage(message
                        .replace("%amount%", MethodUtils.formatCommas(offlineInterest))
                        .replace("%amount_formatted%", MethodUtils.format(offlineInterest, plugin))
                        .replace("%amount_formatted_long%", MethodUtils.formatLong(offlineInterest, plugin))), delay * 20L);
            } else {
                p.sendMessage(message
                        .replace("%amount%", MethodUtils.formatCommas(offlineInterest))
                        .replace("%amount_formatted%", MethodUtils.format(offlineInterest, plugin))
                        .replace("%amount_formatted_long%", MethodUtils.formatLong(offlineInterest, plugin)));
            }
            if (offlineInterest != 0)
                economy.setOfflineInterest(p, 0);
        }

        long balance = Long.parseLong(sBalance);
        MapUtils.BALANCE.put(p.getUniqueId(), balance);
    }
}