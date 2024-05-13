package me.pulsi_.bankplus.listeners.playerChat;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankGui;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPSets;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;

public class PlayerChatMethod {

    public static void process(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!isTyping(p)) return;

        BPPlayer player = PlayerRegistry.get(p);

        BankGui openedBank = player.getOpenedBankGui();
        if (openedBank == null) {
            BPLogger.warn("BankPlus chat-transaction failed for player " + p.getName() + ", he did not have an opened bank. (Try again and if the problem persist contact the developer)");
            BPSets.removePlayerFromDepositing(p);
            BPSets.removePlayerFromWithdrawing(p);
            return;
        }

        String message = ChatColor.stripColor(e.getMessage());

        if (hasTypedExit(message, p, e)) {
            reopenBank(p, openedBank);
            return;
        }
        e.setCancelled(true);

        BigDecimal amount;
        try {
            amount = new BigDecimal(message);
        } catch (NumberFormatException ex) {
            BPMessages.send(p, "Invalid-Number");
            return;
        }

        if (BPUtils.isDepositing(p)) {
            BPSets.removePlayerFromDepositing(p);
            openedBank.getOriginBank().getBankEconomy().deposit(p, amount);
        }
        if (BPUtils.isWithdrawing(p)) {
            BPSets.removePlayerFromWithdrawing(p);
            openedBank.getOriginBank().getBankEconomy().withdraw(p, amount);
        }
        reopenBank(p, openedBank);
    }

    private static boolean hasTypedExit(String message, Player p, AsyncPlayerChatEvent e) {
        if (isTyping(p) && !message.toLowerCase().contains(Values.CONFIG.getChatExitMessage().toLowerCase())) return false;
        e.setCancelled(true);
        executeExitCommands(p);
        return true;
    }

    private static boolean isTyping(Player p) {
        return BPUtils.isDepositing(p) || BPUtils.isWithdrawing(p);
    }

    public static void reopenBank(Player p, BankGui openedBankGui) {
        Bukkit.getScheduler().runTask(BankPlus.INSTANCE(), () -> {
            BukkitTask task = PlayerRegistry.get(p).getClosingTask();
            if (task != null) task.cancel();

            BPSets.playerDepositing.remove(p.getUniqueId());
            BPSets.playerWithdrawing.remove(p.getUniqueId());
            if (Values.CONFIG.isReopeningBankAfterChat()) openedBankGui.openBankGui(p, true);
        });
    }

    private static void executeExitCommands(Player p) {
        Bukkit.getScheduler().runTask(BankPlus.INSTANCE(), () -> {
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
