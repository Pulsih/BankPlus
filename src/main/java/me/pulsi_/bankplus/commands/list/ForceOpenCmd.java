package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ForceOpenCmd extends BPCommand {

    private final String identifier;

    public ForceOpenCmd(String... aliases) {
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
        Player p = Bukkit.getPlayerExact(args[1]);
        if (p == null) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        String bankName = Values.CONFIG.getMainGuiName();
        if (Values.MULTIPLE_BANKS.isMultipleBanksEnabled()) {
            if (args.length == 2) {
                BPMessages.send(s, "Specify-Bank");
                return false;
            }

            bankName = args[2];
            if (!new BankReader(bankName).exist()) {
                BPMessages.send(s, "Invalid-Bank");
                return false;
            }
        }

        if (confirm(s)) return false;
        BankUtils.openBank(p, bankName, true);
        BPMessages.send(s, "Force-Open", "%player%$" + p.getName(), "%bank%$" + bankName);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!s.hasPermission("bankplus." + identifier)) return null;

        if (Values.MULTIPLE_BANKS.isMultipleBanksEnabled() && args.length == 3) {
            List<String> args2 = new ArrayList<>();
            for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                if (arg.startsWith(args[2].toLowerCase())) args2.add(arg);
            return args2;
        }
        return null;
    }
}