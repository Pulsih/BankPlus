package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.EconomyUtils;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SaveAllDataCmd extends BPCommand {

    public SaveAllDataCmd(String... aliases) {
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

        EconomyUtils.saveEveryone(true);
        EconomyUtils.restartSavingInterval();

        if (Values.CONFIG.isSaveBalancesBroadcast()) BPLogger.info("All player data have been saved!");
        BPMessages.send(s, "%prefix% &aSuccessfully saved all player data!", true);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        return null;
    }
}