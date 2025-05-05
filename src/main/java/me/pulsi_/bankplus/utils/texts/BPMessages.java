package me.pulsi_.bankplus.utils.texts;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPLogger;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class BPMessages {

    public static final String DEFAULT_GRADIENT = "<gradient:green:blue:green>";

    private static final HashMap<String, List<String>> messages = new HashMap<>();

    private static String messagesPrefix = null;
    private static boolean alertMissingMessages;

    /**
     * Send the desired message to the receiver.
     *
     * @param receiver   The receiver.
     * @param identifier The message identifier in the messages file.
     */
    public static void send(Object receiver, String identifier) {
        send(receiver, identifier, null);
    }

    /**
     * Send the desired message to the specified receiver.
     * <p>
     * Put a negative boolean somewhere in the replacers to send the whole
     * identifier as message, instead of searching it in the messages file.
     * <p>
     * If the message is not from the messages file, a default gradient will be applied.
     *
     * @param receiver   The receiver (Player or CommandSender).
     * @param identifier The message identifier in the messages file.
     * @param replacers  A list of possible replacements.
     */
    public static void send(Object receiver, String identifier, Object... replacers) {
        if (receiver == null || identifier == null || identifier.isEmpty()) return;

        boolean search = searchInMessages(replacers);
        if (search && !messages.containsKey(identifier)) {
            if (alertMissingMessages)
                sendMessage(
                        receiver,
                        "%prefix% <red>The message \"" + identifier + "\" is missing in the messages file!"
                );
            return;
        }

        for (String message : getReplacedMessages(identifier, search, replacers)) sendMessage(receiver, message);
    }

    /**
     * Get a list of all messages from the identifier, with all the replacement applied.
     *
     * @param identifier       The message identifier.
     * @param searchInMessages Search the id in the messages file or not.
     * @param replacers        A list of possible replacements.
     * @return A list of messages replaced.
     */
    public static List<String> getReplacedMessages(String identifier, boolean searchInMessages, Object... replacers) {
        List<String> messagesToSend = new ArrayList<>();
        if (searchInMessages) messagesToSend.addAll(messages.get(identifier));
        else messagesToSend.add(DEFAULT_GRADIENT + identifier);

        List<String> finalMessages = new ArrayList<>();
        for (String message : messagesToSend) {
            if (replacers != null) {
                for (Object object : replacers) {
                    if (object == null) continue;
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
        for (String identifier : config.getKeys(false)) {
            List<String> configMessages = config.getStringList(identifier);

            if (configMessages.isEmpty()) {
                String singleMessage = config.getString(identifier);
                if (singleMessage != null && !singleMessage.isEmpty()) configMessages.add(singleMessage);
            }

            messages.put(identifier, configMessages);
        }

        if (!messages.containsKey("Prefix")) messagesPrefix = BPChat.PREFIX;
        else {
            List<String> prefixes = messages.get("Prefix");
            messagesPrefix = prefixes.isEmpty() ? BPChat.PREFIX : prefixes.getFirst();
        }

        alertMissingMessages = config.getBoolean("Enable-Missing-Message-Alert");
    }

    /**
     * Simplify the method to send messages to the receiver (must be
     * Player or CommandSender), formatting and placing values.
     *
     * @param receiver The message receiver.
     * @param message  The message.
     */
    private static void sendMessage(Object receiver, String message) {
        message = applyMessagesPrefix(message);

        if (receiver instanceof Player player) {
            if (BankPlus.INSTANCE().isPlaceholderApiHooked())
                message = PlaceholderAPI.setPlaceholders(player, message);
            player.sendMessage(BPChat.color(message));
            return;
        }

        if (receiver instanceof CommandSender sender) {
            sender.sendMessage(BPChat.color(message));
            return;
        }

        BPLogger.Console.error("Could not send message because receiver is neither a Player or CommandSender: " + receiver.toString());
    }

    /**
     * Method to simplify the method to replace the prefix with the
     * one the user set in messages.yml to the specified message.
     *
     * @param message The message where to apply the prefix.
     * @return The message with %prefix% replaced.
     */
    public static String applyMessagesPrefix(String message) {
        return message.replace("%prefix%", messagesPrefix);
    }

    /**
     * Method to check if inside the given replacers, there is a negative boolean,
     * sending the whole identifier as message instead of searching it in the messages file.
     *
     * @param objects The list of replacers.
     * @return true if the identifier is to search in the messages file, false if it needs to be sent entirely.
     */
    private static boolean searchInMessages(Object... objects) {
        if (objects == null) return true;

        for (Object object : objects)
            if (object instanceof Boolean && !((boolean) object)) return false;
        return true;
    }
}