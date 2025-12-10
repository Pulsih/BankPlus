package me.pulsi_.bankplus.values;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.List;

public class ConfigValues extends ValueLoader {

    private static List<String> worldsBlacklist;
    private static List<String> exitCommands;
    private static List<String> bankTopFormat;
    private static List<String> interestLimiter;
    private static String chatExitMessage;
    private static String playerChatPriority;
    private static String bankClickPriority;
    private static String second, seconds, minute, minutes, hour, hours, day, days;
    private static String interestTimeSeparator, interestTimeFinalSeparator, interestTimeFormat;
    private static String k, m, b, t, q, qq, thousandsSeparator, decimalsSeparator;
    private static String infiniteCapacityText, personalSound, withdrawSound, depositSound, viewSound;
    private static BigDecimal maxDepositAmount, maxWithdrawAmount;
    private static BigDecimal depositTaxes, withdrawTaxes;
    private static BigDecimal depositMinimumAmount, withdrawMinimumAmount;
    private static BigDecimal maxBankCapacity;
    private static BigDecimal startAmount;
    private static BigDecimal interestMessageSkipAmount;
    private static BigDecimal interestMaxAmount;
    private static BigDecimal interestRate;
    private static BigDecimal offlineInterestRate;
    private static BigDecimal afkInterestRate;
    private static String bankTopMoneyFormat;
    private static String bankTopUpdateBroadcastMessage;
    private static String bankTopPlayerNotFoundPlaceholder;
    private static String upgradesMaxedPlaceholder, noUpgradeItemsMessage;
    private static String mainGuiName;
    private static String offlineInterestMessage, interestOfflinePermission;
    private static long offlineInterestLimit;
    private static boolean mySqlEnabled, mySqlUsingSSL;
    private static String mySqlHost, mySqlPort, mySqlDatabase, mySqlUsername, mySqlPassword;
    private static long interestDelay, notifyOfflineInterestDelay, saveDelay, updateBankTopDelay;
    private static int afkPlayersTime;
    private static int maxDecimalsAmount;
    private static int bankTopSize;
    private static int chatExitTime;
    private static boolean showingHelpWhenNoBanksAvailable;
    private static boolean openingPermissionsNeeded;
    private static boolean reopeningBankAfterChat;
    private static boolean notifyingOfflineInterest;
    private static boolean storingUUIDs;
    private static boolean loggingTransactions;
    private static boolean interestEnabled;
    private static boolean interestLimiterEnabled;
    private static boolean offlineInterestEnabled;
    private static boolean notifyingNewPlayer;
    private static boolean silentInfoMessages;
    private static boolean updateCheckerEnabled;
    private static boolean personalSoundEnabled, depositSoundEnabled, withdrawSoundEnabled, viewSoundEnabled;
    private static boolean accumulatingInterestLimiter;
    private static boolean savingOnQuit;
    private static boolean ignoringAfkPlayers;
    private static boolean usingEssentialsXAFK;
    private static boolean usingCmiAfk;
    private static boolean usingBankBalanceToUpgrade;
    private static boolean guiActionsNeedingPermissions;
    private static boolean bankTopEnabled;
    private static boolean bankTopUpdateOnRequest;
    private static boolean bankTopUpdateBroadcastEnabled, bankTopUpdateBroadcastSilentConsole;
    private static boolean broadcastingSaves;
    private static boolean guiModuleEnabled;
    private static boolean givingInterestOnVaultBalance;
    private static boolean loanCheckEnoughMoney;
    private static BigDecimal loanMaxAmount, loanInterest;
    private static int loanDelay, loanInstalments, loanAcceptTime, loadDelay;

