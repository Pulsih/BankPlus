package me.pulsi_.bankplus.debug;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.loanSystem.BPLoanRegistry;
import me.pulsi_.bankplus.managers.BPTaskManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.SavesFile;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import me.pulsi_.bankplus.values.MultipleBanksValues;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BPDump {

    /**
     * Create a BankPlus dump and upload it to mclo.gs asynchronously.
     *
     * @param receiver Who will receive the result message.
     */
    public static void dump(Object receiver) {
        Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
            try {
                String url = uploadDump(createDump());
                if (url == null) BPMessages.sendIdentifier(receiver, "Dump-Fail");
                else BPMessages.sendIdentifier(receiver, "Dump-Success", "%url%$" + url);
            } catch (Exception e) {
                BPLogger.Console.error(e, "Could not upload the BankPlus dump.");
                BPMessages.sendIdentifier(receiver, "Dump-Fail");
            }
        });
    }

    private static String createDump() {
        BankPlus plugin = BankPlus.INSTANCE();
        StringBuilder dump = new StringBuilder();

        // Same idea as ViaVersion's versionInfo: environment + plugin identity.
        dump.append("=== versionInfo ===\n");
        dump.append("generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())).append('\n');
        dump.append("javaVersion: ").append(System.getProperty("java.version")).append('\n');
        dump.append("javaVendor: ").append(System.getProperty("java.vendor")).append('\n');
        dump.append("operatingSystem: ").append(System.getProperty("os.name")).append('\n');
        dump.append("osVersion: ").append(System.getProperty("os.version")).append('\n');
        dump.append("osArch: ").append(System.getProperty("os.arch")).append('\n');
        dump.append("platformName: ").append(Bukkit.getName()).append('\n');
        dump.append("platformVersion: ").append(Bukkit.getVersion()).append('\n');
        dump.append("bukkitVersion: ").append(Bukkit.getBukkitVersion()).append('\n');
        dump.append("serverVersion: ").append(BankPlus.getServerVersion()).append('\n');
        dump.append("pluginVersion: ").append(plugin.getPluginMeta().getVersion()).append('\n');
        dump.append("actualVersion: ").append(BankPlus.actualVersion).append('\n');
        dump.append("alpha: ").append(BankPlus.isAlphaVersion()).append('\n');
        dump.append("apiVersion: ").append(plugin.getPluginMeta().getAPIVersion()).append('\n');
        dump.append("onlinePlayers: ").append(Bukkit.getOnlinePlayers().size()).append('/').append(Bukkit.getMaxPlayers()).append('\n');
        dump.append("viewDistance: ").append(Bukkit.getViewDistance()).append('\n');
        dump.append("onlineMode: ").append(Bukkit.getOnlineMode()).append('\n');
        dump.append('\n');

        // Soft hooks + Vault provider (BankPlus equivalent of subPlatforms).
        dump.append("=== hooks ===\n");
        dump.append("vaultEconomy: ").append(plugin.getVaultEconomy() != null ? plugin.getVaultEconomy().getName() : "none").append('\n');
        appendHook(dump, "PlaceholderAPI", plugin.isPlaceholderApiHooked());
        appendHook(dump, "Essentials", plugin.isEssentialsXHooked());
        appendHook(dump, "CMI", plugin.isCmiHooked());
        dump.append('\n');

        dump.append("=== storage ===\n");
        dump.append("mysqlEnabled: ").append(ConfigValues.isMySqlEnabled()).append('\n');
        dump.append("mysqlSsl: ").append(ConfigValues.isMySqlUsingSSL()).append('\n');
        dump.append("mysqlHost: ").append(ConfigValues.getMySqlHost()).append('\n');
        dump.append("mysqlPort: ").append(ConfigValues.getMySqlPort()).append('\n');
        dump.append("mysqlDatabase: ").append(ConfigValues.getMySqlDatabase()).append('\n');
        dump.append("mysqlUsername: ").append(ConfigValues.getMySqlUsername()).append('\n');
        dump.append("mysqlPassword: ********\n"); // Scrub the password.
        dump.append("useUuids: ").append(ConfigValues.isStoringUUIDs()).append('\n');
        dump.append("savesVersion: ").append(SavesFile.getString("version")).append('\n');
        dump.append('\n');

        dump.append("=== runtime ===\n");
        dump.append("banksLoaded: ").append(BankRegistry.getBanks().size()).append('\n');
        dump.append("activeLoans: ").append(BPLoanRegistry.getLoans().size()).append('\n');
        dump.append("pendingLoanRequests: ").append(BPLoanRegistry.getRequests().size()).append('\n');
        dump.append("task.interest: ").append(BPTaskManager.contains(BPTaskManager.INTEREST_TASK)).append('\n');
        dump.append("task.moneySaving: ").append(BPTaskManager.contains(BPTaskManager.MONEY_SAVING_TASK)).append('\n');
        dump.append("task.bankTopBroadcast: ").append(BPTaskManager.contains(BPTaskManager.BANKTOP_BROADCAST_TASK)).append('\n');
        if (ConfigValues.isInterestEnabled())
            dump.append("interestCooldownMs: ").append(plugin.getInterest().getInterestCooldownMillis()).append('\n');
        dump.append('\n');

        // Full useful config snapshot, like ViaVersion's configuration map.
        dump.append("=== configuration ===\n");
        appendConfig(dump);
        dump.append('\n');

        dump.append("=== multipleBanks ===\n");
        dump.append("enabled: ").append(MultipleBanksValues.enableMultipleBanksModule()).append('\n');
        dump.append("showNotAvailableBanks: ").append(MultipleBanksValues.isShowNotAvailableBanks()).append('\n');
        dump.append("directlyOpenIf1IsAvailable: ").append(MultipleBanksValues.isDirectlyOpenIf1IsAvailable()).append('\n');
        dump.append("bankListGuiLines: ").append(MultipleBanksValues.getBankListGuiLines()).append('\n');
        dump.append("updateDelay: ").append(MultipleBanksValues.getUpdateDelay()).append('\n');
        dump.append("autoBanksUnlocker: ").append(MultipleBanksValues.getAutoBanksUnlocker()).append('\n');
        dump.append('\n');

        dump.append("=== banks ===\n");
        List<String> bankNames = new ArrayList<>(BankRegistry.getBanks().keySet());
        Collections.sort(bankNames);
        for (String name : bankNames) {
            Bank bank = BankRegistry.getBank(name);
            if (bank != null) appendBank(dump, bank);
        }
        dump.append('\n');

        // ViaVersion platformDump.plugins style.
        dump.append("=== plugins ===\n");
        for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
            PluginMeta meta = pl.getPluginMeta();
            dump.append("- name: ").append(meta.getName()).append('\n');
            dump.append("  version: ").append(meta.getVersion()).append('\n');
            dump.append("  enabled: ").append(pl.isEnabled()).append('\n');
            dump.append("  main: ").append(meta.getMainClass()).append('\n');
            dump.append("  authors: ").append(meta.getAuthors()).append('\n');
            dump.append("  api-version: ").append(meta.getAPIVersion()).append('\n');
            dump.append("  depend: ").append(meta.getPluginDependencies()).append('\n');
            dump.append("  softdepend: ").append(meta.getPluginSoftDependencies()).append('\n');
        }
        dump.append('\n');

        appendRecentLogs(dump);
        return dump.toString();
    }

    private static void appendHook(StringBuilder dump, String name, boolean hooked) {
        Plugin pl = Bukkit.getPluginManager().getPlugin(name);
        dump.append(name).append(": hooked=").append(hooked);
        if (pl != null) dump.append(", version=").append(pl.getPluginMeta().getVersion());
        dump.append('\n');
    }

    private static void appendConfig(StringBuilder dump) {
        dump.append("mainGui: ").append(ConfigValues.getMainGuiName()).append('\n');
        dump.append("enableGuis: ").append(ConfigValues.isGuiModuleEnabled()).append('\n');
        dump.append("worldsBlacklist: ").append(ConfigValues.getWorldsBlacklist()).append('\n');
        dump.append("chatExitMessage: ").append(ConfigValues.getChatExitMessage()).append('\n');
        dump.append("chatExitCommands: ").append(ConfigValues.getExitCommands()).append('\n');
        dump.append("chatExitTime: ").append(ConfigValues.getChatExitTime()).append('\n');
        dump.append("playerChatPriority: ").append(ConfigValues.getPlayerChatPriority()).append('\n');
        dump.append("bankClickPriority: ").append(ConfigValues.getBankClickPriority()).append('\n');
        dump.append("needOpenPermission: ").append(ConfigValues.isOpeningPermissionsNeeded()).append('\n');
        dump.append("reopenBankAfterChat: ").append(ConfigValues.isReopeningBankAfterChat()).append('\n');
        dump.append("showHelpWhenNoBanks: ").append(ConfigValues.isShowingHelpWhenNoBanksAvailable()).append('\n');
        dump.append("guiActionsNeedPermissions: ").append(ConfigValues.isGuiActionsNeedingPermissions()).append('\n');
        dump.append("useBankBalanceToUpgrade: ").append(ConfigValues.isUsingBankBalanceToUpgrade()).append('\n');
        dump.append("maxBankCapacity: ").append(ConfigValues.getMaxBankCapacity()).append('\n');
        dump.append("startAmount: ").append(ConfigValues.getStartAmount()).append('\n');
        dump.append("maxDecimalsAmount: ").append(ConfigValues.getMaxDecimalsAmount()).append('\n');
        dump.append("infiniteCapacityText: ").append(ConfigValues.getInfiniteCapacityText()).append('\n');
        dump.append("notifyRegisteredPlayer: ").append(ConfigValues.isNotifyingNewPlayer()).append('\n');
        dump.append("silentInfoMessages: ").append(ConfigValues.isSilentInfoMessages()).append('\n');
        dump.append("updateChecker: ").append(ConfigValues.isUpdateCheckerEnabled()).append('\n');
        dump.append("logTransactions: ").append(ConfigValues.isLoggingTransactions()).append('\n');
        dump.append("saveOnQuit: ").append(ConfigValues.isSavingOnQuit()).append('\n');
        dump.append("saveDelay: ").append(ConfigValues.getSaveDelay()).append('\n');
        dump.append("saveBroadcast: ").append(ConfigValues.isBroadcastingSaves()).append('\n');
        dump.append("loadDelay: ").append(ConfigValues.getLoadDelay()).append('\n');

        dump.append("maxDepositAmount: ").append(ConfigValues.getMaxDepositAmount()).append('\n');
        dump.append("maxWithdrawAmount: ").append(ConfigValues.getMaxWithdrawAmount()).append('\n');
        dump.append("depositTaxes: ").append(ConfigValues.getDepositTaxes()).append('\n');
        dump.append("withdrawTaxes: ").append(ConfigValues.getWithdrawTaxes()).append('\n');
        dump.append("depositMinimumAmount: ").append(ConfigValues.getDepositMinimumAmount()).append('\n');
        dump.append("withdrawMinimumAmount: ").append(ConfigValues.getWithdrawMinimumAmount()).append('\n');

        dump.append("interestEnabled: ").append(ConfigValues.isInterestEnabled()).append('\n');
        dump.append("interestDelay: ").append(ConfigValues.getInterestDelay()).append('\n');
        dump.append("interestRate: ").append(ConfigValues.getInterestRate()).append('\n');
        dump.append("offlineInterestEnabled: ").append(ConfigValues.isOfflineInterestEnabled()).append('\n');
        dump.append("offlineInterestRate: ").append(ConfigValues.getOfflineInterestRate()).append('\n');
        dump.append("offlineInterestPermission: ").append(ConfigValues.getInterestOfflinePermission()).append('\n');
        dump.append("offlineInterestLimit: ").append(ConfigValues.getOfflineInterestLimit()).append('\n');
        dump.append("interestMaxAmount: ").append(ConfigValues.getInterestMaxAmount()).append('\n');
        dump.append("interestSkipMessageIfLowerThan: ").append(ConfigValues.getInterestMessageSkipAmount()).append('\n');
        dump.append("interestLimiterEnabled: ").append(ConfigValues.isInterestLimiterEnabled()).append('\n');
        dump.append("accumulateInterestLimiter: ").append(ConfigValues.isAccumulatingInterestLimiter()).append('\n');
        dump.append("interestLimiter: ").append(ConfigValues.getInterestLimiter()).append('\n');
        dump.append("giveInterestOnVaultBalance: ").append(ConfigValues.isGivingInterestOnVaultBalance()).append('\n');
        dump.append("ignoreAfkPlayers: ").append(ConfigValues.isIgnoringAfkPlayers()).append('\n');
        dump.append("afkTime: ").append(ConfigValues.getAfkPlayersTime()).append('\n');
        dump.append("afkInterestRate: ").append(ConfigValues.getAfkInterestRate()).append('\n');
        dump.append("useEssentialsAfk: ").append(ConfigValues.isUsingEssentialsXAFK()).append('\n');
        dump.append("useCmiAfk: ").append(ConfigValues.isUsingCmiAfk()).append('\n');
        dump.append("notifyOfflineInterest: ").append(ConfigValues.isNotifyingOfflineInterest()).append('\n');
        dump.append("notifyOfflineInterestDelay: ").append(ConfigValues.getNotifyOfflineInterestDelay()).append('\n');

        dump.append("bankTopEnabled: ").append(ConfigValues.isBankTopEnabled()).append('\n');
        dump.append("bankTopSize: ").append(ConfigValues.getBankTopSize()).append('\n');
        dump.append("bankTopUpdateDelay: ").append(ConfigValues.getUpdateBankTopDelay()).append('\n');
        dump.append("bankTopUpdateOnRequest: ").append(ConfigValues.isBankTopUpdateOnRequest()).append('\n');
        dump.append("bankTopMoneyFormat: ").append(ConfigValues.getBankTopMoneyFormat()).append('\n');
        dump.append("bankTopFormat: ").append(ConfigValues.getBankTopFormat()).append('\n');
        dump.append("bankTopBroadcastEnabled: ").append(ConfigValues.isBankTopUpdateBroadcastEnabled()).append('\n');
        dump.append("bankTopBroadcastSilentConsole: ").append(ConfigValues.isBankTopUpdateBroadcastSilentConsole()).append('\n');

        dump.append("loanCheckEnoughMoney: ").append(ConfigValues.isLoanCheckEnoughMoney()).append('\n');
        dump.append("loanMaxAmount: ").append(ConfigValues.getLoanMaxAmount()).append('\n');
        dump.append("loanInterest: ").append(ConfigValues.getLoanInterest()).append('\n');
        dump.append("loanInstallments: ").append(ConfigValues.getLoanInstalments()).append('\n');
        dump.append("loanAcceptTime: ").append(ConfigValues.getLoanAcceptTime()).append('\n');
        dump.append("loanDelay: ").append(ConfigValues.getLoanDelay()).append('\n');

        dump.append("personalSoundEnabled: ").append(ConfigValues.isPersonalSoundEnabled()).append('\n');
        dump.append("depositSoundEnabled: ").append(ConfigValues.isDepositSoundEnabled()).append('\n');
        dump.append("withdrawSoundEnabled: ").append(ConfigValues.isWithdrawSoundEnabled()).append('\n');
        dump.append("viewSoundEnabled: ").append(ConfigValues.isViewSoundEnabled()).append('\n');
    }

    private static void appendBank(StringBuilder dump, Bank bank) {
        dump.append("- id: ").append(bank.getIdentifier()).append('\n');
        dump.append("  main: ").append(BankUtils.isMainBank(bank)).append('\n');
        dump.append("  permission: ").append(bank.getAccessPermission()).append('\n');
        dump.append("  giveInterestIfNotAvailable: ").append(bank.isGiveInterestIfNotAvailable()).append('\n');
        dump.append("  levels: ").append(bank.getBankLevels().size()).append('\n');

        List<Integer> levels = new ArrayList<>(bank.getBankLevels().keySet());
        Collections.sort(levels);
        for (int level : levels) {
            dump.append("  level ").append(level).append(":\n");
            dump.append("    cost: ").append(BankUtils.getLevelCost(bank, level)).append('\n');
            dump.append("    capacity: ").append(BankUtils.getCapacity(bank, level)).append('\n');
            dump.append("    maxInterestAmount: ").append(BankUtils.getMaxInterestAmount(bank, level)).append('\n');
            dump.append("    requiredItems: ").append(BankUtils.getRequiredItems(bank, level).size()).append('\n');
            dump.append("    interestLimiter: ").append(BankUtils.getInterestLimiter(bank, level)).append('\n');

            // Base rates from the bank file. Skip player-limited values when the limiter needs a balance.
            if (!ConfigValues.isInterestLimiterEnabled()) {
                dump.append("    interest: ").append(BankUtils.getOnlineInterestRate(bank, null, level)).append('\n');
                dump.append("    offlineInterest: ").append(BankUtils.getOfflineInterestRate(bank, null, level)).append('\n');
                dump.append("    afkInterest: ").append(BankUtils.getAfkInterestRate(bank, null, level)).append('\n');
            }
        }
    }

    private static void appendRecentLogs(StringBuilder dump) {
        String day = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
        File logFile = new File(BankPlus.INSTANCE().getDataFolder(), "logs" + File.separator + day + ".txt");
        if (!logFile.exists()) return;

        dump.append("=== recentLogs (").append(day).append(") ===\n");
        try {
            List<String> lines = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8);
            int start = Math.max(0, lines.size() - 200);
            for (int i = start; i < lines.size(); i++) dump.append(lines.get(i)).append('\n');
        } catch (Exception e) {
            dump.append("Could not read log file: ").append(e.getMessage()).append('\n');
        }
    }

    private static String uploadDump(String content) throws Exception {
        HttpURLConnection con = (HttpURLConnection) URI.create("https://api.mclo.gs/1/log").toURL().openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("User-Agent", "BankPlus/" + BankPlus.INSTANCE().getPluginMeta().getVersion());

        byte[] body = ("content=" + URLEncoder.encode(content, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
        try (OutputStream out = con.getOutputStream()) {
            out.write(body);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
        }

        String raw = response.toString();
        if (raw.contains("\"success\":false") || raw.contains("\"success\": false")) {
            BPLogger.Console.warn("mclo.gs rejected the dump: " + raw);
            return null;
        }

        String key = "\"url\":\"";
        int start = raw.indexOf(key);
        if (start == -1) {
            key = "\"url\": \"";
            start = raw.indexOf(key);
        }
        if (start == -1) return null;

        start += key.length();
        int end = raw.indexOf('"', start);
        if (end == -1) return null;
        return raw.substring(start, end);
    }
}
