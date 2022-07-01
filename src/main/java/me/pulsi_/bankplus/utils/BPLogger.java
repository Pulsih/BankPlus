package me.pulsi_.bankplus.utils;

import org.bukkit.Bukkit;

public class BPLogger {

    public static void error(String error) {
        if (error == null) error = "null";
        log(BPChat.prefix + " &8[&cERROR&8] &c" + error);
    }

    public static void warn(String warn) {
        if (warn == null) warn = "null";
        log(BPChat.prefix + " &8[&eWARN&8] &e" + warn);
    }

    public static void info(String info) {
        if (info == null) info = "null";
        log(BPChat.prefix + " &8[&9INFO&8] &9" + info);
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().getServer().getConsoleSender().sendMessage(BPChat.color(message));
    }
}