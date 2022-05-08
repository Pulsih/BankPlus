package me.pulsi_.bankplus.values.configs;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

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
    private boolean isInterestBroadcastEnabled;
    private boolean isGivingInterestToOfflinePlayers;
    private boolean isOfflineInterestEarnedMessageEnabled;
    private boolean isTitleCustomAmountEnabled;
    private boolean isUpdateCheckerEnabled;

    private boolean isWithdrawSoundEnabled;
    private boolean isDepositSoundEnabled;
    private boolean isViewSoundEnabled;
    private boolean isPersonalSoundEnabled;

    public static ConfigValues getInstance() {
        return new ConfigValues();
    }

    public void setupValues() {
        BankPlus plugin = JavaPlugin.getPlugin(BankPlus.class);

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
        isGuiFillerEnabled = plugin.config().getBoolean("Gui.Filler.Enabled");

        withdrawSound = plugin.config().getString("General.Withdraw-Sound.Sound");
        depositSound = plugin.config().getString("General.Deposit-Sound.Sound");
        viewSound = plugin.config().getString("General.View-Sound.Sound");
        personalSound = plugin.config().getString("General.Personal-Sound.Sound");

        guiTitle = plugin.config().getString("Gui.Title");
        guiUpdateDelay = plugin.config().getInt("Gui.Update-Delay");
        guiLines = plugin.config().getInt("Gui.Lines");
        guiItems = plugin.config().getConfigurationSection("Gui.Items");

        notifyOfflineInterestMessage = plugin.config().getString("General.Offline-Interest-Earned-Message.Message");
        interestDelay = plugin.config().getInt("Interest.Delay");
        maxWithdrawAmount = plugin.config().getLong("General.Max-Withdrawn-Amount");
        maxDepositAmount = plugin.config().getLong("General.Max-Deposit-Amount");
        minimumAmount = plugin.config().getLong("General.Minimum-Amount");
        maxBankCapacity = plugin.config().getLong("General.Max-Bank-Capacity");
        startAmount = plugin.config().getLong("General.Join-Start-Amount");
        notifyOfflineInterestDelay = plugin.config().getLong("General.Offline-Interest-Earned-Message.Delay");
        interestMaxAmount = plugin.config().getLong("Interest.Max-Amount");
        interestMoneyGiven = plugin.config().getDouble("Interest.Money-Given");
        worldsBlacklist = plugin.config().getStringList("General.Worlds-Blacklist");
        exitCommands = plugin.config().getStringList("General.Chat-Exit-Commands");
        isGuiEnabled = plugin.config().getBoolean("Gui.Enabled");
        isReopeningBankAfterChat = plugin.config().getBoolean("General.Reopen-Bank-After-Chat");
        isInterestEnabled = plugin.config().getBoolean("Interest.Enabled");
        isNotifyOfflineInterest = plugin.config().getBoolean("General.Offline-Interest-Earned-Message.Enabled");
        isStoringUUIDs = plugin.config().getBoolean("General.Use-UUIDs");
        isInterestBroadcastEnabled = plugin.messages().getBoolean("Interest-Broadcast.Enabled");
        isGivingInterestToOfflinePlayers = plugin.config().getBoolean("Interest.Give-To-Offline-Players");
        isOfflineInterestEarnedMessageEnabled = plugin.config().getBoolean("General.Offline-Interest-Earned-Message.Enabled");
        isTitleCustomAmountEnabled = plugin.messages().getBoolean("Title-Custom-Amount.Enabled");
        isUpdateCheckerEnabled = plugin.getConfig().getBoolean("Update-Checker");

        isWithdrawSoundEnabled = plugin.config().getBoolean("General.Withdraw-Sound.Enabled");
        isDepositSoundEnabled = plugin.config().getBoolean("General.Deposit-Sound.Enabled");
        isViewSoundEnabled = plugin.config().getBoolean("General.View-Sound.Enabled");
        isPersonalSoundEnabled = plugin.config().getBoolean("General.Personal-Sound.Enabled");
    }

    // Interest Placeholders
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

    // Money Placeholders
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

    // Gui Filler Values
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

    // Config Sounds
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

    // Gui Values
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

    // Config Values
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
    public boolean isInterestBroadcastEnabled() {
        return isInterestBroadcastEnabled;
    }
    public boolean isGivingInterestToOfflinePlayers() {
        return isGivingInterestToOfflinePlayers;
    }
    public boolean isOfflineInterestEarnedMessageEnabled() {
        return isOfflineInterestEarnedMessageEnabled;
    }
    public boolean isTitleCustomAmountEnabled() {
        return isTitleCustomAmountEnabled;
    }
    public boolean isUpdateCheckerEnabled() {
        return isUpdateCheckerEnabled;
    }

    // Config Booleans Sounds
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
}