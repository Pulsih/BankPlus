package me.pulsi_.bankplus.events;

import me.pulsi_.bankplus.BankPlus;
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
        final boolean isUUIDStorage = plugin.getConfiguration().getBoolean("General.Use-UUID");

        if (isUUIDStorage) {
            if (name == null) {
                plugin.getPlayers().set("Players." + p.getUniqueId() + ".Name", p.getName());
                plugin.savePlayers();
            }
            if (uuidMoney == null) {
                plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", startAmount);
                plugin.savePlayers();
            }
        } else {
            if (nameMoney == null) {
                plugin.getPlayers().set("Players." + p.getName() + ".Money", startAmount);
                plugin.savePlayers();
            }
        }
    }
}