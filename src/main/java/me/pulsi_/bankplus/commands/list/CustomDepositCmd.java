package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CustomDepositCmd extends BPCommand {

    private final String identifier;

    public CustomDepositCmd(String... aliases) {
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
        BPMethods.customDeposit(p, bankName);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!s.hasPermission("bankplus." + identifier)) return null;

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled() && args.length == 2) {
            List<String> args1 = new ArrayList<>();
            for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                if (arg.startsWith(args[1].toLowerCase())) args1.add(arg);
            return args1;
        }
        return null;
    }
}