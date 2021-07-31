package me.pulsi_.bankplus.events;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.guis.GuiBank;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.SetUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChat implements Listener {

    private BankPlus plugin;
    public PlayerChat(BankPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {

        Player p = e.getPlayer();
        GuiBank guiBank = new GuiBank(plugin);
        String amount = e.getMessage();

        if (SetUtils.playerDepositing.contains(p.getUniqueId())) {
            try {
                EconomyManager.deposit(p, Long.parseLong(amount), plugin);
                if (plugin.getConfiguration().getBoolean("General.Reopen-Bank-After-Chat")) {
                    guiBank.openGui(p);
                }
            } catch (NumberFormatException ex) {
                MessageManager.invalidNumber(p, plugin);
            }
            e.setCancelled(true);
            SetUtils.playerDepositing.remove(p.getUniqueId());
        }
        if (SetUtils.playerWithdrawing.contains(p.getUniqueId())) {
            try {
                EconomyManager.withdraw(p, Long.parseLong(amount), plugin);
                if (plugin.getConfiguration().getBoolean("General.Reopen-Bank-After-Chat")) {
                    guiBank.openGui(p);
                }
            } catch (NumberFormatException ex) {
                MessageManager.invalidNumber(p, plugin);
            }
            e.setCancelled(true);
            SetUtils.playerWithdrawing.remove(p.getUniqueId());
        }
    }
}
