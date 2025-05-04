package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
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
import me.pulsi_.bankplus.mySQL.BPSQL;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPChat;
import me.pulsi_.bankplus.values.ConfigValues;
import me.pulsi_.bankplus.values.MessageValues;
import me.pulsi_.bankplus.values.MultipleBanksValues;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;

public class BPData {

    private boolean start = true;

    private final BankPlus plugin;

    public BPData(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void setupPlugin() {
        long startTime = System.currentTimeMillis();

        BPLogger.log("");
        BPLogger.log("    " + BPChat.PREFIX + " <green>Enabling plugin...");
        BPLogger.log("    <green>Running on version <white>" + plugin.getDescription().getVersion() + "</white>!");
        BPLogger.log("    <green>Detected server version: <white>" + BankPlus.getServerVersion());
        BPLogger.log("    <green>Setting up the plugin...");
        BPLogger.log("");

        new bStats(plugin);
        plugin.getConfigs().setupConfigs();
        reloadPlugin();

        BPLoanRegistry.loadAllLoans();

        registerEvents();
        setupCommands();

        BPLogger.log("    <green>Done! <dark_gray>(<aqua>" + (System.currentTimeMillis() - startTime) + "ms</aqua>)");
        BPLogger.log("");

        if (ConfigValues.isBankTopEnabled()) BPBankTop.updateBankTop();
        start = false;
    }

    public void shutdownPlugin() {
        EconomyUtils.saveEveryone(false);
        if (ConfigValues.isInterestEnabled()) plugin.getInterest().saveInterest();
        BPLoanRegistry.saveAllLoans();

        BPLogger.log("");
        BPLogger.log("    " + BPChat.PREFIX + " <red>Plugin successfully disabled!");
        BPLogger.log("");
    }

    public boolean reloadPlugin() {
        boolean success = true;
        try {
            ConfigValues.setupValues();
            MessageValues.setupValues();
            MultipleBanksValues.setupValues();

            BPCmdRegistry.registerPluginCommands();

            if (ConfigValues.isLoggingTransactions()) plugin.getBpLogUtils().setupLoggerFile();
            if (ConfigValues.isIgnoringAfkPlayers()) plugin.getAfkManager().startCountdown();
            if (ConfigValues.isBankTopEnabled() && !BPTaskManager.contains(BPTaskManager.BANKTOP_BROADCAST_TASK)) BPBankTop.restartBankTopUpdateTask();

            BPAFK BPAFK = plugin.getAfkManager();
            if (!BPAFK.isPlayerCountdownActive()) BPAFK.startCountdown();

            BPInterest interest = plugin.getInterest();
            if (ConfigValues.isInterestEnabled() && interest.wasDisabled()) interest.restartInterest(start);

            plugin.getBankRegistry().loadBanks();

            BPSQL sql = plugin.getMySql();
            sql.disconnect();

            if (ConfigValues.isSqlEnabled()) {
                sql.setupMySQL();
                sql.connect();
            }

            if (!BPTaskManager.contains(BPTaskManager.MONEY_SAVING_TASK)) EconomyUtils.restartSavingInterval();

            Bukkit.getOnlinePlayers().forEach(p -> {
                BPPlayer player = PlayerRegistry.get(p);
                if (player != null && player.getOpenedBankGui() != null) p.closeInventory();
            });
        } catch (Exception e) {
            BPLogger.warn(e, "Something went wrong while trying to reload the plugin.");
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