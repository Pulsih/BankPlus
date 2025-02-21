package me.pulsi_.bankplus.utils;

import javafx.print.Collation;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
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
            BPMessages.send(s, "Invalid-Number");
            return true;
        }
        if (number.contains("%")) number = number.replace("%", "");

        try {
            BigDecimal num = new BigDecimal(number);
            if (num.doubleValue() < 0) {
                BPMessages.send(s, "Cannot-Use-Negative-Number");
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            BPMessages.send(s, "Invalid-Number");
            return true;
        }
    }

    public static boolean isDepositing(Player p) {
        return BPSets.playerDepositing.contains(p.getUniqueId());
    }

    public static boolean isWithdrawing(Player p) {
        return BPSets.playerWithdrawing.contains(p.getUniqueId());
    }

    public static boolean isPlayer(CommandSender s) {
        if (s instanceof Player) return true;
        BPMessages.send(s, "Not-Player");
        return false;
    }

    public static boolean hasPermission(CommandSender s, String permission) {
        if (s.hasPermission(permission)) return true;
        BPMessages.send(s, "No-Permission", "%permission%$" + permission);
        return false;
    }

    public static void sendTitle(String title, Player p) {
        if (title == null || p == null) return;

        title = BPMessages.format(title);
        if (title.contains(",")) {
            String[] titles = title.split(",");
            String title1 = titles[0], title2 = titles[1];

            if (titles.length == 2) p.sendTitle(title1, title2);
            else {
                int[] values = {20, 20, 20};
                boolean error = false;

                for (int i = 2; i < titles.length; i++) {
                    try {
                        values[i - 2] = Integer.parseInt(titles[i]);
                    } catch (NumberFormatException e) {
                        error = true;
                    }
                }
                if (error)
                    BPLogger.warn("Invalid number in the title fades values! Please correct it as soon as possible! (Title: " + title + "&a)");

                try {
                    p.sendTitle(title1, title2, values[0], values[1], values[2]);
                } catch (NoSuchMethodError e) {
                    p.sendTitle(title1, title2);
                }
            }
        } else p.sendTitle(title, "");
    }

    /**
     * Play a sound for that player.
     *
     * @param soundString The sound string.
     * @param p           The player target.
     * @return Return true on successfully play.
     */
    public static boolean playSound(String soundString, Player p) {
        if (soundString == null || soundString.isEmpty()) {
            BPLogger.warn("No sound have been specified.");
            return false;
        }

        String[] values;
        if (soundString.contains(",")) values = soundString.split(",");
        else values = new String[]{soundString};

        Sound sound;
        float volume = 5, pitch = 1;

        try {
            sound = Sound.valueOf(values[0]);
        } catch (IllegalArgumentException e) {
            BPLogger.warn("\"" + values[0] + "\" is an invalid sound enum, change it in config.yml searching the correct enum name based on your server version. (This is not an error)");
            return false;
        }

        if (values.length > 1) {
            try {
                volume = Float.parseFloat(values[1]);
            } catch (NumberFormatException e) {
                BPLogger.warn("\"" + values[1] + "\" is not a valid volume number.");
            }
        }
        if (values.length > 2) {
            try {
                pitch = Float.parseFloat(values[2]);
            } catch (NumberFormatException e) {
                BPLogger.warn("\"" + values[2] + "\" is not a valid pitch number.");
            }
        }

        p.playSound(p.getLocation(), sound, volume, pitch);
        return true;
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
        if (amount.doubleValue() < 0) {
            BPMessages.send(p, "Cannot-Use-Negative-Number");
            return false;
        }
        if (money.doubleValue() == 0) {
            BPMessages.send(p, "Insufficient-Money");
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

        values.add("%" + identifier + "%$" + BPFormatter.formatDecimals(amount));
        values.add("%" + identifier + "_long%$" + amount.toPlainString());
        values.add("%" + identifier + "_formatted%$" + BPFormatter.formatPrecise(amount));
        values.add("%" + identifier + "_formatted_long%$" + BPFormatter.formatLong(amount));
        values.add("%" + identifier + "_formatted_commas%$" + BPFormatter.formatCommas(amount));

        if (level > 0) values.add("%level%$" + level);
        return values;
    }

    public static boolean isBankFull(Player p) {
        return isBankFull(p, BankUtils.getBank(ConfigValues.getMainGuiName()));
    }

    public static boolean isBankFull(Player p, Bank bank) {
        BigDecimal capacity = BankUtils.getCapacity(bank, p);
        if (capacity.compareTo(BigDecimal.ZERO) <= 0) return false;

        if (bank.getBankEconomy().getBankBalance(p).compareTo(capacity) >= 0) {
            BPMessages.send(p, "Cannot-Deposit-Anymore");
            return true;
        }
        return false;
    }

    public static boolean hasFailed(Player p, EconomyResponse response) {
        if (!response.transactionSuccess()) {
            BPMessages.send(p, "Internal-Error");
            BPLogger.warn("Vault has failed his transaction task. To avoid dupe bugs, bankplus has also cancelled the transaction.");
            BPLogger.warn("Additional Vault error info:");
            BPLogger.warn("  Error message: " + response.errorMessage);
            BPLogger.warn("  Transaction amount: " + response.amount);
            BPLogger.warn("  Transaction type: " + response.type);
            BPLogger.warn("  Player wallet: " + response.balance);
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
    public static String getRequiredItemsFormatted(Collection<ItemStack> requiredItems) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (ItemStack requiredItem : requiredItems) {
            int amount = requiredItem.getAmount();

            String name;
            ItemMeta meta = requiredItem.getItemMeta();
            if (meta != null && meta.hasDisplayName()) name = meta.getDisplayName();
            else name = requiredItem.getType().toString();

            builder.append(amount).append(" ").append(name);
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