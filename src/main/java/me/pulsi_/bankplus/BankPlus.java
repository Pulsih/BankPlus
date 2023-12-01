package me.pulsi_.bankplus;

import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.bankSystem.BankGuiRegistry;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.interest.BPInterest;
import me.pulsi_.bankplus.loanSystem.LoanRegistry;
import me.pulsi_.bankplus.logSystem.BPLogUtils;
import me.pulsi_.bankplus.managers.*;
import me.pulsi_.bankplus.mySQL.BPSQL;
import me.pulsi_.bankplus.placeholders.BPPlaceholders;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPVersions;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public final class BankPlus extends JavaPlugin {

    public static final String actualVersion = "6.0";
    private static BankPlus INSTANCE;
    private static boolean mainClassesInitialized = false;

    private BPEconomy bpEconomy;
    private BankManager bankManager;

    private BPLogUtils bpLogUtils;
    private PlayerRegistry playerRegistry;
    private BankGuiRegistry bankGuiRegistry;
    private LoanRegistry loanRegistry;
    private Economy vaultEconomy = null;
    private Permission perms = null;

    private BPBankTop bankTopManager;
    private BPConfigs bpConfigs;
    private BPData bpData;
    private BPAFK BPAFK;
    private TaskManager taskManager;
    private BPInterest interest;

    private BPSQL sql;

    private boolean isPlaceholderApiHooked = false, isEssentialsXHooked = false, isUpdated;
    private String serverVersion;
    private int serverVersionInt;

    private int tries = 1;

    @Override
    public void onEnable() {
        PluginManager plManager = Bukkit.getPluginManager();
        if (plManager.getPlugin("Vault") == null) {
            BPLogger.log("");
            BPLogger.log("&cCannot load " + BPChat.prefix + "&c, Vault is not installed!");
            BPLogger.log("&cPlease download it in order to use this plugin!");
            BPLogger.log("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomy()) {
            if (tries < 4) {
                BPLogger.warn("BankPlus didn't find any economy plugin on this server! The plugin will re-search in 2 seconds! (" + tries + " try)");
                Bukkit.getScheduler().runTaskLater(this, this::onEnable, 40);
                tries++;
                return;
            }
            BPLogger.log("");
            BPLogger.log("&cCannot load " + BPChat.prefix + ", No economy plugin found!");
            BPLogger.log("&cPlease download an economy plugin to use BankPlus!");
            BPLogger.log("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        INSTANCE = this;

        this.bpLogUtils = new BPLogUtils();
        this.playerRegistry = new PlayerRegistry();
        this.bankGuiRegistry = new BankGuiRegistry();
        this.loanRegistry = new LoanRegistry();

        try {
            this.bpEconomy = new BPEconomy();
            this.bankManager = new BankManager();
        } catch (Exception e) {
            BPLogger.warn("It looks like someone tried to re-enable bankplus with another plugin, bankplus does not support this action and it may cause several problems, please restart the server if you have any issues.");
            return;
        }
        mainClassesInitialized = true;

        this.serverVersion = getServer().getVersion();

        int index = serverVersion.lastIndexOf("MC:");
        String version = serverVersion.substring(index, serverVersion.length() - 1);

        int number;
        try {
            number = Integer.parseInt(version.split("\\.")[1]);
        } catch (NumberFormatException e) {
            BPLogger.error("Failed to identify server version, contact the developer if the issue persist!");
            number = -1;
        }

        this.serverVersionInt = number;

        this.bankTopManager = new BPBankTop(this);
        this.bpConfigs = new BPConfigs(this);
        this.bpData = new BPData(this);
        this.BPAFK = new BPAFK(this);
        this.taskManager = new TaskManager();
        this.interest = new BPInterest(this);

        this.sql = new BPSQL();

        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) perms = rsp.getProvider();

        bpData.setupPlugin();

        if (plManager.getPlugin("PlaceholderAPI") != null) {
            BPLogger.info("Hooked into PlaceholderAPI!");
            new BPPlaceholders().register();
            isPlaceholderApiHooked = true;
        }
        if (plManager.getPlugin("Essentials") != null) {
            BPLogger.info("Hooked into Essentials!");
            isEssentialsXHooked = true;
        }

        if (Values.CONFIG.isUpdateCheckerEnabled())
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> isUpdated = isPluginUpdated(), 0, (8 * 1200) * 60 /*8 hours*/);

        if (!BPConfigs.isUpdated()) BPVersions.convertPlayerFilesToNewStyle();
    }

    @Override
    public void onDisable() {
        bpData.shutdownPlugin();
    }

    public static BankPlus INSTANCE() {
        return INSTANCE;
    }

    public static boolean isMainClassesInitialized() {
        return mainClassesInitialized;
    }

    public static BPEconomy getBPEconomy() {
        return INSTANCE.bpEconomy;
    }

    public static BankManager getBankManager() {
        return INSTANCE.bankManager;
    }

    public PlayerRegistry getPlayerRegistry() {
        return playerRegistry;
    }

    public BankGuiRegistry getBankGuiRegistry() {
        return bankGuiRegistry;
    }

    public LoanRegistry getLoanRegistry() {
        return loanRegistry;
    }

    public Economy getVaultEconomy() {
        return vaultEconomy;
    }

    public Permission getPermissions() {
        return perms;
    }

    public boolean isPlaceholderApiHooked() {
        return isPlaceholderApiHooked;
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

    public int getServerVersionInt() {
        return serverVersionInt;
    }

    public BPLogUtils getBpLogUtils() {
        return bpLogUtils;
    }

    public BPBankTop getBankTopManager() {
        return bankTopManager;
    }

    public BPConfigs getConfigs() {
        return bpConfigs;
    }

    public BPData getDataManager() {
        return bpData;
    }

    public BPAFK getAfkManager() {
        return BPAFK;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public BPInterest getInterest() {
        return interest;
    }

    public BPSQL getSql() {
        return sql;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        vaultEconomy = rsp.getProvider();
        return true;
    }

    private boolean isPluginUpdated() {
        String version = getDescription().getVersion();
        String newVersion = version;
        boolean updated = true;
        try {
            newVersion = new BufferedReader(new InputStreamReader(
                    new URL("https://api.spigotmc.org/legacy/update.php?resource=93130").openConnection().getInputStream()
            )).readLine();

            updated = actualVersion.equals(newVersion);
        } catch (Exception e) {
            BPLogger.error(e, "Could not check for updates!");
        }

        if (version.toLowerCase().contains("-alpha") && !Values.CONFIG.silentInfoMessages())
            BPLogger.info("You are using an alpha version of the plugin, please report any bug or problem found in my discord!");

        if (!Values.CONFIG.silentInfoMessages()) {
            if (updated) BPLogger.info("The plugin is updated!");
            else {
                BPLogger.info("New version of the plugin available! (v" + newVersion + ").");
                BPLogger.info("Please download the latest version here: https://www.spigotmc.org/resources/%E2%9C%A8-bankplus-%E2%9C%A8.93130/.");
            }
        }
        return updated;
    }
}