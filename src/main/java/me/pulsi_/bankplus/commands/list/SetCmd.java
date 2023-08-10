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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetCmd extends BPCommand {

    private final String identifier;

    public SetCmd(String... aliases) {
        super(aliases);
        this.identifier = aliases[0];
    }

    @Override
    public void execute(CommandSender s, String args[]) {
        if (!preExecute(s, args, false, false)) return;

        if (args.length == 2) {
            BPMessages.send(s, "Specify-Number");
            return;
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
        if (!p.hasPlayedBefore()) {
            BPMessages.send(s, "Invalid-Player");
            return;
        }

        String num = args[2];

        if (BPMethods.isInvalidNumber(num, s)) return;
        BigDecimal amount = new BigDecimal(num);

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (args.length == 3) {
                BPMessages.send(s, "Specify-Bank");
                return;
            }

            String bankName = args[3];
            if (!new BankReader(bankName).exist()) {
                BPMessages.send(s, "Invalid-Bank");
                return;
            }

            MultiEconomyManager em = new MultiEconomyManager(p);
            boolean silent = args.length > 4 && args[4].toLowerCase().contains("true");

            BigDecimal capacity = new BankReader().getCapacity(p);
            if (amount.doubleValue() >= capacity.doubleValue()) {
                if (!silent) BPMessages.send(s, "Set-Message", BPMethods.placeValues(p, capacity));
                em.setBankBalance(capacity, bankName);
                return;
            }
            em.setBankBalance(amount, bankName);
            if (!silent) BPMessages.send(s, "Set-Message", BPMethods.placeValues(p, amount));

        } else {
            SingleEconomyManager em = new SingleEconomyManager(p);
            boolean silent = args.length > 3 && args[3].toLowerCase().contains("true");

            BigDecimal capacity = new BankReader().getCapacity(p);
            if (amount.doubleValue() >= capacity.doubleValue()) {
                if (!silent) BPMessages.send(s, "Set-Message", BPMethods.placeValues(p, capacity));
                em.setBankBalance(capacity);
                return;
            }
            em.setBankBalance(amount);
            if (!silent) BPMessages.send(s, "Set-Message", BPMethods.placeValues(p, amount));
        }
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!s.hasPermission("bankplus." + identifier)) return null;

        if (args.length == 3) {
            List<String> args2 = new ArrayList<>();
            for (String arg : Arrays.asList("1", "2", "3"))
                if (arg.startsWith(args[2].toLowerCase())) args2.add(arg);
            return args2;
        }

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (args.length == 4) {
                List<String> args3 = new ArrayList<>();
                for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                    if (arg.startsWith(args[3].toLowerCase())) args3.add(arg);
                return args3;
            }

            if (args.length == 5) {
                List<String> args4 = new ArrayList<>();
                for (String arg : Arrays.asList("silent=true", "silent=false"))
                    if (arg.startsWith(args[4].toLowerCase())) args4.add(arg);
                return args4;
            }
        } else {
            if (args.length == 4) {
                List<String> args3 = new ArrayList<>();
                for (String arg : Arrays.asList("silent=true", "silent=false"))
                    if (arg.startsWith(args[3].toLowerCase())) args3.add(arg);
                return args3;
            }
        }
        return null;
    }
}