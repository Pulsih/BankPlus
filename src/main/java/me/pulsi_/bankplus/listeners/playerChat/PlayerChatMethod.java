package me.pulsi_.bankplus.listeners.playerChat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.bankSystem.BankGui;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPSets;
import me.pulsi_.bankplus.utils.BPUtils;
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
        if (!BPUtils.isDepositing(p) && !BPUtils.isWithdrawing(p)) return;

        BankGui openedBank = PlayerRegistry.get(p).getOpenedBankGui();
        if (openedBank == null) {
            BPSets.removePlayerFromDepositing(p);
            BPSets.removePlayerFromWithdrawing(p);
            return;
        }
        e.setCancelled(true);

        MiniMessage mm = MiniMessage.miniMessage();
        String text = mm.serialize(e.message());

        // If some chat format plugin is adding a "." at the end, remove it.
        if (text.endsWith(".")) text = text.substring(0, text.length() - 1);
        if (hasTypedExit(text, p, openedBank)) return;

        BigDecimal amount;
        try {
            amount = new BigDecimal(text);
        } catch (NumberFormatException ex) {
            BPMessages.send(p, "Invalid-Number");
            return;
        }

        BPEconomy economy = openedBank.getOriginBank().getBankEconomy();
        if (BPUtils.isDepositing(p)) economy.deposit(p, amount);
        else economy.withdraw(p, amount);

        reopenBank(p, openedBank);
        removeFromTyping(p);
    }

    private static boolean hasTypedExit(String message, Player p, BankGui openedBank) {
        if (!message.toLowerCase().contains(ConfigValues.getChatExitMessage().toLowerCase())) return false;

        reopenBank(p, openedBank);
        executeExitCommands(p);
        return true;
    }

    private static void removeFromTyping(Player p) {
        BPSets.playerDepositing.remove(p.getUniqueId());
        BPSets.playerWithdrawing.remove(p.getUniqueId());
    }

    public static void reopenBank(Player p, BankGui openedBankGui) {
        Bukkit.getScheduler().runTask(BankPlus.INSTANCE(), () -> {
            BukkitTask task = PlayerRegistry.get(p).getClosingTask();
            if (task != null) task.cancel();

            removeFromTyping(p);
            if (ConfigValues.isReopeningBankAfterChat()) openedBankGui.openBankGui(p, true);
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
