package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class ForceWithdrawCmd extends BPCommand {

    public ForceWithdrawCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% &cUsage: &7/bank forceWithdraw [player] [amount/half/all/custom] <bankName>");
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
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        Bank bank = BankUtils.getBank(getPossibleBank(args, 3));
        if (!BankUtils.exist(getPossibleBank(args, 3))) return false;

        if (!BankUtils.isAvailable(bank, target)) {
            BPMessages.send(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
            return false;
        }

        if (args.length == 2) {
            BPMessages.send(s, "Specify-Number");
            return false;
        }

        String arg2 = args[2].toLowerCase();
        return arg2.equals("custom") || arg2.equals("all") || arg2.equals("half") || !BPUtils.isInvalidNumber(arg2, s);
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        Player target = Bukkit.getPlayerExact(args[1]);
        BPEconomy economy = BPEconomy.get(getPossibleBank(args, 3));

        BigDecimal amount;
        String arg2 = args[2].toLowerCase();
        switch (arg2) {
            case "all":
                amount = economy.getBankBalance(target);
                break;

            case "half":
                amount = economy.getBankBalance(target).divide(BigDecimal.valueOf(2));
                break;

            case "custom":
                economy.customWithdraw(target);
                return;

            default:
                amount = BPFormatter.getStyledBigDecimal(arg2);
        }
        economy.withdraw(target, amount);
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return BPArgs.getOnlinePlayers(args);

        if (args.length == 3)
            return BPArgs.getArgs(args, "1", "2", "3", "half", "all", "custom");

        if (args.length == 4)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames(Bukkit.getPlayer(args[1])));

        return null;
    }
}