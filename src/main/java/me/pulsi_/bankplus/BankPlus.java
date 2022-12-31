package me.pulsi_.bankplus;

import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankGuis.BankGuiRegistry;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.*;
import me.pulsi_.bankplus.placeholders.Placeholders;
import me.pulsi_.bankplus.utils.BPDebugger;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPVersions;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public final class BankPlus extends JavaPlugin {

    public static boolean wasOnSingleEconomy;

    private PlayerRegistry playerRegistry;
    private BankGuiRegistry bankGuiRegistry;

    public static BankPlus INSTANCE;
    public static BPDebugger DEBUGGER;
    private Economy econ = null;
    private Permission perms = null;

    private BankTopManager bankTopManager;
    private ConfigManager configManager;
    private DataManager dataManager;
    private AFKManager afkManager;
    private TaskManager taskManager;
    private Interest interest;

    private boolean isPlaceholderAPIHooked = false, isEssentialsXHooked = false, isUpdated;
    private String serverVersion;

    private int tries = 1;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            BPLogger.log("");
            BPLogger.log("&cCannot load &a&lBank&9&lPlus&c, Vault is not installed!");
            BPLogger.log("&cPlease download it in order to use this plugin!");
            BPLogger.log("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomy()) {
            if (tries < 4) {
                BPLogger.warn("BankPlus didn't find any economy on this server! The plugin will try to enable in 2 seconds! (%i try)".replace("%i", tries + ""));
                Bukkit.getScheduler().runTaskLater(this, this::onEnable, 40);
                tries++;
                return;
            }
            BPLogger.log("");
            BPLogger.log("&cCannot load &a&lBank&9&lPlus&c, No economy plugin found!");
            BPLogger.log("&cPlease download an economy plugin to use BankPlus!");
            BPLogger.log("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        INSTANCE = this;
        DEBUGGER = new BPDebugger();

        this.playerRegistry = new PlayerRegistry();
        this.bankGuiRegistry = new BankGuiRegistry();

        this.serverVersion = getServer().getVersion();
        this.bankTopManager = new BankTopManager(this);
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        this.afkManager = new AFKManager(this);
        this.taskManager = new TaskManager();
        this.interest = new Interest();

        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) perms = rsp.getProvider();

        dataManager.setupPlugin();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            BPLogger.info("Hooked into PlaceholderAPI!");
            new Placeholders().register();
            isPlaceholderAPIHooked = true;
        }
        if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
            BPLogger.info("Hooked into Essentials!");
            isEssentialsXHooked = true;
        }

        if (Values.CONFIG.isUpdateCheckerEnabled()) Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> isUpdated = isPluginUpdated(), 0, (8 * 1200) * 60);
        wasOnSingleEconomy = !Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled();

        for (Player p : Bukkit.getOnlinePlayers()) {
            BankPlusPlayerFiles files = new BankPlusPlayerFiles(p);
            files.registerPlayer();

            BankPlusPlayer player = new BankPlusPlayer(p, files.getPlayerFile(), files.getPlayerConfig());
            BankPlus.INSTANCE.getPlayerRegistry().put(p, player);
        }

        BPVersions.moveBankFileToBanksFolder();
        BPVersions.changePlayerStoragePosition();
        BPVersions.updateBankFileActions();
    }

    @Override
    public void onDisable() {
        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) Bukkit.getOnlinePlayers().forEach(p -> new MultiEconomyManager(p).saveBankBalance(false));
        else Bukkit.getOnlinePlayers().forEach(p -> new SingleEconomyManager(p).saveBankBalance(false));
        if (Values.CONFIG.isInterestEnabled()) interest.saveInterest();
        dataManager.shutdownPlugin();
    }

    public PlayerRegistry getPlayerRegistry() {
        return playerRegistry;
    }

    public BankGuiRegistry getBankGuiRegistry() {
        return bankGuiRegistry;
    }

    public Economy getEconomy() {
        return econ;
    }

    public Permission getPermissions() {
        return perms;
    }

    public boolean isPlaceholderAPIHooked() {
        return isPlaceholderAPIHooked;
    }

    public boolean isEssentialsXHooked() {
        return isEssentialsXHooked;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public BankTopManager getBankTopManager() {
        return bankTopManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public AFKManager getAfkManager() {
        return afkManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public Interest getInterest() {
        return interest;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return true;
    }

    private boolean isPluginUpdated() {
        boolean updated;
        try {
            String newVersion = new BufferedReader(new InputStreamReader(new URL("https://api.spigotmc.org/legacy/update.php?resource=93130").openConnection().getInputStream())).readLine();
            updated = getDescription().getVersion().equals(newVersion);
        } catch (Exception e) {
            updated = true;
        }

        if (updated) BPLogger.info("The plugin is updated!");
        else BPLogger.info("The plugin is outdated! Please download the latest version here: https://www.spigotmc.org/resources/%E2%9C%A8-bankplus-%E2%9C%A8.93130/");

        return updated;
    }
}