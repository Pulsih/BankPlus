package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPArgs;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore()) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        if (args.length == 2) {
            if (skipToConfirm(s)) return false;

            if (s instanceof Player) BPUtils.playSound("VIEW", (Player) s);

            List<String> availableBanks = BankUtils.getAvailableBankNames(target);
            if (availableBanks.size() > 1) BPMessages.send(target, "Multiple-Bank-Others", BPUtils.placeValues(target, BPEconomy.getBankBalancesSum(target)));
            else {
                String name = availableBanks.get(0);
                BPMessages.send(s, "Bank-Others", BPUtils.placeValues(target, BPEconomy.get(name).getBankBalance(target), BankUtils.getCurrentLevel(name, target)));
            }
            return true;
        }

        String bankName = args[2];
        if (!BankUtils.exist(bankName)) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }

        if (!BankUtils.isAvailable(bankName, target)) {
            BPMessages.send(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
            return false;
        }

        if (skipToConfirm(s)) return false;
        if (s instanceof Player) BPUtils.playSound("VIEW", (Player) s);
        BPMessages.send(s, "Bank-Others", BPUtils.placeValues(target, BPEconomy.get(bankName).getBankBalance(target), BankUtils.getCurrentLevel(bankName, target)));
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getBanks(args);
        return null;
    }
}