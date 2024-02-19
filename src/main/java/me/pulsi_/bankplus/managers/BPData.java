package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.commands.BankTopCmd;
import me.pulsi_.bankplus.commands.CmdRegisterer;
import me.pulsi_.bankplus.commands.MainCmd;
import me.pulsi_.bankplus.external.UpdateChecker;
import me.pulsi_.bankplus.external.bStats;
import me.pulsi_.bankplus.interest.BPInterest;
import me.pulsi_.bankplus.listeners.*;
import me.pulsi_.bankplus.listeners.bankListener.*;
import me.pulsi_.bankplus.listeners.playerChat.*;
import me.pulsi_.bankplus.mySQL.BPSQL;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;

public class BPData {

    private final BankPlus plugin;

    public BPData(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void setupPlugin() {
        long startTime = System.currentTimeMillis();

        BPLogger.log("");
        BPLogger.log("    " + BPChat.prefix + " &2Enabling plugin...");
        BPLogger.log("    &aRunning on version &f" + plugin.getDescription().getVersion() + "&a!");
        BPLogger.log("    &aDetected server version: &f" + plugin.getServerVersion());

        BPLogger.log("    &aSetting up the plugin...");
        new bStats(plugin);
        plugin.getConfigs().setupConfigs();
        reloadPlugin();

        plugin.getLoanRegistry().loadAllLoans();

        registerEvents();
        setupCommands();

        BPLogger.log("    &aDone! &8(&3" + (System.currentTimeMillis() - startTime) + "ms&8)");
        BPLogger.log("");

        if (Values.CONFIG.isBanktopEnabled()) plugin.getBankTopManager().updateBankTop();
    }

    public void shutdownPlugin() {
        plugin.getEconomyRegistry().saveEveryone(false);

        BPConfigs configs = plugin.getConfigs();
        File file = configs.getFile(BPConfigs.Type.SAVES.name);
        FileConfiguration savesConfig = configs.getConfig(file);

        if (Values.CONFIG.isInterestEnabled()) plugin.getInterest().saveInterest(savesConfig);
        plugin.getLoanRegistry().saveAllLoans(savesConfig);

        try {
            savesConfig.save(file);
        } catch (IOException e) {
            BPLogger.error(e, "Failed to save changes to \"saves.yml\" file! " + e.getMessage());
        }

        BPLogger.log("");
        BPLogger.log("    " + BPChat.prefix + " &cPlugin successfully disabled!");
        BPLogger.log("");
    }

    public boolean reloadPlugin() {
        boolean success = true;
        try {
            Values.CONFIG.setupValues();
            Values.MESSAGES.setupValues();
            Values.MULTIPLE_BANKS.setupValues();
            BPMessages.loadMessages();

            CmdRegisterer registerer = new CmdRegisterer();
            registerer.resetCmds();
            registerer.registerCmds();

            if (Values.CONFIG.isLogTransactions()) plugin.getBpLogUtils().setupLoggerFile();
            if (Values.CONFIG.isIgnoringAfkPlayers()) plugin.getAfkManager().startCountdown();
            if (Values.CONFIG.isBanktopEnabled()) plugin.getBankTopManager().startUpdateTask();

            BPAFK BPAFK = plugin.getAfkManager();
            if (!BPAFK.isPlayerCountdownActive()) BPAFK.startCountdown();

            BPInterest interest = plugin.getInterest();
            if (Values.CONFIG.isInterestEnabled() && interest.wasDisabled()) interest.startInterest();

            if (Values.CONFIG.isSqlEnabled()) {
                BPSQL sql = plugin.getMySql();
                sql.disconnect();
                sql.setupMySQL();
                sql.connect();
                sql.setupTables();
            }

            Bukkit.getOnlinePlayers().forEach(p -> {
                BPPlayer player = PlayerRegistry.get(p);
                if (player != null && player.getOpenedBank() != null) p.closeInventory();
            });

            plugin.getBankGuiRegistry().loadBanks();
        } catch (Exception e) {
            BPLogger.warn(e, "Something went wrong while trying to reload the plugin, check the console logs and if the error persist, ask for support in the support discord.");
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

        String chatPriority = Values.CONFIG.getPlayerChatPriority();
        if (chatPriority == null) plManager.registerEvents(new PlayerChatNormal(), plugin);
        else switch (chatPriority) {
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

        String bankClickPriority = Values.CONFIG.getBankClickPriority();
        if (bankClickPriority == null) plManager.registerEvents(new BankClickNormal(), plugin);
        else switch (bankClickPriority) {
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