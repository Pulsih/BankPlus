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
    public boolean playerOnly() {
        return false;
    }

    @Override
    public boolean skipUsageWarn() {
        return false;
    }

    @Override
    public boolean onCommand(CommandSender s, String args[]) {
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
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
            BankReader reader = new BankReader(bankName);
            if (!reader.exist()) {
                BPMessages.send(s, "Invalid-Bank");
                return false;
            }
            if (!reader.isAvailable(target)) {
                BPMessages.send(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
                return false;
            }
        }

        if (confirm(s)) return false;
        BankReader reader = new BankReader(bankName);
        reader.upgradeBank(target);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!(s instanceof Player) || !s.hasPermission("bankplus." + identifier)) return null;
        Player p = (Player) s;

        if (Values.MULTIPLE_BANKS.isMultipleBanksEnabled() && args.length == 3) {
            List<String> args2 = new ArrayList<>();
            for (String arg : new BankReader().getAvailableBanks(p))
                if (arg.startsWith(args[2].toLowerCase())) args2.add(arg);
            return args2;
        }
        return null;
    }
}