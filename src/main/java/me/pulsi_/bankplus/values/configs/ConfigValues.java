package me.pulsi_.bankplus.values.configs;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigValues {

    private String minute;
    private String minutes;
    private String hour;
    private String hours;
    private String day;
    private String days;
    private String k;
    private String m;
    private String b;
    private String t;
    private String q;
    private String guiFillerMaterial;
    private String guiFillerDisplayname;
    private boolean isGuiFillerGlowing;
    private boolean isGuiFillerEnabled;
    private String withdrawSound;
    private String depositSound;
    private String viewSound;
    private String personalSound;
    private String guiTitle;
    private int guiUpdateDelay;
    private int guiLines;
    private ConfigurationSection guiItems;
    private String notifyOfflineInterestMessage;
    private int interestDelay;
    private long maxWithdrawAmount;
    private long maxDepositAmount;
    private long minimumAmount;
    private long maxBankCapacity;
    private long startAmount;
    private long notifyOfflineInterestDelay;
    private long interestMaxAmount;
    private double interestMoneyGiven;
    private List<String> worldsBlacklist;
    private List<String> exitCommands;
    private boolean isGuiEnabled;
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

    public static ConfigValues getInstance() {
        return new ConfigValues();
    }

    public void setupValues() {
        FileConfiguration config = BankPlus.getCm().getConfig("config");

        minute = config.getString("Placeholders.Time.Minute");
        minutes = config.getString("Placeholders.Time.Minutes");
        hour = config.getString("Placeholders.Time.Hour");
        hours = config.getString("Placeholders.Time.Hours");
        day = config.getString("Placeholders.Time.Day");
        days = config.getString("Placeholders.Time.Days");
        k = config.getString("Placeholders.Money.Thousands");
        m = config.getString("Placeholders.Money.Millions");
        b = config.getString("Placeholders.Money.Billions");
        t = config.getString("Placeholders.Money.Trillions");
        q = config.getString("Placeholders.Money.Quadrillions");
        guiFillerMaterial = config.getString("Gui.Filler.Material");
        guiFillerDisplayname = config.getString("Gui.Filler.DisplayName");
        isGuiFillerGlowing = config.getBoolean("Gui.Filler.Glowing");
        isGuiFillerEnabled = config.getBoolean("Gui.Filler.Enabled");
        withdrawSound = config.getString("General.Withdraw-Sound.Sound");
        depositSound = config.getString("General.Deposit-Sound.Sound");
        viewSound = config.getString("General.View-Sound.Sound");
        personalSound = config.getString("General.Personal-Sound.Sound");
        guiTitle = config.getString("Gui.Title");
        guiUpdateDelay = config.getInt("Gui.Update-Delay");
        guiLines = config.getInt("Gui.Lines");
        guiItems = config.getConfigurationSection("Gui.Items");
        notifyOfflineInterestMessage = config.getString("General.Offline-Interest-Earned-Message.Message");
        interestDelay = config.getInt("Interest.Delay");
        maxWithdrawAmount = config.getLong("General.Max-Withdrawn-Amount");
        maxDepositAmount = config.getLong("General.Max-Deposit-Amount");
        minimumAmount = config.getLong("General.Minimum-Amount");
        maxBankCapacity = config.getLong("General.Max-Bank-Capacity");
        startAmount = config.getLong("General.Join-Start-Amount");
        notifyOfflineInterestDelay = config.getLong("General.Offline-Interest-Earned-Message.Delay");
        interestMaxAmount = config.getLong("Interest.Max-Amount");
        interestMoneyGiven = config.getDouble("Interest.Money-Given");
        worldsBlacklist = config.getStringList("General.Worlds-Blacklist");
        exitCommands = config.getStringList("General.Chat-Exit-Commands");
        isGuiEnabled = config.getBoolean("Gui.Enabled");
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

    public String getK() {
        return k;
    }

    public String getM() {
        return m;
    }

    public String getB() {
        return b;
    }

    public String getT() {
        return t;
    }

    public String getQ() {
        return q;
    }

    public String getGuiFillerMaterial() {
        return guiFillerMaterial;
    }

    public String getGuiFillerDisplayname() {
        return guiFillerDisplayname;
    }

    public boolean isGuiFillerGlowing() {
        return isGuiFillerGlowing;
    }

    public boolean isGuiFillerEnabled() {
        return isGuiFillerEnabled;
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

    public String getGuiTitle() {
        return guiTitle;
    }

    public int getGuiUpdateDelay() {
        return guiUpdateDelay;
    }

    public int getGuiLines() {
        return guiLines;
    }

    public ConfigurationSection getGuiItems() {
        return guiItems;
    }

    public String getNotifyOfflineInterestMessage() {
        return notifyOfflineInterestMessage;
    }

    public int getInterestDelay() {
        return interestDelay;
    }

    public long getMaxWithdrawAmount() {
        return maxWithdrawAmount;
    }

    public long getMaxDepositAmount() {
        return maxDepositAmount;
    }

    public long getMinimumAmount() {
        return minimumAmount;
    }

    public long getMaxBankCapacity() {
        return maxBankCapacity;
    }

    public long getStartAmount() {
        return startAmount;
    }

    public long getNotifyOfflineInterestDelay() {
        return notifyOfflineInterestDelay;
    }

    public long getInterestMaxAmount() {
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

    public boolean isGuiEnabled() {
        return isGuiEnabled;
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

    public int getAfkPlayersTime() {
        return afkPlayersTime;
    }
}