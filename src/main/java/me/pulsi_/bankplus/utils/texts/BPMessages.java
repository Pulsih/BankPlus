package me.pulsi_.bankplus.utils.texts;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class BPMessages {

    private static final HashMap<String, List<String>> messages = new HashMap<>();

    private static String prefix = BPChat.prefix;

    private static boolean alertMissingMessages;

    /**
     * Send the desired message to the player.
     * @param p The player.
     * @param identifier The message identifier in the messages file.
     */
    public static void send(Player p, String identifier) {
        send(p, identifier, (Object) null);
    }

    /**
     * Send the desired message to the player.
     * Put a boolean between the replacers to use the identifier as
     * message instead of searching for that identifier in the messages file.
     * @param p The player.
     * @param identifier The message identifier in the messages file.
     * @param replacers A list of possible replacements.
     */
    public static void send(Player p, String identifier, Object... replacers) {
        if (p == null || identifier == null || identifier.isEmpty()) return;

        boolean fromString = fromString(replacers);
        if (!fromString && !messages.containsKey(identifier)) {
            if (alertMissingMessages) p.sendMessage(addPrefix("%prefix% &cThe \"" + identifier + "&c\" messages is missing in the messages file!"));
            return;
        }

        for (String message : getReplacedMessages(identifier, fromString, replacers)) p.sendMessage(format(p, message));
    }

    /**
     * Send the desired message to the command sender.
     * @param s The command sender.
     * @param identifier The message identifier in the messages file.
     */
    public static void send(CommandSender s, String identifier) {
        send(s, identifier, (Object) null);
    }

    /**
     * Send the desired message to the command sender.
     * Put a boolean between the replacers to use the identifier as
     * message instead of searching for that identifier in the messages file.
     * @param s The player.
     * @param identifier The message identifier in the messages file.
     * @param replacers A list of possible replacements.
     */
    public static void send(CommandSender s, String identifier, Object... replacers) {
        if (s == null || identifier == null || identifier.isEmpty()) return;

        boolean fromString = fromString(replacers);
        if (!fromString && !messages.containsKey(identifier)) {
            if (alertMissingMessages) s.sendMessage(addPrefix("%prefix% &cThe \"" + identifier + "&c\" messages is missing in the messages file!"));
            return;
        }

        for (String message : getReplacedMessages(identifier, fromString, replacers)) s.sendMessage(format(s, message));
    }

    /**
     * Get a list of all messages from the identifier or not, with all the replacement applied.
     * @param identifier The message identifier.
     * @param fromString Use the identifier as message instead of searching in the messages file.
     * @param replacers A list of possible replacements.
     * @return A list of messages.
     */
    public static List<String> getReplacedMessages(String identifier, boolean fromString, Object... replacers) {
        List<String> messagesToSend = new ArrayList<>();
        if (fromString) messagesToSend.add(identifier);
        else messagesToSend.addAll(messages.get(identifier));

        List<String> finalMessages = new ArrayList<>();
        for (String message : messagesToSend) {
            if (replacers != null) {
                for (Object object : replacers) {
                    List<String> possibleReplacers = new ArrayList<>();

                    if (!(object instanceof Collection)) possibleReplacers.add(object.toString());
                    else for (Object collectionObject : ((Collection<?>) object).toArray())
                        possibleReplacers.add(collectionObject.toString());

                    for (String replacer : possibleReplacers) {
                        if (!replacer.contains("$")) continue;

                        String[] split = replacer.split("\\$");
                        String target = split[0], replacement = split[1];
                        message = message.replace(target, replacement);
                    }
                }
            }
            if (!message.isEmpty()) finalMessages.add(message);
        }
        return finalMessages;
    }

    public static void loadMessages() {
        messages.clear();

        FileConfiguration config = BankPlus.INSTANCE().getConfigs().getConfig("messages.yml");

        for (String identifier : Objects.requireNonNull(config.getConfigurationSection("")).getKeys(false)) {
            if (!identifier.equals("Enable-Missing-Message-Alert") && !identifier.equals("Prefix") && !identifier.equals("Title-Custom-Transaction") && !identifier.equals("Interest-Broadcast"))
                messages.put(identifier, getPossibleMessages(config, identifier));
        }

        String prefixString = config.getString("Prefix");
        prefix = prefixString == null ? BPChat.prefix : prefixString;
        alertMissingMessages = config.getBoolean("Enable-Missing-Message-Alert");
    }

    public static String addPrefix(String message) {
        return BPChat.color(message.replace("%prefix%", prefix));
    }

    public static List<String> getPossibleMessages(FileConfiguration config, String path) {
        List<String> configMessages = config.getStringList(path);
        if (configMessages.isEmpty()) {
            String singleMessage = config.getString(path);
            if (singleMessage != null && !singleMessage.isEmpty()) configMessages.add(singleMessage);
        }
        return configMessages;
    }

    private static String format(Player p, String text) {
        return BankPlus.INSTANCE().isPlaceholderApiHooked() ? PlaceholderAPI.setPlaceholders(p, addPrefix(text)) : addPrefix(text);
    }

    private static String format(CommandSender s, String text) {
        return s instanceof Player ? format((Player) s, addPrefix(text)) : addPrefix(text);
    }

    private static boolean fromString(Object... objects) {
        if (objects == null) return false;
        for (Object object : objects)
            if (object instanceof Boolean && ((boolean) object)) return true;
        return false;
    }
}