package me.pulsi_.bankplus.values.configs;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.ConfigManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMethods;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.List;

public class ConfigValues {

    private List<String> worldsBlacklist, exitCommands, bankTopFormat;
    private String exitMessage, playerChatPriority, bankClickPriority;
    private String second, seconds, minute, minutes, hour, hours, day, days;
    private String interestTimeOnlySeconds, interestTimeOnlyMinutes, interestTimeOnlyHours, interestTimeOnlyDays, interestTimeSecondsMinutes, interestTimeMinutesHours;
    private String interestTimeSecondsHours, interestTimeSecondsMinutesHours, interestTimeHoursDays, interestTimeMinutesDays, interestTimeSecondsDays;
    private String interestTimeMinutesHoursDays, interestTimeSecondsHoursDays, interestTimeSecondsMinutesHoursDays, interestTimeSecondsMinutesDays;
    private String k, m, b, t, q, qq;
    private String withdrawSound, depositSound, viewSound, personalSound;
    private String notifyOfflineInterestMessage, interestDelay, interestOfflinePermission;
    private String maxDepositAmount, maxWithdrawAmount, depositTaxes, withdrawTaxes, minimumAmount, maxBankCapacity, startAmount;
    private String bankTopMoneyFormat, banktopUpdateBroadcastMessage, interestMaxAmount, interestMoneyGiven, bankUpgradedMax, mainGuiName;
    private long notifyOfflineInterestDelay, saveBalancedDelay, updateBankTopDelay;
    private int afkPlayersTime, maxDecimalsAmount, bankTopSize;
    private boolean isReopeningBankAfterChat, isInterestEnabled, isNotifyOfflineInterest, isStoringUUIDs, isGivingInterestToOfflinePlayers;
    private boolean isOfflineInterestEarnedMessageEnabled, isUpdateCheckerEnabled, isWithdrawSoundEnabled, isDepositSoundEnabled;
    private boolean isViewSoundEnabled, isPersonalSoundEnabled, isIgnoringAfkPlayers, useEssentialsXAFK;
    private boolean banktopEnabled, banktopUpdateBroadcastEnabled, banktopUpdateBroadcastOnlyConsole, saveBalancesBroadcast, guiModuleEnabled;

    public static ConfigValues getInstance() {
        return new ConfigValues();
    }

    public void setupValues() {
        FileConfiguration config = BankPlus.INSTANCE.getConfigManager().getConfig(ConfigManager.Type.CONFIG);

        exitMessage = config.getString("General.Chat-Exit-Message");
        playerChatPriority = config.getString("General.Event-Priorities.PlayerChat");
        bankClickPriority = config.getString("General.Event-Priorities.BankClick");
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
        interestOfflinePermission = config.getString("Interest.Offline-Permission");
        maxDepositAmount = config.getString("General.Max-Deposit-Amount");
        maxWithdrawAmount = config.getString("General.Max-Withdrawn-Amount");
        depositTaxes = config.getString("General.Deposit-Taxes");
        withdrawTaxes = config.getString("General.Withdraw-Taxes");
        minimumAmount = config.getString("General.Minimum-Amount");
        maxBankCapacity = config.getString("General.Max-Bank-Capacity");
        startAmount = config.getString("General.Join-Start-Amount");
        notifyOfflineInterestDelay = config.getLong("General.Offline-Interest-Earned-Message.Delay");
        interestMaxAmount = config.getString("Interest.Max-Amount");
        interestMoneyGiven = config.getString("Interest.Money-Given");
        bankUpgradedMax = config.getString("Placeholders.Upgrades.Max-Level");
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
        useEssentialsXAFK = config.getBoolean("Interest.AFK-Settings.Use-EssentialsX-AFK");
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
        guiModuleEnabled = config.getBoolean("General.Enable-Guis");
        mainGuiName = config.getString("General.Main-Gui");
    }

    public String getPlayerChatPriority() {
        return playerChatPriority;
    }

    public String getBankClickPriority() {
        return bankClickPriority;
    }

    public String getExitMessage() {
        return exitMessage == null ? "exit" : exitMessage;
    }

    public String getSecond() {
        return second == null ? "Second" : second;
    }

    public String getSeconds() {
        return seconds == null ? "Seconds" : seconds;
    }

    public String getMinute() {
        return minute == null ? "Minute" : minute;
    }

    public String getMinutes() {
        return minutes == null ? "Minutes" : minutes;
    }

    public String getHour() {
        return hour == null ? "Hour" : hour;
    }

    public String getHours() {
        return hours == null ? "Hours" : hours;
    }

    public String getDay() {
        return day == null ? "Day" : day;
    }

    public String getDays() {
        return days == null ? "Days" : days;
    }

    public String getInterestTimeOnlySeconds() {
        return interestTimeOnlySeconds;
    }

    public String getInterestTimeOnlyMinutes() {
        return interestTimeOnlyMinutes;
    }

    public String getInterestTimeOnlyHours() {
        return interestTimeOnlyHours;
    }

    public String getInterestTimeOnlyDays() {
        return interestTimeOnlyDays;
    }

    public String getInterestTimeSecondsMinutes() {
        return interestTimeSecondsMinutes;
    }

    public String getInterestTimeMinutesHours() {
        return interestTimeMinutesHours;
    }

    public String getInterestTimeSecondsHours() {
        return interestTimeSecondsHours;
    }

    public String getInterestTimeSecondsMinutesHours() {
        return interestTimeSecondsMinutesHours;
    }

    public String getInterestTimeHoursDays() {
        return interestTimeHoursDays;
    }

    public String getInterestTimeMinutesDays() {
        return interestTimeMinutesDays;
    }

    public String getInterestTimeSecondsDays() {
        return interestTimeSecondsDays;
    }

