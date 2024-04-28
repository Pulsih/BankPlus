package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPArgs;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BalanceCmd extends BPCommand {

    public BalanceCmd(String... aliases) {
        super(aliases);
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
    public boolean onCommand(CommandSender s, String[] args) {
        Player p = (Player) s;

        if (args.length == 1) {
            if (skipToConfirm(s)) return false;

            List<String> availableBanks = BankUtils.getAvailableBankNames(p);
            if (availableBanks.isEmpty()) {
                BPMessages.send(p, "No-Available-Banks");
                return false;
            }

            if (availableBanks.size() > 1) BPMessages.send(p, "Multiple-Personal-Bank", BPUtils.placeValues(p, BPEconomy.getBankBalancesSum(p)));
            else {
                String name = availableBanks.get(0);
                BPMessages.send(p, "Personal-Bank", BPUtils.placeValues(p, BPEconomy.getBankBalancesSum(p), BankUtils.getCurrentLevel(name, p)));
            }
        } else {
            String bankName = args[1];
            if (!BankUtils.exist(bankName)) {
                BPMessages.send(s, "Invalid-Bank");
                return false;
            }
            if (skipToConfirm(s)) return false;
            BPMessages.send(p, "Personal-Bank", BPUtils.placeValues(p, BPEconomy.get(bankName).getBankBalance(p), BankUtils.getCurrentLevel(bankName, p)));
        }
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        Player p = (Player) s;

        if (args.length == 2)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames(p));
        return null;
    }
}