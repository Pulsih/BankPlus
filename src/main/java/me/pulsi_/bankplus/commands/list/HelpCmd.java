package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPMessages;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HelpCmd extends BPCommand {

    public HelpCmd(String... aliases) {
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
    public boolean onCommand(CommandSender s, String[] args) {
        if (!skipToConfirm(s)) BPMessages.send(s, "Help-Message");
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        return null;
    }
}