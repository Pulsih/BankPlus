package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ForceOpenCmd extends BPCommand {

    public ForceOpenCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% &cUsage: &7/bank forceopen [player] <bankName>");
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
        return false;
    }

    @Override
    public boolean preCmdChecks(CommandSender s, String[] args) {
        if (!ConfigValues.isGuiModuleEnabled()) {
            BPMessages.send(s, "Gui-Module-Disabled");
            return false;
        }

        if (Bukkit.getPlayerExact(args[1]) == null) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        return BankUtils.exist(getPossibleBank(args, 2), s);
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        Player target = Bukkit.getPlayerExact(args[1]);
        String bankName = getPossibleBank(args, 2);

        BankUtils.getBank(bankName).getBankGui().openBankGui(target, true);
        if (!isSilent(args)) BPMessages.send(s, "Force-Open", "%player%$" + target.getName(), "%bank%$" + bankName);
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getBanks(args);

        if (args.length == 4)
            return BPArgs.getArgs(args, "silent=true", "silent=false");
        return null;
    }
}