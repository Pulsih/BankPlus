package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.gui.GuiHolder;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.utils.SetUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class PlayerChat implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(PlayerChatEvent e) {

        Player p = e.getPlayer();
        String mess = ChatColor.stripColor(e.getMessage());

        if (mess.equalsIgnoreCase("exit")) {
            e.setCancelled(true);
            SetUtils.playerDepositing.remove(p);
            SetUtils.playerWithdrawing.remove(p);
            reopenBank(p);
            return;
        }

        if (SetUtils.playerDepositing.contains(p)) {
            try {
                Methods.deposit(p, Long.parseLong(mess));
                reopenBank(p);
            } catch (NumberFormatException ex) {
                e.setCancelled(true);
                MessageManager.invalidNumber(p);
                return;
            }
            e.setCancelled(true);
            SetUtils.playerDepositing.remove(p);
        }

        if (SetUtils.playerWithdrawing.contains(p)) {
            try {
                Methods.withdraw(p, Long.parseLong(mess));
                reopenBank(p);
            } catch (NumberFormatException ex) {
                e.setCancelled(true);
                MessageManager.invalidNumber(p);
                return;
            }
            e.setCancelled(true);
            SetUtils.playerWithdrawing.remove(p);
        }
    }

    private void reopenBank(Player p) {
        if (Values.CONFIG.isReopeningBankAfterChat()) GuiHolder.openBank(p);
    }
}