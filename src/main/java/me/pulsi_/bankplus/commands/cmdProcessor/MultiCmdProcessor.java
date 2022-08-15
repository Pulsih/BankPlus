package me.pulsi_.bankplus.commands.cmdProcessor;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayerFiles;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.bankGuis.BanksHolder;
import me.pulsi_.bankplus.bankGuis.BanksManager;
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

public class MultiCmdProcessor {

    public static void processCmd(CommandSender s, String[] args) {

        BanksHolder banksHolder = new BanksHolder();
        MultiEconomyManager multiEconomyManager = null;
        if (s instanceof Player) multiEconomyManager = new MultiEconomyManager((Player) s);

        if (args.length == 0) {
            if (!BPMethods.hasPermission(s, "bankplus.use")) return;
            if (!(s instanceof Player)) {
                MessageManager.send(s, "Help-Message");
                return;
            }

            Player p = (Player) s;
            if (Values.CONFIG.isGuiModuleEnabled()) banksHolder.openBank(p, "MultipleBanksGui");
            else {
                MessageManager.send(p, "Multiple-Personal-Bank", BPMethods.placeValues(p, multiEconomyManager.getBankBalance()));
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
                if (args.length == 2) {
                    MessageManager.send(s, "Specify-Bank");
                    return;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    MessageManager.send(s, "Invalid-Player");
                    return;
                }

                String bankName = args[2];
                if (!new BanksManager().exist(bankName)) {
                    MessageManager.send(s, "Invalid-Bank");
                    return;
                }

                BPMethods.customWithdraw(p, bankName);
            }
            break;

            case "customdeposit": {
                if (!BPMethods.hasPermission(s, "bankplus.customdeposit")) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    MessageManager.send(s, "Specify-Bank");
                    return;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    MessageManager.send(s, "Invalid-Player");
                    return;
                }

                String bankName = args[2];
                if (!new BanksManager().exist(bankName)) {
                    MessageManager.send(s, "Invalid-Bank");
                    return;
                }

                BPMethods.customDeposit(p, bankName);
            }
            break;

            case "force-open": {
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

                if (!new BanksManager().exist(identifier)) {
                    MessageManager.send(s, "Invalid-Bank");
                    return;
                }
                banksHolder.openBank(p, identifier);
                MessageManager.send(s, "Force-Open", "%player%$" + p.getName());
            }
            break;

            case "open": {
                if (!BPMethods.isPlayer(s) || !BPMethods.hasPermission(s, "bankplus.open")) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Bank");
                    return;
                }
                banksHolder.openBank((Player) s, args[1]);
            }
            break;

            case "pay": {
                if (!BPMethods.hasPermission(s, "bankplus.pay") || !BPMethods.isPlayer(s)) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2 || args.length == 3) {
                    MessageManager.send(s, "Specify-Bank");
                    return;
                }
                if (args.length == 4) {
                    MessageManager.send(s, "Specify-Number");
                    return;
                }

