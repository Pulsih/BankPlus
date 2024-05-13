package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class BalanceCmd extends BPCommand {

    public BalanceCmd(FileConfiguration commandsConfig, String... aliases) {
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
    public boolean onSuccessExecution(CommandSender s, String[] args) {
        Player p = (Player) s;

        if (args.length == 1) {
            if (hasConfirmed(s)) return false;

            List<Bank> availableBanks = BankUtils.getAvailableBanks(p);
            if (availableBanks.isEmpty()) {
                BPMessages.send(p, "No-Available-Banks");
                return false;
            }

            if (availableBanks.size() > 1) BPMessages.send(p, "Multiple-Personal-Bank", BPUtils.placeValues(p, BPEconomy.getBankBalancesSum(p)));
            else BPMessages.send(p, "Personal-Bank", BPUtils.placeValues(p, BPEconomy.getBankBalancesSum(p), BankUtils.getCurrentLevel(availableBanks.get(0), p)));
        } else {
            String bankName = args[1];
            if (!BankUtils.exist(bankName)) {
                BPMessages.send(s, "Invalid-Bank");
                return false;
            }
            if (hasConfirmed(s)) return false;
            BPMessages.send(p, "Personal-Bank", BPUtils.placeValues(p, BPEconomy.get(bankName).getBankBalance(p), BankUtils.getCurrentLevel(BankUtils.getBank(bankName), p)));
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