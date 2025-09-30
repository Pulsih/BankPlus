package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCmdExecution;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class PlaceholdersCmd extends BPCommand {

    public PlaceholdersCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public PlaceholdersCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
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
        return false;
    }

    @Override
    public boolean skipUsage() {
        return true;
    }

    @Override
    public BPCmdExecution onExecution(CommandSender s, String[] args) {
        return new BPCmdExecution() {
            @Override
            public void execute() {
                List<String> placeholders =  BankPlus.INSTANCE().getBpPlaceholders().getRegisteredPlaceholders();
                int size = placeholders.size();

                BPMessages.sendMessage(s, "%prefix% Currently registered placeholders <dark_gray>(<aqua>" + size + "</aqua>)</dark_gray>:");

                StringBuilder builder = new StringBuilder(" <dark_gray>* ");
                for (int i = 0; i < size; i++) {
                    builder.append("<dark_gray>[<green>").append(placeholders.get(i));

                    if (i + 1 >= size) builder.append("<dark_gray>]<gray>.");
                    else builder.append("<dark_gray>]<gray>, ");
                }
                BPMessages.sendMessage(s, builder.toString());
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        Player p = (Player) s;

        if (args.length == 2)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames(p));
        return null;
    }
}