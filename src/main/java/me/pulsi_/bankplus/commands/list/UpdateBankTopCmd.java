package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPMessages;
import org.bukkit.command.CommandSender;

import java.util.List;

public class UpdateBankTopCmd extends BPCommand {

    public UpdateBankTopCmd(String... aliases) {
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
        if (confirm(s)) return false;

        BankPlus.INSTANCE().getBankTopManager().updateBankTop();
        BPMessages.send(s, "BankTop-Updated");
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        return null;
    }
}