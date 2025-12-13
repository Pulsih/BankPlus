package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankTop.BPBankTop;
import me.pulsi_.bankplus.commands.BPCmdRegistry;
import me.pulsi_.bankplus.commands.BankTopCmd;
import me.pulsi_.bankplus.commands.MainCmd;
import me.pulsi_.bankplus.economy.EconomyUtils;
import me.pulsi_.bankplus.external.UpdateChecker;
import me.pulsi_.bankplus.external.bStats;
import me.pulsi_.bankplus.interest.BPInterest;
import me.pulsi_.bankplus.listeners.AFKListener;
import me.pulsi_.bankplus.listeners.BPTransactionListener;
import me.pulsi_.bankplus.listeners.InventoryCloseListener;
import me.pulsi_.bankplus.listeners.PlayerServerListener;
import me.pulsi_.bankplus.listeners.bankListener.*;
import me.pulsi_.bankplus.listeners.playerChat.*;
import me.pulsi_.bankplus.loanSystem.BPLoanRegistry;
import me.pulsi_.bankplus.sql.BPSQL;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPChat;
import me.pulsi_.bankplus.values.ConfigValues;
import me.pulsi_.bankplus.values.MessageValues;
import me.pulsi_.bankplus.values.MultipleBanksValues;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class BPData {

    private boolean start = true;

    private final BankPlus plugin;

    public BPData(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void setupPlugin() {
        long startTime = System.currentTimeMillis();

        BPLogger.Console.log("");
        BPLogger.Console.log("    " + BPChat.PREFIX + " <green>Enabling plugin...");
        BPLogger.Console.log("    <green>Running on version <white>" + plugin.getDescription().getVersion() + "</white>!");
        BPLogger.Console.log("    <green>Detected server version: <white>" + BankPlus.getServerVersion());
        BPLogger.Console.log("    <green>Setting up the plugin...");
        BPLogger.Console.log("");

        new bStats(plugin);
        plugin.getConfigs().setupConfigs();
        reloadPlugin();

        BPLoanRegistry.loadAllLoans();

        registerEvents();
        setupCommands();

        BPLogger.Console.log("    <green>Done! <dark_gray>(<aqua>" + (System.currentTimeMillis() - startTime) + "ms</aqua>)");
        BPLogger.Console.log("");

        if (ConfigValues.isBankTopEnabled()) BPBankTop.updateBankTop();
        start = false;
    }

    public void shutdownPlugin() {
        EconomyUtils.saveEveryone(false);
        if (ConfigValues.isInterestEnabled()) plugin.getInterest().saveInterest();
        BPLoanRegistry.saveAllLoans();

        BPLogger.Console.log("");
        BPLogger.Console.log("    " + BPChat.PREFIX + " <red>Plugin successfully disabled!");
        BPLogger.Console.log("");
    }

    public boolean reloadPlugin() {
        boolean success = true;
        try {
            ConfigValues.setupValues();
            MessageValues.setupValues();
            MultipleBanksValues.setupValues();

            BPCmdRegistry.registerPluginCommands();
            BPLogger.LogsFile.setupLoggerFile();

            if (ConfigValues.isIgnoringAfkPlayers()) plugin.getAfkManager().startCountdown();
            if (ConfigValues.isBankTopEnabled() && !BPTaskManager.contains(BPTaskManager.BANKTOP_BROADCAST_TASK)) BPBankTop.restartBankTopUpdateTask();

            BPAFK BPAFK = plugin.getAfkManager();
            if (!BPAFK.isPlayerCountdownActive()) BPAFK.startCountdown();

            BPInterest interest = plugin.getInterest();
            if (ConfigValues.isInterestEnabled() && interest.wasDisabled()) interest.restartInterest(start);

            // Load the banks to the registry before to make MySQL able to create the tables.
            BankRegistry.loadBanks();

            BPSQL.disconnect();
            if (ConfigValues.isMySqlEnabled()) BPSQL.MySQL.connect();
            else BPSQL.SQLite.connect();

            // Do this check to avoid restarting the saving interval if another one is finishing.
            if (!BPTaskManager.contains(BPTaskManager.MONEY_SAVING_TASK)) EconomyUtils.restartSavingInterval();

            Bukkit.getOnlinePlayers().forEach(p -> {
                BPPlayer player = PlayerRegistry.get(p);
                if (player != null && player.getOpenedBank() != null) p.closeInventory();
            });
        } catch (Exception e) {
            BPLogger.Console.warn(e, "Something went wrong while trying to reload the plugin.");
            success = false;
        }
        return success;
    }

    private void registerEvents() {
        PluginManager plManager = plugin.getServer().getPluginManager();

        plManager.registerEvents(new PlayerServerListener(), plugin);
        plManager.registerEvents(new UpdateChecker(), plugin);
        plManager.registerEvents(new AFKListener(plugin), plugin);
        plManager.registerEvents(new InventoryCloseListener(), plugin);
        plManager.registerEvents(new BPTransactionListener(), plugin);

        String chatPriority = ConfigValues.getPlayerChatPriority();
        if (chatPriority == null) plManager.registerEvents(new PlayerChatNormal(), plugin);
        else switch (chatPriority) {
            case "LOWEST":
                plManager.registerEvents(new PlayerChatLowest(), plugin);
                break;
            case "LOW":
                plManager.registerEvents(new PlayerChatLow(), plugin);
                break;
            case "HIGH":
                plManager.registerEvents(new PlayerChatHigh(), plugin);
                break;
            case "HIGHEST":
                plManager.registerEvents(new PlayerChatHighest(), plugin);
                break;
            default:
                plManager.registerEvents(new PlayerChatNormal(), plugin);
                break;
        }

        String bankClickPriority = ConfigValues.getBankClickPriority();
        if (bankClickPriority == null) plManager.registerEvents(new BankClickNormal(), plugin);
        else switch (bankClickPriority) {
            case "LOWEST":
                plManager.registerEvents(new BankClickLowest(), plugin);
                break;
            case "LOW":
                plManager.registerEvents(new BankClickLow(), plugin);
                break;
            case "HIGH":
                plManager.registerEvents(new BankClickHigh(), plugin);
                break;
            case "HIGHEST":
                plManager.registerEvents(new BankClickHighest(), plugin);
                break;
            default:
                plManager.registerEvents(new BankClickNormal(), plugin);
                break;
        }
    }

    private void setupCommands() {
        plugin.getCommand("bankplus").setExecutor(new MainCmd());
        plugin.getCommand("bankplus").setTabCompleter(new MainCmd());
        plugin.getCommand("banktop").setExecutor(new BankTopCmd());
    }
}