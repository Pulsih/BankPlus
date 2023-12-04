package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPArgs;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class UpgradeCmd extends BPCommand {

    public UpgradeCmd(String... aliases) {
        super(aliases);
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public boolean skipUsageWarn() {
        return true;
    }

    @Override
    public boolean onCommand(CommandSender s, String[] args) {
        Player p = (Player) s;

        String bankName = Values.CONFIG.getMainGuiName();
        if (args.length > 1) bankName = args[1];

        if (!BankManager.exist(bankName)) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }

        if (!BankManager.isAvailable(bankName, p)) {
            BPMessages.send(p, "Cannot-Access-Bank");
            return false;
        }

        if (!confirm(s)) BankManager.upgradeBank(bankName, p);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        Player p = (Player) s;

        if (args.length == 2)
            return BPArgs.getArgs(args, BankManager.getAvailableBanks(p));
        return null;
    }
}