package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPArgs;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddAllCmd extends BPCommand {

    public AddAllCmd(String... aliases) {
        super(aliases);
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
    public boolean onCommand(CommandSender s, String args[]) {
        String num = args[1];
        if (BPUtils.isInvalidNumber(num, s)) return false;
        String bankName = args[2];

        BankReader reader = new BankReader(bankName);
        if (!reader.exist()) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }

        if (confirm(s)) return false;
        multiAddAll(s, new ArrayList<>(Bukkit.getOnlinePlayers()), new BankReader(bankName), new BigDecimal(num), bankName);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 3)
            return BPArgs.getBanks(args);
        return null;
    }

    private void multiAddAll(CommandSender s, List<Player> players, BankReader reader, BigDecimal amount, String bankName) {
        if (players.size() == 0) {
            BPMessages.send(s, BPMessages.getPrefix() + " &2Task finished!", true);
            return;
        }

        List<Player> newPlayers = new ArrayList<>(players);

        int count = 1;
        for (Player p : players) {
            if (count >= 30) {
                Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> multiAddAll(s, newPlayers, reader, amount, bankName), 1);
                return;
            }

            BPEconomy economy = BankPlus.getBPEconomy();
            BigDecimal capacity = reader.getCapacity(p), balance = economy.getBankBalance(p, bankName);

            if (capacity.subtract(balance).doubleValue() > 0) {
                if (balance.add(amount).doubleValue() < capacity.doubleValue()) economy.addBankBalance(p, amount, bankName);
                else economy.setBankBalance(p, capacity, bankName);
            }
            count++;
        }
        BPMessages.send(s, BPMessages.getPrefix() + " &2Task finished!", true);
    }
}