    public static void setupValues() {
        FileConfiguration config = BankPlus.INSTANCE().getConfigs().getConfig("config.yml");

        worldsBlacklist = config.getStringList("General-Settings.Worlds-Blacklist");
        exitCommands = config.getStringList("General-Settings.Chat-Exit-Commands");
        bankTopFormat = config.getStringList("BankTop.Format");
        interestLimiter = config.getStringList("Interest.Interest-Limiter");
        chatExitMessage = config.getString("General-Settings.Chat-Exit-Message", "exit");
        playerChatPriority = config.getString("General-Settings.Event-Priorities.PlayerChat");
        bankClickPriority = config.getString("General-Settings.Event-Priorities.BankClick");
        second = config.getString("Placeholders.Time.Second", "Second");
        seconds = config.getString("Placeholders.Time.Seconds", "Seconds");
        minute = config.getString("Placeholders.Time.Minute", "Minute");
        minutes = config.getString("Placeholders.Time.Minutes", "minutes");
        hour = config.getString("Placeholders.Time.Hour", "Hour");
        hours = config.getString("Placeholders.Time.Hours", "Hours");
        day = config.getString("Placeholders.Time.Day", "Day");
        days = config.getString("Placeholders.Time.Days", "Days");
        interestTimeSeparator = config.getString("Placeholders.Time.Separator");
        interestTimeFinalSeparator = config.getString("Placeholders.Time.Final-Separator");
        interestTimeFormat = config.getString("Placeholders.Time.Format");
        k = config.getString("Placeholders.Money.Thousands", "K");
        m = config.getString("Placeholders.Money.Millions", "B");
        b = config.getString("Placeholders.Money.Billions", "B");
        t = config.getString("Placeholders.Money.Trillions", "T");
        q = config.getString("Placeholders.Money.Quadrillions", "Q");
        qq = config.getString("Placeholders.Money.Quintillions", "QQ");
        thousandsSeparator = config.getString("Placeholders.Money.Thousands-Separator", ".");
        decimalsSeparator = config.getString("Placeholders.Money.Decimals-Separator", ",");
        infiniteCapacityText = config.getString("General-Settings.Infinite-Capacity-Text");
        withdrawSound = config.getString("General-Settings.Withdraw-Sound.Sound");
        depositSound = config.getString("General-Settings.Deposit-Sound.Sound");
        viewSound = config.getString("General-Settings.View-Sound.Sound");
        personalSound = config.getString("General-Settings.Personal-Sound.Sound");
        maxDepositAmount = getBigDecimal(config, "Deposit-Settings.Max-Deposit-Amount");
        maxWithdrawAmount = getBigDecimal(config, "Withdraw-Settings.Max-Withdraw-Amount");
        depositTaxes = getBigDecimal(config, "Deposit-Settings.Deposit-Taxes");
        withdrawTaxes = getBigDecimal(config, "Withdraw-Settings.Withdraw-Taxes");
        depositMinimumAmount = getBigDecimal(config, "Deposit-Settings.Minimum-Deposit-Amount");
        withdrawMinimumAmount = getBigDecimal(config, "Withdraw-Settings.Minimum-Withdraw-Amount");
        maxBankCapacity = getBigDecimal(config, "General-Settings.Max-Bank-Capacity");
        startAmount = getBigDecimal(config, "General-Settings.Join-Start-Amount");
        interestMessageSkipAmount = getBigDecimal(config, "Interest.Skip-Message-If-Lower-Than");
        interestMaxAmount = getBigDecimal(config, "Interest.Max-Amount");
        interestRate = getBigDecimal(config, "Interest.Rate");
        offlineInterestRate = getBigDecimal(config, "Interest.Offline-Rate");
        bankTopMoneyFormat = config.getString("BankTop.Money-Format");
        bankTopUpdateBroadcastMessage = config.getString("BankTop.Update-Broadcast.Message");
        bankTopPlayerNotFoundPlaceholder = config.getString("Placeholders.BankTop.Player-Not-Found", "Not found yet.");
        upgradesMaxedPlaceholder = config.getString("Placeholders.Upgrades.Max-Level", "Maxed");
        noUpgradeItemsMessage = config.getString("Placeholders.No-Required-Items", "None");
        mainGuiName = config.getString("General-Settings.Main-Gui");
        offlineInterestMessage = config.getString("General-Settings.Offline-Interest-Earned-Message.Message");
        interestOfflinePermission = config.getString("Interest.Offline-Permission");
        offlineInterestLimit = getDelayMilliseconds(config, "Interest.Offline-Limit");
        mySqlEnabled = config.getBoolean("General-Settings.MySQL.Enabled");
        mySqlUsingSSL = config.getBoolean("General-Settings.MySQL.Use-SSL");
        mySqlHost = config.getString("General-Settings.MySQL.Host");
        mySqlPort = config.getString("General-Settings.MySQL.Port");
        mySqlDatabase = config.getString("General-Settings.MySQL.Database");
        mySqlUsername = config.getString("General-Settings.MySQL.Username");
        mySqlPassword = config.getString("General-Settings.MySQL.Password");
        interestDelay = getDelayMilliseconds(config, "Interest.Delay");
        notifyOfflineInterestDelay = config.getLong("General-Settings.Offline-Interest-Earned-Message.Delay");
        saveDelay = config.getLong("General-Settings.Save-Delay");
        updateBankTopDelay = config.getLong("BankTop.Update-Delay");
        loanDelay = config.getInt("Loan-Settings.Delay");
        afkPlayersTime = config.getInt("Interest.AFK-Settings.AFK-Time");
        maxDecimalsAmount = config.getInt("General-Settings.Max-Decimals-Amount");
        bankTopSize = config.getInt("BankTop.Size");
        chatExitTime = config.getInt("General-Settings.Chat-Exit-Time");
        showingHelpWhenNoBanksAvailable = config.getBoolean("General-Settings.Show-Help-Message-When-No-Available-Banks");
        openingPermissionsNeeded = config.getBoolean("General-Settings.Need-Open-Permission-To-Open");
        reopeningBankAfterChat = config.getBoolean("General-Settings.Reopen-Bank-After-Chat");
        notifyingOfflineInterest = config.getBoolean("General-Settings.Offline-Interest-Earned-Message.Enabled");
        storingUUIDs = config.getBoolean("General-Settings.Use-UUIDs");
        loggingTransactions = config.getBoolean("General-Settings.Log-Transactions");
        interestEnabled = config.getBoolean("Interest.Enabled");
        interestLimiterEnabled = config.getBoolean("Interest.Enable-Interest-Limiter");
        offlineInterestEnabled = config.getBoolean("Interest.Give-To-Offline-Players");
        notifyingNewPlayer = config.getBoolean("General-Settings.Notify-Registered-Player");
        silentInfoMessages = config.getBoolean("General-Settings.Silent-Info-Messages");
        updateCheckerEnabled = config.getBoolean("Update-Checker");
        personalSoundEnabled = config.getBoolean("General-Settings.Personal-Sound.Enabled");
        depositSoundEnabled = config.getBoolean("General-Settings.Deposit-Sound.Enabled");
        withdrawSoundEnabled = config.getBoolean("General-Settings.Withdraw-Sound.Enabled");
        viewSoundEnabled = config.getBoolean("General-Settings.View-Sound.Enabled");
        accumulatingInterestLimiter = config.getBoolean("Interest.Accumulate-Interest-Limiter");
        savingOnQuit = config.getBoolean("General-Settings.Save-On-Quit");
        loadDelay = config.getInt("General-Settings.Load-Delay");
        ignoringAfkPlayers = config.getBoolean("Interest.AFK-Settings.Ignore-AFK-Players");
        afkInterestRate = getBigDecimal(config, "Interest.AFK-Settings.AFK-Rate");
        usingEssentialsXAFK = config.getBoolean("Interest.AFK-Settings.Use-EssentialsX-AFK");
        usingCmiAfk = config.getBoolean("Interest.AFK-Settings.Use-CMI-AFK");
        usingBankBalanceToUpgrade = config.getBoolean("General-Settings.Use-Bank-Balance-To-Upgrade");
        guiActionsNeedingPermissions = config.getBoolean("General-Settings.Gui-Actions-Need-Permissions");
        bankTopEnabled = config.getBoolean("BankTop.Enabled");
        bankTopUpdateOnRequest = config.getBoolean("BankTop.Update-On-Request");
        bankTopUpdateBroadcastEnabled = config.getBoolean("BankTop.Update-Broadcast.Enabled");
        bankTopUpdateBroadcastSilentConsole = config.getBoolean("BankTop.Update-Broadcast.Silent-Console");
        broadcastingSaves = config.getBoolean("General-Settings.Save-Broadcast");
        guiModuleEnabled = config.getBoolean("General-Settings.Enable-Guis");
        givingInterestOnVaultBalance = config.getBoolean("Interest.Give-Interest-On-Vault-Balance");
        loanCheckEnoughMoney = config.getBoolean("Loan-Settings.Enable-Check-Enough-Money");
        loanMaxAmount = getBigDecimal(config, "Loan-Settings.Max-Amount");
        loanInterest = getBigDecimal(config, "Loan-Settings.Interest");
        loanInstalments = config.getInt("Loan-Settings.Installments");
        loanAcceptTime = config.getInt("Loan-Settings.Accept-Time");
        loanDelay = config.getInt("Loan-Settings.Delay");
    }

