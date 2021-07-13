package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;

public class Interest {

    private final BankPlus plugin;
    public Interest(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void giveInterest() {
        if (!plugin.getConfiguration().getBoolean("Interest.Enabled")) return;

        String delay = plugin.getConfiguration().getString("Interest.Delay");

        if (plugin.getPlayers().getString("Interest-Cooldown") == null) {
            plugin.getPlayers().set("Interest-Cooldown", delay);
        }

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (Integer.parseInt(plugin.getPlayers().getString("Interest-Cooldown")) != 1) {
                plugin.getPlayers().set("Interest-Cooldown", String.valueOf(Integer.parseInt(plugin.getPlayers().getString("Interest-Cooldown")) -1));
                try {
                    plugin.savePlayers();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                giveInterestToEveryone();
                plugin.getPlayers().set("Interest-Cooldown", delay);
                try {
                    plugin.savePlayers();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, MethodUtils.ticksInMinutes(1));
    }

    public void giveInterestToEveryone() {

        String moneyPercentage = plugin.getConfiguration().getString("Interest.Money-Given");

        double finalMoneyPercentage = Double.parseDouble(moneyPercentage);
        long maxAmount = plugin.getConfiguration().getLong("Interest.Max-Amount");

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("bankplus.receive.interest")) continue;
            if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
                long bankBalance = plugin.getPlayers().getLong("Players." + p.getUniqueId() + ".Money");
                if ((long) (bankBalance * finalMoneyPercentage) >= maxAmount) {
                    plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", bankBalance + maxAmount);
                    if (plugin.getMessages().getBoolean("Interest-Broadcast.Enabled")) {
                        MessageManager.interestBroadcastMessageMax(p, plugin, maxAmount);
                    }
                    return;
                }
                if ((long) (bankBalance * finalMoneyPercentage) == 0) {
                    plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", bankBalance + 1);
                } else {
                    plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", (long) (bankBalance + bankBalance * finalMoneyPercentage));
                }
                if (plugin.getMessages().getBoolean("Interest-Broadcast.Enabled")) {
                    MessageManager.interestBroadcastMessage(p, plugin, bankBalance, finalMoneyPercentage);
                }
            } else {
                long bankBalance = plugin.getPlayers().getLong("Players." + p.getName() + ".Money");
                if ((long) (bankBalance * finalMoneyPercentage) >= maxAmount) {
                    plugin.getPlayers().set("Players." + p.getName() + ".Money", bankBalance + maxAmount);
                    if (plugin.getMessages().getBoolean("Interest-Broadcast.Enabled")) {
                        MessageManager.interestBroadcastMessageMax(p, plugin, maxAmount);
                    }
                    return;
                }
                if ((long) (bankBalance * finalMoneyPercentage) == 0) {
                    plugin.getPlayers().set("Players." + p.getName() + ".Money", bankBalance + 1);
                } else {
                    plugin.getPlayers().set("Players." + p.getName() + ".Money", (long) (bankBalance + bankBalance * finalMoneyPercentage));
                }
                if (plugin.getMessages().getBoolean("Interest-Broadcast.Enabled")) {
                    MessageManager.interestBroadcastMessage(p, plugin, bankBalance, finalMoneyPercentage);
                }
            }
            try {
                plugin.savePlayers();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}