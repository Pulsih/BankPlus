package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPSets;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scheduler.BukkitTask;

public class CmdListener implements Listener {

    private final String rCmd = "restart", sCmd1 = "stop", sCmd2 = "minecraft:stop", restart = "bukkit.command.restart", stop = "minecraft.command.stop";

    @EventHandler
    public void onCmd(ServerCommandEvent e) {
        CommandSender s = e.getSender();
        String cmd = e.getCommand(), cmdL = cmd.toLowerCase();

        if (BankPlus.isShuttingDown || ((!cmdL.startsWith(rCmd) || !s.hasPermission(restart)) && ((!cmdL.startsWith(sCmd1) && !cmdL.startsWith(sCmd2)) || !s.hasPermission(this.stop)))) return;
        e.setCancelled(true);
        BankPlus.isShuttingDown = true;

        savePlayers();
        Bukkit.dispatchCommand(e.getSender(), cmd);
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String cmd = e.getMessage(), cmdL = cmd.toLowerCase();

        if (BankPlus.isShuttingDown || ((!cmdL.startsWith("/" + rCmd) || !p.hasPermission(restart)) && ((!cmdL.startsWith("/" + sCmd1) && !cmdL.startsWith("/" + sCmd2)) || !p.hasPermission(stop)))) return;
        e.setCancelled(true);
        BankPlus.isShuttingDown = true;

        savePlayers();
        p.chat(cmd);
    }

    private void savePlayers() {
        PlayerRegistry registry = BankPlus.INSTANCE.getPlayerRegistry();
        for (Player p : Bukkit.getOnlinePlayers()) registry.savePlayer(p);
    }
}