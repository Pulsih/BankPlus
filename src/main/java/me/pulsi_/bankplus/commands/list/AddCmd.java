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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddCmd extends BPCommand {

    private final String identifier;

    public AddCmd(String... aliases) {
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
        OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
        if (!op.hasPlayedBefore()) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        Player p = null;
        if (op.isOnline()) p = Bukkit.getPlayer(op.getUniqueId());

        if (args.length == 2) {
            BPMessages.send(s, "Specify-Number");
            return false;
        }
        String num = args[2];

        if (BPMethods.isInvalidNumber(num, s)) return false;
        BigDecimal amount = new BigDecimal(num);

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
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

            MultiEconomyManager em = p == null ? new MultiEconomyManager(op) : new MultiEconomyManager(p);
            boolean silent = args.length > 4 && args[4].toLowerCase().contains("true");

            BigDecimal capacity = reader.getCapacity(op), balance = em.getBankBalance(bankName);

            if (capacity.doubleValue() > 0d && capacity.subtract(balance).doubleValue() <= 0) {
                if (!silent) BPMessages.send(s, "Bank-Full", "%player%$" + op.getName());
                return true;
            }
            if (balance.add(amount).doubleValue() >= capacity.doubleValue()) {
                if (capacity.doubleValue() > 0d) {
                    if (!silent) BPMessages.send(s, "Add-Message", BPMethods.placeValues(op, capacity.subtract(balance)));
                    em.setBankBalance(capacity, bankName);
                    return true;
                }

                if (!silent) BPMessages.send(s, "Add-Message", BPMethods.placeValues(op, amount));
                em.addBankBalance(amount, bankName);
                return true;
            }
            if (!silent) BPMessages.send(s, "Add-Message", BPMethods.placeValues(op, amount));
            em.addBankBalance(amount, bankName);

        } else {
            if (confirm(s)) return false;

            SingleEconomyManager em = p == null ? new SingleEconomyManager(op) : new SingleEconomyManager(p);
            boolean silent = args.length > 3 && args[3].toLowerCase().contains("true");

            BigDecimal capacity = new BankReader(Values.CONFIG.getMainGuiName()).getCapacity(op), balance = em.getBankBalance();
            if (capacity.doubleValue() > 0d && capacity.subtract(balance).doubleValue() <= 0) {
                if (!silent) BPMessages.send(s, "Bank-Full", "%player%$" + op.getName());
                return true;
            }
            if (balance.add(amount).doubleValue() >= capacity.doubleValue()) {
                if (capacity.doubleValue() > 0d) {
                    if (!silent) BPMessages.send(s, "Add-Message", BPMethods.placeValues(op, capacity.subtract(balance)));
                    em.setBankBalance(capacity);
                    return true;
                }

                if (!silent) BPMessages.send(s, "Add-Message", BPMethods.placeValues(op, amount));
                em.addBankBalance(amount);
                return false;
            }

            if (!silent) BPMessages.send(s, "Add-Message", BPMethods.placeValues(op, amount));
            em.addBankBalance(amount);
        }
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!s.hasPermission("bankplus." + identifier)) return null;

        if (args.length == 3) {
            List<String> args2 = new ArrayList<>();
            for (String arg : Arrays.asList("1", "2", "3"))
                if (arg.startsWith(args[2].toLowerCase())) args2.add(arg);
            return args2;
        }

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            if (args.length == 4) {
                List<String> args3 = new ArrayList<>();
                for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                    if (arg.startsWith(args[3].toLowerCase())) args3.add(arg);
                return args3;
            }

            if (args.length == 5) {
                List<String> args4 = new ArrayList<>();
                for (String arg : Arrays.asList("silent=true", "silent=false"))
                    if (arg.startsWith(args[4].toLowerCase())) args4.add(arg);
                return args4;
            }
        } else {
            if (args.length == 4) {
                List<String> args3 = new ArrayList<>();
                for (String arg : Arrays.asList("silent=true", "silent=false"))
                    if (arg.startsWith(args[3].toLowerCase())) args3.add(arg);
                return args3;
            }
        }
        return null;
    }
}