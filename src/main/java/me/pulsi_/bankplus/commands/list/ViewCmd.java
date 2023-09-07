package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.MultiEconomyManager;
import me.pulsi_.bankplus.economy.SingleEconomyManager;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
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
    public boolean playerOnly() {
        return false;
    }

    @Override
    public boolean skipUsageWarn() {
        return false;
    }

    @Override
    public boolean onCommand(CommandSender s, String args[]) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
        if (!p.hasPlayedBefore()) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        if (Values.MULTIPLE_BANKS.isMultipleBanksEnabled()) {
            if (args.length == 2) {
                if (s instanceof Player) BPUtils.playSound("VIEW", (Player) s);
                BPMessages.send(s, "Multiple-Bank-Others", BPUtils.placeValues(p, new MultiEconomyManager(p).getBankBalance()));
            } else {
                String bankName = args[2];
                BankReader bankReader = new BankReader(bankName);
                if (!bankReader.exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return false;
                }

                if (!bankReader.isAvailable(p)) {
                    BPMessages.send(s, "Cannot-Access-Bank-Others", "%player%$" + p.getName());
                    return false;
                }
                if (confirm(s)) return false;
                if (s instanceof Player) BPUtils.playSound("VIEW", (Player) s);
                BPMessages.send(s, "Bank-Others", BPUtils.placeValues(p, new MultiEconomyManager(p).getBankBalance(bankName)));
            }
        } else {
            if (confirm(s)) return false;
            if (s instanceof Player) BPUtils.playSound("VIEW", (Player) s);
            BPMessages.send(s, "Bank-Others", BPUtils.placeValues(p, new SingleEconomyManager(p).getBankBalance()));
        }
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!s.hasPermission("bankplus." + identifier)) return null;

        if (Values.MULTIPLE_BANKS.isMultipleBanksEnabled()) {
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