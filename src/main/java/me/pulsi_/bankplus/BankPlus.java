package me.pulsi_.bankplus;

import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.interest.BPInterest;
import me.pulsi_.bankplus.logSystem.BPLogUtils;
import me.pulsi_.bankplus.managers.BPAFK;
import me.pulsi_.bankplus.managers.BPConfigs;
import me.pulsi_.bankplus.managers.BPData;
import me.pulsi_.bankplus.mySQL.BPSQL;
import me.pulsi_.bankplus.placeholders.BPPlaceholders;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPVersions;
import me.pulsi_.bankplus.utils.texts.BPChat;
import me.pulsi_.bankplus.values.ConfigValues;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public final class BankPlus extends JavaPlugin {

    public static final String actualVersion = "6.3";

    private static String serverVersion;
    private static BankPlus INSTANCE;

    private final BPLogUtils bpLogUtils = new BPLogUtils();
    private final BankRegistry bankRegistry = new BankRegistry();
    private final BPSQL sql = new BPSQL();

    private Economy vaultEconomy = null;
    private Permission perms = null;

    private BPPlaceholders bpPlaceholders;
    private BPConfigs bpConfigs;
    private BPData bpData;
    private BPAFK BPAfk;
    private BPInterest interest;

    private boolean isPlaceholderApiHooked = false, isEssentialsXHooked = false, isCmiHooked = false, isUpdated;

    private int tries = 1;

    @Override
    public void onEnable() {
        INSTANCE = this;

        PluginManager plManager = Bukkit.getPluginManager();
        if (plManager.getPlugin("Vault") == null) {
            BPLogger.log("");
            BPLogger.log("<red>Cannot load " + BPChat.PREFIX + ", Vault is not installed.");
            BPLogger.log("<red>Please download it in order to use this plugin.");
            BPLogger.log("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomy()) {
            if (tries < 4) {
                BPLogger.warn("BankPlus didn't find any economy plugin on this server, the plugin will re-search in 2 seconds. (" + tries + " try)");
                Bukkit.getScheduler().runTaskLater(this, this::onEnable, 40);
                tries++;
                return;
            }
            BPLogger.log("");
            BPLogger.log("<red>Cannot load " + BPChat.PREFIX + ", No economy plugin found.");
            BPLogger.log("<red>Please download an economy plugin to use this plugin.");
            BPLogger.log("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        String v = getServer().getVersion();
        serverVersion = v.substring(v.lastIndexOf("MC:"), v.length() - 1).replace("MC: ", "");

        this.bpConfigs = new BPConfigs(this);
        this.bpData = new BPData(this);
        this.BPAfk = new BPAFK(this);
        this.interest = new BPInterest(this);

        if (!BPConfigs.isUpdated()) {
            BPVersions.renameInterestMoneyGiveToRate();
            BPVersions.convertPlayerFilesToNewStyle();
            BPVersions.changeBankUpgradesSection();
        }

        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) perms = rsp.getProvider();

        bpData.setupPlugin();

        if (plManager.getPlugin("PlaceholderAPI") != null) {
            BPLogger.info("Hooked into PlaceholderAPI!");
            bpPlaceholders = new BPPlaceholders();
            bpPlaceholders.registerPlaceholders();
            bpPlaceholders.register();
            isPlaceholderApiHooked = true;
        }
        if (plManager.getPlugin("Essentials") != null) {
            BPLogger.info("Hooked into Essentials!");
            isEssentialsXHooked = true;
        }
        if (plManager.getPlugin("CMI") != null) {
            BPLogger.info("Hooked into CMI!");
            isCmiHooked = true;
        }

        if (ConfigValues.isUpdateCheckerEnabled())
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> isUpdated = isPluginUpdated(), 0, (8 * 1200) * 60 /*8 hours*/);
    }

    @Override
    public void onDisable() {
        bpData.shutdownPlugin();
    }

    public static BankPlus INSTANCE() {
        return INSTANCE;
    }

    public static String getServerVersion() {
        return serverVersion;
    }

    public static boolean isAlphaVersion() {
        return INSTANCE.getDescription().getVersion().toLowerCase().contains("-alpha");
    }

    public BankRegistry getBankRegistry() {
        return bankRegistry;
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

    public boolean isCmiHooked() {
        return isCmiHooked;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public BPLogUtils getBpLogUtils() {
        return bpLogUtils;
    }

    public BPConfigs getConfigs() {
        return bpConfigs;
    }

    public BPData getDataManager() {
        return bpData;
    }

    public BPAFK getAfkManager() {
        return BPAfk;
    }

    public BPInterest getInterest() {
        return interest;
    }

    public BPPlaceholders getBpPlaceholders() {
        return bpPlaceholders;
    }

    public BPSQL getMySql() {
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
        String newVersion = getDescription().getVersion();
        boolean updated = true;
        try {
            newVersion = new BufferedReader(new InputStreamReader(
                    URI.create("https://api.spigotmc.org/legacy/update.php?resource=93130").toURL().openConnection().getInputStream()
            )).readLine();

            updated = actualVersion.equals(newVersion);
        } catch (Exception e) {
            BPLogger.warn("Could not check for updates. (No internet connection)");
        }

        if (isAlphaVersion() && !ConfigValues.isSilentInfoMessages())
            BPLogger.info("You are using an alpha version of the plugin, please report any bug or problem found in my discord!");

        if (updated) {
            if (!ConfigValues.isSilentInfoMessages()) BPLogger.info("The plugin is updated!");
        } else {
            // Even if the info is disabled, notify when there is a new update
            // because it is important to keep users at the latest version.
            BPLogger.info("New version of the plugin available! (v" + newVersion + ").");
            BPLogger.info("Please download the latest version here: https://www.spigotmc.org/resources/%E2%9C%A8-bankplus-%E2%9C%A8.93130/.");
        }
        return updated;
    }
}