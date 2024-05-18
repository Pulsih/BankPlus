package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.EconomyUtils;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;

public class SaveAllDataCmd extends BPCommand {

    public SaveAllDataCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
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
    public boolean skipUsageWarn() {
        return true;
    }

    @Override
    public boolean preCmdChecks(CommandSender s, String[] args) {
        return true;
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        EconomyUtils.saveEveryone(true);
        EconomyUtils.restartSavingInterval();

        if (Values.CONFIG.isSaveBalancesBroadcast()) BPLogger.info("All player data have been saved!");
        BPMessages.send(s, "%prefix% &aSuccessfully saved all player data!", true);
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        return null;
    }
}