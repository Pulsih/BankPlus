package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.listeners.playerChat.PlayerChatMethod;
import me.pulsi_.bankplus.managers.BPConfigs;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BPUtils {

    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        String format = Values.CONFIG.getInterestTimeFormat();
        String[] parts = format.split("%");
        int amount = parts.length;

        for (int i = 1; i < amount; i++) {
            String identifier = parts[i];
            long time = 0;

            switch (identifier) {
                case "s":
                    time = seconds - (minutes * 60);
                    break;
                case "m":
                    time = minutes - (hours * 60);
                    break;
                case "h":
                    time = hours - (days * 24);
                    break;
                case "d":
                    time = days;
                    break;
            }
            if (time <= 0) format = format.replace("%" + identifier, "");
        }

        parts = format.split("%");
        amount = parts.length;

        for (int i = 1; i < amount; i++) {
            String identifier = parts[i], timeIdentifier = "";
            long time = 0;

            switch (identifier) {
                case "s":
                    time = seconds - (minutes * 60);
                    timeIdentifier = "seconds";
                    break;
                case "m":
                    time = minutes - (hours * 60);
                    timeIdentifier = "minutes";
                    break;
                case "h":
                    time = hours - (days * 24);
                    timeIdentifier = "hours";
                    break;
                case "d":
                    time = days;
                    timeIdentifier = "days";
                    break;
            }

            int last = i + 2;
            String separator = last > amount ? "" : last == amount ? Values.CONFIG.getInterestTimeFinalSeparator() : Values.CONFIG.getInterestTimeSeparator();
            String replacer = time <= 0 ? "" : time + getTimeIdentifier(timeIdentifier, time) + separator;

            format = format.replace("%" + identifier, replacer);
        }

        return format;
    }

    private static String getTimeIdentifier(String id, long time) {
        switch (id) {
            case "seconds":
                return time == 1 ? Values.CONFIG.getSecond() : Values.CONFIG.getSeconds();
            case "minutes":
                return time == 1 ? Values.CONFIG.getMinute() : Values.CONFIG.getMinutes();
            case "hours":
                return time == 1 ? Values.CONFIG.getHour() : Values.CONFIG.getHours();
            case "days":
                return time == 1 ? Values.CONFIG.getDay() : Values.CONFIG.getDays();
        }
        return "";
    }

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

    public static void customWithdraw(Player p) {
        customWithdraw(p, Values.CONFIG.getMainGuiName());
    }

    public static void customWithdraw(Player p, String identifier) {
        if (Values.MESSAGES.isTitleCustomAmountEnabled())
            BPUtils.sendTitle(BankPlus.INSTANCE().getConfigs().getConfig(BPConfigs.Type.MESSAGES.name).getString("Title-Custom-Transaction.Title-Withdraw"), p);
        BPMessages.send(p, "Chat-Withdraw");
        BPSets.addPlayerToWithdraw(p);
        p.closeInventory();

        BPPlayer pl = PlayerRegistry.get(p);
        pl.setOpenedBank(BankPlus.INSTANCE().getBankRegistry().getBanks().get(identifier));
        pl.setClosingTask(Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> {
            PlayerChatMethod.reopenBank(p, identifier);
            BPMessages.send(p, "Chat-Time-Expired");
        }, Values.CONFIG.getChatExitTime() * 20L));
    }

    public static void customDeposit(Player p) {
        customDeposit(p, Values.CONFIG.getMainGuiName());
    }

    public static void customDeposit(Player p, String identifier) {
        if (Values.MESSAGES.isTitleCustomAmountEnabled())
            BPUtils.sendTitle(BankPlus.INSTANCE().getConfigs().getConfig(BPConfigs.Type.MESSAGES.name).getString("Title-Custom-Transaction.Title-Deposit"), p);
        BPMessages.send(p, "Chat-Deposit");
        BPSets.addPlayerToDeposit(p);
        p.closeInventory();

        BPPlayer pl = PlayerRegistry.get(p);
        pl.setOpenedBank(BankPlus.INSTANCE().getBankRegistry().getBanks().get(identifier));
        pl.setClosingTask(Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> {
            PlayerChatMethod.reopenBank(p, identifier);
            BPMessages.send(p, "Chat-Time-Expired");
        }, Values.CONFIG.getChatExitTime() * 20L));
    }

    public static void sendTitle(String title, Player p) {
        if (title == null || p == null) return;

        title = BPMessages.addPrefix(title);
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

    public static void playSound(String input, Player p) {

        String sound;
        switch (input) {
            case "WITHDRAW": {
                if (!Values.CONFIG.isWithdrawSoundEnabled()) return;
                sound = Values.CONFIG.getWithdrawSound();
                if (sound == null) {
                    BPLogger.warn("You are missing a string! &8(&ePath: General.Withdraw-Sound.Sound in config.yml&8)");
                    return;
                }
            }
            break;

            case "DEPOSIT": {
                if (!Values.CONFIG.isDepositSoundEnabled()) return;
                sound = Values.CONFIG.getDepositSound();
                if (sound == null) {
                    BPLogger.warn("You are missing a string! &8(&ePath: General.Deposit-Sound.Sound in config.yml&8)");
                    return;
                }
            }
            break;

            case "VIEW": {
                if (!Values.CONFIG.isViewSoundEnabled()) return;
                sound = Values.CONFIG.getViewSound();
                if (sound == null) {
                    BPLogger.warn("You are missing a string! &8(&ePath: General.View-Sound.Sound in config.yml&8)");
                    return;
                }
            }
            break;

            case "PERSONAL": {
                if (!Values.CONFIG.isPersonalSoundEnabled()) return;
                sound = Values.CONFIG.getPersonalSound();
                if (sound == null) {
                    BPLogger.warn("You are missing a string! &8(&ePath: General.Personal-Sound.Sound in config.yml&8)");
                    return;
                }
            }
            break;

            default:
                return;
        }

        if (!sound.contains(",")) {
            BPLogger.warn("The format of the sound \"" + sound + "\" is wrong! ");
            BPLogger.warn("Please correct it in the config!");
            return;
        }
        String[] pathSlitted = sound.split(",");
        String soundType;
        float volume, pitch;

        try {
            soundType = pathSlitted[0];
            volume = Float.parseFloat(pathSlitted[1]);
            pitch = Float.parseFloat(pathSlitted[2]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            BPLogger.warn("The format of the sound \"" + sound + "\" is wrong! ");
            BPLogger.warn("Please correct it in the config!");
            return;
        }

        try {
            p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
        } catch (IllegalArgumentException e) {
            BPLogger.warn("\"" + sound + "\" is an invalid sound type for your server version!");
            BPLogger.warn("Please change it in the config!");
        }
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
        boolean hasPermission = true;

        if (perm != null && permission != null && !permission.isEmpty()) {
            for (World world : Bukkit.getWorlds()) {
                if (perm.playerHas(world.getName(), p, permission)) continue;
                hasPermission = false;
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
        return isBankFull(p, Values.CONFIG.getMainGuiName());
    }

    public static boolean isBankFull(Player p, String bankName) {
        BigDecimal capacity = BankManager.getCapacity(bankName, p);
        if (capacity.doubleValue() <= 0d) return false;

        if (BPEconomy.get(bankName).getBankBalance(p).doubleValue() >= capacity.doubleValue()) {
            BPMessages.send(p, "Cannot-Deposit-Anymore");
            return true;
        }
        return false;
    }

    public static boolean hasFailed(Player p, EconomyResponse response) {
        if (!response.transactionSuccess()) {
            BPMessages.send(p, "Internal-Error");
            BPLogger.warn("Vault has failed his transaction task. To avoid dupe bugs, bankplus has also cancelled the transaction. Reason: " + response.errorMessage);
            return true;
        }
        return false;
    }

    public static void callEvent(Event event) {
        Bukkit.getScheduler().runTask(BankPlus.INSTANCE(), () -> Bukkit.getPluginManager().callEvent(event));
    }

    public static String getRequiredItems(List<ItemStack> requiredItems) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < requiredItems.size(); i++) {
            ItemStack item = requiredItems.get(i);
            int amount = item.getAmount();
            String material = (item.getType() + (amount > 1 ? "s" : "")).toLowerCase();

            builder.append(amount).append(" ").append(material);
            if (i == requiredItems.size() - 1) continue;
            if (i + 1 == requiredItems.size() - 1) builder.append(" and ");
            else builder.append(", ");
        }
        return builder.toString();
    }
}