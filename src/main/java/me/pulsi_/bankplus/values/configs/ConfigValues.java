package me.pulsi_.bankplus.values.configs;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.BPConfigs;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.List;

public class ConfigValues {

    private static final String DEF_ERROR = "Invalid number for \"%\", Please correct it in the config as soon as possible!";

    private List<String> worldsBlacklist, exitCommands, bankTopFormat, interestLimiter;
    private String chatExitMessage, playerChatPriority, bankClickPriority;
    private String second, seconds, minute, minutes, hour, hours, day, days;
    private String interestTimeSeparator, interestTimeFinalSeparator, interestTimeFormat;
    private String k, m, b, t, q, qq;
    private String infiniteCapacityText, withdrawSound, depositSound, viewSound, personalSound;
    private String maxDepositAmount, maxWithdrawAmount, depositTaxes, withdrawTaxes, depositMinimumAmount, withdrawMinimumAmount, maxBankCapacity, startAmount;
    private String bankTopMoneyFormat, banktopUpdateBroadcastMessage, upgradesMaxedPlaceholder, upgradesNoRequiredItems, banktopPlayerNotFoundPlaceholder, mainGuiName;
    private String notifyOfflineInterestMessage, interestDelay, interestOfflinePermission, interestMaxAmount, interestMoneyGiven, offlineInterestMoneyGiven, offlineInterestLimit;
    private long notifyOfflineInterestDelay, saveBalancedDelay, updateBankTopDelay;
    private int afkPlayersTime, maxDecimalsAmount, bankTopSize, chatExitTime;
    private boolean isReopeningBankAfterChat, isNotifyOfflineInterest, isStoringUUIDs, logTransactions, enableInterestLimiter;
    private boolean isInterestEnabled, isGivingInterestToOfflinePlayers, isOfflineInterestDifferentRate, notifyRegisteredPlayer, silentInfoMessages;
    private boolean isUpdateCheckerEnabled, isWithdrawSoundEnabled, isDepositSoundEnabled, accumulateInterestLimiter;
    private boolean isViewSoundEnabled, isPersonalSoundEnabled, isIgnoringAfkPlayers, useEssentialsXAFK, useBankBalanceToUpgrade, guiActionsNeedPermissions;
    private boolean banktopEnabled, banktopUpdateBroadcastEnabled, banktopUpdateBroadcastSilentConsole, saveBalancesBroadcast, guiModuleEnabled;
    private String loanMaxAmount, loanInterest;
    private int loanInstalments, loanDelay, loanAcceptTime;

