package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AddAllCmd extends BPCommand {

    public AddAllCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% &cUsage: &7/bank addall [amount] <bankName>");
    }

    @Override
    public int defaultConfirmCooldown() {
        return 5;
    }

    @Override
    public List<String> defaultConfirmMessage() {
        return Collections.singletonList("%prefix% &7Type the command again within 5 seconds to add to every online player (%server_online% players) the specified amount.");
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
        return !BPUtils.isInvalidNumber(args[1], s) && BankUtils.exist(getPossibleBank(args, 2), s);
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        BigDecimal num = new BigDecimal(args[1]);
        BPMessages.send(s, "%prefix% &aSuccessfully added &f" + num + " &amoney to all online players!", true);
        addAll(Bukkit.getOnlinePlayers(), num, BPEconomy.get(getPossibleBank(args, 2)));
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 3)
            return BPArgs.getBanks(args);
        return null;
    }

    /**
     * Method to add to each online player the selected amount, split
     * the task every 80 players to avoid freezing in bigger servers.
     * @param onlinePlayers The initial online players list.
     * @param amount The selected amount to add.
     * @param economy The bank economy where to add the money.
     */
    private void addAll(Collection<? extends Player> onlinePlayers, BigDecimal amount, BPEconomy economy) {
        List<Player> copy = new ArrayList<>(onlinePlayers);

        for (int i = 0; i < 80; i++) {
            if (copy.isEmpty()) return;
            economy.addBankBalance(copy.remove(0), amount);
        }

        Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> addAll(copy, amount, economy), 1);
    }
}