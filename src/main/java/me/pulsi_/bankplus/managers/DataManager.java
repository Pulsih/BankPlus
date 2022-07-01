package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.BankTopCmd;
import me.pulsi_.bankplus.commands.MainCmd;
import me.pulsi_.bankplus.external.UpdateChecker;
import me.pulsi_.bankplus.external.bStats;
import me.pulsi_.bankplus.gui.GuiHolder;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.listeners.*;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.plugin.PluginManager;

public class DataManager {

    public static void setupPlugin() {
        long startTime = System.currentTimeMillis();
        long time;

        BPLogger.log("");
        BPLogger.log("  &a&lBank&9&lPlus &2Enabling plugin...");
        BPLogger.log("  &aRunning on version &f" + BankPlus.getInstance().getDescription().getVersion() + "&a!");
        BPLogger.log("  &aDetected server version: &f" + BankPlus.getInstance().getServerVersion());

        time = System.currentTimeMillis();
        new bStats(BankPlus.getInstance());
        BankPlus.getCm().createConfigs();
        Values.CONFIG.setupValues();
        Values.MESSAGES.setupValues();
        Values.BANK.setupValues();
        BPLogger.log("  &aLoaded config files! &8(&3" + (System.currentTimeMillis() - time) + "ms&8)");

        time = System.currentTimeMillis();
        registerEvents();
        BPLogger.log("  &aRegistered events! &8(&3" + (System.currentTimeMillis() - time) + "ms&8)");

        time = System.currentTimeMillis();
        setupCommands();
        BPLogger.log("  &aLoaded plugin command! &8(&3" + (System.currentTimeMillis() - time) + "ms&8)");
        BPLogger.log("  &aDone! &8(&3" + (System.currentTimeMillis() - startTime) + " total ms&8)");
        BPLogger.log("");
    }

    public static void shutdownPlugin() {
        BPLogger.log("");
        BPLogger.log("&a&lBank&9&lPlus &cDisabling Plugin!");
        BPLogger.log("");
    }

    public static void reloadPlugin() {
        BankPlus.getCm().reloadConfig("config");
        BankPlus.getCm().reloadConfig("messages");
        BankPlus.getCm().reloadConfig("bank");
        Values.CONFIG.setupValues();
        Values.MESSAGES.setupValues();
        Values.BANK.setupValues();

        if (Values.BANK.isGuiEnabled()) new GuiHolder().loadBank();
        if (!AFKManager.isPlayerCountdownActive) AFKManager.startCountdown();
        if (Values.CONFIG.isInterestEnabled() && Interest.wasDisabled) Interest.startInterest();
        if (Values.CONFIG.isBanktopEnabled()) BankTopManager.startUpdateTask();
        Methods.startSavingBalancesTask();
    }

    private static void registerEvents() {
        BankPlus plugin = BankPlus.getInstance();
        PluginManager plManager = plugin.getServer().getPluginManager();

        plManager.registerEvents(new PlayerJoin(), plugin);
        plManager.registerEvents(new PlayerQuit(), plugin);
        plManager.registerEvents(new GuiListener(), plugin);
        plManager.registerEvents(new UpdateChecker(plugin), plugin);
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