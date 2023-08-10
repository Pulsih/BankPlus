package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PayCmd extends BPCommand {

    private final String identifier;

    public PayCmd(String... aliases) {
        super(aliases);
        this.identifier = aliases[0];
    }

    @Override
    public void execute(CommandSender s, String args[]) {
        if (!preExecute(s, args, true, false)) return;

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            BPMessages.send(s, "Invalid-Player");
            return;
        }

        if (args.length == 2) {
            BPMessages.send(s, "Specify-Number");
            return;
        }

        String num = args[2];
        if (BPMethods.isInvalidNumber(num, s)) return;

        BigDecimal amount = new BigDecimal(num);
        Player payer = (Player) s;

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (args.length == 3 || args.length == 4) {
                BPMessages.send(s, "Specify-Bank");
                return;
            }

            String fromBank = args[3];
            BankReader fromReader = new BankReader(fromBank);
            if (fromReader.exist()) {
                BPMessages.send(s, "Invalid-Bank");
                return;
            }
            if (!fromReader.isAvailable(payer)) {
                BPMessages.send(s, "Cannot-Access-Bank");
                return;
            }

            String toBank = args[4];
            BankReader toReader = new BankReader(toBank);
            if (toReader.exist()) {
                BPMessages.send(s, "Invalid-Bank");
                return;
            }
            if (!toReader.isAvailable(target)) {
                BPMessages.send(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
                return;
            }

            new MultiEconomyManager(payer).pay(target, amount, fromBank, toBank);
        } else new SingleEconomyManager(payer).pay(target, amount);
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!(s instanceof Player) || !s.hasPermission("bankplus." + identifier)) return null;
        Player p = (Player) s;

        if (args.length == 3) {
            List<String> args2 = new ArrayList<>();
            for (String arg : Arrays.asList("1", "2", "3"))
                if (arg.startsWith(args[2].toLowerCase())) args2.add(arg);
            return args2;
        }

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (args.length == 4) {
                List<String> args3 = new ArrayList<>();
                for (String arg : new BankReader().getAvailableBanks(p))
                    if (arg.startsWith(args[3].toLowerCase())) args3.add(arg);
                return args3;
            }

            if (args.length == 5) {
                List<String> args4 = new ArrayList<>();
                for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                    if (arg.startsWith(args[4].toLowerCase())) args4.add(arg);
                return args4;
            }
        }
        return null;
    }
}