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

import java.util.Collections;
import java.util.List;

public class BalanceCmd extends BPCommand {

    public BalanceCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.emptyList();
    }

    @Override
    public int defaultConfirmCooldown() {
        return 0;
    }

    @Override
    public List<String> defaultConfirmMessage() {
        return Collections.emptyList();
    }

    @Override
    public int defaultCooldown() {
        return 0;
    }

    @Override
    public List<String> defaultCooldownMessage() {
        return Collections.emptyList();
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
    public boolean preCmdChecks(CommandSender s, String[] args) {
        Player p = (Player) s;

        if (args.length == 1) {
            List<Bank> availableBanks = BankUtils.getAvailableBanks(p);
            if (availableBanks.isEmpty()) {
                BPMessages.send(p, "No-Available-Banks");
                return false;
            }
        } else {
            Bank bank = BankUtils.getBank(args[1]);
            if (bank == null) {
                BPMessages.send(s, "Invalid-Bank");
                return false;
            }
            if (!BankUtils.isAvailable(bank, p)) {
                BPMessages.send(s, "Invalid-Bank");
                return false;
            }
        }
        return true;
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        Player p = (Player) s;
        if (args.length == 1) {
            List<Bank> availableBanks = BankUtils.getAvailableBanks(p);
            if (availableBanks.size() > 1) BPMessages.send(p, "Multiple-Personal-Bank", BPUtils.placeValues(p, BPEconomy.getBankBalancesSum(p)));
            else BPMessages.send(p, "Personal-Bank", BPUtils.placeValues(p, BPEconomy.getBankBalancesSum(p), BankUtils.getCurrentLevel(availableBanks.get(0), p)));
        } else {
            Bank bank = BankUtils.getBank(args[1]);
            BPMessages.send(p, "Personal-Bank", BPUtils.placeValues(p, bank.getBankEconomy().getBankBalance(p), BankUtils.getCurrentLevel(bank, p)));
        }
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2) return BPArgs.getArgs(args, BankUtils.getAvailableBankNames((Player) s));
        return null;
    }
}