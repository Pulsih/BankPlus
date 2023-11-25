package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.mySQL.SQLPlayerManager;
import me.pulsi_.bankplus.utils.BPArgs;
import me.pulsi_.bankplus.utils.BPMessages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

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
        if (confirm(s)) return false;

        switch (mode) {
            case "filestodatabase": {

            }
            break;

            case "databasetofiles": {

            }
        }
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 1)
            return BPArgs.getArgs(args, "databaseToFiles", "filesToDatabase");
        return null;
    }

    private void moveData(CommandSender s) {
        File folder = new File(BankPlus.INSTANCE.getDataFolder(), "playerdata");
        File[] files = folder.listFiles();
        if (files == null) {
            BPMessages.send(s, "%prefix% &cNo files found in playerdata folder!");
            return;
        }

        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            BPPlayerManager pManager = new BPPlayerManager(p);
            if (!pManager.isPlayerRegistered()) continue;

            FileConfiguration config = pManager.getPlayerConfig();
            SQLPlayerManager sqlManager = new SQLPlayerManager(p);

            for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
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
}