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
    public boolean playerOnly() {
        return false;
    }

    @Override
    public boolean skipUsageWarn() {
        return true;
    }

    @Override
    public boolean onCommand(CommandSender s, String[] args) {
        if (skipToConfirm(s)) return false;

        long time = System.currentTimeMillis();
        BPMessages.send(s, "%prefix% &aThe plugin will now try to reload...", true);

        boolean reloaded = BankPlus.INSTANCE().getDataManager().reloadPlugin();
        if (reloaded) BPMessages.send(s, "%prefix% &2Plugin successfully reloaded! &8(&b" + (System.currentTimeMillis() - time) + "ms&8)", true);
        else BPMessages.send(s, "%prefix% &cFailed reloading task, please check the console for more info.", true);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        return null;
    }
}