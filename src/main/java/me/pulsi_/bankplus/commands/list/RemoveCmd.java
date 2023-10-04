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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.List;

public class RemoveCmd extends BPCommand {

    public RemoveCmd(String... aliases) {
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
        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
        if (!p.hasPlayedBefore()) {
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

        String bankName = Values.CONFIG.getMainGuiName();
        if (args.length > 3) bankName = args[3];

        if (!new BankReader(bankName).exist()) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }

        boolean silent = args.length > 4 && args[4].toLowerCase().contains("true");

        if (confirm(s)) return false;

        BigDecimal removed = BankPlus.getBPEconomy().removeBankBalance(p, amount, bankName);
        if (silent) return true;

        if (removed.doubleValue() <= 0D) BPMessages.send(s, "Bank-Empty", "%player%$" + p.getName());
        else BPMessages.send(s, "Remove-Message", BPUtils.placeValues(p, removed));
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 4)
            return BPArgs.getBanks(args);

        if (args.length == 5)
            return BPArgs.getArgs(args, "silent=true", "silent=false");
        return null;
    }
}