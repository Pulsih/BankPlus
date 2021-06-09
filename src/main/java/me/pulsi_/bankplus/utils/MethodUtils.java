package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.entity.Player;

public class MethodUtils {

    private BankPlus plugin;
    public MethodUtils(BankPlus plugin) {
        this.plugin = plugin;
    }

    public static String formatter(int balance) {
        if (balance < 1000) {
            return String.valueOf(balance);
        }
        if (balance >= 1000 && balance < 1000000) {
            return Math.round(balance / 1000) + "K";
        }
        if (balance >= 1000000 && balance < 1000000000) {
            return Math.round(balance / 1000000) + "M";
        }
        if (balance >= 1000000000) {
            return Math.round(balance / 1000000000) + "B";
        }
        return null;
    }

    public static void playSound(String path, Player p, BankPlus plugin, boolean booleanPath) {

        if (!booleanPath) return;

        String[] pathSlitted = path.split(",");
        String soundType = pathSlitted[0];
        int volume = Integer.parseInt(pathSlitted[1]);
        int pitch = Integer.parseInt(pathSlitted[2]);

        try {
            p.playSound(p.getLocation(), soundType, volume, pitch);
        } catch (NullPointerException exception) {
            plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&a&lBank&9&lPlus &cCannot find the SoundType at: &f" + path));
        } catch (IllegalArgumentException exception) {
            plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&a&lBank&9&lPlus &cInvalid SoundType at: &f" + path));
        }
    }

    public static double ticksInMinutes(double delay) {
        return delay * 1200;
    }
    public static double ticksInHours(double delay) {
        return delay * 72000;
    }
    public static double ticksInDays(double delay) {
        return delay * 1728000;
    }
}