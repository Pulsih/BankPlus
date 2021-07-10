package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MethodUtils {

    public static String formatTime(int cooldown, BankPlus plugin) {

        String minute = plugin.getConfiguration().getString("Placeholders.Time.Minute");
        String minutes = plugin.getConfiguration().getString("Placeholders.Time.Minutes");
        String hour = plugin.getConfiguration().getString("Placeholders.Time.Hour");
        String hours = plugin.getConfiguration().getString("Placeholders.Time.Hours");
        String day = plugin.getConfiguration().getString("Placeholders.Time.Day");
        String days = plugin.getConfiguration().getString("Placeholders.Time.Days");

        if (cooldown < 60) {
            if (cooldown == 1) {
                return cooldown + minute;
            } else {
                return cooldown + minutes;
            }
        }
        if (cooldown >= 60 && cooldown < 1440) {
            if (cooldown == 60 && cooldown < 120) {
                return cooldown / 60 + hour;
            } else {
                return cooldown / 60 + hours;
            }
        }
        if (cooldown >= 1440) {
            if (cooldown == 1440 && cooldown < 2880) {
                return cooldown / 1440 + day;
            } else {
                return cooldown / 1440 + days;
            }
        }
        return null;
    }

    public static String formatLong(long balance, BankPlus plugin) {

        String k = plugin.getConfiguration().getString("Placeholders.Money.Thousands");
        String m = plugin.getConfiguration().getString("Placeholders.Money.Millions");
        String b = plugin.getConfiguration().getString("Placeholders.Money.Billions");
        String t = plugin.getConfiguration().getString("Placeholders.Money.Trillions");
        String q = plugin.getConfiguration().getString("Placeholders.Money.Quadrillions");

        if (balance < 1000L) {
            return "" + balance;
        }
        if (balance >= 1000L && balance < 1000000L) {
            return Math.round(balance / 1000L) + k;
        }
        if (balance >= 1000000L && balance < 1000000000L) {
            return Math.round(balance / 1000000L) + m;
        }
        if (balance >= 1000000000L && balance < 1000000000000L) {
            return Math.round(balance / 1000000000L) + b;
        }
        if (balance >= 1000000000000L && balance < 1000000000000000L) {
            return Math.round(balance / 1000000000000L) + t;
        }
        if (balance >= 1000000000000000L && balance < 1000000000000000000L) {
            return Math.round(balance / 1000000000000000L) + q;
        }
        return null;
    }

    public static String format(long balance, BankPlus plugin) {

        String k = plugin.getConfiguration().getString("Placeholders.Money.Thousands");
        String m = plugin.getConfiguration().getString("Placeholders.Money.Millions");
        String b = plugin.getConfiguration().getString("Placeholders.Money.Billions");
        String t = plugin.getConfiguration().getString("Placeholders.Money.Trillions");
        String q = plugin.getConfiguration().getString("Placeholders.Money.Quadrillions");

        if (Double.parseDouble(String.valueOf(balance)) < 1000L) {
            return formatString(balance);
        }
        if (Double.parseDouble(String.valueOf(balance)) >= 1000L && Double.parseDouble(String.valueOf(balance)) < 1000000L) {
            return formatString(Double.parseDouble(String.valueOf(balance)) / 1000L) + k;
        }
        if (Double.parseDouble(String.valueOf(balance)) >= 1000000L && Double.parseDouble(String.valueOf(balance)) < 1000000000L) {
            return formatString(Double.parseDouble(String.valueOf(balance)) / 1000000L) + m;
        }
        if (Double.parseDouble(String.valueOf(balance)) >= 1000000000L && Double.parseDouble(String.valueOf(balance)) < 1000000000000L) {
            return formatString(Double.parseDouble(String.valueOf(balance)) / 1000000000L) + b;
        }
        if (Double.parseDouble(String.valueOf(balance)) >= 1000000000000L && Double.parseDouble(String.valueOf(balance)) < 1000000000000000L) {
            return formatString(Double.parseDouble(String.valueOf(balance)) / 1000000000000L) + t;
        }
        if (Double.parseDouble(String.valueOf(balance)) >= 1000000000000000L && Double.parseDouble(String.valueOf(balance)) < 1000000000000000000L) {
            return formatString(Double.parseDouble(String.valueOf(balance)) / 1000000000000000L) + q;
        }
        return null;
    }

    private static String formatString(double balance) {
        NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(0);
        return format.format(balance);
    }

    public static void playSound(String path, Player p, BankPlus plugin, boolean booleanPath) {

        if (booleanPath) return;

        String[] pathSlitted = path.split(",");
        String soundType = pathSlitted[0];
        int volume = Integer.parseInt(pathSlitted[1]);
        int pitch = Integer.parseInt(pathSlitted[2]);

        try {
            p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
        } catch (NullPointerException exception) {
            plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&a&lBank&9&lPlus &cCannot find the SoundType at path: &f" + path));
        } catch (IllegalArgumentException exception) {
            plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&a&lBank&9&lPlus &cInvalid SoundType at: &f" + path));
        }
    }

    public static void sendTitle(String path, Player p, BankPlus plugin) {

        String[] pathSlitted = plugin.getMessages().getString(path).split(",");
        String title1 = pathSlitted[0];
        String title2 = pathSlitted[1];

        p.sendTitle(ChatUtils.c(title1), ChatUtils.c(title2));
    }

    public static int ticksInMinutes(int delay) {
        return delay * 1200;
    }
}