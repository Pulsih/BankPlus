package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankGuis.BanksManager;
import me.pulsi_.bankplus.managers.ConfigManager;
import me.pulsi_.bankplus.managers.TaskManager;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BPMethods {

    public static String formatTime(long milliseconds) {
        if (!Values.CONFIG.isInterestEnabled()) return BPChat.color("&cInterest disabled.");

        long seconds = milliseconds / 1000;
        if (seconds <= 0) return placeSeconds(Values.CONFIG.getInterestTimeOnlySeconds(), 0);
        if (seconds < 60) return placeSeconds(Values.CONFIG.getInterestTimeOnlySeconds(), seconds);

        long minutes = seconds / 60;
        long newSeconds = seconds - (60 * minutes);
        if (seconds < 3600) {
            if (seconds % 60 == 0) return placeMinutes(Values.CONFIG.getInterestTimeOnlyMinutes(), minutes);

            String secondsPlaced = placeSeconds(Values.CONFIG.getInterestTimeSecondsMinutes(), newSeconds);
            return placeMinutes(secondsPlaced, minutes);
        }

        long hours = seconds / 3600;
        long newMinutes = minutes - (60 * hours);
        if (seconds < 86400) {
            if (newSeconds == 0 && newMinutes == 0)
                return placeHours(Values.CONFIG.getInterestTimeOnlyHours(), hours);
            if (newSeconds == 0) {
                String minutesPlaced = placeMinutes(Values.CONFIG.getInterestTimeMinutesHours(), newMinutes);
                return placeHours(minutesPlaced, hours);
            }
            if (newMinutes == 0) {
                String secondsPlaced = placeSeconds(Values.CONFIG.getInterestTimeSecondsHours(), newSeconds);
                return placeHours(secondsPlaced, hours);
            }

            String secondsPlaced = placeSeconds(Values.CONFIG.getInterestTimeSecondsMinutesHours(), newSeconds);
            String minutesPlaced = placeMinutes(secondsPlaced, newMinutes);
            return placeHours(minutesPlaced, hours);
        }

        long days = seconds / 86400;
        long newHours = hours - (24 * days);
        if (newSeconds == 0 && newMinutes == 0 && newHours == 0)
            return placeDays(Values.CONFIG.getInterestTimeOnlyDays(), days);
        if (newSeconds == 0 && newMinutes == 0) {
            String hoursPlaced = placeHours(Values.CONFIG.getInterestTimeHoursDays(), newHours);
            return placeDays(hoursPlaced, days);
        }
        if (newSeconds == 0 && newHours == 0) {
            String minutesPlaced = placeMinutes(Values.CONFIG.getInterestTimeMinutesDays(), newMinutes);
            return placeDays(minutesPlaced, days);
        }
        if (newMinutes == 0 && newHours == 0) {
            String secondsPlaced = placeMinutes(Values.CONFIG.getInterestTimeSecondsDays(), newSeconds);
            return placeDays(secondsPlaced, days);
        }

        if (newSeconds == 0) {
            String minutesPlaced = placeMinutes(Values.CONFIG.getInterestTimeMinutesHoursDays(), newMinutes);
            String hoursPlaced = placeHours(minutesPlaced, newHours);
            return placeDays(hoursPlaced, days);
        }
        if (newMinutes == 0) {
            String secondsPlaced = placeSeconds(Values.CONFIG.getInterestTimeSecondsHoursDays(), newSeconds);
            String hoursPlaced = placeHours(secondsPlaced, newHours);
            return placeDays(hoursPlaced, days);
        }
        if (newHours == 0) {
            String secondsPlaced = placeSeconds(Values.CONFIG.getInterestTimeSecondsMinutesDays(), newSeconds);
            String minutesPlaced = placeMinutes(secondsPlaced, newMinutes);
            return placeDays(minutesPlaced, days);
        }

        String secondsPlaced = placeSeconds(Values.CONFIG.getInterestTimeSecondsMinutesHoursDays(), newSeconds);
        String minutesPlaced = placeMinutes(secondsPlaced, newMinutes);
        String hoursPlaced = placeHours(minutesPlaced, newHours);
        return placeDays(hoursPlaced, days);
    }

    private static String placeSeconds(String message, long seconds) {
        String time = message.replace("%seconds%", String.valueOf(seconds));
        if (seconds == 1) return time.replace("%seconds_placeholder%", Values.CONFIG.getSecond());
        else return time.replace("%seconds_placeholder%", Values.CONFIG.getSeconds());
    }

    private static String placeMinutes(String message, long minutes) {
        String time = message.replace("%minutes%", String.valueOf(minutes));
        if (minutes == 1) return time.replace("%minutes_placeholder%", Values.CONFIG.getMinute());
        else return time.replace("%minutes_placeholder%", Values.CONFIG.getMinutes());
    }

    private static String placeHours(String message, long hours) {
        String time = message.replace("%hours%", String.valueOf(hours));
        if (hours == 1) return time.replace("%hours_placeholder%", Values.CONFIG.getHour());
        else return time.replace("%hours_placeholder%", Values.CONFIG.getHours());
    }

    private static String placeDays(String message, long days) {
        String time = message.replace("%days%", String.valueOf(days));
        if (days == 1) return time.replace("%days_placeholder%", Values.CONFIG.getDay());
        else return time.replace("%days_placeholder%", Values.CONFIG.getDays());
    }

    /**
     * BankPlus does not accept negative numbers, if a number is lower than 0, it will return true.
     * @param number The number to check.
     * @return true if is invalid or false if is not.
     */
    public static boolean isInvalidNumber(String number) {
        return isInvalidNumber(number, null);
    }

    /**
     * BankPlus does not accept negative numbers, if a number is lower than 0, it will return true.
     * @param number The number to check.
     * @param s The command sender to automatically alert if number is invalid.
     * @return true if is invalid or false if is not.
     */
    public static boolean isInvalidNumber(String number, CommandSender s) {
        try {
            if (number == null) {
                BPMessages.send(s, "Invalid-Number");
                return true;
            }
            if (Values.CONFIG.getMaxDecimalsAmount() <= 0 && number.contains(".")) {
                if (s != null) BPMessages.send(s, "Invalid-Number");
                return true;
            }

            if (number.contains("%")) number = number.replace("%", "");
            BigDecimal num = new BigDecimal(number);
            if (num.doubleValue() < 0) {
                if (s != null) BPMessages.send(s, "Cannot-Use-Negative-Number");
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            if (s != null) BPMessages.send(s, "Invalid-Number");
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

    public static String formatBigDouble(BigDecimal balance) {
        String bal = balance.toString();
        int maxDecimals = Values.CONFIG.getMaxDecimalsAmount();

        if (maxDecimals <= 0) return bal.contains(".") ? bal.split("\\.")[0] : bal;

        if (balance.doubleValue() > 0 && bal.contains(".")) {
            String decimals = bal.split("\\.")[1];
            if (decimals.length() > maxDecimals) {
                String correctedDecimals = decimals.substring(0, maxDecimals);
                bal = bal.split("\\.")[0] + "." + correctedDecimals;
            }
        } else {
            StringBuilder decimals = new StringBuilder();
            for (int i = 0; i < maxDecimals; i++) decimals.append("0");
            if (balance.doubleValue() <= 0) bal = "0." + decimals;
            else bal += "." + decimals;
        }
        return bal;
    }

    public static void startSavingBalancesTask() {
        TaskManager tasks = BankPlus.INSTANCE.getTaskManager();
        BukkitTask task = tasks.getSavingTask();
        if (task != null) task.cancel();

        if (Values.CONFIG.getSaveBalancedDelay() <= 0) return;

        // Cache the values out the runnable to improve a bit the performance.
        long delay = Values.CONFIG.getSaveBalancedDelay();
        boolean multi = Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled(), saveBroadcast = Values.CONFIG.isSaveBalancesBroadcast();

        tasks.setSavingTask(Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE, () -> {
            if (multi) Bukkit.getOnlinePlayers().forEach(p -> new MultiEconomyManager(p).saveBankBalance(true));
            else Bukkit.getOnlinePlayers().forEach(p-> new SingleEconomyManager(p).saveBankBalance(true));
            if (saveBroadcast) BPLogger.info("All player balances have been saved!");
        }, delay * 1200L, delay * 1200L));
    }

    public static String formatLong(BigDecimal balance) {
        double bal = balance.doubleValue();
        if (bal < 1000L) return "" + balance;
        if (bal < 1000000L) return Math.round(bal / 1000D) + Values.CONFIG.getK();
        if (bal < 1000000000L) return Math.round(bal / 1000000D) + Values.CONFIG.getM();
        if (bal < 1000000000000L) return Math.round(bal / 1000000000D) + Values.CONFIG.getB();
        if (bal < 1000000000000000L) return Math.round(bal / 1000000000000D) + Values.CONFIG.getT();
        if (bal < 1000000000000000000L) return Math.round(bal / 1000000000000000D) + Values.CONFIG.getQ();
        return Math.round(bal / 1000000000000000000D) + Values.CONFIG.getQq();
    }

    public static String format(BigDecimal balance) {
        double bal = balance.doubleValue();
        if (bal < 1000L) return setMaxDigits(balance, 2);
        if (bal < 1000000L) return setMaxDigits(BigDecimal.valueOf(bal / 1000L), 2) + Values.CONFIG.getK();
        if (bal < 1000000000L) return setMaxDigits(BigDecimal.valueOf(bal / 1000000D), 2) + Values.CONFIG.getM();
        if (bal < 1000000000000L) return setMaxDigits(BigDecimal.valueOf(bal / 1000000000D), 2) + Values.CONFIG.getB();
        if (bal < 1000000000000000L) return setMaxDigits(BigDecimal.valueOf(bal / 1000000000000D), 2) + Values.CONFIG.getT();
        if (bal < 1000000000000000000L) return setMaxDigits(BigDecimal.valueOf(bal / 1000000000000000D), 2) + Values.CONFIG.getQ();
        return setMaxDigits(BigDecimal.valueOf(bal / 1000000000000000000L), 2) + Values.CONFIG.getQq();
    }

    public static String setMaxDigits(BigDecimal balance, int digits) {
        NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
        format.setMaximumFractionDigits(digits);
        format.setMinimumFractionDigits(0);
        return format.format(balance);
    }

    public static String formatCommas(BigDecimal amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }

    public static void customWithdraw(Player p) {
        customWithdraw(p, Values.CONFIG.getMainGuiName());
    }

    public static void customWithdraw(Player p, String identifier) {
        if (!hasPermission(p, "bankplus.withdraw")) return;

        if (Values.MESSAGES.isTitleCustomAmountEnabled())
            BPMethods.sendTitle(BankPlus.INSTANCE.getConfigManager().getConfig(ConfigManager.Type.MESSAGES).getString("Title-Custom-Transaction.Title-Withdraw"), p);
        BPMessages.send(p, "Chat-Withdraw");
        BPSets.addPlayerToWithdraw(p);
        p.closeInventory();
        BankPlus.INSTANCE.getPlayerRegistry().get(p).setOpenedBank(BankPlus.INSTANCE.getBankGuiRegistry().get(identifier));
    }

    public static void customDeposit(Player p) {
        customDeposit(p, Values.CONFIG.getMainGuiName());
    }

    public static void customDeposit(Player p, String identifier) {
        if (!hasPermission(p, "bankplus.deposit")) return;

        if (Values.MESSAGES.isTitleCustomAmountEnabled())
            BPMethods.sendTitle(BankPlus.INSTANCE.getConfigManager().getConfig(ConfigManager.Type.MESSAGES).getString("Title-Custom-Transaction.Title-Deposit"), p);
        BPMessages.send(p, "Chat-Deposit");
        BPSets.addPlayerToDeposit(p);
        p.closeInventory();
        BankPlus.INSTANCE.getPlayerRegistry().get(p).setOpenedBank(BankPlus.INSTANCE.getBankGuiRegistry().get(identifier));
    }

    public static void sendTitle(String title, Player p) {
        if (title == null) return;

        String newTitle = BPMessages.addPrefix(title);
        if (newTitle.contains(",")) {
            String[] titles = newTitle.split(",");
            String title1 = titles[0], title2 = titles[1];

            if (titles.length == 2) p.sendTitle(BPChat.color(title1), BPChat.color(title2));
            else {
                int fadeIn, stay, fadeOut;
                try {
                    fadeIn = Integer.parseInt(titles[2]);
                    stay = Integer.parseInt(titles[3]);
                    fadeOut = Integer.parseInt(titles[4]);
                } catch (IllegalArgumentException e) {
                    BPLogger.warn("Invalid title string! (NOT BANKPLUS ERROR!) Title: " + title + ". Error: " + e.getMessage());
                    return;
                }
                try {
                    p.sendTitle(BPChat.color(title1), BPChat.color(title2), fadeIn, stay, fadeOut);
                } catch (NoSuchMethodError e) {
                    p.sendTitle(BPChat.color(title1), BPChat.color(title2));
                }
            }
        } else p.sendTitle(BPChat.color(newTitle), "");
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
        int volume, pitch;

        try {
            soundType = pathSlitted[0];
            volume = Integer.parseInt(pathSlitted[1]);
            pitch = Integer.parseInt(pathSlitted[2]);
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

    public static String getSoundBasedOnServerVersion() {
        String v = BankPlus.INSTANCE.getServerVersion();
        if (v.contains("1.7") || v.contains("1.8")) return "ORB_PICKUP,5,1";
        else return "ENTITY_EXPERIENCE_ORB_PICKUP,5,1";
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

    public static boolean checkPreRequisites(BigDecimal money, BigDecimal amount, Player p) {
        if (amount.doubleValue() < 0) {
            BPMessages.send(p, "Cannot-Use-Negative-Number");
            return false;
        }
        if (money.doubleValue() <= 0) {
            BPMessages.send(p, "Insufficient-Money");
            return false;
        }
        return true;
    }

    public static List<String> placeValues(Player p, BigDecimal amount) {
        List<String> values = new ArrayList<>();
        values.add("%player%$" + p.getName());
        values.add("%player_name%$" + p.getName());

        values.add("%amount%$" + BPMethods.formatCommas(amount));
        values.add("%amount_long%$" + amount);
        values.add("%amount_formatted%$" + BPMethods.format(amount));
        values.add("%amount_formatted_long%$" + BPMethods.formatLong(amount));
        return values;
    }

    public static List<String> placeValues(OfflinePlayer p, BigDecimal amount) {
        List<String> values = new ArrayList<>();
        values.add("%player%$" + p.getName());
        values.add("%player_name%$" + p.getName());

        values.add("%amount%$" + BPMethods.formatCommas(amount));
        values.add("%amount_long%$" + amount);
        values.add("%amount_formatted%$" + BPMethods.format(amount));
        values.add("%amount_formatted_long%$" + BPMethods.formatLong(amount));
        return values;
    }

    public static List<String> placeValues(OfflinePlayer p, BigDecimal amount, BigDecimal taxes) {
        List<String> values = new ArrayList<>();
        values.add("%player%$" + p.getName());
        values.add("%player_name%$" + p.getName());

        values.add("%amount%$" + BPMethods.formatCommas(amount));
        values.add("%amount_long%$" + amount);
        values.add("%amount_formatted%$" + BPMethods.format(amount));
        values.add("%amount_formatted_long%$" + BPMethods.formatLong(amount));

        values.add("%taxes%$" + BPMethods.formatCommas(taxes));
        values.add("%taxes_long%$" + taxes);
        values.add("%taxes_formatted%$" + BPMethods.format(taxes));
        values.add("%taxes_formatted_long%$" + BPMethods.formatLong(taxes));
        return values;
    }

    public static boolean isBankFull(Player p, String bankName) {
        BigDecimal capacity = new BanksManager(bankName).getCapacity(p);
        if (new MultiEconomyManager(p).getBankBalance(bankName).doubleValue() >= capacity.doubleValue()) {
            BPMessages.send(p, "Cannot-Deposit-Anymore");
            return true;
        }
        return false;
    }

    public static boolean isBankFull(Player p) {
        BigDecimal capacity = new BanksManager(Values.CONFIG.getMainGuiName()).getCapacity(p);
        if (new SingleEconomyManager(p).getBankBalance().doubleValue() >= capacity.doubleValue()) {
            BPMessages.send(p, "Cannot-Deposit-Anymore");
            return true;
        }
        return false;
    }

    public static boolean hasFailed(Player p, EconomyResponse response) {
        if (!response.transactionSuccess()) {
            BPMessages.send(p, "Internal-Error");
            BPLogger.warn("Warning! (THIS IS NOT A BANKPLUS ERROR!) Vault has failed his transaction task. To" +
                    " avoid dupe bugs also bankplus has cancelled the transaction.");
            return true;
        }
        return false;
    }

    public static boolean isLegacyServer() {
        String v = BankPlus.INSTANCE.getServerVersion();
        return v.contains("1.7") || v.contains("1.8") || v.contains("1.9") ||
                v.contains("1.10") || v.contains("1.11") || v.contains("1.12");
    }
}