    public static List<String> getWorldsBlacklist() {
        return worldsBlacklist;
    }

    public static List<String> getExitCommands() {
        return exitCommands;
    }

    public static List<String> getBankTopFormat() {
        return bankTopFormat;
    }

    public static List<String> getInterestLimiter() {
        return interestLimiter;
    }

    public static String getChatExitMessage() {
        return chatExitMessage;
    }

    public static String getPlayerChatPriority() {
        return playerChatPriority;
    }

    public static String getBankClickPriority() {
        return bankClickPriority;
    }

    public static String getSecond() {
        return second;
    }

    public static String getSeconds() {
        return seconds;
    }

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

    public static String getInterestTimeSeparator() {
        return interestTimeSeparator;
    }

    public static String getInterestTimeFinalSeparator() {
        return interestTimeFinalSeparator;
    }

    public static String getInterestTimeFormat() {
        return interestTimeFormat;
    }

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

    public static String getQq() {
        return qq;
    }

    public static String getThousandsSeparator() {
        return thousandsSeparator;
    }

    public static String getDecimalsSeparator() {
        return decimalsSeparator;
    }

    public static String getInfiniteCapacityText() {
        return infiniteCapacityText;
    }

    public static String getPersonalSound() {
        return personalSound;
    }

    public static String getWithdrawSound() {
        return withdrawSound;
    }

