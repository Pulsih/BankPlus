package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;

public class SetLevelCmd extends BPCommand {

    public SetLevelCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% &cUsage: &7/bank setLevel [level] <bankName>");
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
        OfflinePlayer p = Bukkit.getPlayerExact(args[1]);
        if (!p.hasPlayedBefore()) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        if (args.length == 2) {
            BPMessages.send(s, "Specify-Number");
            return false;
        }

        String level = args[2];
        if (BPUtils.isInvalidNumber(level, s)) return false;

        String bankName = getPossibleBank(args, 3);
        if (!BankUtils.exist(bankName)) return false;

        if (!BankUtils.getLevels(BankUtils.getBank(bankName)).contains(level)) {
            BPMessages.send(s, "Invalid-Bank-Level");
            return false;
        }
        return true;
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        OfflinePlayer p = Bukkit.getPlayerExact(args[1]);

        String level = args[2];
        BankUtils.setLevel(BankUtils.getBank(getPossibleBank(args, 3)), p, Integer.parseInt(level));
        BPMessages.send(s, "Set-Level-Message", "%player%$" + p.getName(), "%level%$" + level);
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 4)
            return BPArgs.getBanks(args);
        return null;
    }
}