package me.pulsi_.bankplus.values.configs;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.utils.Methods;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.List;

public class ConfigValues {

    private String exitMessage;
    private String second;
    private String seconds;
    private String minute;
    private String minutes;
    private String hour;
    private String hours;
    private String day;
    private String days;
    private String interestTimeOnlySeconds;
    private String interestTimeOnlyMinutes;
    private String interestTimeOnlyHours;
    private String interestTimeOnlyDays;
    private String interestTimeSecondsMinutes;
    private String interestTimeMinutesHours;
    private String interestTimeSecondsHours;
    private String interestTimeSecondsMinutesHours;
    private String interestTimeHoursDays;
    private String interestTimeMinutesDays;
    private String interestTimeSecondsDays;
    private String interestTimeMinutesHoursDays;
    private String interestTimeSecondsHoursDays;
    private String interestTimeSecondsMinutesHoursDays;
    private String interestTimeSecondsMinutesDays;
    private String k;
    private String m;
    private String b;
    private String t;
    private String q;
    private String qq;
    private String withdrawSound;
    private String depositSound;
    private String viewSound;
    private String personalSound;
    private String notifyOfflineInterestMessage;
    private String interestDelay;
    private BigDecimal maxWithdrawAmount;
    private BigDecimal maxDepositAmount;
    private BigDecimal minimumAmount;
    private BigDecimal maxBankCapacity;
    private BigDecimal startAmount;
    private long notifyOfflineInterestDelay;
    private BigDecimal interestMaxAmount;
    private double interestMoneyGiven;
    private List<String> worldsBlacklist;
    private List<String> exitCommands;
    private boolean isReopeningBankAfterChat;
    private boolean isInterestEnabled;
    private boolean isNotifyOfflineInterest;
    private boolean isStoringUUIDs;
    private boolean isGivingInterestToOfflinePlayers;
    private boolean isOfflineInterestEarnedMessageEnabled;
    private boolean isUpdateCheckerEnabled;
    private boolean isWithdrawSoundEnabled;
    private boolean isDepositSoundEnabled;
    private boolean isViewSoundEnabled;
    private boolean isPersonalSoundEnabled;
    private boolean isIgnoringAfkPlayers;
    private int afkPlayersTime;
    private int maxDecimalsAmount;
    private long saveBalancedDelay;
    private boolean banktopEnabled;
    private long updateBankTopDelay;
    private int bankTopSize;
    private String bankTopMoneyFormat;
    private List<String> bankTopFormat;
    private boolean banktopUpdateBroadcastEnabled;
    private boolean banktopUpdateBroadcastOnlyConsole;
    private String banktopUpdateBroadcastMessage;
    private boolean saveBalancesBroadcast;

    public static ConfigValues getInstance() {
        return new ConfigValues();
    }