    public static String getDepositSound() {
        return depositSound;
    }

    public static String getViewSound() {
        return viewSound;
    }

    public static BigDecimal getMaxDepositAmount() {
        return maxDepositAmount;
    }

    public static BigDecimal getMaxWithdrawAmount() {
        return maxWithdrawAmount;
    }

    public static BigDecimal getDepositTaxes() {
        return depositTaxes;
    }

    public static BigDecimal getWithdrawTaxes() {
        return withdrawTaxes;
    }

    public static BigDecimal getDepositMinimumAmount() {
        return depositMinimumAmount;
    }

    public static BigDecimal getWithdrawMinimumAmount() {
        return withdrawMinimumAmount;
    }

    public static BigDecimal getMaxBankCapacity() {
        return maxBankCapacity;
    }

    public static BigDecimal getStartAmount() {
        return startAmount;
    }

    public static BigDecimal getInterestMessageSkipAmount() {
        return interestMessageSkipAmount;
    }

    public static BigDecimal getInterestMaxAmount() {
        return interestMaxAmount;
    }

    public static BigDecimal getInterestRate() {
        return interestRate;
    }

    public static BigDecimal getOfflineInterestRate() {
        return offlineInterestRate;
    }

    public static BigDecimal getAfkInterestRate() {
        return afkInterestRate;
    }

    public static String getBankTopMoneyFormat() {
        return bankTopMoneyFormat;
    }

    public static String getBankTopUpdateBroadcastMessage() {
        return bankTopUpdateBroadcastMessage;
    }

    public static String getBankTopPlayerNotFoundPlaceholder() {
        return bankTopPlayerNotFoundPlaceholder;
    }

    public static String getUpgradesMaxedPlaceholder() {
        return upgradesMaxedPlaceholder;
    }

    public static String getNoUpgradeItemsMessage() {
        return noUpgradeItemsMessage;
    }

    public static String getMainGuiName() {
        return mainGuiName;
    }

    public static String getOfflineInterestMessage() {
        return offlineInterestMessage;
    }

    public static String getInterestOfflinePermission() {
        return interestOfflinePermission;
    }

    public static long getOfflineInterestLimit() {
        return offlineInterestLimit;
    }

    public static boolean isMySqlEnabled() {
        return mySqlEnabled;
    }

    public static boolean isMySqlUsingSSL() {
        return mySqlUsingSSL;
    }

    public static String getMySqlHost() {
        return mySqlHost;
    }

    public static String getMySqlPort() {
        return mySqlPort;
    }

    public static String getMySqlDatabase() {
        return mySqlDatabase;
    }

