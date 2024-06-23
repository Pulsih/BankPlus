package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class AddCmd extends BPCommand {

    public AddCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% &cUsage: &7/bank add [player] [amount] <bankName>");
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
        if (!Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        if (args.length == 2) {
            BPMessages.send(s, "Specify-Number");
            return false;
        }

        return !BPUtils.isInvalidNumber(args[2], s) && BankUtils.exist(getPossibleBank(args, 3));
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
        BigDecimal added = BPEconomy.get(getPossibleBank(args, 3)).addBankBalance(p, new BigDecimal(args[2]));

        if (isSilent(args)) return;

        if (added.doubleValue() <= 0D) BPMessages.send(s, "Bank-Full", "%player%$" + p.getName());
        else BPMessages.send(s, "Add-Message", BPUtils.placeValues(p, added));
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 4)
            return BPArgs.getBanks(args);

        if (args.length == 5)
            return BPArgs.getArgs(args, "silent=true", "silent=false");
        return null;
    }
}