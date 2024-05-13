package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class HelpCmd extends BPCommand {

    public HelpCmd(FileConfiguration commandsConfig, String... aliases) {
        super(aliases);
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public boolean skipUsageWarn() {
        return true;
    }

    @Override
    public boolean onSuccessExecution(CommandSender s, String[] args) {
        if (!hasConfirmed(s)) BPMessages.send(s, "Help-Message");
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        return null;
    }
}