    public static String getMySqlUsername() {
        return mySqlUsername;
    }

    public static String getMySqlPassword() {
        return mySqlPassword;
    }

    public static long getInterestDelay() {
        return interestDelay;
    }

    public static long getNotifyOfflineInterestDelay() {
        return notifyOfflineInterestDelay;
    }

    public static long getSaveDelay() {
        return saveDelay;
    }

    public static long getUpdateBankTopDelay() {
        return updateBankTopDelay;
    }

    public static int getLoanDelay() {
        return loanDelay;
    }

    public static int getAfkPlayersTime() {
        return afkPlayersTime;
    }

    public static int getMaxDecimalsAmount() {
        return maxDecimalsAmount;
    }

    public static int getBankTopSize() {
        return bankTopSize;
    }

    public static int getChatExitTime() {
        return chatExitTime;
    }

    public static boolean isShowingHelpWhenNoBanksAvailable() {
        return showingHelpWhenNoBanksAvailable;
    }

    public static boolean isOpeningPermissionsNeeded() {
        return openingPermissionsNeeded;
    }

    public static boolean isReopeningBankAfterChat() {
        return reopeningBankAfterChat;
    }

    public static boolean isNotifyingOfflineInterest() {
        return notifyingOfflineInterest;
    }

    public static boolean isStoringUUIDs() {
        return storingUUIDs;
    }

    public static boolean isLoggingTransactions() {
        return loggingTransactions;
    }

    public static boolean isInterestEnabled() {
        return interestEnabled;
    }

    public static boolean isInterestLimiterEnabled() {
        return interestLimiterEnabled;
    }

    public static boolean isOfflineInterestEnabled() {
        return offlineInterestEnabled;
    }

    public static boolean isNotifyingNewPlayer() {
        return notifyingNewPlayer;
    }

    public static boolean isSilentInfoMessages() {
        return silentInfoMessages;
    }

    public static boolean isUpdateCheckerEnabled() {
        return updateCheckerEnabled;
    }

    public static boolean isPersonalSoundEnabled() {
        return personalSoundEnabled;
    }

    public static boolean isDepositSoundEnabled() {
        return depositSoundEnabled;
    }

    public static boolean isWithdrawSoundEnabled() {
        return withdrawSoundEnabled;
    }

    public static boolean isViewSoundEnabled() {
        return viewSoundEnabled;
    }

    public static boolean isAccumulatingInterestLimiter() {
        return accumulatingInterestLimiter;
    }

    public static boolean isSavingOnQuit() {
        return savingOnQuit;
    }

    public static boolean isIgnoringAfkPlayers() {
        return ignoringAfkPlayers;
    }

    public static boolean isUsingEssentialsXAFK() {
        return usingEssentialsXAFK;
    }

    public static boolean isUsingCmiAfk() { return usingCmiAfk;}

    public static boolean isUsingBankBalanceToUpgrade() {
        return usingBankBalanceToUpgrade;
    }

    public static boolean isGuiActionsNeedingPermissions() {
        return guiActionsNeedingPermissions;
    }

    public static boolean isBankTopEnabled() {
        return bankTopEnabled;
    }

    public static boolean isBankTopUpdateOnRequest() {
        return bankTopUpdateOnRequest;
    }

    public static boolean isBankTopUpdateBroadcastEnabled() {
        return bankTopUpdateBroadcastEnabled;
    }

    public static boolean isBankTopUpdateBroadcastSilentConsole() {
        return bankTopUpdateBroadcastSilentConsole;
    }

    public static boolean isBroadcastingSaves() {
        return broadcastingSaves;
    }

    public static boolean isGuiModuleEnabled() {
        return guiModuleEnabled;
    }

    public static boolean isGivingInterestOnVaultBalance() {
        return givingInterestOnVaultBalance;
    }

    public static boolean isLoanCheckEnoughMoney() {
        return loanCheckEnoughMoney;
    }

    public static BigDecimal getLoanMaxAmount() {
        return loanMaxAmount;
    }

    public static BigDecimal getLoanInterest() {
        return loanInterest;
    }

    public static int getLoanInstalments() {
        return loanInstalments;
    }

    public static int getLoanAcceptTime() {
        return loanAcceptTime;
    }

    public static int getLoadDelay() {
        return loadDelay;
    }
}