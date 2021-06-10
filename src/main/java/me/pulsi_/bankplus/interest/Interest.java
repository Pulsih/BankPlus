package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class Interest {

    private BankPlus plugin;
    public Interest(BankPlus plugin) {
        this.plugin = plugin;
    }

    private BukkitTask giveInterestPerTime;
    public void giveInterest() {
        if (!plugin.getConfiguration().getBoolean("Interest.Enabled")) return;

        String getDelay = plugin.getConfiguration().getString("Interest.Delay");
        String[] time = getDelay.split("-");

        double intDelay = Integer.parseInt(time[0]);

        if (time[1].equalsIgnoreCase("m")) {
            intDelay = MethodUtils.ticksInMinutes(intDelay);
        }
        if (time[1].equalsIgnoreCase("h")) {
            intDelay = MethodUtils.ticksInHours(intDelay);
        }
        if (time[1].equalsIgnoreCase("d")) {
            intDelay = MethodUtils.ticksInDays(intDelay);
        }

        String moneyPercentage = plugin.getConfiguration().getString("Interest.Money-Given");
        String intMoneyPercentage = "0";
        if (moneyPercentage.length() == 1) {
            intMoneyPercentage = "0.0" + moneyPercentage;
        }
        if (moneyPercentage.length() == 2) {
            intMoneyPercentage = "0." + moneyPercentage;
        }
        if (moneyPercentage.length() == 3) {
            intMoneyPercentage = moneyPercentage;
        }

        double finalMoneyPercentage = Double.parseDouble(intMoneyPercentage);

        this.giveInterestPerTime = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                int bankBalance = plugin.getPlayers().getInt("Players." + p.getUniqueId() + ".Money");
                plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", bankBalance + bankBalance * finalMoneyPercentage);
                if (plugin.getMessages().getBoolean("Interest-Broadcast.Enabled")) {
                    Bukkit.broadcastMessage(ChatUtils.c(plugin.getMessages().getString("Interest-Broadcast.Message")));
                }
            }
        }, 0, (long) intDelay);
    }

    public BukkitTask getGiveInterestPerTime() {
        return giveInterestPerTime;
    }
}