package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCmdExecution;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class ForceWithdrawCmd extends BPCommand {

    public ForceWithdrawCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public ForceWithdrawCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank forceWithdraw [player] [amount/half/all/custom] [bankName]");
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
    public boolean skipUsage() {
        return false;
    }

    @Override
    public BPCmdExecution onExecution(CommandSender s, String[] args) {
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            BPMessages.sendIdentifier(s, "Invalid-Player");
            return BPCmdExecution.invalidExecution();
        }

        if (args.length == 2) {
            BPMessages.sendIdentifier(s, "Specify-Number");
            return BPCmdExecution.invalidExecution();
        }

        String amount = args[2].toLowerCase();
        if (!amount.equalsIgnoreCase("custom") && BPUtils.isInvalidNumber(amount, s))
            return BPCmdExecution.invalidExecution();

        Bank bank = BankRegistry.getBank(getPossibleBank(args, 3));
        if (!BankUtils.exist(bank)) return BPCmdExecution.invalidExecution();

        if (!BankUtils.isAvailable(bank, target)) {
            BPMessages.sendIdentifier(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
            return BPCmdExecution.invalidExecution();
        }

        return new BPCmdExecution() {
            @Override
            public void execute() {
                BPEconomy economy = bank.getBankEconomy();
                if (amount.equalsIgnoreCase("custom")) {
                    economy.customWithdraw(target);
                    return;
                }

                BigDecimal result, h = BigDecimal.valueOf(100);
                if (!amount.contains("%")) result = new BigDecimal(amount);
                else {
                    BigDecimal percentage = new BigDecimal(amount.replace("%", ""));

                    // If the percentage is <= 0 or > 100 return.
                    if (percentage.compareTo(BigDecimal.ZERO) <= 0 || percentage.compareTo(h) > 0) {
                        BPMessages.sendIdentifier(target, "Invalid-Number");
                        return;
                    }

                    // Start from the player bank balance, then modify it if it's not 100%.
                    result = economy.getBankBalance(target);

                    // Do that only if the % is < 100% to avoid odd numbers bug.
                    if (percentage.compareTo(h) < 0) result = result.divide(h).multiply(percentage);
                }
                economy.withdraw(target, result);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return BPArgs.getOnlinePlayers(args);

        if (args.length == 3)
            return BPArgs.getArgs(args, "1", "2", "3", "10%", "20%", "custom");

        if (args.length == 4)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames(Bukkit.getPlayer(args[1])));

        return null;
    }
}