    public void setupValues() {
        FileConfiguration config = BankPlus.INSTANCE.getConfigManager().getConfig(BPConfigs.Type.CONFIG.name);

        chatExitMessage = config.getString("General-Settings.Chat-Exit-Message");
        chatExitTime = config.getInt("General-Settings.Chat-Exit-Time");
        playerChatPriority = config.getString("General-Settings.Event-Priorities.PlayerChat");
        bankClickPriority = config.getString("General-Settings.Event-Priorities.BankClick");
        second = config.getString("Placeholders.Time.Second");
        seconds = config.getString("Placeholders.Time.Seconds");
        minute = config.getString("Placeholders.Time.Minute");
        minutes = config.getString("Placeholders.Time.Minutes");
        hour = config.getString("Placeholders.Time.Hour");
        hours = config.getString("Placeholders.Time.Hours");
        day = config.getString("Placeholders.Time.Day");
        days = config.getString("Placeholders.Time.Days");
        interestTimeSeparator = config.getString("Placeholders.Time.Separator");
        interestTimeFinalSeparator = config.getString("Placeholders.Time.Final-Separator");
        interestTimeFormat = config.getString("Placeholders.Time.Format");
        k = config.getString("Placeholders.Money.Thousands");
        m = config.getString("Placeholders.Money.Millions");
        b = config.getString("Placeholders.Money.Billions");
        t = config.getString("Placeholders.Money.Trillions");
        q = config.getString("Placeholders.Money.Quadrillions");
        qq = config.getString("Placeholders.Money.Quintillions");
        withdrawSound = config.getString("General-Settings.Withdraw-Sound.Sound");
        depositSound = config.getString("General-Settings.Deposit-Sound.Sound");
        viewSound = config.getString("General-Settings.View-Sound.Sound");
        personalSound = config.getString("General-Settings.Personal-Sound.Sound");
        notifyOfflineInterestMessage = config.getString("General-Settings.Offline-Interest-Earned-Message.Message");
        interestDelay = config.getString("Interest.Delay");
        interestOfflinePermission = config.getString("Interest.Offline-Permission");
        maxDepositAmount = config.getString("Deposit-Settings.Max-Deposit-Amount");
        maxWithdrawAmount = config.getString("Withdraw-Settings.Max-Withdraw-Amount");
        depositTaxes = config.getString("Deposit-Settings.Deposit-Taxes");
        withdrawTaxes = config.getString("Withdraw-Settings.Withdraw-Taxes");
        depositMinimumAmount = config.getString("Deposit-Settings.Minimum-Deposit-Amount");
        withdrawMinimumAmount = config.getString("Withdraw-Settings.Minimum-Withdraw-Amount");
        maxBankCapacity = config.getString("General-Settings.Max-Bank-Capacity");
        infiniteCapacityText = config.getString("General-Settings.Infinite-Capacity-Text");
        startAmount = config.getString("General-Settings.Join-Start-Amount");
        notifyOfflineInterestDelay = config.getLong("General-Settings.Offline-Interest-Earned-Message.Delay");
        interestMaxAmount = config.getString("Interest.Max-Amount");
        interestMoneyGiven = config.getString("Interest.Money-Given");
        isOfflineInterestDifferentRate = config.getBoolean("Interest.Different-Offline-Rate");
        offlineInterestMoneyGiven = config.getString("Interest.Offline-Money-Given");
        offlineInterestLimit = config.getString("Interest.Offline-Limit");
        upgradesMaxedPlaceholder = config.getString("Placeholders.Upgrades.Max-Level");
        upgradesNoRequiredItems = config.getString("Placeholders.No-Required-Items");
        banktopPlayerNotFoundPlaceholder = config.getString("Placeholders.BankTop.Player-Not-Found");
        worldsBlacklist = config.getStringList("General-Settings.Worlds-Blacklist");
        exitCommands = config.getStringList("General-Settings.Chat-Exit-Commands");
        isReopeningBankAfterChat = config.getBoolean("General-Settings.Reopen-Bank-After-Chat");
        isInterestEnabled = config.getBoolean("Interest.Enabled");
        isNotifyOfflineInterest = config.getBoolean("General-Settings.Offline-Interest-Earned-Message.Enabled");
        isStoringUUIDs = config.getBoolean("General-Settings.Use-UUIDs");
        logTransactions = config.getBoolean("General-Settings.Log-Transactions");
        enableInterestLimiter = config.getBoolean("Interest.Enable-Interest-Limiter");
        isGivingInterestToOfflinePlayers = config.getBoolean("Interest.Give-To-Offline-Players");
        isUpdateCheckerEnabled = config.getBoolean("Update-Checker");
        isWithdrawSoundEnabled = config.getBoolean("General-Settings.Withdraw-Sound.Enabled");
        isDepositSoundEnabled = config.getBoolean("General-Settings.Deposit-Sound.Enabled");
        isViewSoundEnabled = config.getBoolean("General-Settings.View-Sound.Enabled");
        isPersonalSoundEnabled = config.getBoolean("General-Settings.Personal-Sound.Enabled");
        isIgnoringAfkPlayers = config.getBoolean("Interest.AFK-Settings.Ignore-AFK-Players");
        useEssentialsXAFK = config.getBoolean("Interest.AFK-Settings.Use-EssentialsX-AFK");
        useBankBalanceToUpgrade = config.getBoolean("General-Settings.Use-Bank-Balance-To-Upgrade");
        guiActionsNeedPermissions = config.getBoolean("General-Settings.Gui-Actions-Need-Permissions");
        afkPlayersTime = config.getInt("Interest.AFK-Settings.AFK-Time");
        maxDecimalsAmount = config.getInt("General-Settings.Max-Decimals-Amount");
        saveBalancedDelay = config.getLong("General-Settings.Save-Delay");
        banktopEnabled = config.getBoolean("BankTop.Enabled");
        updateBankTopDelay = config.getLong("BankTop.Update-Delay");
        bankTopSize = config.getInt("BankTop.Size");
        bankTopMoneyFormat = config.getString("BankTop.Money-Format");
        bankTopFormat = config.getStringList("BankTop.Format");
        interestLimiter = config.getStringList("Interest.Interest-Limiter");
        banktopUpdateBroadcastEnabled = config.getBoolean("BankTop.Update-Broadcast.Enabled");
        banktopUpdateBroadcastSilentConsole = config.getBoolean("BankTop.Update-Broadcast.Silent-Console");
        banktopUpdateBroadcastMessage = config.getString("BankTop.Update-Broadcast.Message");
        saveBalancesBroadcast = config.getBoolean("General-Settings.Save-Broadcast");
        guiModuleEnabled = config.getBoolean("General-Settings.Enable-Guis");
        mainGuiName = config.getString("General-Settings.Main-Gui");
        loanMaxAmount = config.getString("Loan-Settings.Max-Amount");
        loanInterest = config.getString("Loan-Settings.Interest");
        loanInstalments = config.getInt("Loan-Settings.Installments");
        loanDelay = config.getInt("Loan-Settings.Delay");
        loanAcceptTime = config.getInt("Loan-Settings.Accept-Time");
        notifyRegisteredPlayer = config.getBoolean("General-Settings.Notify-Registered-Player");
        silentInfoMessages = config.getBoolean("General-Settings.Silent-Info-Messages");
        accumulateInterestLimiter = config.getBoolean("Interest.Accumulate-Interest-Limiter");
    }

