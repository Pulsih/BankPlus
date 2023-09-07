package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.MultiEconomyManager;
import me.pulsi_.bankplus.economy.SingleEconomyManager;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WithdrawCmd extends BPCommand {

    private final String identifier;

    public WithdrawCmd(String... aliases) {
        super(aliases);
        this.identifier = aliases[0];
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
    public boolean onCommand(CommandSender s, String args[]) {
        Player p = (Player) s;

        BigDecimal amount;
        switch (args[1]) {
            case "all":
                amount = BigDecimal.valueOf(BankPlus.INSTANCE.getEconomy().getBalance(p));
                break;

            case "half":
                amount = BigDecimal.valueOf(BankPlus.INSTANCE.getEconomy().getBalance(p) / 2);
                break;

            default:
                String num = args[1];
                if (BPUtils.isInvalidNumber(num, s)) return false;
                amount = new BigDecimal(num);
        }

        if (Values.MULTIPLE_BANKS.isMultipleBanksEnabled()) {
            if (args.length == 2) {
                BPMessages.send(s, "Specify-Bank");
                return false;
            }

            String bankName = args[2];
            if (!new BankReader(bankName).exist()) {
                BPMessages.send(s, "Invalid-Bank");
                return false;
            }

            if (confirm(s)) return false;
            new MultiEconomyManager(p).withdraw(amount, bankName);
        } else {

            if (confirm(s)) return false;
            new SingleEconomyManager(p).withdraw(amount);
        }
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!s.hasPermission("bankplus." + identifier)) return null;

        if (args.length == 2) {
            List<String> args1 = new ArrayList<>();
            for (String arg : Arrays.asList("1", "2", "3"))
                if (arg.startsWith(args[1].toLowerCase())) args1.add(arg);
            return args1;
        }

        if (Values.MULTIPLE_BANKS.isMultipleBanksEnabled() && args.length == 3) {
            List<String> args2 = new ArrayList<>();
            for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                if (arg.startsWith(args[2].toLowerCase())) args2.add(arg);
            return args2;
        }
        return null;
    }
}