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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class RemoveCmd extends BPCommand {

    public RemoveCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public RemoveCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank remove [player] [amount] [bankName]");
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
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore()) {
            BPMessages.sendIdentifier(s, "Invalid-Player");
            return BPCmdExecution.invalidExecution();
        }

        if (args.length == 2) {
            BPMessages.sendIdentifier(s, "Specify-Number");
            return BPCmdExecution.invalidExecution();
        }
        String amount = args[2];

        if (BPUtils.isInvalidNumber(amount, s)) return BPCmdExecution.invalidExecution();

        Bank bank = BankRegistry.getBank(getPossibleBank(args, 3));
        if (!BankUtils.exist(bank, s)) return BPCmdExecution.invalidExecution();

        return new BPCmdExecution() {
            @Override
            public void execute() {
                BigDecimal removed = bank.getBankEconomy().removeBankBalance(target, new BigDecimal(amount));

                if (isSilent(args)) return;
                if (removed.compareTo(BigDecimal.ZERO) <= 0) BPMessages.sendIdentifier(s, "Bank-Empty", "%player%$" + target.getName());
                else BPMessages.sendIdentifier(s, "Remove-Message", BPUtils.placeValues(target, removed));
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 4)
            return BPArgs.getBanks(args);

        if (args.length == 5)
            return BPArgs.getArgs(args, "silent=true", "silent=false");
        return null;
    }
}