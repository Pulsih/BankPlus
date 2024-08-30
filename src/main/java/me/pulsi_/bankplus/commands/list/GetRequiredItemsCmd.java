package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetRequiredItemsCmd extends BPCommand {

    public GetRequiredItemsCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% &cUsage: &7/bank getRequiredItems <bankName> <bankLevel> [1,2../all]");
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
        return true;
    }

    @Override
    public boolean skipUsageWarn() {
        return false;
    }

    @Override
    public boolean preCmdChecks(CommandSender s, String[] args) {
        Bank bank = BankUtils.getBank(getPossibleBank(args, 1));
        if (bank == null) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }

        if (args.length == 2) {
            BPMessages.send(s, "Specify-Number");
            return false;
        }

        String level = args[2];
        if (BPUtils.isInvalidNumber(level, s)) return false;

        if (!BankUtils.hasLevel(bank, level)) {
            BPMessages.send(s, "Invalid-Bank-Level");
            return false;
        }

        if (args.length > 3) {
            String choose = args[3];
            if (!choose.equalsIgnoreCase("all")) {
                if (BPUtils.isInvalidNumber(choose, s)) return false;

                int item = Integer.parseInt(choose);
                if (item <= 0 || item > BankUtils.getRequiredItems(bank, Integer.parseInt(level)).size()) {
                    BPMessages.send(s, "Invalid-Number");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        Player p = (Player) s;

        String bankName = args[1], level = args[2];
        List<ItemStack> requiredItems = BankUtils.getRequiredItems(BankUtils.getBank(bankName), Integer.parseInt(level));

        if (args.length == 3) {
            for (ItemStack item : requiredItems) p.getInventory().addItem(item);
            return;
        }

        String choose = args[3];
        if (choose.equalsIgnoreCase("all")) {
            for (ItemStack item : requiredItems) p.getInventory().addItem(item);
            return;
        }

        p.getInventory().addItem(requiredItems.get(Integer.parseInt(choose) - 1));
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {

        if (args.length == 2)
            return BPArgs.getBanks(args);

        if (args.length == 3)
            return BPArgs.getArgs(args, BankUtils.getLevels(BankUtils.getBank(args[1])));

        if (args.length == 4) {
            List<String> choose = new ArrayList<>();
            choose.add("all");

            for (int i = 1; i <= BankUtils.getRequiredItems(BankUtils.getBank(args[1]), Integer.parseInt(args[2])).size(); i++)
                choose.add(i + "");

            return BPArgs.getArgs(args, choose);
        }

        return null;
    }
}
