package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPArgs;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PayCmd extends BPCommand {

    public PayCmd(String... aliases) {
        super(aliases);
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public boolean skipUsageWarn() {
        return false;
    }

    @Override
    public boolean onCommand(CommandSender s, String[] args) {
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || target.equals(s)) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        if (args.length == 2) {
            BPMessages.send(s, "Specify-Number");
            return false;
        }

        String num = args[2];
        if (BPUtils.isInvalidNumber(num, s)) return false;

        BigDecimal amount = new BigDecimal(num);
        Player payer = (Player) s;

        if (args.length == 3 || args.length == 4) {
            BPMessages.send(s, "Specify-Bank");
            return false;
        }

        String fromBank = args[3];
        BankReader fromReader = new BankReader(fromBank);
        if (fromReader.exist()) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }
        if (!fromReader.isAvailable(payer)) {
            BPMessages.send(s, "Cannot-Access-Bank");
            return false;
        }

        String toBank = args[4];
        BankReader toReader = new BankReader(toBank);
        if (toReader.exist()) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }
        if (!toReader.isAvailable(target)) {
            BPMessages.send(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
            return false;
        }

        if (confirm(s)) return false;
        BankPlus.getBPEconomy().pay((Player) s, target, amount, fromBank, toBank);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        Player p = (Player) s;

        Player target = args.length > 1 ? Bukkit.getPlayerExact(args[1]) : null;

        if (args.length == 3)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 4)
            return BPArgs.getArgs(args, new BankReader().getAvailableBanks(p));

        if (args.length == 5)
            return BPArgs.getArgs(args, new BankReader().getAvailableBanks(target));
        return null;
    }
}