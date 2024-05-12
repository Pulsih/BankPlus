package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RestartInterestCmd extends BPCommand {

    public RestartInterestCmd(String... aliases) {
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
        if (!Values.CONFIG.isInterestEnabled()) {
            BPMessages.send(s, "Interest-Disabled");
            return false;
        }
        if (skipToConfirm(s)) return false;
        BankPlus.INSTANCE().getInterest().restartInterest();
        BPMessages.send(s, "%prefix% &aInterest Restarted!", true);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        return null;
    }
}