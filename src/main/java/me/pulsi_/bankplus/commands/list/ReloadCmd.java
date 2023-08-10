package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPMessages;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCmd extends BPCommand {

    public ReloadCmd(String... aliases) {
        super(aliases);
    }

    @Override
    public void execute(CommandSender s, String args[]) {
        if (!preExecute(s, args, false, true)) return;

        boolean reloaded = BankPlus.INSTANCE.getDataManager().reloadPlugin();
        if (reloaded) BPMessages.send(s, "Reload");
        else BPMessages.send(s, "Failed-Reload");
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        return null;
    }
}