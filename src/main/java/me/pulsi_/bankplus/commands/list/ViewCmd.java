package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class ViewCmd extends BPCommand {

    public ViewCmd(FileConfiguration commandsConfig, String... aliases) {
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
    public boolean onSuccessExecution(CommandSender s, String[] args) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore()) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        if (args.length == 2) {
            if (hasConfirmed(s)) return false;

            if (s instanceof Player) BPUtils.playSound("VIEW", (Player) s);

            List<Bank> availableBanks = BankUtils.getAvailableBanks(target);
            if (availableBanks.size() > 1) BPMessages.send(target, "Multiple-Bank-Others", BPUtils.placeValues(target, BPEconomy.getBankBalancesSum(target)));
            else {
                Bank bank = availableBanks.get(0);
                BPMessages.send(s, "Bank-Others", BPUtils.placeValues(target, bank.getBankEconomy().getBankBalance(target), BankUtils.getCurrentLevel(bank, target)));
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

        if (hasConfirmed(s)) return false;
        if (s instanceof Player) BPUtils.playSound("VIEW", (Player) s);
        Bank bank = BankUtils.getBank(bankName);
        BPMessages.send(s, "Bank-Others", BPUtils.placeValues(target, bank.getBankEconomy().getBankBalance(target), BankUtils.getCurrentLevel(bank, target)));
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getBanks(args);
        return null;
    }
}