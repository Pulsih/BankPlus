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
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ViewCmd extends BPCommand {

    private final String identifier;

    public ViewCmd(String... aliases) {
        super(aliases);
        this.identifier = aliases[0];
    }

    @Override
    public void execute(CommandSender s, String args[]) {
        if (!preExecute(s, args, false, false)) return;

        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
        if (!p.hasPlayedBefore()) {
            BPMessages.send(s, "Invalid-Player");
            return;
        }

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (args.length == 2) {
                if (s instanceof Player) BPMethods.playSound("VIEW", (Player) s);
                BPMessages.send(s, "Multiple-Bank-Others", BPMethods.placeValues(p, new MultiEconomyManager(p).getBankBalance()));
            } else {
                String bankName = args[2];
                BankReader bankReader = new BankReader(bankName);
                if (!bankReader.exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }

                if (s instanceof Player) BPMethods.playSound("VIEW", (Player) s);
                if (!bankReader.isAvailable(p)) {
                    BPMessages.send(s, "Cannot-Access-Bank-Others", "%player%$" + p.getName());
                    return;
                }
                BPMessages.send(s, "Bank-Others", BPMethods.placeValues(p, new MultiEconomyManager(p).getBankBalance(bankName)));
            }
        } else {

            if (s instanceof Player) BPMethods.playSound("VIEW", (Player) s);
            BPMessages.send(s, "Bank-Others", BPMethods.placeValues(p, new SingleEconomyManager(p).getBankBalance()));
        }
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!s.hasPermission("bankplus." + identifier)) return null;

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (args.length == 3) {
                List<String> args2 = new ArrayList<>();
                for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                    if (arg.startsWith(args[2].toLowerCase())) args2.add(arg);
                return args2;
            }
        }
        return null;
    }
}