    public void setupValues() {
        FileConfiguration config = BankPlus.getCm().getConfig("config");

        exitMessage = config.getString("General.Chat-Exit-Message");
        second = config.getString("Placeholders.Time.Second");
        seconds = config.getString("Placeholders.Time.Seconds");
        minute = config.getString("Placeholders.Time.Minute");
        minutes = config.getString("Placeholders.Time.Minutes");
        hour = config.getString("Placeholders.Time.Hour");
        hours = config.getString("Placeholders.Time.Hours");
        day = config.getString("Placeholders.Time.Day");
        days = config.getString("Placeholders.Time.Days");
        interestTimeOnlySeconds = config.getString("Placeholders.Time.Interest-Time.Only-Seconds");
        interestTimeOnlyMinutes = config.getString("Placeholders.Time.Interest-Time.Only-Minutes");
        interestTimeOnlyHours = config.getString("Placeholders.Time.Interest-Time.Only-Hours");
        interestTimeOnlyDays = config.getString("Placeholders.Time.Interest-Time.Only-Days");
        interestTimeSecondsMinutes = config.getString("Placeholders.Time.Interest-Time.Seconds-Minutes");
        interestTimeMinutesHours = config.getString("Placeholders.Time.Interest-Time.Minutes-Hours");
        interestTimeSecondsHours = config.getString("Placeholders.Time.Interest-Time.Seconds-Hours");
        interestTimeSecondsMinutesHours = config.getString("Placeholders.Time.Interest-Time.Seconds-Minutes-Hours");
        interestTimeHoursDays = config.getString("Placeholders.Time.Interest-Time.Hours-Days");
        interestTimeMinutesDays = config.getString("Placeholders.Time.Interest-Time.Minutes-Days");
        interestTimeSecondsDays = config.getString("Placeholders.Time.Interest-Time.Seconds-Days");
        interestTimeMinutesHoursDays = config.getString("Placeholders.Time.Interest-Time.Minutes-Hours-Days");
        interestTimeSecondsHoursDays = config.getString("Placeholders.Time.Interest-Time.Seconds-Hours-Days");
        interestTimeSecondsMinutesDays = config.getString("Placeholders.Time.Interest-Time.Seconds-Minutes-Days");
        interestTimeSecondsMinutesHoursDays = config.getString("Placeholders.Time.Interest-Time.Seconds-Minutes-Hours-Days");
        k = config.getString("Placeholders.Money.Thousands");
        m = config.getString("Placeholders.Money.Millions");
        b = config.getString("Placeholders.Money.Billions");
        t = config.getString("Placeholders.Money.Trillions");
        q = config.getString("Placeholders.Money.Quadrillions");
        qq = config.getString("Placeholders.Money.Quintillions");
        withdrawSound = config.getString("General.Withdraw-Sound.Sound");
        depositSound = config.getString("General.Deposit-Sound.Sound");
        viewSound = config.getString("General.View-Sound.Sound");
        personalSound = config.getString("General.Personal-Sound.Sound");
        notifyOfflineInterestMessage = config.getString("General.Offline-Interest-Earned-Message.Message");
        interestDelay = config.getString("Interest.Delay");
        maxWithdrawAmount = BigDecimal.valueOf(config.getDouble("General.Max-Withdrawn-Amount"));
        maxDepositAmount = BigDecimal.valueOf(config.getDouble("General.Max-Deposit-Amount"));
        minimumAmount = BigDecimal.valueOf(config.getDouble("General.Minimum-Amount"));
        maxBankCapacity = BigDecimal.valueOf(config.getDouble("General.Max-Bank-Capacity"));
        startAmount = BigDecimal.valueOf(config.getDouble("General.Join-Start-Amount"));
        notifyOfflineInterestDelay = config.getLong("General.Offline-Interest-Earned-Message.Delay");
        interestMaxAmount = BigDecimal.valueOf(config.getDouble("Interest.Max-Amount"));
        interestMoneyGiven = config.getDouble("Interest.Money-Given");
        worldsBlacklist = config.getStringList("General.Worlds-Blacklist");
        exitCommands = config.getStringList("General.Chat-Exit-Commands");
        isReopeningBankAfterChat = config.getBoolean("General.Reopen-Bank-After-Chat");
        isInterestEnabled = config.getBoolean("Interest.Enabled");
        isNotifyOfflineInterest = config.getBoolean("General.Offline-Interest-Earned-Message.Enabled");
        isStoringUUIDs = config.getBoolean("General.Use-UUIDs");
        isGivingInterestToOfflinePlayers = config.getBoolean("Interest.Give-To-Offline-Players");
        isOfflineInterestEarnedMessageEnabled = config.getBoolean("General.Offline-Interest-Earned-Message.Enabled");
        isUpdateCheckerEnabled = config.getBoolean("Update-Checker");
        isWithdrawSoundEnabled = config.getBoolean("General.Withdraw-Sound.Enabled");
        isDepositSoundEnabled = config.getBoolean("General.Deposit-Sound.Enabled");
        isViewSoundEnabled = config.getBoolean("General.View-Sound.Enabled");
        isPersonalSoundEnabled = config.getBoolean("General.Personal-Sound.Enabled");
        isIgnoringAfkPlayers = config.getBoolean("Interest.AFK-Settings.Ignore-AFK-Players");
        afkPlayersTime = config.getInt("Interest.AFK-Settings.AFK-Time");
        maxDecimalsAmount = config.getInt("General.Max-Decimals-Amount");
        saveBalancedDelay = config.getLong("General.Save-Delay");
        banktopEnabled = config.getBoolean("BankTop.Enabled");
        updateBankTopDelay = config.getLong("BankTop.Update-Delay");
        bankTopSize = config.getInt("BankTop.Size");
        bankTopMoneyFormat = config.getString("BankTop.Money-Format");
        bankTopFormat = config.getStringList("BankTop.Format");
        banktopUpdateBroadcastEnabled = config.getBoolean("BankTop.Update-Broadcast.Enabled");
        banktopUpdateBroadcastOnlyConsole = config.getBoolean("BankTop.Update-Broadcast.Only-Console");
        banktopUpdateBroadcastMessage = config.getString("BankTop.Update-Broadcast.Message");
        saveBalancesBroadcast = config.getBoolean("General.Save-Broadcast");
    }

