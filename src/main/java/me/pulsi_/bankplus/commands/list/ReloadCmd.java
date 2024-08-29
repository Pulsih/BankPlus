package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;

public class ReloadCmd extends BPCommand {

    public ReloadCmd(FileConfiguration commandsConfig, String... aliases) {
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
       long time = System.currentTimeMillis();
        BPMessages.send(s, "%prefix% &aThe plugin will now try to reload...", true);

        boolean reloaded = BankPlus.INSTANCE().getDataManager().reloadPlugin();
        if (reloaded) BPMessages.send(s, "%prefix% &2Plugin successfully reloaded! &8(&b" + (System.currentTimeMillis() - time) + "ms&8)", true);
        else BPMessages.send(s, "%prefix% &cThe plugin may not have fully reloaded due to an error, please check the console for more info.", true);
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        return null;
    }
}