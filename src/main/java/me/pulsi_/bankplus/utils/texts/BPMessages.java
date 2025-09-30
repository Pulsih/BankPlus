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
     * Send the desired message to the specified receiver.
     *
     * @param receiver   The receiver (Player or CommandSender).
     * @param text The text.
     */
    public static void sendMessage(Object receiver, String text) {
        sendMessage(receiver, text, null);
    }

    /**
     * Send the desired message to the specified receiver.
     *
     * @param receiver   The receiver (Player or CommandSender).
     * @param text The text.
     * @param replacers  A list of possible replacements.
     */
    public static void sendMessage(Object receiver, String text, Object... replacers) {
        if (receiver == null || text == null || text.isEmpty()) return;

        for (String message : getReplacedMessages(text, false, replacers)) send(receiver, message);
    }

    /**
     * Send a message defined in the "messages.yml" file from its id.
     *
     * @param receiver   The receiver (Player or CommandSender).
     * @param identifier The identifier of the message.
     */
    public static void sendIdentifier(Object receiver, String identifier) {
        sendIdentifier(receiver, identifier, null);
    }

    /**
     * Send a message defined in the "messages.yml" file from its id.
     *
     * @param receiver   The receiver (Player or CommandSender).
     * @param identifier The identifier of the message.
     * @param replacers  A list of possible replacements.
     */
    public static void sendIdentifier(Object receiver, String identifier, Object... replacers) {
        if (receiver == null || identifier == null || identifier.isEmpty()) return;

        if (alertMissingMessages && !messages.containsKey(identifier)) {
                sendMessage(
                        receiver,
                        "%prefix% <red>The message \"" + identifier + "\" is missing in the messages file!"
                );
            return;
        }

        for (String message : getReplacedMessages(identifier, true, replacers)) send(receiver, message);
    }

    /**
     * Get a list of all messages from the identifier, with all the replacement applied.
     * The replacer format is: [oldValue$newValue]
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
    private static void send(Object receiver, String message) {
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
}