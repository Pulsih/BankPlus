package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
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
    public void execute(CommandSender s, String args[]) {
        if (!preExecute(s, args, true, true)) return;

        Player p = (Player) s;

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            MultiEconomyManager em = new MultiEconomyManager(p);
            if (args.length == 1)
                BPMessages.send(p, "Multiple-Personal-Bank", BPMethods.placeValues(p, em.getBankBalance()));
            else {
                String bankName = args[1];
                if (!new BankReader(bankName).exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }
                BPMessages.send(p, "Personal-Bank", BPMethods.placeValues(p, em.getBankBalance(bankName)));
            }
        } else {

            BPMessages.send(p, "Personal-Bank", BPMethods.placeValues(p, new SingleEconomyManager(p).getBankBalance()));
        }
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