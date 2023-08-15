package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerFiles;
import me.pulsi_.bankplus.commands.BankTopCmd;
import me.pulsi_.bankplus.commands.CmdRegisterer;
import me.pulsi_.bankplus.commands.MainCmd;
import me.pulsi_.bankplus.external.UpdateChecker;
import me.pulsi_.bankplus.external.bStats;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.listeners.*;
import me.pulsi_.bankplus.listeners.bankListener.*;
import me.pulsi_.bankplus.listeners.playerChat.*;
import me.pulsi_.bankplus.loanSystem.LoanUtils;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
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

        BPLogger.log("");
        BPLogger.log("    &a&lBank&9&lPlus &2Enabling plugin...");
        BPLogger.log("    &aRunning on version &f" + plugin.getDescription().getVersion() + "&a!");
        BPLogger.log("    &aDetected server version: &f" + plugin.getServerVersion());

        BPLogger.log("    &aSetting up the plugin...");
        new bStats(plugin);
        plugin.getConfigManager().setupConfigs();
        reloadPlugin();
        registerEvents();
        setupCommands();

        BPLogger.log("    &aDone! &8(&3" + (System.currentTimeMillis() - startTime) + "ms&8)");
        BPLogger.log("");
    }

    public void shutdownPlugin() {
        BPLogger.log("");
        BPLogger.log("    &a&lBank&9&lPlus &cPlugin successfully disabled!");
        BPLogger.log("");
    }

    public boolean reloadPlugin() {
        boolean success = true;

        ConfigManager configManager = plugin.getConfigManager();
        if (!configManager.reloadConfig(ConfigManager.Type.COMMANDS)) success = false;
        if (!configManager.reloadConfig(ConfigManager.Type.CONFIG)) success = false;
        if (!configManager.reloadConfig(ConfigManager.Type.MESSAGES)) success = false;
        if (!configManager.reloadConfig(ConfigManager.Type.MULTIPLE_BANKS)) success = false;

        Values.CONFIG.setupValues();
        Values.MESSAGES.setupValues();
        Values.MULTIPLE_BANKS.setupValues();
        BPMessages.loadMessages();

        CmdRegisterer registerer = new CmdRegisterer();
        registerer.resetCmds();
        registerer.registerCmds();

        if (Values.CONFIG.isInterestEnabled()) plugin.getInterest().startInterest();
        if (Values.CONFIG.isIgnoringAfkPlayers()) plugin.getAfkManager().startCountdown();
        if (Values.CONFIG.isBanktopEnabled()) plugin.getBankTopManager().startUpdateTask();
        if (Values.CONFIG.isGuiModuleEnabled() && !plugin.getBankGuiRegistry().loadBanks()) success = false;

        AFKManager afkManager = plugin.getAfkManager();
        if (!afkManager.isPlayerCountdownActive()) afkManager.startCountdown();

        Interest interest = plugin.getInterest();
        if (Values.CONFIG.isInterestEnabled() && interest.wasDisabled()) interest.startInterest();

        LoanUtils.loadAllLoans();
        BPMethods.startSavingBalancesTask();
        Bukkit.getOnlinePlayers().forEach(p -> new BPPlayerFiles(p).checkForFileFixes());
        return success;
    }

    private void registerEvents() {
        PluginManager plManager = plugin.getServer().getPluginManager();

        plManager.registerEvents(new PlayerJoinListener(), plugin);
        plManager.registerEvents(new PlayerQuitListener(), plugin);
        plManager.registerEvents(new UpdateChecker(), plugin);
        plManager.registerEvents(new AFKListener(plugin), plugin);
        plManager.registerEvents(new InventoryCloseListener(), plugin);
        plManager.registerEvents(new BPTransactionListener(), plugin);


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

    private void setupCommands() {
        plugin.getCommand("bankplus").setExecutor(new MainCmd());
        plugin.getCommand("bankplus").setTabCompleter(new MainCmd());
        plugin.getCommand("banktop").setExecutor(new BankTopCmd());
    }
}