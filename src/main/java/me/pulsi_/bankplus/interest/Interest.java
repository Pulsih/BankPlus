package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Interest {

    private final BankPlus plugin;
    public Interest(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void giveInterest() {
        if (!plugin.getConfiguration().getBoolean("Interest.Enabled")) return;

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            long cooldown = plugin.getPlayers().getLong("Interest-Cooldown");
            long delay = plugin.getConfiguration().getLong("Interest.Delay");

            if (cooldown <= 1) {
                giveInterestToEveryone();
                plugin.getPlayers().set("Interest-Cooldown", delay);
            } else {
                plugin.getPlayers().set("Interest-Cooldown", cooldown - 1);
            }
            plugin.savePlayers();
        }, 0, MethodUtils.ticksInMinutes(1));
    }

    public void giveInterestToEveryone() {

        double moneyPercentage = plugin.getConfiguration().getDouble("Interest.Money-Given");
        long maxAmount = plugin.getConfiguration().getLong("Interest.Max-Amount");

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("bankplus.receive.interest")) continue;
            if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
                long bankBalance = plugin.getPlayers().getLong("Players." + p.getUniqueId() + ".Money");
                if ((long) (bankBalance * moneyPercentage) >= maxAmount) {
                    plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", bankBalance + maxAmount);
                    if (plugin.getMessages().getBoolean("Interest-Broadcast.Enabled")) {
                        MessageManager.interestBroadcastMessageMax(p, plugin, maxAmount);
                    }
                    return;
                }
                if ((long) (bankBalance * moneyPercentage) == 0) {
                    plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", bankBalance + 1);
                } else {
                    plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", (long) (bankBalance + bankBalance * moneyPercentage));
                }
                if (plugin.getMessages().getBoolean("Interest-Broadcast.Enabled")) {
                    MessageManager.interestBroadcastMessage(p, plugin, bankBalance, moneyPercentage);
                }
            } else {
                long bankBalance = plugin.getPlayers().getLong("Players." + p.getName() + ".Money");
                if ((long) (bankBalance * moneyPercentage) >= maxAmount) {
                    plugin.getPlayers().set("Players." + p.getName() + ".Money", bankBalance + maxAmount);
                    if (plugin.getMessages().getBoolean("Interest-Broadcast.Enabled")) {
                        MessageManager.interestBroadcastMessageMax(p, plugin, maxAmount);
                    }
                    return;
                }
                if ((long) (bankBalance * moneyPercentage) == 0) {
                    plugin.getPlayers().set("Players." + p.getName() + ".Money", bankBalance + 1);
                } else {
                    plugin.getPlayers().set("Players." + p.getName() + ".Money", (long) (bankBalance + bankBalance * moneyPercentage));
                }
                if (plugin.getMessages().getBoolean("Interest-Broadcast.Enabled")) {
                    MessageManager.interestBroadcastMessage(p, plugin, bankBalance, moneyPercentage);
                }
            }
            plugin.savePlayers();
        }
    }
}