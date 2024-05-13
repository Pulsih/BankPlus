package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        if (BPUtils.isInvalidNumber(args[1], s)) return false;

        String bankName = Values.CONFIG.getMainGuiName();
        if (args.length > 2) bankName = args[2];

        if (!BankUtils.exist(bankName)) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }
        return true;
    }

    @Override
    public void onSuccessExecution(CommandSender s, String[] args) {
        String num = args[1];
        BPMessages.send(s, "%prefix% &aSuccessfully added &f" + num + " &amoney to all online players!", true);
        addAll(new ArrayList<>(Bukkit.getOnlinePlayers()), new BigDecimal(num), args[2]);
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 3)
            return BPArgs.getBanks(args);
        return null;
    }

    private void addAll(List<Player> onlinePlayers, BigDecimal amount, String bankName) {
        List<Player> copy = new ArrayList<>(onlinePlayers);

        BPEconomy economy = BPEconomy.get(bankName);
        for (int i = 0; i < 80; i++) {
            if (copy.isEmpty()) return;
            economy.addBankBalance(copy.remove(0), amount);
        }

        if (!onlinePlayers.isEmpty())
            Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> addAll(copy, amount, bankName), 1);
    }
}