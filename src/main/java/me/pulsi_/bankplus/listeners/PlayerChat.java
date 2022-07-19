package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.guis.BanksHolder;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.BPDebugger;
import me.pulsi_.bankplus.utils.BPMethods;
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
            MessageManager.send(p, "Invalid-Number");
            return;
        }

        if (BPMethods.isDepositing(p)) {
            SetUtils.removeFromDepositingPlayers(p);
            BPMethods.deposit(p, amount);
        }
        if (BPMethods.isWithdrawing(p)) {
            SetUtils.removeFromWithdrawingPlayers(p);
            BPMethods.withdraw(p, amount);
        }
        e.setCancelled(true);
        reopenBank(p);
    }

    private boolean hasTypedExit(String message, Player p, AsyncPlayerChatEvent e) {
        if (isTyping(p) && !message.equalsIgnoreCase(Values.CONFIG.getExitMessage())) return false;
        e.setCancelled(true);
        SetUtils.playerDepositing.remove(p.getUniqueId());
        SetUtils.playerWithdrawing.remove(p.getUniqueId());
        executeExitCommands(p);
        reopenBank(p);
        return true;
    }

    private boolean isTyping(Player p) {
        return BPMethods.isDepositing(p) || BPMethods.isWithdrawing(p);
    }

    private void reopenBank(Player p) {
        Bukkit.getScheduler().runTask(BankPlus.getInstance(), () -> {
            String identifier = BanksHolder.openedInventory.getOrDefault(p, Values.CONFIG.getMainGuiName());
            if (Values.CONFIG.isReopeningBankAfterChat()) new BanksHolder().openBank(p, identifier);
        });
    }

    private void executeExitCommands(Player p) {
        Bukkit.getScheduler().runTask(BankPlus.getInstance(), () -> {
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
        });
    }
}