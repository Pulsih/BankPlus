package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.Commands;
import me.pulsi_.bankplus.commands.TabCompletion;
import me.pulsi_.bankplus.external.UpdateChecker;
import me.pulsi_.bankplus.gui.GuiHolder;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.listeners.*;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.plugin.PluginManager;

public class DataManager {

    public static void setupPlugin() {
        long startTime = System.currentTimeMillis();
        long time;

        ChatUtils.log("");
        ChatUtils.log("  &a&lBank&9&lPlus &2Enabling plugin...");
        ChatUtils.log("  &aRunning on version &f" + BankPlus.getInstance().getDescription().getVersion() + "&a!");
        ChatUtils.log("  &aDetected server version: &f" + BankPlus.getInstance().getServerVersion());

        time = System.currentTimeMillis();
        Values.CONFIG.setupValues();
        Values.MESSAGES.setupValues();
        Values.BANK.setupValues();
        ChatUtils.log("  &aLoaded config files! &8(&3" + (System.currentTimeMillis() - time) + "ms&8)");

        time = System.currentTimeMillis();
        registerEvents();
        ChatUtils.log("  &aRegistered events! &8(&3" + (System.currentTimeMillis() - time) + "ms&8)");

        time = System.currentTimeMillis();
        setupCommands();
        ChatUtils.log("  &aLoaded plugin command! &8(&3" + (System.currentTimeMillis() - time) + "ms&8)");
        ChatUtils.log("  &aDone! &8(&3" + (System.currentTimeMillis() - startTime) + " total ms&8)");
        ChatUtils.log("");
    }

    public static void shutdownPlugin() {
        ChatUtils.log("");
        ChatUtils.log("&a&lBank&9&lPlus &cDisabling Plugin!");
        ChatUtils.log("");
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
        if (!Interest.isInterestActive) Interest.startsInterest();
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
        plugin.getCommand("bankplus").setExecutor(new Commands());
        plugin.getCommand("bankplus").setTabCompleter(new TabCompletion());
    }
}