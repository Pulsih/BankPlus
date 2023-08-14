package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.BankHolder;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class OpenCmd extends BPCommand {

    private final String identifier;

    public OpenCmd(String... aliases) {
        super(aliases);
        this.identifier = aliases[0];
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

        String bankName = Values.CONFIG.getMainGuiName();
        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (args.length == 1) {
                if (getUsage() != null && !getUsage().equals("")) BPMessages.send(s, getUsage(), true);
                return false;
            }

            bankName = args[1];
            if (!new BankReader(bankName).exist()) {
                BPMessages.send(s, "Invalid-Bank");
                return false;
            }
        }

        if (confirm(s)) return false;
        BankUtils.openBank(p, bankName, false);
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