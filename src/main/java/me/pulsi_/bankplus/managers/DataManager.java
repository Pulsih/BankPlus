package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayerFiles;
import me.pulsi_.bankplus.bankGuis.BanksManager;
import me.pulsi_.bankplus.commands.BankTopCmd;
import me.pulsi_.bankplus.commands.MainCmd;
import me.pulsi_.bankplus.external.UpdateChecker;
import me.pulsi_.bankplus.external.bStats;
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

    private final BankPlus plugin;

    public DataManager(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void setupPlugin() {
        long startTime = System.currentTimeMillis();
        long time;

        BPLogger.log("");
        BPLogger.log("    &a&lBank&9&lPlus &2Enabling plugin...");
        BPLogger.log("    &aRunning on version &f" + plugin.getDescription().getVersion() + "&a!");
        BPLogger.log("    &aDetected server version: &f" + plugin.getServerVersion());

        time = System.currentTimeMillis();
        new bStats(plugin);
        plugin.getConfigManager().createConfigs();
        BPLogger.log("    &aLoaded config files! &8(&3" + (System.currentTimeMillis() - time) + "ms&8)");

        time = System.currentTimeMillis();
        registerEvents();
        BPLogger.log("    &aRegistered events! &8(&3" + (System.currentTimeMillis() - time) + "ms&8)");

        time = System.currentTimeMillis();
        setupCommands();
        BPLogger.log("    &aLoaded plugin command! &8(&3" + (System.currentTimeMillis() - time) + "ms&8)");
        BPLogger.log("    &aDone! &8(&3" + (System.currentTimeMillis() - startTime) + " total ms&8)");
        BPLogger.log("");

        if (Values.CONFIG.isInterestEnabled()) plugin.getInterest().startInterest();
        if (Values.CONFIG.isIgnoringAfkPlayers()) AFKManager.startCountdown();
        if (Values.CONFIG.isBanktopEnabled()) {
            BankTopManager bankTop = plugin.getBankTopManager();
            bankTop.updateBankTop();
            bankTop.startUpdateTask();
        }
        BPMethods.startSavingBalancesTask();
    }

    public void shutdownPlugin() {
        BPLogger.log("");
        BPLogger.log("    &a&lBank&9&lPlus &cDisabling Plugin!");
        BPLogger.log("");
    }

    public void reloadPlugin() {
        ConfigManager configManager = plugin.getConfigManager();
        configManager.reloadConfig(ConfigManager.Type.CONFIG);
        configManager.reloadConfig(ConfigManager.Type.MESSAGES);
        configManager.reloadConfig(ConfigManager.Type.MULTIPLE_BANKS);

        Values.CONFIG.setupValues();
        Values.MESSAGES.setupValues();
        Values.MULTIPLE_BANKS.setupValues();
        MessageManager.loadMessages();

        if (Values.CONFIG.isGuiModuleEnabled()) new BanksManager().loadBanks();
        if (!AFKManager.isPlayerCountdownActive) AFKManager.startCountdown();
        Interest interest = plugin.getInterest();
        if (Values.CONFIG.isInterestEnabled() && interest.wasDisabled()) interest.startInterest();
        if (Values.CONFIG.isBanktopEnabled()) plugin.getBankTopManager().startUpdateTask();
        BPMethods.startSavingBalancesTask();

        Bukkit.getOnlinePlayers().forEach(p -> new BankPlusPlayerFiles(p).checkForFileFixes());
    }

    private void registerEvents() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        pluginManager.registerEvents(new PlayerJoinListener(), plugin);
        pluginManager.registerEvents(new PlayerQuitListener(), plugin);
        pluginManager.registerEvents(new UpdateChecker(), plugin);
        pluginManager.registerEvents(new AFKListener(), plugin);
        pluginManager.registerEvents(new InventoryCloseListener(), plugin);

        switch (Values.CONFIG.getPlayerChatPriority()) {
            case "LOWEST":
                pluginManager.registerEvents(new PlayerChatLowest(), plugin);
                break;
            case "LOW":
                pluginManager.registerEvents(new PlayerChatLow(), plugin);
                break;
            default:
                pluginManager.registerEvents(new PlayerChatNormal(), plugin);
                break;
            case "HIGH":
                pluginManager.registerEvents(new PlayerChatHigh(), plugin);
                break;
            case "HIGHEST":
                pluginManager.registerEvents(new PlayerChatHighest(), plugin);
                break;
        }

        switch (Values.CONFIG.getBankClickPriority()) {
            case "LOWEST":
                pluginManager.registerEvents(new BankClickLowest(), plugin);
                break;
            case "LOW":
                pluginManager.registerEvents(new BankClickLow(), plugin);
                break;
            default:
                pluginManager.registerEvents(new BankClickNormal(), plugin);
                break;
            case "HIGH":
                pluginManager.registerEvents(new BankClickHigh(), plugin);
                break;
            case "HIGHEST":
                pluginManager.registerEvents(new BankClickHighest(), plugin);
                break;
        }
    }

    private void setupCommands() {
        plugin.getCommand("bankplus").setExecutor(new MainCmd());
        plugin.getCommand("bankplus").setTabCompleter(new MainCmd());
        plugin.getCommand("banktop").setExecutor(new BankTopCmd());
    }
}