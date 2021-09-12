package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ConfigValues {

    private static String minute;
    private static String minutes;
    private static String hour;
    private static String hours;
    private static String day;
    private static String days;

    private static String k;
    private static String m;
    private static String b;
    private static String t;
    private static String q;

    private static String guiFillerMaterial;
    private static String guiFillerDisplayname;
    private static boolean isGuiFillerGlowing;

    private static String withdrawSound;
    private static String depositSound;
    private static String viewSound;
    private static String personalSound;

    private static String notifyOfflineInterestMessage;
    private static long interestDelay;
    private static long maxWithdrawAmount;
    private static long maxDepositAmount;
    private static long maxBankCapacity;
    private static long startAmount;
    private static long notifyOfflineInterestDelay;
    private static List<String> worldsBlacklist;
    private static boolean isGuiEnabled;
    private static boolean isReopeningBankAfterChat;
    private static boolean isInterestEnabled;
    private static boolean isNotifyOfflineInterest;

    private static boolean isWithdrawSoundEnabled;
    private static boolean isDepositSoundEnabled;
    private static boolean isViewSoundEnabled;
    private static boolean isPersonalSoundEnabled;

    public static void setupValues() {
        final BankPlus plugin = JavaPlugin.getPlugin(BankPlus.class);

        minute = plugin.config().getString("Placeholders.Time.Minute");
        minutes = plugin.config().getString("Placeholders.Time.Minutes");
        hour = plugin.config().getString("Placeholders.Time.Hour");
        hours = plugin.config().getString("Placeholders.Time.Hours");
        day = plugin.config().getString("Placeholders.Time.Day");
        days = plugin.config().getString("Placeholders.Time.Days");

        k = plugin.config().getString("Placeholders.Money.Thousands");
        m = plugin.config().getString("Placeholders.Money.Millions");
        b = plugin.config().getString("Placeholders.Money.Billions");
        t = plugin.config().getString("Placeholders.Money.Trillions");
        q = plugin.config().getString("Placeholders.Money.Quadrillions");

        guiFillerMaterial = plugin.config().getString("Gui.Filler.Material");
        guiFillerDisplayname = plugin.config().getString("Gui.Filler.DisplayName");
        isGuiFillerGlowing = plugin.config().getBoolean("Gui.Filler.Glowing");

        withdrawSound = plugin.config().getString("General.Withdraw-Sound.Sound");
        depositSound = plugin.config().getString("General.Deposit-Sound.Sound");
        viewSound = plugin.config().getString("General.View-Sound.Sound");
        personalSound = plugin.config().getString("General.Personal-Sound.Sound");

        notifyOfflineInterestMessage = plugin.config().getString("General.Offline-Interest-Earned-Message.Message");
        interestDelay = plugin.config().getLong("Interest.Delay");
        maxWithdrawAmount = plugin.config().getLong("General.Max-Withdrawn-Amount");
        maxDepositAmount = plugin.config().getLong("General.Max-Deposit-Amount");
        maxBankCapacity = plugin.config().getLong("General.Max-Bank-Capacity");
        startAmount = plugin.config().getLong("General.Join-Start-Amount");
        notifyOfflineInterestDelay = plugin.config().getLong("General.Offline-Interest-Earned-Message.Delay");
        worldsBlacklist = plugin.config().getStringList("General.Worlds-Blacklist");
        isGuiEnabled = plugin.config().getBoolean("Gui.Enabled");
        isReopeningBankAfterChat = plugin.config().getBoolean("General.Reopen-Bank-After-Chat");
        isInterestEnabled = plugin.config().getBoolean("Interest.Enabled");
        isNotifyOfflineInterest = plugin.config().getBoolean("General.Offline-Interest-Earned-Message.Enabled");

        isWithdrawSoundEnabled = plugin.config().getBoolean("General.Withdraw-Sound.Enabled");
        isDepositSoundEnabled = plugin.config().getBoolean("General.Deposit-Sound.Enabled");
        isViewSoundEnabled = plugin.config().getBoolean("General.View-Sound.Enabled");
        isPersonalSoundEnabled = plugin.config().getBoolean("General.Personal-Sound.Enabled");
    }

    // Interest Placeholders
    public static String getMinute() {
        return minute;
    }
    public static String getMinutes() {
        return minutes;
    }
    public static String getHour() {
        return hour;
    }
    public static String getHours() {
        return hours;
    }
    public static String getDay() {
        return day;
    }
    public static String getDays() {
        return days;
    }

    // Money Placeholders
    public static String getK() {
        return k;
    }
    public static String getM() {
        return m;
    }
    public static String getB() {
        return b;
    }
    public static String getT() {
        return t;
    }
    public static String getQ() {
        return q;
    }

    // Gui Filler Values
    public static String getGuiFillerMaterial() {
        return guiFillerMaterial;
    }
    public static String getGuiFillerDisplayname() {
        return guiFillerDisplayname;
    }
    public static boolean isGuiFillerGlowing() {
        return isGuiFillerGlowing;
    }

    // Config Sounds
    public static String getWithdrawSound() {
        return withdrawSound;
    }
    public static String getDepositSound() {
        return depositSound;
    }
    public static String getViewSound() {
        return viewSound;
    }
    public static String getPersonalSound() {
        return personalSound;
    }

    // Config Values
    public static String getNotifyOfflineInterestMessage() {
        return notifyOfflineInterestMessage;
    }
    public static long getInterestDelay() {
        return interestDelay;
    }
    public static long getMaxWithdrawAmount() {
        return maxWithdrawAmount;
    }
    public static long getMaxDepositAmount() {
        return maxDepositAmount;
    }
    public static long getMaxBankCapacity() {
        return maxBankCapacity;
    }
    public static long getStartAmount() {
        return startAmount;
    }
    public static long getNotifyOfflineInterestDelay() {
        return notifyOfflineInterestDelay;
    }
    public static List<String> getWorldsBlacklist() {
        return worldsBlacklist;
    }
    public static boolean isGuiEnabled() {
        return isGuiEnabled;
    }
    public static boolean isReopeningBankAfterChat() {
        return isReopeningBankAfterChat;
    }
    public static boolean isInterestEnabled() {
        return isInterestEnabled;
    }
    public static boolean isNotifyOfflineInterest() {
        return isNotifyOfflineInterest;
    }

    // Config Booleans Sounds
    public static boolean isWithdrawSoundEnabled() {
        return isWithdrawSoundEnabled;
    }
    public static boolean isDepositSoundEnabled() {
        return isDepositSoundEnabled;
    }
    public static boolean isViewSoundEnabled() {
        return isViewSoundEnabled;
    }
    public static boolean isPersonalSoundEnabled() {
        return isPersonalSoundEnabled;
    }
}