package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DepositCmd extends BPCommand {

    private final String identifier;

    public DepositCmd(String... aliases) {
        super(aliases);
        this.identifier = aliases[0];
    }

    @Override
    public void execute(CommandSender s, String args[]) {
        if (!preExecute(s, args, true, false)) return;

        Player p = (Player) s;

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (args.length == 2) {
                BPMessages.send(s, "Specify-Bank");
                return;
            }

            String bankName = args[2];
            if (!new BankReader(bankName).exist()) {
                BPMessages.send(s, "Invalid-Bank");
                return;
            }

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
                    if (BPMethods.isInvalidNumber(num, s)) return;
                    amount = new BigDecimal(num);
            }
            new MultiEconomyManager(p).deposit(amount, bankName);
        } else {

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
                    if (BPMethods.isInvalidNumber(num, s)) return;
                    amount = new BigDecimal(num);
            }
            new SingleEconomyManager(p).deposit(amount);
        }
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

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled() && args.length == 3) {
            List<String> args2 = new ArrayList<>();
            for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                if (arg.startsWith(args[2].toLowerCase())) args2.add(arg);
            return args2;
        }
        return null;
    }
}