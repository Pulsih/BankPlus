package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPLogger;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageManager {

    private static final Map<String, List<String>> messages = new HashMap<>();

    public static void send(Player p, String message, boolean fromString) {
        if (!fromString) {
            send(p, message);
            return;
        }
        p.sendMessage(BPChat.color(message.replace("%prefix%", getPrefix())));
    }

    public static void send(Player p, String message, List<String> stringsToReplace, boolean fromString) {
        if (!fromString) {
            send(p, message);
            return;
        }
        for (String stringToReplace : stringsToReplace) {
            if (!stringToReplace.contains("$")) continue;
            String oldChar = stringToReplace.split("\\$")[0];
            String replacement = stringToReplace.split("\\$")[1];
            message = message.replace(oldChar, replacement);
        }
        p.sendMessage(BPChat.color(message.replace("%prefix%", getPrefix())));
    }

    public static void send(Player p, String identifier) {
        if (!messages.containsKey(identifier)) return;

        List<String> listOfMessages = messages.get(identifier);
        if (listOfMessages.isEmpty()) {
            BPLogger.error("The message \"" + identifier + "\" is missing in the messages file!");
            return;
        }

        for (String message : listOfMessages) p.sendMessage(BPChat.color(message.replace("%prefix%", getPrefix())));
    }

    public static void send(Player p, String identifier, String... stringsToReplace) {
        if (!messages.containsKey(identifier)) return;

        List<String> listOfMessages = messages.get(identifier);
        if (listOfMessages.isEmpty()) {
            BPLogger.error("The message \"" + identifier + "\" is missing in the messages file!");
            return;
        }

        for (String message : listOfMessages) {
            for (String stringToReplace : stringsToReplace) {
                if (!stringToReplace.contains("$")) continue;
                String oldChar = stringToReplace.split("\\$")[0];
                String replacement = stringToReplace.split("\\$")[1];
                message = message.replace(oldChar, replacement);
            }
            p.sendMessage(BPChat.color(message.replace("%prefix%", getPrefix())));
        }
    }

    public static void send(Player p, String identifier, List<String> stringsToReplace) {
        if (!messages.containsKey(identifier)) return;

        List<String> listOfMessages = messages.get(identifier);
        if (listOfMessages.isEmpty()) {
            BPLogger.error("The message \"" + identifier + "\" is missing in the messages file!");
            return;
        }

        for (String message : listOfMessages) {
            for (String stringToReplace : stringsToReplace) {
                if (!stringToReplace.contains("$")) continue;
                String oldChar = stringToReplace.split("\\$")[0];
                String replacement = stringToReplace.split("\\$")[1];
                message = message.replace(oldChar, replacement);
            }
            p.sendMessage(BPChat.color(message.replace("%prefix%", getPrefix())));
        }
    }

    public static void send(CommandSender s, String identifier) {
        if (!messages.containsKey(identifier)) return;

        List<String> listOfMessages = messages.get(identifier);
        if (listOfMessages.isEmpty()) {
            BPLogger.error("The message \"" + identifier + "\" is missing in the messages file!");
            return;
        }

        for (String message : listOfMessages) s.sendMessage(BPChat.color(message.replace("%prefix%", getPrefix())));
    }

    public static void send(CommandSender s, String identifier, String... stringsToReplace) {
        if (!messages.containsKey(identifier)) return;

        List<String> listOfMessages = messages.get(identifier);
        if (listOfMessages.isEmpty()) {
            BPLogger.error("The message \"" + identifier + "\" is missing in the messages file!");
            return;
        }

        for (String message : listOfMessages) {
            for (String stringToReplace : stringsToReplace) {
                if (!stringToReplace.contains("$")) continue;
                String oldChar = stringToReplace.split("\\$")[0];
                String replacement = stringToReplace.split("\\$")[1];
                message = message.replace(oldChar, replacement);
            }
            s.sendMessage(BPChat.color(message.replace("%prefix%", getPrefix())));
        }
    }

    public static void send(CommandSender s, String identifier, List<String> stringsToReplace) {
        if (!messages.containsKey(identifier)) return;

        List<String> listOfMessages = messages.get(identifier);
        if (listOfMessages.isEmpty()) {
            BPLogger.error("The message \"" + identifier + "\" is missing in the messages file!");
            return;
        }

        for (String message : listOfMessages) {
            for (String stringToReplace : stringsToReplace) {
                if (!stringToReplace.contains("$")) continue;
                String oldChar = stringToReplace.split("\\$")[0];
                String replacement = stringToReplace.split("\\$")[1];
                message = message.replace(oldChar, replacement);
            }
            s.sendMessage(BPChat.color(message.replace("%prefix%", getPrefix())));
        }
    }

    public static void loadMessages() {
        messages.clear();
        FileConfiguration config = BankPlus.getCm().getConfig(ConfigManager.Type.MESSAGES);
        for (String path : config.getConfigurationSection("").getKeys(false)) {
            List<String> listOfMessages = config.getStringList(path);
            if (listOfMessages.isEmpty()) {
                String message = config.getString(path);
                messages.put(path, Collections.singletonList(message));
                continue;
            }
            messages.put(path, config.getStringList(path));
        }
    }

    public static String addPrefix(String message) {
        return BPChat.color(message.replace("%prefix%", getPrefix()));
    }

    private static String getPrefix() {
        if (!messages.containsKey("Prefix")) return BPChat.prefix;
        List<String> prefix = messages.get("Prefix");
        if (prefix.isEmpty()) return BPChat.prefix;
        return prefix.get(0);
    }
}