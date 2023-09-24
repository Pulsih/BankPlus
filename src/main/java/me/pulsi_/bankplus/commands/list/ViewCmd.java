package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPArgs;
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

    public ViewCmd(String... aliases) {
        super(aliases);
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
    public boolean onCommand(CommandSender s, String[] args) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
        if (!p.hasPlayedBefore()) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        BPEconomy economy = BankPlus.getBPEconomy();
        if (args.length == 2) {
            if (s instanceof Player) BPUtils.playSound("VIEW", (Player) s);
            BPMessages.send(s, "Multiple-Bank-Others", BPUtils.placeValues(p, economy.getBankBalance(p)));
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
            BPMessages.send(s, "Bank-Others", BPUtils.placeValues(p, economy.getBankBalance(p, bankName)));
        }
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getBanks(args);
        return null;
    }
}