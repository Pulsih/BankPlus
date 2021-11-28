package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.managers.ConfigValues;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.guis.GuiBankHolder;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.ListUtils;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.utils.SetUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class PlayerChat implements Listener {

    private final BankPlus plugin;
    public PlayerChat(BankPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler ( priority = EventPriority.MONITOR )
    public void onChat(PlayerChatEvent e) {

        Player p = e.getPlayer();
        String mess = e.getMessage();
        final MessageManager messMan = new MessageManager(plugin);

        if (SetUtils.playerDepositing.contains(p.getUniqueId())) {
            debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fMessage sent: " + e.getMessage());
            if (mess.startsWith("exit")) {
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &f" + p.getName() + " typed exit");
                e.setCancelled(true);
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fEvent cancelled");
                SetUtils.playerDepositing.remove(p.getUniqueId());
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fRemoved playerDepositing Set");
                reopenBank(p);
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fFired reopenBank method");
                return;
            }
            try {
                Methods.deposit(p, Long.parseLong(mess), plugin);
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fDeposited " + Long.parseLong(mess) + " money for " + p.getName());
                reopenBank(p);
            } catch (NumberFormatException ex) {
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fMessage wasn't a number");
                e.setCancelled(true);
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fEvent cancelled");
                messMan.invalidNumber(p);
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fSent invalid number message");
                return;
            }
            e.setCancelled(true);
            debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fEvent cancelled");
            SetUtils.playerDepositing.remove(p.getUniqueId());
            debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fRemoved playerDepositing Set");
        }
        if (SetUtils.playerWithdrawing.contains(p.getUniqueId())) {
            debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fMessage sent: " + e.getMessage());
            if (mess.startsWith("exit")) {
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &f" + p.getName() + " typed exit");
                e.setCancelled(true);
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fEvent cancelled");
                SetUtils.playerWithdrawing.remove(p.getUniqueId());
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fRemoved playerWithdrawing Set");
                reopenBank(p);
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fFired reopenBank method");
                return;
            }
            try {
                Methods.withdraw(p, Long.parseLong(mess), plugin);
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fWithdrew " + Long.parseLong(mess) + " money for " + p.getName());
                reopenBank(p);
            } catch (NumberFormatException ex) {
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fMessage wasn't a number");
                e.setCancelled(true);
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fEvent cancelled");
                messMan.invalidNumber(p);
                debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fSent invalid number message");
                return;
            }
            e.setCancelled(true);
            debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fEvent cancelled");
            SetUtils.playerWithdrawing.remove(p.getUniqueId());
            debug("&aBANKPLUS &8-> &3PLAYERCHAT &8: &fRemoved playerWithdrawing Set");
        }
    }

    private void reopenBank(Player p) {
        if (ConfigValues.isReopeningBankAfterChat()) GuiBankHolder.getEnchanterHolder().openBank(p);
    }

    private void debug(String message) {
        if (ListUtils.PLAYERCHAT_DEBUG.get(0).equals("ENABLED"))
            ChatUtils.consoleMessage(message);
    }
}