    public String getExitMessage() {
        if (exitMessage == null) return "exit";
        return exitMessage;
    }

    public String getSecond() {
        return second;
    }

    public String getSeconds() {
        return seconds;
    }

    public String getMinute() {
        return minute;
    }

    public String getMinutes() {
        return minutes;
    }

    public String getHour() {
        return hour;
    }

    public String getHours() {
        return hours;
    }

    public String getDay() {
        return day;
    }

    public String getDays() {
        return days;
    }

    public String getInterestTimeOnlySeconds() {
        if (interestTimeOnlySeconds == null) return "";
        return interestTimeOnlySeconds;
    }

    public String getInterestTimeOnlyMinutes() {
        if (interestTimeOnlyMinutes == null) return "";
        return interestTimeOnlyMinutes;
    }

    public String getInterestTimeOnlyHours() {
        if (interestTimeOnlyHours == null) return "";
        return interestTimeOnlyHours;
    }

    public String getInterestTimeOnlyDays() {
        if (interestTimeOnlyDays == null) return "";
        return interestTimeOnlyDays;
    }

    public String getInterestTimeSecondsMinutes() {
        if (interestTimeSecondsMinutes == null) return "";
        return interestTimeSecondsMinutes;
    }

    public String getInterestTimeMinutesHours() {
        if (interestTimeMinutesHours == null) return "";
        return interestTimeMinutesHours;
    }

    public String getInterestTimeSecondsHours() {
        if (interestTimeSecondsHours == null) return "";
        return interestTimeSecondsHours;
    }

    public String getInterestTimeSecondsMinutesHours() {
        if (interestTimeSecondsMinutesHours == null) return "";
        return interestTimeSecondsMinutesHours;
    }

    public String getInterestTimeHoursDays() {
        if (interestTimeHoursDays == null) return "";
        return interestTimeHoursDays;
    }

    public String getInterestTimeMinutesDays() {
        if (interestTimeMinutesDays == null) return "";
        return interestTimeMinutesDays;
    }

    public String getInterestTimeSecondsDays() {
        if (interestTimeSecondsDays == null) return "";
        return interestTimeSecondsDays;
    }


    public String getInterestTimeMinutesHoursDays() {
        if (interestTimeMinutesHoursDays == null) return "";
        return interestTimeMinutesHoursDays;
    }

    public String getInterestTimeSecondsHoursDays() {
        if (interestTimeSecondsHoursDays == null) return "";
        return interestTimeSecondsHoursDays;
    }

    public String getInterestTimeSecondsMinutesHoursDays() {
        if (interestTimeSecondsMinutesHoursDays == null) return "";
        return interestTimeSecondsMinutesHoursDays;
    }

    public String getInterestTimeSecondsMinutesDays() {
        if (interestTimeSecondsMinutesDays == null) return "";
        return interestTimeSecondsMinutesDays;
    }

    public String getK() {
        if (k == null) return "K";
        return k;
    }

    public String getM() {
        if (m == null) return "M";
        return m;
    }

    public String getB() {
        if (b == null) return "B";
        return b;
    }

    public String getT() {
        if (t == null) return "T";
        return t;
    }

    public String getQ() {
        if (q == null) return "Q";
        return q;
    }

    public String getQq() {
        if (qq == null) return "QQ";
        return qq;
    }

    public String getWithdrawSound() {
        return withdrawSound;
    }

    public String getDepositSound() {
        return depositSound;
    }

