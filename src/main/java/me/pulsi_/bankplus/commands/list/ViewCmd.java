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

import java.util.Collections;
import java.util.List;

public class ViewCmd extends BPCommand {

    public ViewCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% &cUsage: &7/bank view [player] <bankName>");
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
        return false;
    }

    @Override
    public boolean skipUsageWarn() {
        return false;
    }

    @Override
    public boolean preCmdChecks(CommandSender s, String[] args) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore()) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        if (args.length > 2) {
            Bank bank = BankUtils.getBank(getPossibleBank(args, 2));
            if (!BankUtils.exist(bank, s)) return false;

            if (!BankUtils.isAvailable(bank, target)) {
                BPMessages.send(s, "Cannot-Access-Bank");
                return false;
            }
        }
        return true;
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (args.length == 2) {
            if (s instanceof Player) BPUtils.playSound("VIEW", (Player) s);

            List<Bank> availableBanks = BankUtils.getAvailableBanks(target);
            if (availableBanks.size() > 1) BPMessages.send(s, "Multiple-Bank-Others", BPUtils.placeValues(target, BPEconomy.getBankBalancesSum(target)));
            else {
                Bank bank = availableBanks.get(0);
                BPMessages.send(s, "Bank-Others", BPUtils.placeValues(target, bank.getBankEconomy().getBankBalance(target), BankUtils.getCurrentLevel(bank, target)));
            }
            return;
        }

        if (s instanceof Player) BPUtils.playSound("VIEW", (Player) s);
        Bank bank = BankUtils.getBank(getPossibleBank(args, 2));
        BPMessages.send(s, "Bank-Others", BPUtils.placeValues(target, bank.getBankEconomy().getBankBalance(target), BankUtils.getCurrentLevel(bank, target)));
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames(Bukkit.getOfflinePlayer(args[1])));
        return null;
    }
}