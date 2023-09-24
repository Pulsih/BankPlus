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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddCmd extends BPCommand {

    public AddCmd(String... aliases) {
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
    public boolean onCommand(CommandSender s, String[] args) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
        if (!p.hasPlayedBefore()) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        if (args.length == 2) {
            BPMessages.send(s, "Specify-Number");
            return false;
        }
        String num = args[2];

        if (BPUtils.isInvalidNumber(num, s)) return false;
        BigDecimal amount = new BigDecimal(num);

        if (args.length == 3) {
            BPMessages.send(s, "Specify-Bank");
            return false;
        }

        String bankName = args[3];
        BankReader reader = new BankReader(bankName);
        if (!reader.exist()) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }
        if (confirm(s)) return false;

        BPEconomy economy = BankPlus.getBPEconomy();
        boolean silent = args.length > 4 && args[4].toLowerCase().contains("true");

        BigDecimal capacity = reader.getCapacity(p), balance = economy.getBankBalance(p, bankName);

        if (capacity.doubleValue() > 0d && capacity.subtract(balance).doubleValue() <= 0) {
            if (!silent) BPMessages.send(s, "Bank-Full", "%player%$" + p.getName());
            return true;
        }
        if (balance.add(amount).doubleValue() >= capacity.doubleValue()) {
            if (capacity.doubleValue() > 0d) {
                if (!silent) BPMessages.send(s, "Add-Message", BPUtils.placeValues(p, capacity.subtract(balance)));
                economy.setBankBalance(p, capacity, bankName);
                return true;
            }

            if (!silent) BPMessages.send(s, "Add-Message", BPUtils.placeValues(p, amount));
            economy.addBankBalance(p, amount, bankName);
            return true;
        }
        if (!silent) BPMessages.send(s, "Add-Message", BPUtils.placeValues(p, amount));
        economy.addBankBalance(p, amount, bankName);
        return true;
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