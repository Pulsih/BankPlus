package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.gui.GuiHolder;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.utils.SetUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
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

        if (SetUtils.playerDepositing.contains(p)) {

            if (mess.equalsIgnoreCase("exit")) {
                e.setCancelled(true);
                SetUtils.playerDepositing.remove(p);
                executeExitCommands(p);
                reopenBank(p);
                return;
            }

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

            if (mess.equalsIgnoreCase("exit")) {
                e.setCancelled(true);
                SetUtils.playerWithdrawing.remove(p);
                executeExitCommands(p);
                reopenBank(p);
                return;
            }

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
        if (Values.CONFIG.isReopeningBankAfterChat()) new GuiHolder().openBank(p);
    }

    private void executeExitCommands(Player p) {
        for (String cmd : Values.CONFIG.getExitCommands()) {
            if (cmd.startsWith("[CONSOLE]")) {
                String s = cmd.replace("[CONSOLE] ", "").replace("%player%", p.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
            }
            if (cmd.startsWith("[PLAYER]")) {
                String s = cmd.replace("[PLAYER] ", "");
                p.chat("/" + s);
            }
        }
    }
}