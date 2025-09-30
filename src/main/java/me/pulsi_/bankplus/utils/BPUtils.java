package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BPUtils {

    /**
     * BankPlus does not accept negative numbers, if a number is lower than 0, it will return true.
     *
     * @param number The number to check.
     * @return true if is invalid or false if is not.
     */
    public static boolean isInvalidNumber(String number) {
        return isInvalidNumber(number, null);
    }

    /**
     * BankPlus does not accept negative numbers, if a number is lower than 0, it will return true.
     *
     * @param number The number to check.
     * @param s      The command sender to automatically alert if number is invalid.
     * @return true if is invalid or false if is not.
     */
    public static boolean isInvalidNumber(String number, CommandSender s) {
        if (number == null || number.isEmpty()) {
            BPMessages.sendIdentifier(s, "Invalid-Number");
            return true;
        }
        if (number.contains("%")) number = number.replace("%", "");

        try {
            BigDecimal num = new BigDecimal(number);
            if (num.doubleValue() < 0) {
                BPMessages.sendIdentifier(s, "Cannot-Use-Negative-Number");
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            BPMessages.sendIdentifier(s, "Invalid-Number");
            return true;
        }
    }

    public static boolean isPlayer(CommandSender s) {
        if (s instanceof Player) return true;
        BPMessages.sendIdentifier(s, "Not-Player");
        return false;
    }

    public static boolean hasPermission(CommandSender s, String permission) {
        if (s.hasPermission(permission)) return true;
        BPMessages.sendIdentifier(s, "No-Permission", "%permission%$" + permission);
        return false;
    }

    /**
     * Tries to convert the specified string to a number, or returns
     * the fallback value in case the string is not a valid number.
     *
     * @param number   The number string to convert.
     * @param fallBack The fall-back value in case the string is not a valid number.
     * @return The converted string or fall-back.
     */
    public static Number convertToNumber(String number, Number fallBack) {
        return convertToNumber(number, fallBack, null);
    }

    /**
     * Tries to convert the specified string to a number, or returns
     * the fallback value in case the string is not a valid number.
     *
     * @param number       The number string to convert.
     * @param fallBack     The fall-back value in case the string is not a valid number.
     * @param errorMessage The message to show in the console warning.
     * @return The converted string or fall-back.
     */
    public static <T extends Number> T convertToNumber(String number, T fallBack, String errorMessage) {
        try {
            return switch (fallBack) {
                case Integer ignored -> (T) Integer.valueOf(number);
                case Double ignored -> (T) Double.valueOf(number);
                case Float ignored -> (T) Float.valueOf(number);
                case Long ignored -> (T) Long.valueOf(number);
                case Short ignored -> (T) Short.valueOf(number);
                case Byte ignored -> (T) Byte.valueOf(number);
                default -> fallBack;
            };
        } catch (NumberFormatException e) {
            if (errorMessage != null) BPLogger.Console.warn(errorMessage);
        }
        return fallBack;
    }

    /**
     * Send the specified title to the player, analyzing and splitting the title in different values.
     * <p>
     * The format is: Title,Subtitle,fadeInTicks,stayTicks,fadeOutTicks.
     *
     * @param titleString The title format.
     * @param p           The receiver of the title.
     */
    public static void sendTitle(String titleString, Player p) {
        if (titleString == null || p == null) return;

        MiniMessage mm = MiniMessage.miniMessage();
        Component title, subtitle;
        int fadeIn = 20, stay = 20, fadeOut = 20;

        if (!titleString.contains(",")) {
            title = mm.deserialize(titleString);
            subtitle = mm.deserialize(" ");
        } else {
            String[] values = titleString.split(",");
            int l = values.length;

            title = mm.deserialize(values[0]);
            subtitle = mm.deserialize(values[1]);
            if (l > 2)
                fadeIn = convertToNumber(values[2], fadeIn, "The fadeIn value in the title \"" + titleString + "\" is invalid.");
            if (l > 3)
                stay = convertToNumber(values[3], stay, "The stay value in the title \"" + titleString + "\" is invalid.");
            if (l > 4)
                fadeOut = convertToNumber(values[4], fadeOut, "The fadeOut value in the title \"" + titleString + "\" is invalid.");
        }

        p.showTitle(Title.title(title, subtitle, Title.Times.times(toTicks(fadeIn), toTicks(stay), toTicks(fadeOut))));
    }

    /**
     * Play the specified title to the player, analyzing and splitting the sound in different values.
     * <p>
     * The format is: Sound,Volume,Pitch.
     *
     * @param soundString The sound format.
     * @param p           The receiver of the sound.
     */
    public static void playSound(String soundString, Player p) {
        if (soundString == null || p == null) return;

        String soundName;
        float volume = 5, pitch = 1;

        // Catch IllegalArgumentException for registry
        if (!soundString.contains(",")) soundName = soundString;
        else {
            String[] values = soundString.split(",");
            int l = values.length;

            soundName = values[0]; // If there is a "," there will be 2 values.
            volume = convertToNumber(values[1], volume, "The volume value in the sound \"" + soundString + "\" is invalid.");
            if (l > 2)
                pitch = convertToNumber(values[2], pitch, "The pitch value in the sound \"" + soundString + "\" is invalid.");
        }

        Sound sound;
        try {
            sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName));
        } catch (IllegalArgumentException e) {
            BPLogger.Console.warn("The sound \"" + soundString + "\" has an invalid sound namespacekey.");
            return;
        }

        if (sound != null) p.playSound(p.getLocation(), sound, volume, pitch);
        else BPLogger.Console.warn("The sound \"" + soundString + "\" has an invalid sound namespacekey.");
    }

    /**
     * Create a Duration instance of delay in ticks.
     *
     * @param ticks The ticks.
     * @return The duration instance in ticks.
     */
    public static Duration toTicks(int ticks) {
        return Duration.ofMillis(ticks * 50L);
    }

    /**
     * Convert a list of string to a list of MiniMessage components.
     *
     * @param list The list to convert.
     * @return A list of components.
     */
    public static List<Component> stringListToComponentList(List<String> list) {
        List<Component> result = new ArrayList<>();
        MiniMessage mm = MiniMessage.miniMessage();
        for (String line : list) result.add(mm.deserialize(line));
        return result;
    }

    /**
     * Convert a list of MiniMessage components to a list of strings.
     *
     * @param list The list to convert.
     * @return A list of string.
     */
    public static List<String> componentListToStringList(List<Component> list) {
        List<String> result = new ArrayList<>();
        MiniMessage mm = MiniMessage.miniMessage();
        for (Component line : list) result.add(mm.serialize(line));
        return result;
    }

    public static long secondsInMilliseconds(int seconds) {
        return seconds * 1000L;
    }

    public static long minutesInMilliseconds(int minutes) {
        return minutes * secondsInMilliseconds(60);
    }

    public static long hoursInMilliseconds(int hours) {
        return hours * minutesInMilliseconds(60);
    }

    public static long daysInMilliseconds(int days) {
        return days * hoursInMilliseconds(24);
    }

    public static int millisecondsInTicks(long milliseconds) {
        return (int) ((milliseconds / 1000) * 20);
    }

    public static long ticksInMilliseconds(int ticks) {
        return (ticks / 20) * 1000;
    }

    public static boolean checkPreRequisites(BigDecimal money, BigDecimal amount, Player p) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            BPMessages.sendIdentifier(p, "Cannot-Use-Negative-Number");
            return false;
        }
        if (money.compareTo(BigDecimal.ZERO) == 0) {
            BPMessages.sendIdentifier(p, "Insufficient-Money");
            return false;
        }
        return true;
    }

    public static boolean hasOfflinePermission(OfflinePlayer p, String permission) {
        Permission perm = BankPlus.INSTANCE().getPermissions();
        boolean hasPermission = false;
        if (perm != null && permission != null && !permission.isEmpty()) {
            for (World world : Bukkit.getWorlds()) {
                if (!perm.playerHas(world.getName(), p, permission)) continue;

                hasPermission = true;
                break;
            }
        }
        return hasPermission;
    }

    public static List<String> placeValues(BigDecimal amount) {
        return placeValues(null, null, amount, "amount", 0);
    }

    public static List<String> placeValues(String bank, BigDecimal amount) {
        return placeValues(null, bank, amount, "amount", 0);
    }

    public static List<String> placeValues(OfflinePlayer p, BigDecimal amount) {
        return placeValues(p, null, amount, "amount", 0);
    }

    public static List<String> placeValues(OfflinePlayer p, BigDecimal amount, int level) {
        return placeValues(p, null, amount, "amount", level);
    }

    public static List<String> placeValues(BigDecimal amount, String identifier) {
        return placeValues(null, null, amount, identifier, 0);
    }

    public static List<String> placeValues(OfflinePlayer p, BigDecimal amount, String identifier) {
        return placeValues(p, null, amount, identifier, 0);
    }

    public static List<String> placeValues(OfflinePlayer p, String bank, BigDecimal amount, String identifier, int level) {
        List<String> values = new ArrayList<>();
        if (p != null) {
            values.add("%player%$" + p.getName());
            values.add("%player_name%$" + p.getName());
        }

        if (bank != null) values.add("%bank%$" + bank);

        values.add("%" + identifier + "%$" + BPFormatter.formatCommas(amount));
        values.add("%" + identifier + "_long%$" + amount.toPlainString());
        values.add("%" + identifier + "_formatted%$" + BPFormatter.formatPrecise(amount));
        values.add("%" + identifier + "_formatted_long%$" + BPFormatter.formatLong(amount));

        if (level > 0) values.add("%level%$" + level);
        return values;
    }

    public static boolean isBankFull(Player p) {
        return isBankFull(p, BankRegistry.getBank(ConfigValues.getMainGuiName()));
    }

    public static boolean isBankFull(Player p, Bank bank) {
        BigDecimal capacity = BankUtils.getCapacity(bank, p);
        if (capacity.compareTo(BigDecimal.ZERO) <= 0) return false;

        if (bank.getBankEconomy().getBankBalance(p).compareTo(capacity) >= 0) {
            BPMessages.sendIdentifier(p, "Cannot-Deposit-Anymore");
            return true;
        }
        return false;
    }

    public static boolean hasFailed(Player p, EconomyResponse response) {
        if (!response.transactionSuccess()) {
            BPMessages.sendIdentifier(p, "Internal-Error");
            BPLogger.Console.warn("Vault has failed his transaction task. To avoid dupe bugs, bankplus has also cancelled the transaction.");
            BPLogger.Console.warn("Additional Vault error info:");
            BPLogger.Console.warn("  Error message: " + response.errorMessage);
            BPLogger.Console.warn("  Transaction amount: " + response.amount);
            BPLogger.Console.warn("  Transaction type: " + response.type);
            BPLogger.Console.warn("  Player wallet: " + response.balance);
            return true;
        }
        return false;
    }

    public static void callEvent(Event event) {
        Bukkit.getScheduler().runTask(BankPlus.INSTANCE(), () -> Bukkit.getPluginManager().callEvent(event));
    }

    /**
     * Return a string with all the required items using the following format:
     * - "[itemAmount] [itemName], [itemAmount] [itemName] and [itemAmount] [itemName]"
     *
     * @param requiredItems A list of required items.
     * @return A string of required items.
     */
    public static String getRequiredItemsFormatted(Collection<Bank.RequiredItem> requiredItems) {
        StringBuilder builder = new StringBuilder();
        MiniMessage mm = MiniMessage.miniMessage();

        int i = 0;
        for (Bank.RequiredItem requiredItem : requiredItems) {
            ItemStack item = requiredItem.item;
            int amount = requiredItem.amount;

            Component displayname;
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) displayname = meta.displayName();
            else displayname = item.displayName();

            builder.append(amount).append(" ").append(mm.serialize(displayname));
            if (i == requiredItems.size() - 1) continue;
            if (i + 1 == requiredItems.size() - 1) builder.append(" and ");
            else builder.append(", ");
            i++;
        }
        return builder.toString();
    }

    /**
     * Check if the selected path in the config exist.
     *
     * @param config The config.
     * @param path   The path.
     * @return true if it exists.
     */
    public static boolean pathExist(FileConfiguration config, String path) {
        return config != null && config.get(path) != null;
    }
}