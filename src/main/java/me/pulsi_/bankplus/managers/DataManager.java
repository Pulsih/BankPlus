package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayerFilesUtils;
import me.pulsi_.bankplus.banks.BanksManager;
import me.pulsi_.bankplus.commands.BankTopCmd;
import me.pulsi_.bankplus.commands.MainCmd;
import me.pulsi_.bankplus.external.UpdateChecker;
import me.pulsi_.bankplus.external.bStats;
import me.pulsi_.bankplus.banks.BanksHolder;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.listeners.AFKListener;
import me.pulsi_.bankplus.listeners.InventoryCloseListener;
import me.pulsi_.bankplus.listeners.PlayerJoinListener;
import me.pulsi_.bankplus.listeners.PlayerQuitListener;
import me.pulsi_.bankplus.listeners.bankListener.*;
import me.pulsi_.bankplus.listeners.playerChat.*;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class DataManager {

    public static void setupPlugin() {
        long startTime = System.currentTimeMillis();
        long time;

        BPLogger.log("");
        BPLogger.log("    &a&lBank&9&lPlus &2Enabling plugin...");
        BPLogger.log("    &aRunning on version &f" + BankPlus.instance().getDescription().getVersion() + "&a!");
        BPLogger.log("    &aDetected server version: &f" + BankPlus.instance().getServerVersion());

        time = System.currentTimeMillis();
        new bStats(BankPlus.instance());
        BankPlus.instance().getCm().createConfigs();
        BPLogger.log("    &aLoaded config files! &8(&3" + (System.currentTimeMillis() - time) + "ms&8)");

        time = System.currentTimeMillis();
        registerEvents();
        BPLogger.log("    &aRegistered events! &8(&3" + (System.currentTimeMillis() - time) + "ms&8)");

        time = System.currentTimeMillis();
        setupCommands();
        BPLogger.log("    &aLoaded plugin command! &8(&3" + (System.currentTimeMillis() - time) + "ms&8)");
        BPLogger.log("    &aDone! &8(&3" + (System.currentTimeMillis() - startTime) + " total ms&8)");
        BPLogger.log("");

        if (Values.CONFIG.isInterestEnabled()) Interest.startInterest();
        if (Values.CONFIG.isIgnoringAfkPlayers()) AFKManager.startCountdown();
        if (Values.CONFIG.isBanktopEnabled()) {
            BankTopManager.updateBankTop();
            BankTopManager.startUpdateTask();
        }
        BPMethods.startSavingBalancesTask();
    }

    public static void shutdownPlugin() {
        BPLogger.log("");
        BPLogger.log("    &a&lBank&9&lPlus &cDisabling Plugin!");
        BPLogger.log("");
    }

    public static void reloadPlugin() {
        ConfigManager cm = BankPlus.instance().getCm();
        cm.reloadConfig(ConfigManager.Type.CONFIG);
        cm.reloadConfig(ConfigManager.Type.MESSAGES);
        cm.reloadConfig(ConfigManager.Type.MULTIPLE_BANKS);
        Values.CONFIG.setupValues();
        Values.MESSAGES.setupValues();
        Values.MULTIPLE_BANKS.setupValues();
        MessageManager.loadMessages();

        if (Values.CONFIG.isGuiModuleEnabled()) new BanksManager().loadBanks();
        if (!AFKManager.isPlayerCountdownActive) AFKManager.startCountdown();
        if (Values.CONFIG.isInterestEnabled() && Interest.wasDisabled) Interest.startInterest();
        if (Values.CONFIG.isBanktopEnabled()) BankTopManager.startUpdateTask();
        BPMethods.startSavingBalancesTask();

        Bukkit.getOnlinePlayers().forEach(BankPlusPlayerFilesUtils::checkForFileFixes);
    }

    private static void registerEvents() {
        BankPlus plugin = BankPlus.instance();
        PluginManager plManager = plugin.getServer().getPluginManager();

        plManager.registerEvents(new PlayerJoinListener(), plugin);
        plManager.registerEvents(new PlayerQuitListener(), plugin);
        plManager.registerEvents(new UpdateChecker(), plugin);
        plManager.registerEvents(new AFKListener(), plugin);
        plManager.registerEvents(new InventoryCloseListener(), plugin);

        switch (Values.CONFIG.getPlayerChatPriority()) {
            case "LOWEST":
                plManager.registerEvents(new PlayerChatLowest(), plugin);
                break;
            case "LOW":
                plManager.registerEvents(new PlayerChatLow(), plugin);
                break;
            default:
                plManager.registerEvents(new PlayerChatNormal(), plugin);
                break;
            case "HIGH":
                plManager.registerEvents(new PlayerChatHigh(), plugin);
                break;
            case "HIGHEST":
                plManager.registerEvents(new PlayerChatHighest(), plugin);
                break;
        }

        switch (Values.CONFIG.getBankClickPriority()) {
            case "LOWEST":
                plManager.registerEvents(new BankClickLowest(), plugin);
                break;
            case "LOW":
                plManager.registerEvents(new BankClickLow(), plugin);
                break;
            default:
                plManager.registerEvents(new BankClickNormal(), plugin);
                break;
            case "HIGH":
                plManager.registerEvents(new BankClickHigh(), plugin);
                break;
            case "HIGHEST":
                plManager.registerEvents(new BankClickHighest(), plugin);
                break;
        }
    }

    private static void setupCommands() {
        BankPlus plugin = BankPlus.instance();
        plugin.getCommand("bankplus").setExecutor(new MainCmd());
        plugin.getCommand("bankplus").setTabCompleter(new MainCmd());
        plugin.getCommand("banktop").setExecutor(new BankTopCmd());
    }
}