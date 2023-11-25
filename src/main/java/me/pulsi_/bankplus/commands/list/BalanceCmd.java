package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPArgs;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BalanceCmd extends BPCommand {

    public BalanceCmd(String... aliases) {
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

        BPEconomy economy = BankPlus.getBPEconomy();
        if (args.length == 1) {
            if (confirm(s)) return false;
            BPMessages.send(p, "Multiple-Personal-Bank", BPUtils.placeValues(p, economy.getBankBalance(p)));
        } else {
            String bankName = args[1];
            if (!new BankManager(bankName).exist()) {
                BPMessages.send(s, "Invalid-Bank");
                return false;
            }
            if (confirm(s)) return false;
            BPMessages.send(p, "Personal-Bank", BPUtils.placeValues(p, economy.getBankBalance(p, bankName)));
        }
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        Player p = (Player) s;

        if (args.length == 2)
            return BPArgs.getArgs(args, new BankManager().getAvailableBanks(p));
        return null;
    }
}