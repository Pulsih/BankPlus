package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCmdExecution;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GiveRequiredItemsCmd extends BPCommand {

    public GiveRequiredItemsCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% &cUsage: &7/bank giveRequiredItems <player> <bankName> <bankLevel> [itemName/all]");
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
    public boolean skipUsage() {
        return false;
    }

    @Override
    public BPCmdExecution onExecution(CommandSender s, String[] args) {
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            BPMessages.send(s, "Invalid-Player");
            return BPCmdExecution.invalidExecution();
        }

        Bank bank = BankUtils.getBank(getPossibleBank(args, 2));
        if (!BankUtils.exist(bank, s)) return BPCmdExecution.invalidExecution();

        if (args.length == 3) {
            BPMessages.send(s, "Specify-Number");
            return BPCmdExecution.invalidExecution();
        }

        String levelString = args[3];
        if (BPUtils.isInvalidNumber(levelString, s)) return BPCmdExecution.invalidExecution();

        int level = Integer.parseInt(levelString);
        if (!BankUtils.hasLevel(bank, level)) {
            BPMessages.send(s, "Invalid-Bank-Level");
            return BPCmdExecution.invalidExecution();
        }

        HashMap<String, ItemStack> requiredItems = BankUtils.getRequiredItems(bank, level);
        Set<ItemStack> givenItems = new HashSet<>();

        if (requiredItems.isEmpty()) {
            BPMessages.send(s, "No-Available-Items");
            return BPCmdExecution.invalidExecution();
        }

        if (args.length > 4) {
            String choose = args[4];
            if (choose.equalsIgnoreCase("all")) givenItems.addAll(requiredItems.values());
            else {
                if (!requiredItems.containsKey(choose)) {
                    BPMessages.send(s, "Invalid-Required-Item");
                    return BPCmdExecution.invalidExecution();
                }

                givenItems.add(requiredItems.get(choose));
            }
        }

        return new BPCmdExecution() {
            @Override
            public void execute() {
                for (ItemStack item : requiredItems.values()) target.getInventory().addItem(item);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {

        if (args.length == 2)
            return BPArgs.getOnlinePlayers(args);

        if (args.length == 3)
            return BPArgs.getBanks(args);

        Bank bank = BankUtils.getBank(args[2]);

        if (args.length == 4) {
            List<String> levelsWithItems = new ArrayList<>();
            for (String level : BankUtils.getLevels(bank))
                if (!BankUtils.getRequiredItems(bank, Integer.parseInt(level)).isEmpty()) levelsWithItems.add(level);

            return BPArgs.getArgs(args, levelsWithItems);
        }

        if (args.length == 5) {
            HashMap<String, ItemStack> requiredItems = BankUtils.getRequiredItems(bank, Integer.parseInt(args[3]));
            if (requiredItems.isEmpty()) return null;

            List<String> choose = new ArrayList<>();
            choose.add("all");
            choose.addAll(requiredItems.keySet());

            return BPArgs.getArgs(args, choose);
        }

        return null;
    }
}
