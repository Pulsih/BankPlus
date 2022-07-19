package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.managers.ConfigManager;
import me.pulsi_.bankplus.managers.MessageManager;
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

    public static boolean isInvalidNumber(String number, CommandSender s) {
        try {
            BigDecimal num = new BigDecimal(number);
            if (num.doubleValue() < 0) {
                MessageManager.send(s, "Cannot-Use-Negative-Number");
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            MessageManager.send(s, "Invalid-Number");
            return true;
        }
    }

    public static boolean isValidNumber(String number) {
        try {
            new BigDecimal(number.replace("%", ""));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDepositing(Player p) {
        return SetUtils.playerDepositing.contains(p.getUniqueId());
    }

    public static boolean isWithdrawing(Player p) {
        return SetUtils.playerWithdrawing.contains(p.getUniqueId());
    }

    public static boolean isPlayer(CommandSender s) {
        if (s instanceof Player) return true;
        MessageManager.send(s, "Not-Player");
        return false;
    }

    public static boolean hasPermission(CommandSender s, String permission) {
        if (s.hasPermission(permission)) return true;
        MessageManager.send(s, "No-Permission", "%permission%$" + permission);
        return false;
    }

    public static String formatBigDouble(BigDecimal bal) {
        String balance = bal.toString();
        if (bal.doubleValue() > 0 && balance.contains(".")) {
            String decimals = balance.split("\\.")[1];
            if (decimals.length() > Values.CONFIG.getMaxDecimalsAmount()) {
                String correctedDecimals = decimals.substring(0, Values.CONFIG.getMaxDecimalsAmount());
                balance = balance.split("\\.")[0] + "." + correctedDecimals;
            }
        } else {
            StringBuilder decimals = new StringBuilder();
            for (int i = 0; i < Values.CONFIG.getMaxDecimalsAmount(); i++) decimals.append("0");
            if (bal.doubleValue() <= 0) balance = "0." + decimals;
            else balance = balance + "." + decimals;
        }
        return balance;
    }

    public static void startSavingBalancesTask() {
        BukkitTask task = TaskManager.getSavingTask();
        if (task != null) task.cancel();
        if (Values.CONFIG.getSaveBalancedDelay() <= 0) return;
        TaskManager.setSavingTask(Bukkit.getScheduler().runTaskTimer(BankPlus.getInstance(), () -> {
            if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) Bukkit.getOnlinePlayers().forEach(MultiEconomyManager::saveBankBalance);
            else Bukkit.getOnlinePlayers().forEach(SingleEconomyManager::saveBankBalance);
            if (Values.CONFIG.isSaveBalancesBroadcast()) BPLogger.info("All player balances have been saved!");
        }, Values.CONFIG.getSaveBalancedDelay() * 1200L, Values.CONFIG.getSaveBalancedDelay() * 1200L));
    }

    public static String formatLong(BigDecimal balance) {
        if (balance.doubleValue() < 1000L) return "" + balance;
        if (balance.doubleValue() < 1000000L)
            return Math.round(balance.divide(BigDecimal.valueOf(1000L)).doubleValue()) + Values.CONFIG.getK();
        if (balance.doubleValue() < 1000000000L)
            return Math.round(balance.divide(BigDecimal.valueOf(1000000L)).doubleValue()) + Values.CONFIG.getM();
        if (balance.doubleValue() < 1000000000000L)
            return Math.round(balance.divide(BigDecimal.valueOf(1000000000L)).doubleValue()) + Values.CONFIG.getB();
        if (balance.doubleValue() < 1000000000000000L)
            return Math.round(balance.divide(BigDecimal.valueOf(1000000000000L)).doubleValue()) + Values.CONFIG.getT();
        if (balance.doubleValue() < 1000000000000000000L)
            return Math.round(balance.divide(BigDecimal.valueOf(1000000000000000L)).doubleValue()) + Values.CONFIG.getQ();
        return Math.round(balance.divide(BigDecimal.valueOf(1000000000000000000L)).doubleValue()) + Values.CONFIG.getQq();
    }

    public static String format(BigDecimal balance) {
        if (balance.doubleValue() < 1000L) return setMaxDigits(balance, 2);
        if (balance.doubleValue() < 1000000L)
            return setMaxDigits(balance.divide(BigDecimal.valueOf(1000L)), 2) + Values.CONFIG.getK();
        if (balance.doubleValue() < 1000000000L)
            return setMaxDigits(balance.divide(BigDecimal.valueOf(1000000L)), 2) + Values.CONFIG.getM();
        if (balance.doubleValue() < 1000000000000L)
            return setMaxDigits(balance.divide(BigDecimal.valueOf(1000000000L)), 2) + Values.CONFIG.getB();
        if (balance.doubleValue() < 1000000000000000L)
            return setMaxDigits(balance.divide(BigDecimal.valueOf(1000000000000L)), 2) + Values.CONFIG.getT();
        if (balance.doubleValue() < 1000000000000000000L)
            return setMaxDigits(balance.divide(BigDecimal.valueOf(1000000000000000L)), 2) + Values.CONFIG.getQ();
        return setMaxDigits(balance.divide(BigDecimal.valueOf(1000000000000000000L)), 2) + Values.CONFIG.getQq();
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
        if (!hasPermission(p, "bankplus.withdraw")) return;
        SetUtils.playerWithdrawing.add(p.getUniqueId());
        if (Values.MESSAGES.isTitleCustomAmountEnabled())
            BPMethods.sendTitle("Title-Custom-Transaction.Title-Withdraw", p);
        MessageManager.send(p, "Chat-Withdraw");
        p.closeInventory();
    }

    public static void customDeposit(Player p) {
        if (!hasPermission(p, "bankplus.deposit")) return;
        SetUtils.playerDepositing.add(p.getUniqueId());
        if (Values.MESSAGES.isTitleCustomAmountEnabled())
            BPMethods.sendTitle("Title-Custom-Transaction.Title-Deposit", p);
        MessageManager.send(p, "Chat-Deposit");
        p.closeInventory();
    }

    public static void sendTitle(String path, Player p) {
        String title = BankPlus.getCm().getConfig(ConfigManager.Type.MESSAGES).getString(path);
        if (title == null) return;

        String newTitle = MessageManager.addPrefix(title);
        if (newTitle.contains(",")) {
            String[] titles = newTitle.split(",");
            String title1 = titles[0];
            String title2 = titles[1];
            p.sendTitle(BPChat.color(title1), BPChat.color(title2));
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
        if (isLegacyServer()) return "ORB_PICKUP,5,1";
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

    public static void withdraw(Player p, BigDecimal amount) {
        if (!hasPermission(p, "bankplus.withdraw")) return;

        BigDecimal bankBalance = SingleEconomyManager.getBankBalance(p);
        if (!hasMoney(bankBalance, amount, p)) return;
        if (bankBalance.doubleValue() <= 0) {
            MessageManager.send(p, "Insufficient-Money");
            return;
        }

        BigDecimal maxWithdrawAmount = Values.CONFIG.getMaxWithdrawAmount();
        if (maxWithdrawAmount.doubleValue() != 0 && amount.doubleValue() >= maxWithdrawAmount.doubleValue())
            amount = maxWithdrawAmount;

        BigDecimal newBalance = bankBalance.subtract(amount);
        if (newBalance.doubleValue() <= 0) amount = bankBalance;

        BigDecimal amountWithTaxes = amount;
        if (Values.CONFIG.getWithdrawTaxes().doubleValue() > 0) {
            amountWithTaxes = amount.subtract(amount.multiply(Values.CONFIG.getWithdrawTaxes().divide(BigDecimal.valueOf(100))));
            amountWithTaxes = new BigDecimal(BPMethods.formatBigDouble(amountWithTaxes));
        }
        EconomyResponse withdrawResponse = BankPlus.getEconomy().depositPlayer(p, amountWithTaxes.doubleValue());
        if (!withdrawResponse.transactionSuccess()) {
            MessageManager.send(p, "Internal-Error");
            BPLogger.warn("Warning! (THIS IS NOT A BANKPLUS ERROR!) Vault has failed his transaction task, to" +
                    " avoid dupe bugs, i also aborted the bankplus withdraw transaction.");
            return;
        }
        BPDebugger.debugWithdraw(p, amount, withdrawResponse);
        SingleEconomyManager.removeBankBalance(p, amount);
        MessageManager.send(p, "Success-Withdraw", BPMethods.placeValues(p, amount));
        BPMethods.playSound("WITHDRAW", p);
    }

    public static void deposit(Player p, BigDecimal amount) {
        if (!hasPermission(p, "bankplus.deposit")) return;

        BigDecimal money = BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p));
        if (!hasMoney(money, amount, p)) return;

        BigDecimal maxDepositAmount = Values.CONFIG.getMaxDepositAmount();
        BigDecimal maxBankCapacity = Values.CONFIG.getMaxBankCapacity();
        BigDecimal bankBalance = SingleEconomyManager.getBankBalance(p);

        if (money.doubleValue() <= amount.doubleValue()) amount = money;
        if (maxDepositAmount.doubleValue() != 0 && amount.doubleValue() >= maxDepositAmount.doubleValue())
            amount = maxDepositAmount;

        BigDecimal newBalance = bankBalance.add(amount);
        if (maxBankCapacity.doubleValue() != 0 && newBalance.doubleValue() >= maxBankCapacity.doubleValue()) {
            if (bankBalance.doubleValue() >= maxBankCapacity.doubleValue()) {
                MessageManager.send(p, "Cannot-Deposit-Anymore");
                return;
            }
            amount = maxBankCapacity.subtract(bankBalance);
        }

        BigDecimal amountWithTaxes = amount;
        if (Values.CONFIG.getDepositTaxes().doubleValue() > 0) {
            amountWithTaxes = amount.subtract(amount.multiply(Values.CONFIG.getDepositTaxes().divide(BigDecimal.valueOf(100))));
            amountWithTaxes = new BigDecimal(BPMethods.formatBigDouble(amountWithTaxes));
        }
        EconomyResponse depositResponse = BankPlus.getEconomy().withdrawPlayer(p, amount.doubleValue());
        if (!depositResponse.transactionSuccess()) {
            MessageManager.send(p, "Internal-Error");
            BPLogger.warn("Warning! (THIS IS NOT A BANKPLUS ERROR!) Vault has failed his transaction task, to" +
                    " avoid dupe bugs, i also aborted the bankplus deposit transaction.");
            return;
        }
        BPDebugger.debugDeposit(p, amount, depositResponse);
        SingleEconomyManager.addBankBalance(p, amountWithTaxes);
        MessageManager.send(p, "Success-Deposit", BPMethods.placeValues(p, amount));
        BPMethods.playSound("DEPOSIT", p);
    }

    private static boolean hasMoney(BigDecimal money, BigDecimal amount, Player p) {
        if (amount.doubleValue() < 0) {
            MessageManager.send(p, "Cannot-Use-Negative-Number");
            return false;
        }
        if (amount.doubleValue() < Values.CONFIG.getMinimumAmount().doubleValue()) {
            MessageManager.send(p, "Minimum-Number");
            return false;
        }
        if (money.doubleValue() <= 0) {
            MessageManager.send(p, "Insufficient-Money");
            return false;
        }
        return true;
    }

    public static List<String> placeValues(Player p, BigDecimal amount) {
        List<String> values = new ArrayList<>();
        values.add("%player_name%$" + p.getName());
        values.add("%amount%$" + BPMethods.formatCommas(amount));
        values.add("%amount_long%$" + amount);
        values.add("%amount_formatted%$" + BPMethods.format(amount));
        values.add("%amount_formatted_long%$" + BPMethods.formatLong(amount));
        return values;
    }

    public static List<String> placeValues(OfflinePlayer p, BigDecimal amount) {
        List<String> values = new ArrayList<>();
        values.add("%player_name%$" + p.getName());
        values.add("%amount%$" + BPMethods.formatCommas(amount));
        values.add("%amount_long%$" + amount);
        values.add("%amount_formatted%$" + BPMethods.format(amount));
        values.add("%amount_formatted_long%$" + BPMethods.formatLong(amount));
        return values;
    }

    public static void pay(Player p1, Player p2, BigDecimal amount) {
        BigDecimal bankBalance = SingleEconomyManager.getBankBalance(p1);
        BigDecimal maxBankCapacity = Values.CONFIG.getMaxBankCapacity();

        if (bankBalance.doubleValue() < amount.doubleValue()) {
            MessageManager.send(p1, "Insufficient-Money");
            return;
        }

        if (maxBankCapacity.doubleValue() == 0) {
            SingleEconomyManager.removeBankBalance(p1, amount);
            MessageManager.send(p1, "Payment-Sent", BPMethods.placeValues(p2, amount));
            SingleEconomyManager.addBankBalance(p2, amount);
            MessageManager.send(p2, "Payment-Received", BPMethods.placeValues(p1, amount));
            return;
        }

        BigDecimal targetMoney = SingleEconomyManager.getBankBalance(p2);
        if (targetMoney.doubleValue() >= maxBankCapacity.doubleValue()) {
            MessageManager.send(p1, "Bank-Full", placeValues(p2, BigDecimal.valueOf(0)));
            return;
        }

        BigDecimal moneyLeft = maxBankCapacity.subtract(targetMoney);
        if (amount.doubleValue() >= moneyLeft.doubleValue()) {
            SingleEconomyManager.removeBankBalance(p1, moneyLeft);
            MessageManager.send(p1, "Payment-Sent", BPMethods.placeValues(p2, moneyLeft));
            SingleEconomyManager.addBankBalance(p2, moneyLeft);
            MessageManager.send(p2, "Payment-Received", BPMethods.placeValues(p1, moneyLeft));
            return;
        }

        SingleEconomyManager.removeBankBalance(p1, amount);
        MessageManager.send(p1, "Payment-Sent", BPMethods.placeValues(p2, amount));
        SingleEconomyManager.addBankBalance(p2, amount);
        MessageManager.send(p2, "Payment-Received", BPMethods.placeValues(p1, amount));
    }

    public static boolean isLegacyServer() {
        String v = BankPlus.getInstance().getServerVersion();
        return v.contains("1.7") || v.contains("1.8");
    }
}