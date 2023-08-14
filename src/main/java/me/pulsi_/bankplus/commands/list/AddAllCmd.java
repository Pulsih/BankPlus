package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddAllCmd extends BPCommand {

    private final String identifier;

    public AddAllCmd(String... aliases) {
        super(aliases);
        this.identifier = aliases[0];
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
        if (BPMethods.isInvalidNumber(num, s)) return false;

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            String bankName = args[2];

            BankReader reader = new BankReader(bankName);
            if (!reader.exist()) {
                BPMessages.send(s, "Invalid-Bank");
                return false;
            }

            if (confirm(s)) return false;
            multiAddAll(s, new ArrayList<>(Bukkit.getOnlinePlayers()), new BankReader(bankName), new BigDecimal(num), bankName);
        } else {
            if (confirm(s)) return false;
            singleAddAll(s, new ArrayList<>(Bukkit.getOnlinePlayers()), new BankReader(Values.CONFIG.getMainGuiName()), new BigDecimal(num));
        }
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!s.hasPermission("bankplus." + identifier)) return null;

        if (args.length == 2) {
            List<String> args1 = new ArrayList<>();
            for (String arg : Arrays.asList("1", "2", "3"))
                if (arg.startsWith(args[1].toLowerCase())) args1.add(arg);
            return args1;
        }

        if (args.length == 3 && Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            List<String> args2 = new ArrayList<>();
            for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                if (arg.startsWith(args[2].toLowerCase())) args2.add(arg);
            return args2;
        }
        return null;
    }

    private void singleAddAll(CommandSender s, List<Player> players, BankReader reader, BigDecimal amount) {
        if (players.size() == 0) {
            BPMessages.send(s, BPMessages.getPrefix() + " &2Task finished!", true);
            return;
        }

        List<Player> newPlayers = new ArrayList<>(players);

        int count = 1;
        for (Player p : players) {
            if (count >= 30) {
                Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> singleAddAll(s, newPlayers, reader, amount), 1);
                return;
            }

            SingleEconomyManager em = new SingleEconomyManager(p);
            BigDecimal capacity = reader.getCapacity(p), balance = em.getBankBalance();

            if (capacity.subtract(balance).doubleValue() > 0) {
                if (balance.add(amount).doubleValue() >= capacity.doubleValue()) em.setBankBalance(capacity);
                else em.addBankBalance(amount);
            }
            newPlayers.remove(p);
            count++;
        }
        BPMessages.send(s, BPMessages.getPrefix() + " &2Task finished!", true);
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

            MultiEconomyManager em = new MultiEconomyManager(p);
            BigDecimal capacity = reader.getCapacity(p), balance = em.getBankBalance(bankName);

            if (capacity.subtract(balance).doubleValue() > 0) {
                if (balance.add(amount).doubleValue() < capacity.doubleValue()) em.addBankBalance(amount, bankName);
                else em.setBankBalance(capacity, bankName);
            }
            count++;
        }
        BPMessages.send(s, BPMessages.getPrefix() + " &2Task finished!", true);
    }
}