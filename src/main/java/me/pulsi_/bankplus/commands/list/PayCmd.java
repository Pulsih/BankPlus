package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCmdExecution;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PayCmd extends BPCommand {

    public PayCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public PayCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Arrays.asList(
                "%prefix% Usage: /bank pay [player] [amount] [fromBankName] [toBankName]",
                "",
                "- fromBankName: The bank from where to take the money.",
                "- toBankName: The bank where to send the money."
        );
    }

    @Override
    public int defaultConfirmCooldown() {
        return 5;
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
        return Collections.singletonList("%prefix% <green>Type again within 5 seconds to confirm your payment.");
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public boolean skipUsage() {
        return false;
    }

    @Override
    public BPCmdExecution onExecution(CommandSender s, String[] args) {
        Player sender = (Player) s, target = Bukkit.getPlayerExact(args[1]);
        if (target == null || target.equals(s)) {
            BPMessages.sendIdentifier(s, "Invalid-Player");
            return BPCmdExecution.invalidExecution();
        }

        if (args.length == 2) {
            BPMessages.sendIdentifier(s, "Specify-Number");
            return BPCmdExecution.invalidExecution();
        }

        String num = args[2];
        if (BPUtils.isInvalidNumber(num, s)) return BPCmdExecution.invalidExecution();

        Bank fromBank = BankRegistry.getBank(getPossibleBank(args, 3));

        if (!BankUtils.exist(fromBank, s)) return BPCmdExecution.invalidExecution();
        if (!BankUtils.isAvailable(fromBank, sender)) {
            BPMessages.sendIdentifier(s, "Cannot-Access-Bank");
            return BPCmdExecution.invalidExecution();
        }

        Bank toBank = BankRegistry.getBank(getPossibleBank(args, 4));

        if (!BankUtils.exist(toBank, s)) return BPCmdExecution.invalidExecution();
        if (!BankUtils.isAvailable(toBank, target)) {
            BPMessages.sendIdentifier(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
            return BPCmdExecution.invalidExecution();
        }

        return new BPCmdExecution() {
            @Override
            public void execute() {
                fromBank.getBankEconomy().pay(sender, target, new BigDecimal(num), toBank);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 4)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames((Player) s));

        if (args.length == 5)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames(Bukkit.getPlayerExact(args[1])));
        return null;
    }
}