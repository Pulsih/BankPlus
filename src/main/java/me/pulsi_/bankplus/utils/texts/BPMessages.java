package me.pulsi_.bankplus.utils.texts;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BPMessages {

    private static final HashMap<String, List<String>> messages = new HashMap<>();
    private static final String[] idsToSkip = {
            "Prefix",
            "Enable-Missing-Message-Alert",
            "Title-Custom-Transaction",
            "Interest-Broadcast"
    };

    private static String prefix = BPChat.prefix;

    /**
     * Send the desired message to the specified target.
     * You can choose to send a message from the
     * messages file using its identifier or a text.
     *
     * @param target     The target.
     * @param identifier The message identifier in the messages file or the text to send.
     */
    public static void send(Player target, String identifier) {
        send(target, identifier, new String[0]);
    }

    /**
     * Send the desired message to the specified target.
     * You can choose to send a message from the
     * messages file using its identifier or a text.
     *
     * @param target     The target.
     * @param identifier The message identifier in the messages file or the text to send.
     * @param replacers  A list of possible replacements.
     */
    public static void send(Player target, String identifier, List<String> replacers) {
        send(target, identifier, replacers.toArray(new String[0]));
    }

    /**
     * Send the desired message to the specified target.
     * You can choose to send a message from the
     * messages file using its identifier or a text.
     *
     * @param target     The target.
     * @param identifier The message identifier in the messages file or the text to send.
     * @param replacers  A list of possible replacements.
     */
    public static void send(Player target, String identifier, String... replacers) {
        if (target == null || identifier == null || identifier.isEmpty()) return;

        List<String> texts = new ArrayList<>();
        if (!messages.containsKey(identifier)) texts.add(identifier);
        else {
            for (String message : messages.get(identifier))
                if (!message.isEmpty()) texts.add(message);
        }

        for (String message : applyReplacers(texts, replacers)) target.sendMessage(format(target, message));
    }

    /**
     * Send the desired message to the specified target.
     * You can choose to send a message from the
     * messages file using its identifier or a text.
     *
     * @param target     The target.
     * @param identifier The message identifier in the messages file or the text to send.
     */
    public static void send(CommandSender target, String identifier) {
        send(target, identifier, new String[0]);
    }

    /**
     * Send the desired message to the specified target.
     * You can choose to send a message from the
     * messages file using its identifier or a text.
     *
     * @param target     The target.
     * @param identifier The message identifier in the messages file or the text to send.
     * @param replacers  A list of possible replacements.
     */
    public static void send(CommandSender target, String identifier, List<String> replacers) {
        send(target, identifier, replacers.toArray(new String[0]));
    }

    /**
     * Send the desired message to the specified target.
     * You can choose to send a message from the
     * messages file using its identifier or a text.
     *
     * @param target     The target.
     * @param identifier The message identifier in the messages file or the text to send.
     * @param replacers  A list of possible replacements.
     */
    public static void send(CommandSender target, String identifier, String... replacers) {
        if (target == null || identifier == null || identifier.isEmpty()) return;

        List<String> texts = new ArrayList<>();
        if (!messages.containsKey(identifier)) texts.add(identifier);
        else {
            for (String message : messages.get(identifier))
                if (!message.isEmpty()) texts.add(message);
        }

        for (String message : applyReplacers(texts, replacers)) target.sendMessage(format(target, message));
    }

    /**
     * Return a list of the input text list with all the replacers applied.
     * <p>
     * The BankPlus replacer has the following format: "textToReplace$replacement"
     *
     * @param texts     The text list input.
     * @param replacers A list of possible replacements.
     * @return A copy of the input list with replacers applied.
     */
    public static List<String> applyReplacers(List<String> texts, String... replacers) {
        if (replacers == null || replacers.length == 0) return texts;

        List<String> replacedTexts = new ArrayList<>();
        for (String message : texts) {
            if (message == null || message.isEmpty()) continue;

            for (String replacer : replacers) {
                if (replacer == null || !replacer.contains("$")) continue;

                String[] split = replacer.split("\\$");
                message = message.replace(split[0], split[1]);
            }
            replacedTexts.add(message);
        }
        return replacedTexts;
    }

    /**
     * Load all the message values from the given config file.
     *
     * @param config The messages config file.
     */
    public static void loadMessages(FileConfiguration config) {
        messages.clear();

        ConfigurationSection section = config.getConfigurationSection("");
        if (section == null) return;

        for (String identifier : section.getKeys(false))
            if (!isIgnoredId(identifier)) messages.put(identifier, getPossibleMessages(config, identifier));

        String prefixString = section.getString("Prefix");
        prefix = prefixString == null ? BPChat.prefix : prefixString;
    }

    /**
     * Get a list of possible messages from the given identifier.
     *
     * @param section The section of the messages.yml.
     * @param id      The identifier name.
     * @return A list of messages.
     */
    public static List<String> getPossibleMessages(ConfigurationSection section, String id) {
        List<String> configMessages = section.getStringList(id);
        if (configMessages.isEmpty()) {
            String singleMessage = section.getString(id);
            if (singleMessage != null && !singleMessage.isEmpty()) configMessages.add(singleMessage);
        }
        return configMessages;
    }

    /**
     * Return the specified message formatted with the existing placeholders.
     *
     * @param text The origin text.
     * @return The formatted message.
     */
    public static String format(String text) {
        return BPChat.color(text.replace("%prefix%", prefix));
    }

    /**
     * Return the specified message formatted with the existing placeholders.
     *
     * @param p    The player, for placeholderApi placeholders.
     * @param text The origin text.
     * @return The formatted message.
     */
    public static String format(Player p, String text) {
        text = BPChat.color(text.replace("%prefix%", prefix));
        return BankPlus.INSTANCE().isPlaceholderApiHooked() ? PlaceholderAPI.setPlaceholders(p, text) : text;
    }

    /**
     * Return the specified message formatted with the existing placeholders.
     *
     * @param s    The command sender.
     * @param text The origin text.
     * @return The formatted message.
     */
    public static String format(CommandSender s, String text) {
        text = BPChat.color(text.replace("%prefix%", prefix));
        return s instanceof Player ? format((Player) s, text) : text;
    }

    /**
     * Check if the specified id is to be skipped. (Example: "Prefix")
     *
     * @param id The id to check.
     * @return true if is skippable, false otherwise.
     */
    public static boolean isIgnoredId(String id) {
        for (String idToSkip : idsToSkip)
            if (id.equals(idToSkip)) return true;
        return false;
    }
}