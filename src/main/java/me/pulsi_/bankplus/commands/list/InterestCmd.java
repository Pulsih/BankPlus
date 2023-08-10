package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.CommandSender;

import java.util.List;

public class InterestCmd extends BPCommand {


    public InterestCmd(String... aliases) {
        super(aliases);
    }

    @Override
    public void execute(CommandSender s, String args[]) {
        if (!preExecute(s, args, false, true)) return;

        if (!Values.CONFIG.isInterestEnabled()) {
            BPMessages.send(s, "Interest-Disabled");
            return;
        }
        BPMessages.send(s, "Interest-Time", "%time%$" + BPMethods.formatTime(BankPlus.INSTANCE.getInterest().getInterestCooldownMillis()));
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        return null;
    }
}