    public String getPlayerChatPriority() {
        return playerChatPriority;
    }

    public String getBankClickPriority() {
        return bankClickPriority;
    }

    public String getChatExitMessage() {
        return chatExitMessage == null ? "exit" : chatExitMessage;
    }

    public int getChatExitTime() {
        return chatExitTime;
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

    public String getInterestTimeSeparator() {
        return interestTimeSeparator;
    }

    public String getInterestTimeFinalSeparator() {
        return interestTimeFinalSeparator;
    }

    public String getInterestTimeFormat() {
        return interestTimeFormat;
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
        if (!interestDelay.contains(" ")) return BPUtils.minutesInMilliseconds(Integer.parseInt(interestDelay));

        int delay;
        try {
            delay = Integer.parseInt(interestDelay.split(" ")[0]);
        } catch (NumberFormatException e) {
            return BPUtils.minutesInMilliseconds(5);
        }

        String delayType = interestDelay.split(" ")[1];
        switch (delayType) {
            case "s":
                return BPUtils.secondsInMilliseconds(delay);
            default:
                return BPUtils.minutesInMilliseconds(delay);
            case "h":
                return BPUtils.hoursInMilliseconds(delay);
            case "d":
                return BPUtils.daysInMilliseconds(delay);
        }
    }

    public String getInterestOfflinePermission() {
        return interestOfflinePermission;
    }

    public BigDecimal getMaxDepositAmount() {
        if (BPUtils.isInvalidNumber(maxDepositAmount)) {
            BPLogger.error(DEF_ERROR.replace("%", "Deposit-Settings.Max-Deposit-Amount"));
            return new BigDecimal(0);
        }
        return new BigDecimal(maxDepositAmount);
    }

    public BigDecimal getMaxWithdrawAmount() {
        if (BPUtils.isInvalidNumber(maxWithdrawAmount)) {
            BPLogger.error(DEF_ERROR.replace("%", "Withdraw-Settings.Max-Withdraw-Amount"));
            return new BigDecimal(0);
        }
        return new BigDecimal(maxWithdrawAmount);
    }

    public String getDepositTaxesString() {
        return depositTaxes;
    }

    public BigDecimal getDepositTaxes() {
        if (BPUtils.isInvalidNumber(depositTaxes)) {
            BPLogger.error(DEF_ERROR.replace("%", "Deposit-Settings.Deposit-Taxes"));
            return new BigDecimal(0);
        }
        return new BigDecimal(depositTaxes.replace("%", ""));
    }

    public String getWithdrawTaxesString() {
        return withdrawTaxes;
    }

    public BigDecimal getWithdrawTaxes() {
        if (BPUtils.isInvalidNumber(withdrawTaxes)) {
            BPLogger.error(DEF_ERROR.replace("%", "Withdraw-Settings.Withdraw-Taxes"));
            return new BigDecimal(0);
        }
        return new BigDecimal(withdrawTaxes.replace("%", ""));
    }

    public BigDecimal getDepositMinimumAmount() {
        if (BPUtils.isInvalidNumber(depositMinimumAmount)) {
            BPLogger.error(DEF_ERROR.replace("%", "Deposit-Settings.Minimum-Deposit-Amount"));
            return new BigDecimal(0);
        }
        return new BigDecimal(depositMinimumAmount);
    }

    public BigDecimal getWithdrawMinimumAmount() {
        if (BPUtils.isInvalidNumber(withdrawMinimumAmount)) {
            BPLogger.error(DEF_ERROR.replace("%", "Withdraw-Settings.Minimum-Withdraw-Amount"));
            return new BigDecimal(0);
        }
        return new BigDecimal(withdrawMinimumAmount);
    }

    public BigDecimal getMaxBankCapacity() {
        if (BPUtils.isInvalidNumber(maxBankCapacity)) {
            BPLogger.error(DEF_ERROR.replace("%", "General-Settings.Max-Bank-Capacity"));
            return new BigDecimal(0);
        }
        return new BigDecimal(maxBankCapacity);
    }

    public String getInfiniteCapacityText() {
        return infiniteCapacityText == null ? "" : infiniteCapacityText;
    }

    public BigDecimal getStartAmount() {
        if (BPUtils.isInvalidNumber(startAmount)) {
            BPLogger.error(DEF_ERROR.replace("%", "General-Settings.Join-Start-Amount"));
            return new BigDecimal(0);
        }
        return new BigDecimal(startAmount);
    }

    public long getNotifyOfflineInterestDelay() {
        return notifyOfflineInterestDelay;
    }

    public BigDecimal getInterestMaxAmount() {
        if (BPUtils.isInvalidNumber(interestMaxAmount)) {
            BPLogger.error(DEF_ERROR.replace("%", "Interest.Max-Amount"));
            return new BigDecimal(0);
        }
        return new BigDecimal(interestMaxAmount);
    }

    public BigDecimal getInterestMoneyGiven() {
        if (BPUtils.isInvalidNumber(interestMoneyGiven)) {
            BPLogger.error(DEF_ERROR.replace("%", "Interest.Money-Given"));
            return new BigDecimal(0);
        }
        return new BigDecimal(interestMoneyGiven.replace("%", ""));
    }

    public BigDecimal getOfflineInterestMoneyGiven() {
        if (isOfflineInterestDifferentRate) {
            if (BPUtils.isInvalidNumber(offlineInterestMoneyGiven)) {
                BPLogger.error(DEF_ERROR.replace("%", "Interest.Offline-Money-Given"));
                return new BigDecimal(0);
            }
            return new BigDecimal(offlineInterestMoneyGiven.replace("%", ""));
        } else {
            if (BPUtils.isInvalidNumber(interestMoneyGiven)) {
                BPLogger.error(DEF_ERROR.replace("%", "Interest.Money-Given"));
                return new BigDecimal(0);
            }
            return new BigDecimal(interestMoneyGiven.replace("%", ""));
        }
    }

    public long getOfflineInterestLimit() {
        if (!offlineInterestLimit.contains(" ")) return BPUtils.minutesInMilliseconds(Integer.parseInt(interestDelay));

        int delay;
        try {
            delay = Integer.parseInt(offlineInterestLimit.split(" ")[0]);
        } catch (NumberFormatException e) {
            return BPUtils.daysInMilliseconds(3);
        }

        String delayType = offlineInterestLimit.split(" ")[1];
        switch (delayType) {
            case "s":
                return BPUtils.secondsInMilliseconds(delay);
            default:
                return BPUtils.minutesInMilliseconds(delay);
            case "h":
                return BPUtils.hoursInMilliseconds(delay);
            case "d":
                return BPUtils.daysInMilliseconds(delay);
        }
    }

    public String getUpgradesMaxedPlaceholder() {
        return upgradesMaxedPlaceholder == null ? "Maxed" : upgradesMaxedPlaceholder;
    }

    public String getUpgradesNoRequiredItems() {
        return upgradesNoRequiredItems == null ? "None" : upgradesNoRequiredItems;
    }

    public String getBanktopPlayerNotFoundPlaceholder() {
        return banktopPlayerNotFoundPlaceholder == null ? "Not found yet." : banktopPlayerNotFoundPlaceholder;
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

    public boolean notifyOfflineInterest() {
        return isNotifyOfflineInterest;
    }

    public boolean isStoringUUIDs() {
        return isStoringUUIDs;
    }

    public boolean isLogTransactions() {
        return logTransactions;
    }

    public boolean enableInterestLimiter() {
        return enableInterestLimiter;
    }

    public boolean isGivingInterestToOfflinePlayers() {
        return isGivingInterestToOfflinePlayers;
    }

    public boolean isOfflineInterestDifferentRate() {
        return isOfflineInterestDifferentRate;
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

    public boolean useEssentialsXAFK() {
        return BankPlus.INSTANCE.isEssentialsXHooked() && useEssentialsXAFK;
    }

    public boolean useBankBalanceToUpgrade() {
        return useBankBalanceToUpgrade;
    }

    public boolean isGuiActionsNeedPermissions() {
        return guiActionsNeedPermissions;
    }

    public int getAfkPlayersTime() {
        return afkPlayersTime;
    }

    public int getMaxDecimalsAmount() {
        return Math.max(maxDecimalsAmount, 0);
    }

    public int getBankTopSize() {
        return bankTopSize;
    }

    public List<String> getBankTopFormat() {
        return bankTopFormat;
    }

    public List<String> getInterestLimiter() {
        return interestLimiter;
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

    public boolean isBanktopUpdateBroadcastSilentConsole() {
        return banktopUpdateBroadcastSilentConsole;
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

    public BigDecimal getLoanMaxAmount() {
        if (BPUtils.isInvalidNumber(loanMaxAmount)) {
            BPLogger.error(DEF_ERROR.replace("%", "Loan-Settings.Max-Amount"));
            return new BigDecimal(0);
        }
        return new BigDecimal(loanMaxAmount);
    }

    public BigDecimal getLoanInterest() {
        if (BPUtils.isInvalidNumber(loanInterest)) {
            BPLogger.error(DEF_ERROR.replace("%", "Loan-Settings.Interest"));
            return new BigDecimal(0);
        }
        return new BigDecimal(loanInterest.replace("%", ""));
    }

    public int getLoanInstalments() {
        return loanInstalments;
    }

    public int getLoanDelay() {
        return loanDelay;
    }

    public int getLoanAcceptTime() {
        return loanAcceptTime;
    }

    public boolean notifyRegisteredPlayer() {
        return notifyRegisteredPlayer;
    }

    public boolean silentInfoMessages() {
        return silentInfoMessages;
    }

    public boolean accumulateInterestLimiter() {
        return accumulateInterestLimiter;
    }
}