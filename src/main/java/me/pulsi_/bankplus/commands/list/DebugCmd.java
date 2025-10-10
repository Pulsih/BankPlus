package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.commands.BPCmdExecution;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.debug.Debug;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;

public class DebugCmd extends BPCommand {

    public DebugCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public DebugCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank debug [bankTop/interest]");
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
        String debugType = args[1].toLowerCase();

        if (!debugType.equals("banktop") && !debugType.equals("interest")) {
            BPMessages.sendIdentifier(s, "Invalid-Action");
            return BPCmdExecution.invalidExecution();
        }

        return new BPCmdExecution() {
            @Override
            public void execute() {
                switch (debugType) {
                    case "banktop":
                        Debug.debugBankTop(s);
                        break;
                    case "interest":
                        // Debug interest.
                        break;
                }
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return BPArgs.getArgs(args, "bankTop", "interest");

        return null;
    }
}