package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.gui.GuiHolder;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.BPDebugger;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.utils.SetUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.math.BigDecimal;

public class PlayerChat implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!isTyping(p)) return;
        BPDebugger.debugChat(e);

        String message = ChatColor.stripColor(e.getMessage());
        if (hasTypedExit(message, p, e)) return;

        BigDecimal amount;
        try {
            amount = new BigDecimal(message);
        } catch (NumberFormatException ex) {
            e.setCancelled(true);
            MessageManager.invalidNumber(p);
            return;
        }

        if (Methods.isDepositing(p)) {
            SetUtils.removeFromDepositingPlayers(p);
            Methods.withdraw(p, amount);
        }
        if (Methods.isWithdrawing(p)) {
            SetUtils.removeFromWithdrawingPlayers(p);
            Methods.withdraw(p, amount);
        }
        e.setCancelled(true);
        reopenBank(p);
    }

    private boolean hasTypedExit(String message, Player p, AsyncPlayerChatEvent e) {
        if (isTyping(p) && !message.equalsIgnoreCase(Values.CONFIG.getExitMessage())) return false;
        e.setCancelled(true);
        Bukkit.getScheduler().runTask(BankPlus.getInstance(), () -> {
            SetUtils.playerDepositing.remove(p.getUniqueId());
            SetUtils.playerWithdrawing.remove(p.getUniqueId());
            executeExitCommands(p);
            reopenBank(p);
        });
        return true;
    }

    private boolean isTyping(Player p) {
        return Methods.isDepositing(p) || Methods.isWithdrawing(p);
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