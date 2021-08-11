package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatUtils {
    public static String c(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    public static void consoleMessage(String message) {
        JavaPlugin.getPlugin(BankPlus.class).getServer().getConsoleSender().sendMessage(c(message));
    }
}