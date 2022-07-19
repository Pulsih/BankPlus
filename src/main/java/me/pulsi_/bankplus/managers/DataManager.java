package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.AccountManager;
import me.pulsi_.bankplus.commands.BankTopCmd;
import me.pulsi_.bankplus.commands.MainCmd;
import me.pulsi_.bankplus.external.UpdateChecker;
import me.pulsi_.bankplus.external.bStats;
import me.pulsi_.bankplus.guis.BanksHolder;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.listeners.*;
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
        BPLogger.log("    &aRunning on version &f" + BankPlus.getInstance().getDescription().getVersion() + "&a!");
        BPLogger.log("    &aDetected server version: &f" + BankPlus.getInstance().getServerVersion());

        time = System.currentTimeMillis();
        new bStats(BankPlus.getInstance());
        BankPlus.getCm().createConfigs();
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
        BankPlus.getCm().reloadConfig(ConfigManager.Type.CONFIG);
        BankPlus.getCm().reloadConfig(ConfigManager.Type.MESSAGES);
        BankPlus.getCm().reloadConfig(ConfigManager.Type.MULTIPLE_BANKS);
        Values.CONFIG.setupValues();
        Values.MESSAGES.setupValues();
        Values.MULTIPLE_BANKS.setupValues();
        MessageManager.loadMessages();

        if (Values.CONFIG.isGuiModuleEnabled()) BanksHolder.loadBanks();
        if (!AFKManager.isPlayerCountdownActive) AFKManager.startCountdown();
        if (Values.CONFIG.isInterestEnabled() && Interest.wasDisabled) Interest.startInterest();
        if (Values.CONFIG.isBanktopEnabled()) BankTopManager.startUpdateTask();
        BPMethods.startSavingBalancesTask();

        Bukkit.getOnlinePlayers().forEach(AccountManager::checkForFileFixes);
    }

    private static void registerEvents() {
        BankPlus plugin = BankPlus.getInstance();
        PluginManager plManager = plugin.getServer().getPluginManager();

        plManager.registerEvents(new PlayerJoin(), plugin);
        plManager.registerEvents(new PlayerQuit(), plugin);
        plManager.registerEvents(new GuiListener(), plugin);
        plManager.registerEvents(new UpdateChecker(), plugin);
        plManager.registerEvents(new PlayerChat(), plugin);
        plManager.registerEvents(new AFKListener(), plugin);
    }

    private static void setupCommands() {
        BankPlus plugin = BankPlus.getInstance();
        plugin.getCommand("bankplus").setExecutor(new MainCmd());
        plugin.getCommand("bankplus").setTabCompleter(new MainCmd());
        plugin.getCommand("banktop").setExecutor(new BankTopCmd());
    }
}