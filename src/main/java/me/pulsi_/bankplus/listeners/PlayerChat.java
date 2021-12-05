package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.guis.GuiBankHolder;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.utils.SetUtils;
import me.pulsi_.bankplus.values.Values;
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(PlayerChatEvent e) {

        Player p = e.getPlayer();
        String mess = e.getMessage();

        if (SetUtils.playerDepositing.contains(p.getUniqueId())) {
            if (mess.startsWith("exit")) {
                e.setCancelled(true);
                SetUtils.playerDepositing.remove(p.getUniqueId());
                reopenBank(p);
                return;
            }
            try {
                Methods.deposit(p, Long.parseLong(mess), plugin);
                reopenBank(p);
            } catch (NumberFormatException ex) {
                e.setCancelled(true);
                MessageManager.invalidNumber(p);
                return;
            }
            e.setCancelled(true);
            SetUtils.playerDepositing.remove(p.getUniqueId());
        }

        if (SetUtils.playerWithdrawing.contains(p.getUniqueId())) {
            if (mess.startsWith("exit")) {
                e.setCancelled(true);
                SetUtils.playerWithdrawing.remove(p.getUniqueId());
                reopenBank(p);
                return;
            }
            try {
                Methods.withdraw(p, Long.parseLong(mess), plugin);
                reopenBank(p);
            } catch (NumberFormatException ex) {
                e.setCancelled(true);
                MessageManager.invalidNumber(p);
                return;
            }
            e.setCancelled(true);
            SetUtils.playerWithdrawing.remove(p.getUniqueId());
        }
    }

    private void reopenBank(Player p) {
        if (Values.CONFIG.isReopeningBankAfterChat()) GuiBankHolder.getEnchanterHolder().openBank(p);
    }
}