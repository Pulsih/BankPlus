package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.utils.texts.BPChat;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.Bukkit;

public class BPLogger {

    public static void error(Throwable e, Object error) {
        error(error);
        error("");
        error("Additional information:");
        error("| Error message:");
        error("| | " + e.getMessage());
        error("| Interested classes:");

        int priority = 1;
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            String path = stackTraceElement.getClassName();
            if (!path.contains("me.pulsi_.bankplus")) continue;

            String name = path.substring(path.lastIndexOf(".") + 1), method = stackTraceElement.getMethodName();
            int line = stackTraceElement.getLineNumber();

            error("| | [" + priority + "*] " + path + " - " + name + "#" + method + "(); [&f" + line + "&c]");
            priority++;
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



    public static void warn(Throwable e, Object warn) {
        warn(warn);
        warn("");
        warn("Additional information:");
        warn("| Error message:");
        warn("| | " + e.getMessage());
        warn("| Interested classes:");

        int priority = 1;
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            String path = stackTraceElement.getClassName();
            if (!path.contains("me.pulsi_.bankplus")) continue;

            String name = path.substring(path.lastIndexOf(".") + 1), method = stackTraceElement.getMethodName();
            int line = stackTraceElement.getLineNumber();

            warn("| | [" + priority + "*] " + path + " - " + name + "#" + method + "(); [&f" + line + "&e]");
            priority++;
        }
    }

    public static void info(Object info) {
        log(BPChat.prefix + " &8[&9INFO&8] &9" + info);
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(BPMessages.addPrefix(message));
    }
}