    public String getViewSound() {
        return viewSound;
    }

    public String getPersonalSound() {
        return personalSound;
    }

    public String getNotifyOfflineInterestMessage() {
        return notifyOfflineInterestMessage;
    }

    public long getInterestDelay() {
        if (!interestDelay.contains(" ")) return Methods.minutesInMilliseconds(Integer.parseInt(interestDelay));

        int delay;
        try {
            delay = Integer.parseInt(interestDelay.split(" ")[0]);
        } catch (NumberFormatException e) {
            return Methods.minutesInMilliseconds(5);
        }

        String delayType = interestDelay.split(" ")[1];
        switch (delayType) {
            case "s":
                return Methods.secondsInMilliseconds(delay);
            default:
                return Methods.minutesInMilliseconds(delay);
            case "h":
                return Methods.hoursInMilliseconds(delay);
            case "d":
                return Methods.daysInMilliseconds(delay);
        }
    }

    public BigDecimal getMaxWithdrawAmount() {
        return maxWithdrawAmount;
    }

    public BigDecimal getMaxDepositAmount() {
        return maxDepositAmount;
    }

    public BigDecimal getMinimumAmount() {
        return minimumAmount;
    }

    public BigDecimal getMaxBankCapacity() {
        return maxBankCapacity;
    }

    public BigDecimal getStartAmount() {
        return startAmount;
    }

    public long getNotifyOfflineInterestDelay() {
        return notifyOfflineInterestDelay;
    }

    public BigDecimal getInterestMaxAmount() {
        return interestMaxAmount;
    }

    public double getInterestMoneyGiven() {
        return interestMoneyGiven;
    }

    public List<String> getWorldsBlacklist() {
        return worldsBlacklist;
    }

    public List<String> getExitCommands() {
        return exitCommands;
    }

    public boolean isReopeningBankAfterChat() {
        return isReopeningBankAfterChat;
    }

    public boolean isInterestEnabled() {
        Interest.isInterestActive = isInterestEnabled;
        return isInterestEnabled;
    }

    public boolean isNotifyOfflineInterest() {
        return isNotifyOfflineInterest;
    }

    public boolean isStoringUUIDs() {
        return isStoringUUIDs;
    }

    public boolean isGivingInterestToOfflinePlayers() {
        return isGivingInterestToOfflinePlayers;
    }

    public boolean isOfflineInterestEarnedMessageEnabled() {
        return isOfflineInterestEarnedMessageEnabled;
    }

    public boolean isUpdateCheckerEnabled() {
        return isUpdateCheckerEnabled;
    }

    public boolean isWithdrawSoundEnabled() {
        return isWithdrawSoundEnabled;
    }

    public boolean isDepositSoundEnabled() {
        return isDepositSoundEnabled;
    }

    public boolean isViewSoundEnabled() {
        return isViewSoundEnabled;
    }

    public boolean isPersonalSoundEnabled() {
        return isPersonalSoundEnabled;
    }

    public boolean isIgnoringAfkPlayers() {
        return isIgnoringAfkPlayers;
    }

    public int getAfkPlayersTime() {
        return afkPlayersTime;
    }

    public int getMaxDecimalsAmount() {
        return maxDecimalsAmount;
    }

    public int getBankTopSize() {
        return bankTopSize;
    }

    public List<String> getBankTopFormat() {
        return bankTopFormat;
    }

    public long getSaveBalancedDelay() {
        return saveBalancedDelay;
    }

    public long getUpdateBankTopDelay() {
        return updateBankTopDelay;
    }

    public String getBankTopMoneyFormat() {
        return bankTopMoneyFormat;
    }

    public boolean isBanktopEnabled() {
        return banktopEnabled;
    }

    public String getBanktopUpdateBroadcastMessage() {
        return banktopUpdateBroadcastMessage;
    }

    public boolean isBanktopUpdateBroadcastEnabled() {
        return banktopUpdateBroadcastEnabled;
    }

    public boolean isBanktopUpdateBroadcastOnlyConsole() {
        return banktopUpdateBroadcastOnlyConsole;
    }

    public boolean isSaveBalancesBroadcast() {
        return saveBalancesBroadcast;
    }
}