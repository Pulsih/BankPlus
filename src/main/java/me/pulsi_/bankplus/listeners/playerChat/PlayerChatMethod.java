package me.pulsi_.bankplus.listeners.playerChat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankGui;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;

public class PlayerChatMethod {

    public static void process(AsyncChatEvent e) {
        Player p = e.getPlayer();
        BPPlayer bpPlayer = PlayerRegistry.get(p);
        if (bpPlayer == null) return;

        if (!bpPlayer.isDepositing() && !bpPlayer.isWithdrawing()) return;
        e.setCancelled(true);
        e.viewers().clear(); // Try stopping chat plugins from still sending the message.

        Bank openedBank = bpPlayer.getOpenedBank();
        if (openedBank == null) {
            removeFromTyping(bpPlayer);
            return;
        }

        MiniMessage mm = MiniMessage.miniMessage();
        String text = mm.serialize(e.message());
        // If some chat format plugin is adding a "." at the end, remove it.
        if (text.endsWith(".")) text = text.substring(0, text.length() - 1);

        if (hasTypedExit(text, p)) reopenBank(bpPlayer, openedBank.getBankGui());
        else {
            BigDecimal amount;
            try {
                amount = new BigDecimal(text);
            } catch (NumberFormatException ex) {
                BPMessages.sendIdentifier(p, "Invalid-Number");
                return;
            }

            BPEconomy economy = openedBank.getBankEconomy();
            if (bpPlayer.isDepositing()) economy.deposit(p, amount);
            else economy.withdraw(p, amount);

            reopenBank(bpPlayer, openedBank.getBankGui());
        }
    }

    private static boolean hasTypedExit(String message, Player p) {
        if (!message.toLowerCase().contains(ConfigValues.getChatExitMessage().toLowerCase())) return false;
        executeExitCommands(p);
        return true;
    }

    private static void removeFromTyping(BPPlayer bpPlayer) {
        bpPlayer.setDepositing(false);
        bpPlayer.setWithdrawing(false);
    }

    public static void reopenBank(BPPlayer bpPlayer, BankGui openedBankGui) {
        Bukkit.getScheduler().runTask(BankPlus.INSTANCE(), () -> {
            BukkitTask task = bpPlayer.getClosingTask();
            if (task != null) task.cancel();

            removeFromTyping(bpPlayer);
            if (ConfigValues.isReopeningBankAfterChat() && ConfigValues.isGuiModuleEnabled()) openedBankGui.openBankGui(bpPlayer.getPlayer(), true);
        });
    }

    private static void executeExitCommands(Player p) {
        Bukkit.getScheduler().runTask(BankPlus.INSTANCE(), () -> {
            for (String cmd : ConfigValues.getExitCommands()) {
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
