package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ForceUpgradeCmd extends BPCommand {

    private final String identifier;

    public ForceUpgradeCmd(String... aliases) {
        super(aliases);
        this.identifier = aliases[0];
    }

    @Override
    public void execute(CommandSender s, String args[]) {
        if (!preExecute(s, args, false, false)) return;

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            BPMessages.send(s, "Invalid-Player");
            return;
        }

        String bankName = Values.CONFIG.getMainGuiName();
        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (args.length == 2) {
                BPMessages.send(s, "Specify-Bank");
                return;
            }

            bankName = args[2];
            BankReader reader = new BankReader(bankName);
            if (!reader.exist()) {
                BPMessages.send(s, "Invalid-Bank");
                return;
            }
            if (!reader.isAvailable(target)) {
                BPMessages.send(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
                return;
            }
        }

        BankReader reader = new BankReader(bankName);
        reader.upgradeBank(target);
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!(s instanceof Player) || !s.hasPermission("bankplus." + identifier)) return null;
        Player p = (Player) s;

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled() && args.length == 3) {
            List<String> args2 = new ArrayList<>();
            for (String arg : new BankReader().getAvailableBanks(p))
                if (arg.startsWith(args[2].toLowerCase())) args2.add(arg);
            return args2;
        }
        return null;
    }
}