    public String getInterestTimeMinutesHoursDays() {
        return interestTimeMinutesHoursDays;
    }

    public String getInterestTimeSecondsHoursDays() {
        return interestTimeSecondsHoursDays;
    }

    public String getInterestTimeSecondsMinutesHoursDays() {
        return interestTimeSecondsMinutesHoursDays;
    }

    public String getInterestTimeSecondsMinutesDays() {
        return interestTimeSecondsMinutesDays;
    }

    public String getK() {
        return k == null ? "K" : k;
    }

    public String getM() {
        return m == null ? "M" : m;
    }

    public String getB() {
        return b == null ? "B" : b;
    }

    public String getT() {
        return t == null ? "T" : t;
    }

    public String getQ() {
        return q == null ? "Q" : q;
    }

    public String getQq() {
        return qq == null ? "QQ" : qq;
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
        if (!interestDelay.contains(" ")) return BPMethods.minutesInMilliseconds(Integer.parseInt(interestDelay));

        int delay;
        try {
            delay = Integer.parseInt(interestDelay.split(" ")[0]);
        } catch (NumberFormatException e) {
            return BPMethods.minutesInMilliseconds(5);
        }

        String delayType = interestDelay.split(" ")[1];
        switch (delayType) {
            case "s":
                return BPMethods.secondsInMilliseconds(delay);
            default:
                return BPMethods.minutesInMilliseconds(delay);
            case "h":
                return BPMethods.hoursInMilliseconds(delay);
            case "d":
                return BPMethods.daysInMilliseconds(delay);
        }
    }

    public String getInterestOfflinePermission() {
        return interestOfflinePermission;
    }

    public BigDecimal getMaxDepositAmount() {
        if (BPMethods.isInvalidNumber(maxDepositAmount)) {
            BPLogger.error("Invalid number for the \"MaxDepositAmount\", Please correct it in the config as soon as possible!");
            return new BigDecimal(0);
        }
        return new BigDecimal(maxDepositAmount);
    }

    public BigDecimal getMaxWithdrawAmount() {
        if (BPMethods.isInvalidNumber(maxWithdrawAmount)) {
            BPLogger.error("Invalid number for the \"MaxWithdrawAmount\", Please correct it in the config as soon as possible!");
            return new BigDecimal(0);
        }
        return new BigDecimal(maxWithdrawAmount);
    }

    public String getDepositTaxesString() {
        return depositTaxes;
    }

    public BigDecimal getDepositTaxes() {
        if (BPMethods.isInvalidNumber(depositTaxes)) {
            BPLogger.error("Invalid number for the \"DepositTaxes\", Please correct it in the config as soon as possible!");
            return new BigDecimal(0);
        }
        return new BigDecimal(depositTaxes.replace("%", ""));
    }

    public String getWithdrawTaxesString() {
        return withdrawTaxes;
    }

    public BigDecimal getWithdrawTaxes() {
        if (BPMethods.isInvalidNumber(withdrawTaxes)) {
            BPLogger.error("Invalid number for the \"WithdrawTaxes\", Please correct it in the config as soon as possible!");
            return new BigDecimal(0);
        }
        return new BigDecimal(withdrawTaxes.replace("%", ""));
    }

    public BigDecimal getMinimumAmount() {
        if (BPMethods.isInvalidNumber(minimumAmount)) {
            BPLogger.error("Invalid number for the \"MinimumAmount\", Please correct it in the config as soon as possible!");
            return new BigDecimal(0);
        }
        return new BigDecimal(minimumAmount);
    }

    public BigDecimal getMaxBankCapacity() {
        if (BPMethods.isInvalidNumber(maxBankCapacity)) {
            BPLogger.error("Invalid number for the \"MaxBankCapacity\", Please correct it in the config as soon as possible!");
            return new BigDecimal(0);
        }
        return new BigDecimal(maxBankCapacity);
    }

    public BigDecimal getStartAmount() {
        if (BPMethods.isInvalidNumber(startAmount)) {
            BPLogger.error("Invalid number for the \"StartAmount\", Please correct it in the config as soon as possible!");
            return new BigDecimal(0);
        }
        return new BigDecimal(startAmount);
    }

    public long getNotifyOfflineInterestDelay() {
        return notifyOfflineInterestDelay;
    }

    public BigDecimal getInterestMaxAmount() {
        if (BPMethods.isInvalidNumber(interestMaxAmount)) {
            BPLogger.error("Invalid number for the \"InterestMaxAmount\", Please correct it in the config as soon as possible!");
            return new BigDecimal(0);
        }
        return new BigDecimal(interestMaxAmount);
    }

    public String getInterestMoneyGivenString() {
        return interestMoneyGiven;
    }

    public BigDecimal getInterestMoneyGiven() {
        if (BPMethods.isInvalidNumber(interestMoneyGiven)) {
            BPLogger.error("Invalid number for the \"InterestMoneyGiven\", Please correct it in the config as soon as possible!");
            return new BigDecimal(0);
        }
        return new BigDecimal(interestMoneyGiven.replace("%", ""));
    }

    public String getBankUpgradedMax() {
        return bankUpgradedMax == null ? "&cMaxed" : bankUpgradedMax;
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

    public boolean isUseEssentialsXAFK() {
        return BankPlus.INSTANCE.isEssentialsXHooked() && useEssentialsXAFK;
    }

    public int getAfkPlayersTime() {
        return afkPlayersTime;
    }

    public int getMaxDecimalsAmount() {
        return Math.max(maxDecimalsAmount, 1);
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

    public boolean isGuiModuleEnabled() {
        return guiModuleEnabled;
    }

    public String getMainGuiName() {
        return mainGuiName == null ? "bank" : mainGuiName;
    }
}