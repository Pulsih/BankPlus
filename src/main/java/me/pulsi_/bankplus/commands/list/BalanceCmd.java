package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.economy.MultiEconomyManager;
import me.pulsi_.bankplus.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BalanceCmd extends BPCommand {

    private final String identifier;

    public BalanceCmd(String... aliases) {
        super(aliases);
        identifier = aliases[0];
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
    public boolean onCommand(CommandSender s, String args[]) {
        Player p = (Player) s;

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            MultiEconomyManager em = new MultiEconomyManager(p);
            if (args.length == 1) {
                if (confirm(s)) return false;
                BPMessages.send(p, "Multiple-Personal-Bank", BPMethods.placeValues(p, em.getBankBalance()));
            } else {
                String bankName = args[1];
                if (!new BankReader(bankName).exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return false;
                }
                if (confirm(s)) return false;
                BPMessages.send(p, "Personal-Bank", BPMethods.placeValues(p, em.getBankBalance(bankName)));
            }
        } else {
            if (confirm(s)) return false;
            BPMessages.send(p, "Personal-Bank", BPMethods.placeValues(p, new SingleEconomyManager(p).getBankBalance()));
        }
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!(s instanceof Player) || !s.hasPermission("bankplus." + identifier)) return null;
        Player p = (Player) s;

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled() && args.length == 2) {
            List<String> args1 = new ArrayList<>();
            for (String arg : new BankReader().getAvailableBanks(p))
                if (arg.startsWith(args[1].toLowerCase())) args1.add(arg);
            return args1;
        }
        return null;
    }
}