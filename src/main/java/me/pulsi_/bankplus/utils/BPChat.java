package me.pulsi_.bankplus.utils;

import org.bukkit.ChatColor;

public class BPChat {

    public static String prefix = "&a&lBank&9&lPlus";

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}