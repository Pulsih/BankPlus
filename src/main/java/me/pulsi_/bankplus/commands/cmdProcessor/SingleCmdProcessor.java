package me.pulsi_.bankplus.commands.cmdProcessor;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.guis.BanksHolder;
import me.pulsi_.bankplus.guis.BanksManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.BPLogger;
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

public class SingleCmdProcessor {

    public static void processCmd(CommandSender s, String[] args) {
        if (args.length == 0) {
            if (!BPMethods.hasPermission(s, "bankplus.use")) return;
            if (!(s instanceof Player)) {
                MessageManager.send(s, "Help-Message");
                return;
            }

            Player p = (Player) s;
            if (Values.CONFIG.isGuiModuleEnabled()) BanksHolder.openBank(p);
            else {
                MessageManager.send(p, "Personal-Bank", BPMethods.placeValues(p, SingleEconomyManager.getBankBalance(p)));
                BPMethods.playSound("PERSONAL", p);
            }
            return;
        }

        switch (args[0].toLowerCase()) {
            case "customwithdraw": {
                if (!BPMethods.hasPermission(s, "bankplus.customwithdraw")) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Player");
                    return;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    MessageManager.send(s, "Invalid-Player");
                    return;
                }

                BPMethods.customWithdraw(p);
            }
            break;

            case "customdeposit": {
                if (!BPMethods.hasPermission(s, "bankplus.customdeposit")) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Player");
                    return;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    MessageManager.send(s, "Invalid-Player");
                    return;
                }

                BPMethods.customDeposit(p);
            }
            break;

            case "force-open": {
                if (!Values.CONFIG.isGuiModuleEnabled()) {
                    MessageManager.send(s, "Gui-Module-Disabled");
                    return;
                }

                if (!BPMethods.hasPermission(s, "bankplus.force-open")) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Player");
                    return;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    MessageManager.send(s, "Invalid-Player");
                    return;
                }

                if (args.length == 2) {
                    MessageManager.send(s, "Specify-Bank");
                    return;
                }
                String identifier = args[2];

