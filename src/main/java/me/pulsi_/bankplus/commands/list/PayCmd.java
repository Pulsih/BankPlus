package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.MultiEconomyManager;
import me.pulsi_.bankplus.economy.SingleEconomyManager;
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

    private final String identifier;

    public PayCmd(String... aliases) {
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

        if (Values.MULTIPLE_BANKS.isMultipleBanksEnabled()) {
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
            new MultiEconomyManager(payer).pay(target, amount, fromBank, toBank);
        } else {
            if (confirm(s)) return false;
            new SingleEconomyManager(payer).pay(target, amount);
        }
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!(s instanceof Player) || !s.hasPermission("bankplus." + identifier)) return null;
        Player p = (Player) s;

        Player target = args.length > 1 ? Bukkit.getPlayerExact(args[1]) : null;

        if (args.length == 3) {
            List<String> args2 = new ArrayList<>();
            for (String arg : Arrays.asList("1", "2", "3"))
                if (arg.startsWith(args[2].toLowerCase())) args2.add(arg);
            return args2;
        }

        if (Values.MULTIPLE_BANKS.isMultipleBanksEnabled()) {
            if (args.length == 4) {
                List<String> args3 = new ArrayList<>();
                for (String arg : new BankReader().getAvailableBanks(p))
                    if (arg.startsWith(args[3].toLowerCase())) args3.add(arg);
                return args3;
            }

            if (args.length == 5) {
                List<String> banks = new ArrayList<>();
                if (target != null) banks = new BankReader().getAvailableBanks(target);

                List<String> args4 = new ArrayList<>();
                for (String arg : banks)
                    if (arg.startsWith(args[4].toLowerCase())) args4.add(arg);
                return args4;
            }
        }
        return null;
    }
}