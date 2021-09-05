package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.guis.GuiBank;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.MethodUtils;
import me.pulsi_.bankplus.utils.SetUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class PlayerChat implements Listener {

    private BankPlus plugin;
    public PlayerChat(BankPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(PlayerChatEvent e) {

        Player p = e.getPlayer();
        String amount = e.getMessage();
        final MessageManager messMan = new MessageManager(plugin);

        if (SetUtils.playerDepositing.contains(p.getUniqueId())) {
            try {
                MethodUtils.deposit(p, Long.parseLong(amount), plugin);
                if (plugin.config().getBoolean("General.Reopen-Bank-After-Chat")) {
                    new GuiBank(plugin).openGui(p);
                }
            } catch (NumberFormatException ex) {
                e.setCancelled(true);
                messMan.invalidNumber(p);
                return;
            }
            e.setCancelled(true);
            SetUtils.playerDepositing.remove(p.getUniqueId());
        }
        if (SetUtils.playerWithdrawing.contains(p.getUniqueId())) {
            try {
                MethodUtils.withdraw(p, Long.parseLong(amount), plugin);
                if (plugin.config().getBoolean("General.Reopen-Bank-After-Chat")) {
                    new GuiBank(plugin).openGui(p);
                }
            } catch (NumberFormatException ex) {
                e.setCancelled(true);
                messMan.invalidNumber(p);
                return;
            }
            e.setCancelled(true);
            SetUtils.playerWithdrawing.remove(p.getUniqueId());
        }
    }
}
