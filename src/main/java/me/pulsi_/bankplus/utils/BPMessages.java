package me.pulsi_.bankplus.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.BPConfigs;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class BPMessages {

    private static final Map<String, List<String>> messages = new HashMap<>();

    private static String prefix = null;

    private static boolean enableMissingMessageAlert;

    /**
     * The "fromString" parameter means that if it's true, it will send a custom message and won't search it from the loaded messages.
     */
    public static void send(Player p, String message, boolean fromString) {
        send(p, message, new ArrayList<>(), fromString);
    }

    /**
     * The "fromString" parameter means that if it's true, it will send a custom message and won't search it from the loaded messages.
     */
    public static void send(CommandSender s, String message, boolean fromString) {
        send(s, message, new ArrayList<>(), fromString);
    }

    /**
     * The "fromString" parameter means that if it's true, it will send a custom message and won't search it from the loaded messages.
     */
    public static void send(Player p, String message, List<String> stringsToReplace, boolean fromString) {
        if (p == null) return;

        if (!fromString) {
            send(p, message, stringsToReplace);
            return;
        }

        for (String stringToReplace : stringsToReplace) {
            if (!stringToReplace.contains("$")) continue;
            String oldChar = stringToReplace.split("\\$")[0];
            String replacement = stringToReplace.split("\\$")[1];
            message = message.replace(oldChar, replacement);
        }
        p.sendMessage(format(p, message));
    }

    /**
     * The "fromString" parameter means that if it's true, it will send a custom message and won't search it from the loaded messages.
     */
    public static void send(CommandSender s, String message, List<String> stringsToReplace, boolean fromString) {
        if (s == null) return;

        if (!fromString) {
            send(s, message, stringsToReplace);
            return;
        }

        for (String stringToReplace : stringsToReplace) {
            if (!stringToReplace.contains("$")) continue;
            String oldChar = stringToReplace.split("\\$")[0];
            String replacement = stringToReplace.split("\\$")[1];
            message = message.replace(oldChar, replacement);
        }
        s.sendMessage(format(s, message));
    }

    public static void send(Player p, String identifier) {
        send(p, identifier, new ArrayList<>());
    }

    public static void send(Player p, String identifier, String... stringsToReplace) {
        send(p, identifier, Arrays.asList(stringsToReplace));
    }

    public static void send(Player p, String identifier, List<String>... stringsToReplace) {
        if (p == null) return;

        if (!messages.containsKey(identifier)) {
            if (enableMissingMessageAlert)
                p.sendMessage(addPrefix("%prefix% &cThe \"" + identifier + "\" message is missing in the messages file!"));
            return;
        }

        List<String> listOfMessages = messages.get(identifier);
        for (String message : listOfMessages) {
            for (List<String> replacers : stringsToReplace) {
                for (String stringToReplace : replacers) {
                    if (!stringToReplace.contains("$")) continue;
                    String oldChar = stringToReplace.split("\\$")[0];
                    String replacement = stringToReplace.split("\\$")[1];
                    message = message.replace(oldChar, replacement);
                }
            }
            if (!message.equals("")) p.sendMessage(format(p, message));
        }
    }

    public static void send(CommandSender s, String identifier) {
        send(s, identifier, new ArrayList<>());
    }

    public static void send(CommandSender s, String identifier, String... stringsToReplace) {
        send(s, identifier, Arrays.asList(stringsToReplace));
    }

    public static void send(CommandSender s, String identifier, List<String>... stringsToReplace) {
        if (s == null) return;

        if (!messages.containsKey(identifier)) {
            if (enableMissingMessageAlert)
                s.sendMessage(addPrefix("%prefix% &cThe \"" + identifier + "\" message is missing in the messages file!"));
            return;
        }

        List<String> listOfMessages = messages.get(identifier);
        for (String message : listOfMessages) {
            for (List<String> replacers : stringsToReplace) {
                for (String stringToReplace : replacers) {
                    if (!stringToReplace.contains("$")) continue;
                    String oldChar = stringToReplace.split("\\$")[0];
                    String replacement = stringToReplace.split("\\$")[1];
                    message = message.replace(oldChar, replacement);
                }
            }
            if (!message.equals("")) s.sendMessage(format(s, message));
        }
    }

    public static void loadMessages() {
        messages.clear();

        FileConfiguration config = BankPlus.INSTANCE.getConfigManager().getConfig(BPConfigs.Type.MESSAGES);
        for (String path : config.getConfigurationSection("").getKeys(false)) {
            if (!path.equals("Update-File") && !path.equals("Enable-Missing-Message-Alert"))
                messages.put(path, config.getStringList(path).isEmpty() ? Collections.singletonList(config.getString(path)) : config.getStringList(path));
        }

        if (messages.containsKey("Prefix")) {
            List<String> prefixes = messages.get("Prefix");
            prefix = prefixes.isEmpty() ? BPChat.prefix : prefixes.get(0);
        } else prefix = BPChat.prefix;
        enableMissingMessageAlert = config.getBoolean("Enable-Missing-Message-Alert");
    }

    public static String addPrefix(String message) {
        return BPChat.color(message.replace("%prefix%", getPrefix()));
    }

    public static String getPrefix() {
        return prefix;
    }

    private static String format(Player p, String text) {
        return BankPlus.INSTANCE.isPlaceholderAPIHooked() ? PlaceholderAPI.setPlaceholders(p, addPrefix(text)) : addPrefix(text);
    }

    private static String format(CommandSender s, String text) {
        return s instanceof Player ? format((Player) s, addPrefix(text)) : addPrefix(text);
    }
}