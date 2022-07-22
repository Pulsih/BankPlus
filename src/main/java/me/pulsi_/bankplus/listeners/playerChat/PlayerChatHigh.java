package me.pulsi_.bankplus.listeners.playerChat;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
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

public class PlayerChatHigh implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!isTyping(p)) return;
        BPDebugger.debugChat(e);

        String identifier = BanksHolder.openedInventory.get(p);
        String message = ChatColor.stripColor(e.getMessage());
        if (hasTypedExit(message, p, e, identifier)) return;

        BigDecimal amount;
        try {
            amount = new BigDecimal(message);
        } catch (NumberFormatException ex) {
            e.setCancelled(true);
            MessageManager.send(p, "Invalid-Number");
            return;
        }
        e.setCancelled(true);

        boolean isMulti = Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled();

        if (BPMethods.isDepositing(p)) {
            SetUtils.removeFromDepositingPlayers(p);
            if (isMulti) MultiEconomyManager.deposit(p, amount, identifier);
            else SingleEconomyManager.deposit(p, amount);
        }
        if (BPMethods.isWithdrawing(p)) {
            SetUtils.removeFromWithdrawingPlayers(p);
            if (isMulti) MultiEconomyManager.withdraw(p, amount, identifier);
            else SingleEconomyManager.withdraw(p, amount);
        }
        reopenBank(p, identifier);
    }

    private boolean hasTypedExit(String message, Player p, AsyncPlayerChatEvent e, String identifier) {
        if (isTyping(p) && !message.equalsIgnoreCase(Values.CONFIG.getExitMessage())) return false;
        e.setCancelled(true);
        SetUtils.playerDepositing.remove(p.getUniqueId());
        SetUtils.playerWithdrawing.remove(p.getUniqueId());
        executeExitCommands(p);
        reopenBank(p, identifier);
        return true;
    }

    private boolean isTyping(Player p) {
        return BPMethods.isDepositing(p) || BPMethods.isWithdrawing(p);
    }

    private void reopenBank(Player p, String identifier) {
        Bukkit.getScheduler().runTask(BankPlus.getInstance(), () -> {
            if (Values.CONFIG.isReopeningBankAfterChat()) BanksHolder.openBank(p, identifier);
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