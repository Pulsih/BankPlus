package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TransferCmd extends BPCommand {

    public TransferCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Arrays.asList(
                "%prefix% &cUsage: &7/bank transfer [mode] | Specify a mode between &a\"filesToDatabase\" &7and &a\"databaseToFiles\"&7.",
                "&7Use this command to &a&ntransfer&7 the playerdata from a place to another in case you &a&nswitch&7 saving mode."
        );
    }

    @Override
    public int defaultConfirmCooldown() {
        return 5;
    }

    @Override
    public List<String> defaultConfirmMessage() {
        return Collections.singletonList("%prefix% &cThis command will overwrite the data from a place to another, type the command again within 5 seconds to confirm this action.");
    }

    @Override
    public int defaultCooldown() {
        return 0;
    }

    @Override
    public List<String> defaultCooldownMessage() {
        return Collections.emptyList();
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public boolean skipUsageWarn() {
        return false;
    }

    @Override
    public boolean preCmdChecks(CommandSender s, String[] args) {
        String mode = args[1].toLowerCase();

        if (!mode.equals("filestodatabase") && !mode.equals("databasetofiles")) {
            BPMessages.send(s, "Invalid-Action");
            return false;
        }

        if (!Values.CONFIG.isSqlEnabled()) {
            BPMessages.send(s, "%prefix% &cCould not initialize the task, MySQL hasn't been enabled in the config file!", true);
            return false;
        }

        if (!BankPlus.INSTANCE().getMySql().isConnected()) {
            BPMessages.send(s, "%prefix% &cCould not initialize the task, MySQL hasn't been connected to it's database yet! &8(Try typing /bp reload)", true);
            return false;
        }
        return true;
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        BPMessages.send(s, "%prefix% &7Task initialized, wait a few moments...", true);

        if (args[1].toLowerCase().equals("filestodatabase")) filesToDatabase();
        else databaseToFile();

        BPMessages.send(s, "%prefix% &2Task finished!", true);
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 1)
            return BPArgs.getArgs(args, "databaseToFiles", "filesToDatabase");
        return null;
    }

    private void filesToDatabase() {
        Set<String> banks = BPEconomy.nameList();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            SQLPlayerManager sqlManager = new SQLPlayerManager(p);

            for (String bankName : banks) {
                BPEconomy economy = BPEconomy.get(bankName);

                sqlManager.setLevel(economy.getBankLevel(p), bankName);
                sqlManager.setMoney(BPFormatter.getStyledBigDecimal(economy.getBankBalance(p)), bankName);
                sqlManager.setDebt(BPFormatter.getStyledBigDecimal(economy.getDebt(p)), bankName);
                sqlManager.setOfflineInterest(BPFormatter.getStyledBigDecimal(economy.getOfflineInterest(p)), bankName);
            }
        }
    }

    private void databaseToFile() {
        Set<String> banks = BPEconomy.nameList();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            BPPlayerManager pManager = new BPPlayerManager(p);
            if (!pManager.isPlayerRegistered()) continue;

            FileConfiguration config = pManager.getPlayerConfig();
            SQLPlayerManager sqlManager = new SQLPlayerManager(p);

            for (String bankName : banks) {
                int level = sqlManager.getLevel(bankName);
                String money = BPFormatter.styleBigDecimal(sqlManager.getMoney(bankName));
                String debt = BPFormatter.styleBigDecimal(sqlManager.getDebt(bankName));
                String interest = BPFormatter.styleBigDecimal(sqlManager.getOfflineInterest(bankName));

                config.set("banks." + bankName + ".level", level);
                config.set("banks." + bankName + ".money", money);
                config.set("banks." + bankName + ".debt", debt);
                config.set("banks." + bankName + ".interest", interest);
            }

            pManager.savePlayerFile(config, pManager.getPlayerFile(), true);
        }
    }
}