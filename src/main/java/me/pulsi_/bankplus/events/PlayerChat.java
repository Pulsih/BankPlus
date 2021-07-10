package me.pulsi_.bankplus.events;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.SetUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.IOException;

public class PlayerChat implements Listener {

    private EconomyManager economyManager;
    public PlayerChat(BankPlus plugin) {
        economyManager = new EconomyManager(plugin);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        if (SetUtils.playerDepositing.contains(p.getUniqueId())) {
            String amount = e.getMessage();

            try {
                economyManager.deposit(p, Long.parseLong(amount));
                SetUtils.playerDepositing.remove(p.getUniqueId());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            e.setCancelled(true);
        }

        if (SetUtils.playerWithdrawing.contains(p.getUniqueId())) {
            String amount = e.getMessage();

            try {
                economyManager.withdraw(p, Long.parseLong(amount));
                SetUtils.playerWithdrawing.remove(p.getUniqueId());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            e.setCancelled(true);
        }
    }
}
