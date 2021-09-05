package me.pulsi_.bankplus.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class ChatUtils {
    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    public static void consoleMessage(String message) {
        Bukkit.getConsoleSender().getServer().getConsoleSender().sendMessage(color(message));
    }
}