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

import java.util.Collections;
import java.util.List;

public class SetLevelCmd extends BPCommand {

    public SetLevelCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public SetLevelCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank setLevel [level] [bankName]");
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

        String level = args[2];
        if (BPUtils.isInvalidNumber(level, s)) return BPCmdExecution.invalidExecution();

        Bank bank = BankRegistry.getBank(getPossibleBank(args, 3));
        if (!BankUtils.exist(bank, s)) return BPCmdExecution.invalidExecution();

        if (!BankUtils.hasLevel(bank, level)) {
            BPMessages.sendIdentifier(s, "Invalid-Bank-Level");
            return BPCmdExecution.invalidExecution();
        }

        return new BPCmdExecution() {
            @Override
            public void execute() {
                BankUtils.setLevel(bank, target, Integer.parseInt(level));
                BPMessages.sendIdentifier(s, "Set-Level-Message", "%player%$" + target.getName(), "%level%$" + level);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 4)
            return BPArgs.getBanks(args);
        return null;
    }
}