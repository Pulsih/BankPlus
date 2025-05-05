package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.texts.BPChat;
import org.bukkit.Bukkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class BPLogger {

    public static class LogsFile {

        private static File logFile = null;
        private static String day = null;

        public static void log(String message) {
            String today = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
            if (!today.equals(day)) {
                // Check if the file name has changed, if
                // yes, setup and log in the new file.
                day = today;

                setupLoggerFile();
                log(message);
                return;
            }

            String date = new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis());

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
                bw.append(date).append(" | ").append(message);
                bw.close();
            } catch (IOException ex) {
                Console.error("Could not write logs to the file, reason: " + ex.getMessage());
            }
        }

        public static void setupLoggerFile() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            day = format.format(System.currentTimeMillis());

            logFile = new File(BankPlus.INSTANCE().getDataFolder() + File.separator + "logs", day + ".txt");
            if (logFile.exists()) return;

            logFile.getParentFile().mkdirs();
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Console.error("Could not create log file: " + e.getMessage());
            }
        }

    }

    public static class Console {

        public static void error(Object error) {
            error(false, error);
        }

        public static void error(boolean logToFile, Object error) {
            if (error == null) error = "null";
            log(BPChat.PREFIX + " <dark_gray>[<red>ERROR</red>] <red>" + error);
            if (logToFile) LogsFile.log("[ERROR] " + error);
        }

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

                error("| | [" + priority + "*] " + path + " - " + name + "#" + method + "(); [<white>" + line + "<red>]");
                priority++;
            }
        }

        public static void warn(Object warn) {
            warn(false, warn);
        }

        public static void warn(boolean logToFile, Object warn) {
            if (warn == null) warn = "null";
            log(BPChat.PREFIX + "<dark_gray>[<yellow>WARN</yellow>] <yellow>" + warn);
            if (logToFile) LogsFile.log("[WARN] " + warn);
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

                warn("| | [" + priority + "*] " + path + " - " + name + "#" + method + "(); [<white>" + line + "<yellow>]");
                priority++;
            }
        }

        public static void info(Object info) {
            info(false, info);
        }

        public static void info(boolean logToFile, Object info) {
            if (info == null) info = "null";
            log(BPChat.PREFIX + " <dark_gray>[<blue>INFO</blue>] <blue>" + info);
            if (logToFile) LogsFile.log("[INFO] " + info);
        }

        public static void log(String message) {
            Bukkit.getConsoleSender().sendMessage(BPChat.color(message));
        }
    }
}