                if (!BanksHolder.bankGetter.containsKey(identifier)) {
                    MessageManager.send(s, "Invalid-Bank");
                    return;
                }
                BanksHolder.openBank(p, identifier);
                MessageManager.send(s, "Force-Open", "%player%$" + p.getName(), "%bank%$", identifier);
            }
            break;

            case "pay": {
                if (!BPMethods.hasPermission(s, "bankplus.pay") || !BPMethods.isPlayer(s)) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    MessageManager.send(s, "Specify-Number");
                    return;
                }

                Player p = (Player) s;
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || target.equals(p)) {
                    MessageManager.send(s, "Invalid-Player");
                    return;
                }

                String num = args[2];
                if (BPMethods.isInvalidNumber(num, s)) return;
                BigDecimal amount = new BigDecimal(num);

                SingleEconomyManager.pay(p, target, amount);
            }
            break;

            case "view": {
                if (!BPMethods.hasPermission(s, "bankplus.view")) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Invalid-Player");
                    return;
                }

                if (s instanceof Player) BPMethods.playSound("VIEW", (Player) s);
                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    MessageManager.send(s, "Bank-Others", BPMethods.placeValues(offlinePlayer, SingleEconomyManager.getBankBalance(offlinePlayer)));
                    return;
                }
                MessageManager.send(s, "Bank-Others", BPMethods.placeValues(p, SingleEconomyManager.getBankBalance(p)));
            }
            break;

            case "bal":
            case "balance": {
                if (!BPMethods.hasPermission(s, "bankplus.balance") || !BPMethods.isPlayer(s)) return;

                Player p = (Player) s;
                MessageManager.send(p, "Personal-Bank", BPMethods.placeValues(p, SingleEconomyManager.getBankBalance(p)));
            }
            break;

            case "withdraw": {
                if (!BPMethods.hasPermission(s, "bankplus.withdraw") || !BPMethods.isPlayer(s)) return;

                Player p = (Player) s;
                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Number");
                    return;
                }

                BigDecimal amount;
                switch (args[1]) {
                    case "all":
                        amount = SingleEconomyManager.getBankBalance(p);
                        SingleEconomyManager.withdraw(p, amount);
                        break;

                    case "half":
                        amount = SingleEconomyManager.getBankBalance(p).divide(BigDecimal.valueOf(2));
                        SingleEconomyManager.withdraw(p, amount);
                        break;

                    default:
                        String num = args[1];
                        if (BPMethods.isInvalidNumber(num, s)) return;
                        amount = new BigDecimal(num);
                        SingleEconomyManager.withdraw(p, amount);
                }
            }
            break;

            case "deposit": {
                if (!BPMethods.hasPermission(s, "bankplus.deposit") || !BPMethods.isPlayer(s)) return;

                Player p = (Player) s;
                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Number");
                    return;
                }

                BigDecimal amount;
                switch (args[1]) {
                    case "all":
                        amount = BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p));
                        SingleEconomyManager.deposit(p, amount);
                        break;

                    case "half":
                        amount = BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p) / 2);
                        SingleEconomyManager.deposit(p, amount);
                        break;

                    default:
                        String num = args[1];
                        if (BPMethods.isInvalidNumber(num, s)) return;
                        amount = new BigDecimal(num);
                        SingleEconomyManager.deposit(p, amount);
                }
            }
            break;

            case "set": {
                if (!BPMethods.hasPermission(s, "bankplus.set")) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    MessageManager.send(s, "Specify-Number");
                    return;
                }

                String num = args[2];
                if (BPMethods.isInvalidNumber(num, s)) return;
                BigDecimal amount = new BigDecimal(num);

                Player p = Bukkit.getPlayerExact(args[1]);
                String bankName = Values.CONFIG.getMainGuiName();
                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);

                    BigDecimal capacity = BanksManager.getCapacity(oP, bankName), balance = SingleEconomyManager.getBankBalance(oP);
                    if (capacity.equals(balance)) {
                        MessageManager.send(s, "Bank-Full", "%player%$" + oP.getName());
                        return;
                    }
                    if (amount.doubleValue() >= capacity.doubleValue()) {
                        MessageManager.send(s, "Set-Message", BPMethods.placeValues(oP, capacity));
                        SingleEconomyManager.setBankBalance(oP, capacity);
                        return;
                    }
                    SingleEconomyManager.setBankBalance(oP, amount);
                    MessageManager.send(s, "Set-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                BigDecimal capacity = BanksManager.getCapacity(p, bankName), balance = SingleEconomyManager.getBankBalance(p);
                if (capacity.equals(balance)) {
                    MessageManager.send(s, "Bank-Full", "%player%$" + p.getName());
                    return;
                }
                if (amount.doubleValue() >= capacity.doubleValue()) {
                    MessageManager.send(s, "Set-Message", BPMethods.placeValues(p, capacity));
                    SingleEconomyManager.setBankBalance(p, capacity);
                    return;
                }
                SingleEconomyManager.setBankBalance(p, amount);
                MessageManager.send(s, "Set-Message", BPMethods.placeValues(p, amount));
            }
            break;

            case "add": {
                if (!BPMethods.hasPermission(s, "bankplus.add")) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    MessageManager.send(s, "Specify-Number");
                    return;
                }

                String num = args[2];
                if (BPMethods.isInvalidNumber(num, s)) return;
                BigDecimal amount = new BigDecimal(num);

                Player p = Bukkit.getPlayerExact(args[1]);
                String bankName = Values.CONFIG.getMainGuiName();
                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);

                    BigDecimal capacity = BanksManager.getCapacity(oP, bankName), balance = SingleEconomyManager.getBankBalance(oP);
                    if (capacity.equals(balance)) {
                        MessageManager.send(s, "Bank-Full", "%player%$" + oP.getName());
                        return;
                    }
                    if (balance.add(amount).doubleValue() >= capacity.doubleValue()) {
                        MessageManager.send(s, "Add-Message", BPMethods.placeValues(oP, capacity.subtract(balance)));
                        SingleEconomyManager.setBankBalance(oP, capacity);
                        return;
                    }
                    SingleEconomyManager.addBankBalance(oP, amount);
                    MessageManager.send(s, "Add-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                BigDecimal capacity = BanksManager.getCapacity(p, bankName), balance = SingleEconomyManager.getBankBalance(p);
                if (capacity.equals(balance)) {
                    MessageManager.send(s, "Bank-Full", "%player%$" + p.getName());
                    return;
                }
                if (balance.add(amount).doubleValue() >= capacity.doubleValue()) {
                    MessageManager.send(s, "Add-Message", BPMethods.placeValues(p, capacity.subtract(balance)));
                    SingleEconomyManager.setBankBalance(p, capacity);
                    return;
                }
                SingleEconomyManager.addBankBalance(p, amount);
                MessageManager.send(s, "Add-Message", BPMethods.placeValues(p, amount));
            }
            break;

            case "remove": {
                if (!BPMethods.hasPermission(s, "bankplus.remove")) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    MessageManager.send(s, "Specify-Number");
                    return;
                }

                String num = args[2];
                if (BPMethods.isInvalidNumber(num, s)) return;
                BigDecimal amount = new BigDecimal(num);

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);

                    BigDecimal balance = SingleEconomyManager.getBankBalance(oP);
                    if (balance.equals(new BigDecimal(0))) {
                        MessageManager.send(s, "Bank-Empty", "%player%$" + oP.getName());
                        return;
                    }
                    if (balance.subtract(amount).doubleValue() <= 0) {
                        MessageManager.send(s, "Remove-Message", BPMethods.placeValues(oP, balance));
                        SingleEconomyManager.setBankBalance(oP, new BigDecimal(0));
                        return;
                    }
                    SingleEconomyManager.removeBankBalance(oP, amount);
                    MessageManager.send(s, "Remove-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                BigDecimal balance = SingleEconomyManager.getBankBalance(p);
                if (balance.equals(new BigDecimal(0))) {
                    MessageManager.send(s, "Bank-Empty", "%player%$" + p.getName());
                    return;
                }
                if (balance.subtract(amount).doubleValue() <= 0) {
                    MessageManager.send(s, "Remove-Message", BPMethods.placeValues(p, balance));
                    SingleEconomyManager.setBankBalance(p, new BigDecimal(0));
                    return;
                }
                SingleEconomyManager.removeBankBalance(p, amount);
                MessageManager.send(s, "Remove-Message", BPMethods.placeValues(p, amount));
            }
            break;

            case "saveallbankbalances": {
                if (!BPMethods.hasPermission(s, "bankplus.saveallbankbalances")) return;
                Bukkit.getOnlinePlayers().forEach(SingleEconomyManager::saveBankBalance);
                MessageManager.send(s, "Balances-Saved");
                if (Values.CONFIG.isSaveBalancesBroadcast()) BPLogger.info("All player balances have been saved!");
            }
            break;

            default:
                MessageManager.send(s, "Unknown-Command");
                break;
        }
    }

    public static List<String> getSingleTabComplete(CommandSender s, String[] args) {
        switch (args.length) {
            case 1: {
                List<String> args1 = new ArrayList<>();
                List<String> listOfArgs = new ArrayList<>();
                if (s.hasPermission("bankplus.add")) listOfArgs.add("add");
                if (s.hasPermission("bankplus.balance")) {
                    listOfArgs.add("balance");
                    listOfArgs.add("bal");
                }
                if (s.hasPermission("bankplus.debug")) listOfArgs.add("debug");
                if (s.hasPermission("bankplus.deposit")) listOfArgs.add("deposit");
                if (s.hasPermission("bankplus.force-open")) listOfArgs.add("force-open");
                if (s.hasPermission("bankplus.give-interest")) listOfArgs.add("giveInterest");
                if (s.hasPermission("bankplus.help")) listOfArgs.add("help");
                if (s.hasPermission("bankplus.interest")) listOfArgs.add("interest");
                if (s.hasPermission("bankplus.interestmillis")) listOfArgs.add("interestMillis");
                if (s.hasPermission("bankplus.pay")) listOfArgs.add("pay");
                if (s.hasPermission("bankplus.reload")) listOfArgs.add("reload");
                if (s.hasPermission("bankplus.remove")) listOfArgs.add("remove");
                if (s.hasPermission("bankplus.restart-interest")) listOfArgs.add("restartInterest");
                if (s.hasPermission("bankplus.saveallbankbalances")) listOfArgs.add("saveAllBankBalances");
                if (s.hasPermission("bankplus.set")) listOfArgs.add("set");
                if (s.hasPermission("bankplus.updatebanktop")) listOfArgs.add("updateBankTop");
                if (s.hasPermission("bankplus.view")) listOfArgs.add("view");
                if (s.hasPermission("bankplus.withdraw")) listOfArgs.add("withdraw");

                for (String arg : listOfArgs) if (arg.startsWith(args[0].toLowerCase())) args1.add(arg);
                return args1;
            }

            case 2: {
                switch (args[0].toLowerCase()) {
                    case "debug": {
                        if (!s.hasPermission("bankplus.debug")) return null;
                        List<String> args2 = new ArrayList<>();
                        List<String> listOfArgs = new ArrayList<>();
                        if (s.hasPermission("bankplus.debug")) {
                            listOfArgs.add("CHAT");
                            listOfArgs.add("DEPOSIT");
                            listOfArgs.add("GUI");
                            listOfArgs.add("INTEREST");
                            listOfArgs.add("WITHDRAW");
                        }
                        for (String arg : listOfArgs)
                            if (arg.toLowerCase().startsWith(args[1].toLowerCase())) args2.add(arg);
                        return args2;
                    }

                    case "deposit": {
                        if (!s.hasPermission("bankplus.deposit")) return null;
                        List<String> args2 = new ArrayList<>();
                        for (String arg : Arrays.asList("1", "2", "3"))
                            if (arg.startsWith(args[1].toLowerCase())) args2.add(arg);
                        return args2;
                    }

                    case "withdraw": {
                        if (!s.hasPermission("bankplus.withdraw")) return null;
                        List<String> args2 = new ArrayList<>();
                        for (String arg : Arrays.asList("1", "2", "3"))
                            if (arg.startsWith(args[1].toLowerCase())) args2.add(arg);
                        return args2;
                    }
                }
            }
            break;

            case 3: {
                switch (args[0].toLowerCase()) {
                    case "force-open": {
                        if (!s.hasPermission("bankplus.force-open")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : new ArrayList<>(BanksManager.getBankNames())) if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }
                }
            }
        }
        return null;
    }
}