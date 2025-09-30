package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCmdExecution;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ForceOpenCmd extends BPCommand {

    public ForceOpenCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public ForceOpenCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank forceopen [player] [bankName]");
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
        if (!ConfigValues.isGuiModuleEnabled()) {
            BPMessages.sendIdentifier(s, "Gui-Module-Disabled");
            return BPCmdExecution.invalidExecution();
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            BPMessages.sendIdentifier(s, "Invalid-Player");
            return BPCmdExecution.invalidExecution();
        }

        String bankName = getPossibleBank(args, 2);
        if (!BankUtils.exist(bankName, s)) return BPCmdExecution.invalidExecution();

        return new BPCmdExecution() {
            @Override
            public void execute() {
                BankRegistry.getBank(bankName).getBankGui().openBankGui(target, true);
                if (!isSilent(args)) BPMessages.sendIdentifier(s, "Force-Open", "%player%$" + target.getName(), "%bank%$" + bankName);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getBanks(args);

        if (args.length == 4)
            return BPArgs.getArgs(args, "silent=true", "silent=false");
        return null;
    }
}