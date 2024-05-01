package me.pulsi_.bankplus.utils;

import org.bukkit.Bukkit;

public class BPLogger {

    public static void error(Exception e, Object error) {
        error(error);
        error("");
        error("Additional information:");
        error("Error message: " + e.getMessage());
        error("Involved classes:");
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            String iClass = stackTraceElement.getClassName();
            if (!iClass.contains("me.pulsi_.bankplus")) continue;

            int line = stackTraceElement.getLineNumber();
            String method = stackTraceElement.getMethodName();
            error("  * " + iClass + " (Line: " + line + ") [Method: " + method + "()]");
        }
    }

    public static void error(Object error) {
        if (error == null) error = "null";
        log(BPChat.prefix + " &8[&cERROR&8] &c" + error);
    }

    public static void warn(Object warn) {
        if (warn == null) warn = "null";
        log(BPChat.prefix + " &8[&eWARN&8] &e" + warn);
    }

    public static void warn(Exception e, Object warn) {
        warn(warn);
        warn("");
        warn("Additional information:");
        warn("Error message: " + e.getMessage());
        warn("Involved classes:");
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            String iClass = stackTraceElement.getClassName();
            if (!iClass.contains("me.pulsi_.bankplus")) continue;

            int line = stackTraceElement.getLineNumber();
            String method = stackTraceElement.getMethodName();
            warn("  * " + iClass + " (Line: " + line + ") [Method: " + method + "()]");
        }
    }

    public static void info(Object info) {
        log(BPChat.prefix + " &8[&9INFO&8] &9" + info);
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(BPMessages.addPrefix(message));
    }
}