                Player p = (Player) s;
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || target.equals(p)) {
                    MessageManager.send(s, "Invalid-Player");
                    return;
                }

                String fromBank = args[2];
                if (new BanksManager().exist(fromBank)) {
                    MessageManager.send(s, "Invalid-Bank");
                    return;
                }
                String toBank = args[3];
                if (new BanksManager().exist(toBank)) {
                    MessageManager.send(s, "Invalid-Bank");
                    return;
                }

                String num = args[4];
                if (BPMethods.isInvalidNumber(num, s)) return;
                BigDecimal amount = new BigDecimal(num);

                multiEconomyManager.pay(fromBank, target, amount, toBank);
            }
            break;

            case "view": {
                if (!BPMethods.hasPermission(s, "bankplus.view")) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Invalid-Player");
                    return;
                }

                if (args.length == 2) {
                    if (s instanceof Player) BPMethods.playSound("VIEW", (Player) s);
                    Player p = Bukkit.getPlayerExact(args[1]);
                    if (p == null) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                        MessageManager.send(s, "Multiple-Bank-Others", BPMethods.placeValues(offlinePlayer, new MultiEconomyManager(offlinePlayer).getBankBalance()));
                        return;
                    }
                    MessageManager.send(s, "Multiple-Bank-Others", BPMethods.placeValues(p, new MultiEconomyManager(p).getBankBalance()));
                } else {
                    String bankName = args[2];
                    if (new BanksManager().exist(bankName)) {
                        MessageManager.send(s, "Invalid-Bank");
                        return;
                    }
                    if (s instanceof Player) BPMethods.playSound("VIEW", (Player) s);
                    Player p = Bukkit.getPlayerExact(args[1]);
                    if (p == null) {
                        OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);
                        if (!new BanksManager(bankName).isAvailable(oP)) {
                            MessageManager.send(s, "Cannot-Access-Bank-Others", "%player%$" + oP.getName());
                            return;
                        }
                        MessageManager.send(s, "Bank-Others", BPMethods.placeValues(oP, new MultiEconomyManager(oP).getBankBalance(bankName)));
                        return;
                    }
                    if (!new BanksManager(bankName).isAvailable(p)) {
                        MessageManager.send(s, "Cannot-Access-Bank-Others", "%player%$" + p.getName());
                        return;
                    }
                    MessageManager.send(s, "Bank-Others", BPMethods.placeValues(p, new MultiEconomyManager(p).getBankBalance(bankName)));
                }
            }
            break;

            case "bal":
            case "balance": {
                if (!BPMethods.hasPermission(s, "bankplus.balance") || !BPMethods.isPlayer(s)) return;

                Player p = (Player) s;
                if (args.length == 1)
                    MessageManager.send(p, "Multiple-Personal-Bank", BPMethods.placeValues(p, multiEconomyManager.getBankBalance()));
                else {
                    String bankName = args[1];
                    if (!new BanksManager().exist(bankName)) {
                        MessageManager.send(s, "Invalid-Bank");
                        return;
                    }
                    MessageManager.send(p, "Personal-Bank", BPMethods.placeValues(p, multiEconomyManager.getBankBalance(bankName)));
                }
            }
            break;

            case "setlevel": {
                if (!BPMethods.hasPermission(s, "bankplus.setlevel")) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    MessageManager.send(s, "Specify-Bank");
                    return;
                }
                if (args.length == 3) {
                    MessageManager.send(s, "Specify-Number");
                    return;
                }

                String bankName = args[2];
                BanksManager banksManager = new BanksManager(bankName);

                if (!banksManager.exist(bankName)) {
                    MessageManager.send(s, "Invalid-Bank");
                    return;
                }

                String level = args[3];
                if (BPMethods.isInvalidNumber(level, s)) return;
                if (!banksManager.getLevels().contains(level)) {
                    MessageManager.send(s, "Invalid-Bank-Level");
                    return;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                BankPlusPlayerFiles files;
                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);
                    files = new BankPlusPlayerFiles(oP);
                    files.getPlayerConfig().set("Banks." + bankName + ".Level", Integer.valueOf(level));
                    MessageManager.send(s, "Set-Level-Message", "%player%$" + oP.getName(), "%level%$" + level);
                } else {
                    files = new BankPlusPlayerFiles(p);
                    files.getPlayerConfig().set("Banks." + bankName + ".Level", Integer.valueOf(level));
                    MessageManager.send(s, "Set-Level-Message", "%player%$" + p.getName(), "%level%$" + level);
                }
                files.savePlayerFile(true);
            }
            break;

            case "withdraw": {
                if (!BPMethods.hasPermission(s, "bankplus.withdraw") || !BPMethods.isPlayer(s)) return;

                if (args.length == 1) {
                    MessageManager.send(s, "Specify-Number");
                    return;
                }
                if (args.length == 2) {
                    MessageManager.send(s, "Specify-Bank");
                    return;
                }
                String bankName = args[2];
                if (!new BanksManager().exist(bankName)) {
                    MessageManager.send(s, "Invalid-Bank");
                    return;
                }

                BigDecimal amount;
                switch (args[1]) {
                    case "all":
                        amount = multiEconomyManager.getBankBalance();
                        multiEconomyManager.withdraw(amount, bankName);
                        break;

                    case "half":
                        amount = multiEconomyManager.getBankBalance().divide(BigDecimal.valueOf(2));
                        multiEconomyManager.withdraw(amount, bankName);
                        break;

                    default:
                        String num = args[1];
                        if (BPMethods.isInvalidNumber(num, s)) return;
                        amount = new BigDecimal(num);
                        multiEconomyManager.withdraw(amount, bankName);
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
                if (args.length == 2) {
                    MessageManager.send(s, "Specify-Bank");
                    return;
                }
                String bankName = args[2];
                if (!new BanksManager().exist(bankName)) {
                    MessageManager.send(s, "Invalid-Bank");
                    return;
                }

                BigDecimal amount;
                switch (args[1]) {
                    case "all":
                        amount = BigDecimal.valueOf(BankPlus.instance().getEconomy().getBalance(p));
                        multiEconomyManager.deposit(amount, bankName);
                        break;

                    case "half":
                        amount = BigDecimal.valueOf(BankPlus.instance().getEconomy().getBalance(p) / 2);
                        multiEconomyManager.deposit(amount, bankName);
                        break;

                    default:
                        String num = args[1];
                        if (BPMethods.isInvalidNumber(num, s)) return;
                        amount = new BigDecimal(num);
                        multiEconomyManager.deposit(amount, bankName);
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
                    MessageManager.send(s, "Specify-Bank");
                    return;
                }
                if (args.length == 3) {
                    MessageManager.send(s, "Specify-Number");
                    return;
                }

                String bankName = args[2];
                if (!new BanksManager().exist(bankName)) {
                    MessageManager.send(s, "Invalid-Bank");
                    return;
                }

                String num = args[3];
                if (BPMethods.isInvalidNumber(num, s)) return;
                BigDecimal amount = new BigDecimal(num);
                BanksManager banksManager = new BanksManager(bankName);

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);

                    BigDecimal capacity = banksManager.getCapacity(oP), balance = new MultiEconomyManager(oP).getBankBalance(bankName);
                    if (capacity.equals(balance)) {
                        MessageManager.send(s, "Bank-Full", "%player%$" + oP.getName());
                        return;
                    }
                    if (amount.doubleValue() >= capacity.doubleValue()) {
                        MessageManager.send(s, "Set-Message", BPMethods.placeValues(oP, capacity));
                        new MultiEconomyManager(oP).setBankBalance(capacity, bankName);
                        return;
                    }
                    new MultiEconomyManager(oP).setBankBalance(amount, bankName);
                    MessageManager.send(s, "Set-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                BigDecimal capacity = banksManager.getCapacity(p), balance = new MultiEconomyManager(p).getBankBalance(bankName);
                if (capacity.equals(balance)) {
                    MessageManager.send(s, "Bank-Full", "%player%$" + p.getName());
                    return;
                }
                if (amount.doubleValue() >= capacity.doubleValue()) {
                    MessageManager.send(s, "Set-Message", BPMethods.placeValues(p, capacity));
                    new MultiEconomyManager(p).setBankBalance(capacity, bankName);
                    return;
                }
                new MultiEconomyManager(p).setBankBalance(amount, bankName);
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
                    MessageManager.send(s, "Specify-Bank");
                    return;
                }
                if (args.length == 3) {
                    MessageManager.send(s, "Specify-Number");
                    return;
                }

                String bankName = args[2];
                if (!new BanksManager().exist(bankName)) {
                    MessageManager.send(s, "Invalid-Bank");
                    return;
                }

                String num = args[3];
                if (BPMethods.isInvalidNumber(num, s)) return;
                BigDecimal amount = new BigDecimal(num);
                BanksManager banksManager = new BanksManager(bankName);

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);

                    BigDecimal capacity = banksManager.getCapacity(oP), balance = new MultiEconomyManager(oP).getBankBalance(bankName);
                    if (capacity.equals(balance)) {
                        MessageManager.send(s, "Bank-Full", "%player%$" + oP.getName());
                        return;
                    }
                    if (balance.add(amount).doubleValue() >= capacity.doubleValue()) {
                        MessageManager.send(s, "Add-Message", BPMethods.placeValues(oP, capacity.subtract(balance)));
                        new MultiEconomyManager(oP).setBankBalance(capacity, bankName);
                        return;
                    }
                    new MultiEconomyManager(oP).addBankBalance(amount, bankName);
                    MessageManager.send(s, "Add-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                BigDecimal capacity = banksManager.getCapacity(p), balance = new MultiEconomyManager(p).getBankBalance(bankName);
                if (capacity.equals(balance)) {
                    MessageManager.send(s, "Bank-Full", "%player%$" + p.getName());
                    return;
                }
                if (balance.add(amount).doubleValue() >= capacity.doubleValue()) {
                    MessageManager.send(s, "Add-Message", BPMethods.placeValues(p, capacity.subtract(balance)));
                    new MultiEconomyManager(p).setBankBalance(capacity, bankName);
                    return;
                }
                new MultiEconomyManager(p).addBankBalance(amount, bankName);
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
                    MessageManager.send(s, "Specify-Bank");
                    return;
                }
                if (args.length == 3) {
                    MessageManager.send(s, "Specify-Number");
                    return;
                }

                String bankName = args[2];
                if (!new BanksManager().exist(bankName)) {
                    MessageManager.send(s, "Invalid-Bank");
                    return;
                }

                String num = args[3];
                if (BPMethods.isInvalidNumber(num, s)) return;
                BigDecimal amount = new BigDecimal(num);

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);

                    BigDecimal balance = new MultiEconomyManager(oP).getBankBalance(bankName);
                    if (balance.equals(new BigDecimal(0))) {
                        MessageManager.send(s, "Bank-Empty", "%player%$" + oP.getName());
                        return;
                    }
                    if (balance.subtract(amount).doubleValue() <= 0) {
                        MessageManager.send(s, "Remove-Message", BPMethods.placeValues(oP, balance));
                        new MultiEconomyManager(oP).setBankBalance(new BigDecimal(0), bankName);
                        return;
                    }
                    new MultiEconomyManager(oP).removeBankBalance(amount, bankName);
                    MessageManager.send(s, "Remove-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                BigDecimal balance = new MultiEconomyManager(p).getBankBalance(bankName);
                if (balance.equals(new BigDecimal(0))) {
                    MessageManager.send(s, "Bank-Empty", "%player%$" + p.getName());
                    return;
                }
                if (balance.subtract(amount).doubleValue() <= 0) {
                    MessageManager.send(s, "Remove-Message", BPMethods.placeValues(p, balance));
                    new MultiEconomyManager(p).setBankBalance(new BigDecimal(0), bankName);
                    return;
                }
                new MultiEconomyManager(p).removeBankBalance(amount, bankName);
                MessageManager.send(s, "Remove-Message", BPMethods.placeValues(p, amount));
            }
            break;

            case "saveallbankbalances": {
                if (!BPMethods.hasPermission(s, "bankplus.saveallbankbalances")) return;
                Bukkit.getOnlinePlayers().forEach(p -> new MultiEconomyManager(p).saveBankBalance());
                MessageManager.send(s, "Balances-Saved");
                if (Values.CONFIG.isSaveBalancesBroadcast()) BPLogger.info("All player balances have been saved!");
            }
            break;

            default:
                MessageManager.send(s, "Unknown-Command");
                break;
        }
    }

    public static List<String> getMultiTabComplete(CommandSender s, String[] args) {
        String a0 = args[0].toLowerCase();
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
                if (s.hasPermission("bankplus.open")) listOfArgs.add("open");
                if (s.hasPermission("bankplus.pay")) listOfArgs.add("pay");
                if (s.hasPermission("bankplus.reload")) listOfArgs.add("reload");
                if (s.hasPermission("bankplus.remove")) listOfArgs.add("remove");
                if (s.hasPermission("bankplus.restart-interest")) listOfArgs.add("restartInterest");
                if (s.hasPermission("bankplus.saveallbankbalances")) listOfArgs.add("saveAllBankBalances");
                if (s.hasPermission("bankplus.set")) listOfArgs.add("set");
                if (s.hasPermission("bankplus.setlevel")) listOfArgs.add("setlevel");
                if (s.hasPermission("bankplus.updatebanktop")) listOfArgs.add("updateBankTop");
                if (s.hasPermission("bankplus.view")) listOfArgs.add("view");
                if (s.hasPermission("bankplus.withdraw")) listOfArgs.add("withdraw");

                for (String arg : listOfArgs) if (arg.startsWith(a0)) args1.add(arg);
                return args1;
            }

            case 2: {
                switch (a0) {
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

                    case "open": {
                        if (!s.hasPermission("bankplus.open")) return null;
                        List<String> args2 = new ArrayList<>();
                        for (String arg : BankPlus.instance().getBanks().keySet())
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
                switch (a0) {
                    case "add": {
                        if (!s.hasPermission("bankplus.add")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.instance().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "bal":
                    case "balance": {
                        if (!s.hasPermission("bankplus.balance")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.instance().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "deposit": {
                        if (!s.hasPermission("bankplus.deposit")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.instance().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "force-open": {
                        if (!s.hasPermission("bankplus.force-open")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.instance().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "pay": {
                        if (!BPMethods.isPlayer(s) || !s.hasPermission("bankplus.pay")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.instance().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "remove": {
                        if (!s.hasPermission("bankplus.remove")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.instance().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "set": {
                        if (!s.hasPermission("bankplus.set")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.instance().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "setlevel": {
                        if (!s.hasPermission("bankplus.setlevel")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.instance().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "view": {
                        if (!s.hasPermission("bankplus.view")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.instance().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "withdraw": {
                        if (!s.hasPermission("bankplus.withdraw")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.instance().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }
                }
            }
            break;

            case 4: {
                switch (a0) {
                    case "pay": {
                        if (!BPMethods.isPlayer(s) || !s.hasPermission("bankplus.pay")) return null;
                        List<String> args4 = new ArrayList<>();
                        for (String arg : BankPlus.instance().getBanks().keySet())
                            if (arg.startsWith(args[3].toLowerCase())) args4.add(arg);
                        return args4;
                    }

                    case "setlevel": {
                        if (!s.hasPermission("bankplus.setlevel")) return null;
                        List<String> args4 = new ArrayList<>();
                        for (String arg : new BanksManager(args[2]).getLevels())
                            if (arg.startsWith(args[3].toLowerCase())) args4.add(arg);
                        return args4;
                    }
                }
            }
        }
        return null;
    }
}