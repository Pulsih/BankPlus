package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.loanSystem.LoanUtils;
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

public class LoanCmd extends BPCommand {

    private final String identifier;

    public LoanCmd(String... aliases) {
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

        if (args.length > 1) {
            if (args[1].toLowerCase().equals("accept")) {
                LoanUtils.acceptRequest(p);
                return false;
            }

            if (args[1].toLowerCase().equals("deny")) {
                LoanUtils.denyRequest(p);
                return false;
            }

            if (args[1].toLowerCase().equals("cancel")) {
                LoanUtils.cancelRequest(p);
                return false;
            }
        }

        if (LoanUtils.sentRequest(p)) {
            BPMessages.send(p, "Loan-Already-Sent");
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || target.equals(s)) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        if (args.length == 2) {
            BPMessages.send(p, "Specify-Number");
            return false;
        }

        String num = args[2];
        if (BPMethods.isInvalidNumber(num, p)) return false;
        BigDecimal amount = new BigDecimal(num);

        String from, to;
        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (args.length == 3 || args.length == 4) {
                BPMessages.send(p, "Specify-Bank");
                return false;
            }

            String fromBankName = args[3];
            BankReader fromReader = new BankReader(fromBankName);
            if (!fromReader.exist()) {
                BPMessages.send(p, "Invalid-Bank");
                return false;
            }
            if (!fromReader.isAvailable(p)) {
                BPMessages.send(p, "Cannot-Access-Bank");
                return false;
            }

            String toBankName = args[4];
            BankReader toReader = new BankReader(toBankName);
            if (!toReader.exist()) {
                BPMessages.send(p, "Invalid-Bank");
                return false;
            }
            if (!toReader.isAvailable(target)) {
                BPMessages.send(p, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
                return false;
            }

            from = fromBankName;
            to = toBankName;
        } else {
            from = Values.CONFIG.getMainGuiName();
            to = Values.CONFIG.getMainGuiName();
        }

        if (confirm(s)) return false;
        LoanUtils.sendRequest(p, target, amount, from, to);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!(s instanceof Player) || !s.hasPermission("bankplus." + identifier)) return null;
        Player p = (Player) s;

        Player target = args.length > 1 ? Bukkit.getPlayerExact(args[1]) : null;

        if (args.length == 2) {
            if (LoanUtils.hasRequest(p)) {
                List<String> args1 = new ArrayList<>();
                for (String arg : Arrays.asList("accept", "deny"))
                    if (arg.startsWith(args[1].toLowerCase())) args1.add(arg);
                return args1;
            }
            if (LoanUtils.sentRequest(p)) {
                List<String> args1 = new ArrayList<>();
                for (String arg : Arrays.asList("cancel"))
                    if (arg.startsWith(args[1].toLowerCase())) args1.add(arg);
                return args1;
            }
            return null;
        }

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