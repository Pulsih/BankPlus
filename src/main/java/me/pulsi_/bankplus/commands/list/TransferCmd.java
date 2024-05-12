package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class TransferCmd extends BPCommand {

    public TransferCmd(String... aliases) {
        super(aliases);
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
    public boolean onCommand(CommandSender s, String[] args) {
        String mode = args[1].toLowerCase();

        if (!mode.equals("filestodatabase") && !mode.equals("databasetofiles")) {
            BPMessages.send(s, "Invalid-Action");
            return false;
        }
        if (skipToConfirm(s)) return false;

        if (!Values.CONFIG.isSqlEnabled()) {
            BPMessages.send(s, "%prefix% &cCould not initialize the task, MySQL hasn't been enabled in the config file!", true);
            return false;
        }

        if (!BankPlus.INSTANCE().getMySql().isConnected()) {
            BPMessages.send(s, "%prefix% &cCould not initialize the task, MySQL hasn't been connected to it's database yet! &8(Try typing /bp reload)", true);
            return false;
        }

        BPMessages.send(s, "%prefix% &7Task initialized, wait a few moments...", true);
        if (mode.equals("filestodatabase")) filesToDatabase();
        else databaseToFile();

        BPMessages.send(s, "%prefix% &2Task finished!", true);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 1)
            return BPArgs.getArgs(args, "databaseToFiles", "filesToDatabase");
        return null;
    }

    private void filesToDatabase() {
        Set<String> banks = BankPlus.INSTANCE().getBankRegistry().getBanks().keySet();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            BPPlayerManager pManager = new BPPlayerManager(p);
            if (pManager.isPlayerRegistered()) continue;

            FileConfiguration config = pManager.getPlayerConfig();
            SQLPlayerManager sqlManager = new SQLPlayerManager(p);

            for (String bankName : banks) {
                String level = config.getString("banks." + bankName + ".level");
                String money = config.getString("banks." + bankName + ".money");
                String debt = config.getString("banks." + bankName + ".debt");
                String interest = config.getString("banks." + bankName + ".interest");

                sqlManager.setLevel(Integer.parseInt(level == null ? "1" : level), bankName);
                sqlManager.setMoney(new BigDecimal(money == null ? "0" : money), bankName);
                sqlManager.setDebt(new BigDecimal(debt == null ? "0" : debt), bankName);
                sqlManager.setOfflineInterest(new BigDecimal(interest == null ? "0" : interest), bankName);
            }
        }
    }

    private void databaseToFile() {
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            BPPlayerManager pManager = new BPPlayerManager(p);
            if (!pManager.isPlayerRegistered()) continue;

            FileConfiguration config = pManager.getPlayerConfig();
            SQLPlayerManager sqlManager = new SQLPlayerManager(p);

            for (String bankName : BankPlus.INSTANCE().getBankRegistry().getBanks().keySet()) {
                int level = sqlManager.getLevel(bankName);
                String money = sqlManager.getMoney(bankName).toString();
                String debt = sqlManager.getDebt(bankName).toString();
                String interest = sqlManager.getOfflineInterest(bankName).toString();

                config.set("banks." + bankName + ".level", level);
                config.set("banks." + bankName + ".money", money);
                config.set("banks." + bankName + ".debt", debt);
                config.set("banks." + bankName + ".interest", interest);
            }

            pManager.savePlayerFile(config, pManager.getPlayerFile(), true);
        }
    }
}