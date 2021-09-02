package me.pulsi_.bankplus.events;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.MethodUtils;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEvent implements Listener {

    private BankPlus plugin;
    public JoinEvent(BankPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player p = e.getPlayer();
        long startAmount = plugin.getConfiguration().getLong("General.Join-Start-Amount");
        final String name = plugin.getPlayers().getString("Players." + p.getUniqueId() + ".Name");
        final String nameMoney = plugin.getPlayers().getString("Players." + p.getName() + ".Money");
        final String uuidMoney = plugin.getPlayers().getString("Players." + p.getUniqueId() + ".Money");
        final String uuidOfflineInterest = plugin.getPlayers().getString("Players." + p.getUniqueId() + ".Offline-Interest");
        final String nameOfflineInterest = plugin.getPlayers().getString("Players." + p.getName() + ".Offline-Interest");
        final boolean isUUIDStorage = plugin.getConfiguration().getBoolean("General.Use-UUID");
        final boolean isSendingOfflineInterestMessage = plugin.getConfiguration().getBoolean("General.Offline-Interest-Earned-Message.Enabled");

        if (isUUIDStorage) {
            if (name == null) {
                plugin.getPlayers().set("Players." + p.getUniqueId() + ".Name", p.getName());
                plugin.savePlayers();
            }
            if (uuidMoney == null) {
                plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", startAmount);
                plugin.savePlayers();
            }
            if (isSendingOfflineInterestMessage)
                if (uuidOfflineInterest == null) {
                    plugin.getPlayers().set("Players." + p.getUniqueId() + ".Offline-Interest", 0);
                    plugin.savePlayers();
                }
        } else {
            if (nameMoney == null) {
                plugin.getPlayers().set("Players." + p.getName() + ".Money", startAmount);
                plugin.savePlayers();
            }
            if (isSendingOfflineInterestMessage)
                if (nameOfflineInterest == null) {
                    plugin.getPlayers().set("Players." + p.getName() + ".Offline-Interest", 0);
                    plugin.savePlayers();
                }
        }

        if (isSendingOfflineInterestMessage) {
            final long delay = plugin.getConfiguration().getLong("General.Offline-Interest-Earned-Message.Delay");
            final long offlineInterest = new EconomyManager(plugin).getOfflineInterest(p);
            if (delay != 0) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> p.sendMessage(ChatUtils.c(plugin.getConfiguration().getString("General.Offline-Interest-Earned-Message.Message")
                        .replace("%amount%", MethodUtils.formatCommas(offlineInterest))
                        .replace("%amount_formatted%", MethodUtils.format(offlineInterest, plugin))
                        .replace("%amount_formatted_long%", MethodUtils.formatLong(offlineInterest, plugin)))), delay * 20L);
            } else {
                p.sendMessage(ChatUtils.c(plugin.getConfiguration().getString("General.Offline-Interest-Earned-Message.Message")
                        .replace("%amount%", MethodUtils.formatCommas(offlineInterest))
                        .replace("%amount_formatted%", MethodUtils.format(offlineInterest, plugin))
                        .replace("%amount_formatted_long%", MethodUtils.formatLong(offlineInterest, plugin))));
            }
            new EconomyManager(plugin).setOfflineInterest(p, 0);
        }
    }
}