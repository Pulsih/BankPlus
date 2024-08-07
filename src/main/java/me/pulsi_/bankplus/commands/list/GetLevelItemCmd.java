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
import org.bukkit.inventory.ItemStack;


import java.util.Collections;
import java.util.List;

public class GetLevelItemCmd extends BPCommand {

    public GetLevelItemCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% &cUsage: &7/bank get <bankName> <level>");
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

        if (args.length == 3) {
            BPMessages.send(s, "Specify-Number");
            return false;
        }

        String level = args[3];
        if (BPUtils.isInvalidNumber(level, s)) return false;

        String bankName = getPossibleBank(args, 2);
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

        String level = args[3];
        List<ItemStack> itemStack = BankUtils.getRequiredItems(BankUtils.getBank(getPossibleBank(args, 2)), Integer.parseInt(level));
        p.getPlayer().getInventory().addItem(itemStack.toArray(new ItemStack[0]));

    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {

        if (args.length == 3)
            return BPArgs.getBanks(args);

        if (args.length == 4)
            return BPArgs.getArgs(args, "1", "2", "3");

        return null;
    }
}
