package me.pulsi_.bankplus.commands.cmdProcessor;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayerFiles;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankGuis.BanksHolder;
import me.pulsi_.bankplus.bankGuis.BanksManager;
import me.pulsi_.bankplus.utils.BPMessages;
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

        BanksHolder banksHolder = new BanksHolder();
        SingleEconomyManager singleEconomyManager = null;
        if (s instanceof Player) singleEconomyManager = new SingleEconomyManager((Player) s);

        if (args.length == 0) {
            if (!BPMethods.hasPermission(s, "bankplus.use")) return;
            if (!(s instanceof Player)) {
                BPMessages.send(s, "Help-Message");
                return;
            }

            Player p = (Player) s;
            if (Values.CONFIG.isGuiModuleEnabled()) banksHolder.openBank(p);
            else {
                BPMessages.send(p, "Personal-Bank", BPMethods.placeValues(p, singleEconomyManager.getBankBalance()));
                BPMethods.playSound("PERSONAL", p);
            }
            return;
        }

        switch (args[0].toLowerCase()) {
            case "customwithdraw": {
                if (!BPMethods.hasPermission(s, "bankplus.customwithdraw")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    BPMessages.send(s, "Invalid-Player");
                    return;
                }

                BPMethods.customWithdraw(p);
            }
            break;

            case "customdeposit": {
                if (!BPMethods.hasPermission(s, "bankplus.customdeposit")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    BPMessages.send(s, "Invalid-Player");
                    return;
                }

                BPMethods.customDeposit(p);
            }
            break;

            case "force-open": {
                if (!Values.CONFIG.isGuiModuleEnabled()) {
                    BPMessages.send(s, "Gui-Module-Disabled");
                    return;
                }

                if (!BPMethods.hasPermission(s, "bankplus.force-open")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    BPMessages.send(s, "Invalid-Player");
                    return;
                }

                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }
                String identifier = args[2];

                if (!new BanksManager().exist(identifier)) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }
                banksHolder.openBank(p, identifier);
                BPMessages.send(s, "Force-Open", "%player%$" + p.getName(), "%bank%$", identifier);
            }
            break;

            case "pay": {
                if (!BPMethods.hasPermission(s, "bankplus.pay") || !BPMethods.isPlayer(s)) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                Player p = (Player) s;
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || target.equals(p)) {
                    BPMessages.send(s, "Invalid-Player");
                    return;
                }

                String num = args[2];
                if (BPMethods.isInvalidNumber(num, s)) return;
                BigDecimal amount = new BigDecimal(num);

                singleEconomyManager.pay(target, amount);
            }
            break;

            case "view": {
                if (!BPMethods.hasPermission(s, "bankplus.view")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Invalid-Player");
                    return;
                }

                if (s instanceof Player) BPMethods.playSound("VIEW", (Player) s);
                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);
                    BPMessages.send(s, "Bank-Others", BPMethods.placeValues(oP, new SingleEconomyManager(oP).getBankBalance()));
                    return;
                }
                BPMessages.send(s, "Bank-Others", BPMethods.placeValues(p, new SingleEconomyManager(p).getBankBalance()));
            }
            break;

            case "bal":
            case "balance": {
                if (!BPMethods.hasPermission(s, "bankplus.balance") || !BPMethods.isPlayer(s)) return;

                Player p = (Player) s;
                BPMessages.send(p, "Personal-Bank", BPMethods.placeValues(p, singleEconomyManager.getBankBalance()));
            }
            break;

            case "setlevel": {
                if (!BPMethods.hasPermission(s, "bankplus.setlevel")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                String level = args[2], bankName = Values.CONFIG.getMainGuiName();
                if (BPMethods.isInvalidNumber(level, s)) return;
                if (!new BanksManager(bankName).getLevels().contains(level)) {
                    BPMessages.send(s, "Invalid-Bank-Level");
                    return;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                BankPlusPlayerFiles files;
                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);
                    files = new BankPlusPlayerFiles(oP);
                    files.getPlayerConfig().set("Banks." + bankName + ".Level", Integer.valueOf(level));
                    BPMessages.send(s, "Set-Level-Message", "%player%$" + oP.getName(), "%level%$" + level);
                } else {
                    files = new BankPlusPlayerFiles(p);
                    files.getPlayerConfig().set("Banks." + bankName + ".Level", Integer.valueOf(level));
                    BPMessages.send(s, "Set-Level-Message", "%player%$" + p.getName(), "%level%$" + level);
                }
                files.savePlayerFile(true);
            }
            break;

            case "withdraw": {
                if (!BPMethods.hasPermission(s, "bankplus.withdraw") || !BPMethods.isPlayer(s)) return;

                Player p = (Player) s;
                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                BigDecimal amount;
                switch (args[1]) {
                    case "all":
                        amount = singleEconomyManager.getBankBalance();
                        singleEconomyManager.withdraw(amount);
                        break;

                    case "half":
                        amount = singleEconomyManager.getBankBalance().divide(BigDecimal.valueOf(2));
                        singleEconomyManager.withdraw(amount);
                        break;

                    default:
                        String num = args[1];
                        if (BPMethods.isInvalidNumber(num, s)) return;
                        amount = new BigDecimal(num);
                        singleEconomyManager.withdraw(amount);
                }
            }
            break;

            case "deposit": {
                if (!BPMethods.hasPermission(s, "bankplus.deposit") || !BPMethods.isPlayer(s)) return;

                Player p = (Player) s;
                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                BigDecimal amount;
                switch (args[1]) {
                    case "all":
                        amount = BigDecimal.valueOf(BankPlus.instance().getEconomy().getBalance(p));
                        singleEconomyManager.deposit(amount);
                        break;

                    case "half":
                        amount = BigDecimal.valueOf(BankPlus.instance().getEconomy().getBalance(p) / 2);
                        singleEconomyManager.deposit(amount);
                        break;

                    default:
                        String num = args[1];
                        if (BPMethods.isInvalidNumber(num, s)) return;
                        amount = new BigDecimal(num);
                        singleEconomyManager.deposit(amount);
                }
            }
            break;

            case "set": {
                if (!BPMethods.hasPermission(s, "bankplus.set")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                String num = args[2];
                if (BPMethods.isInvalidNumber(num, s)) return;
                BigDecimal amount = new BigDecimal(num);

                Player p = Bukkit.getPlayerExact(args[1]);
                String bankName = Values.CONFIG.getMainGuiName();
                BanksManager banksManager = new BanksManager(bankName);

                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);

                    BigDecimal capacity = banksManager.getCapacity(oP), balance = new SingleEconomyManager(oP).getBankBalance();
                    if (capacity.equals(balance)) {
                        BPMessages.send(s, "Bank-Full", "%player%$" + oP.getName());
                        return;
                    }
                    if (amount.doubleValue() >= capacity.doubleValue()) {
                        BPMessages.send(s, "Set-Message", BPMethods.placeValues(oP, capacity));
                        new SingleEconomyManager(oP).setBankBalance(capacity);
                        return;
                    }
                    new SingleEconomyManager(oP).setBankBalance(amount);
                    BPMessages.send(s, "Set-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                BigDecimal capacity = banksManager.getCapacity(p), balance = new SingleEconomyManager(p).getBankBalance();
                if (capacity.equals(balance)) {
                    BPMessages.send(s, "Bank-Full", "%player%$" + p.getName());
                    return;
                }
                if (amount.doubleValue() >= capacity.doubleValue()) {
                    BPMessages.send(s, "Set-Message", BPMethods.placeValues(p, capacity));
                    new SingleEconomyManager(p).setBankBalance(capacity);
                    return;
                }
                new SingleEconomyManager(p).setBankBalance(amount);
                BPMessages.send(s, "Set-Message", BPMethods.placeValues(p, amount));
            }
            break;

            case "add": {
                if (!BPMethods.hasPermission(s, "bankplus.add")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                String num = args[2];
                if (BPMethods.isInvalidNumber(num, s)) return;
                BigDecimal amount = new BigDecimal(num);

                Player p = Bukkit.getPlayerExact(args[1]);
                String bankName = Values.CONFIG.getMainGuiName();
                BanksManager banksManager = new BanksManager(bankName);

                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);

                    BigDecimal capacity = banksManager.getCapacity(oP), balance = new SingleEconomyManager(oP).getBankBalance();
                    if (capacity.equals(balance)) {
                        BPMessages.send(s, "Bank-Full", "%player%$" + oP.getName());
                        return;
                    }
                    if (balance.add(amount).doubleValue() >= capacity.doubleValue()) {
                        BPMessages.send(s, "Add-Message", BPMethods.placeValues(oP, capacity.subtract(balance)));
                        new SingleEconomyManager(oP).setBankBalance(capacity);
                        return;
                    }
                    new SingleEconomyManager(oP).addBankBalance(amount);
                    BPMessages.send(s, "Add-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                BigDecimal capacity = banksManager.getCapacity(p), balance = new SingleEconomyManager(p).getBankBalance();
                if (capacity.equals(balance)) {
                    BPMessages.send(s, "Bank-Full", "%player%$" + p.getName());
                    return;
                }
                if (balance.add(amount).doubleValue() >= capacity.doubleValue()) {
                    BPMessages.send(s, "Add-Message", BPMethods.placeValues(p, capacity.subtract(balance)));
                    new SingleEconomyManager(p).setBankBalance(capacity);
                    return;
                }
                new SingleEconomyManager(p).addBankBalance(amount);
                BPMessages.send(s, "Add-Message", BPMethods.placeValues(p, amount));
            }
            break;

            case "remove": {
                if (!BPMethods.hasPermission(s, "bankplus.remove")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                String num = args[2];
                if (BPMethods.isInvalidNumber(num, s)) return;
                BigDecimal amount = new BigDecimal(num);

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);

                    BigDecimal balance = new SingleEconomyManager(oP).getBankBalance();
                    if (balance.equals(new BigDecimal(0))) {
                        BPMessages.send(s, "Bank-Empty", "%player%$" + oP.getName());
                        return;
                    }
                    if (balance.subtract(amount).doubleValue() <= 0) {
                        BPMessages.send(s, "Remove-Message", BPMethods.placeValues(oP, balance));
                        new SingleEconomyManager(oP).setBankBalance(new BigDecimal(0));
                        return;
                    }
                    new SingleEconomyManager(oP).removeBankBalance(amount);
                    BPMessages.send(s, "Remove-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                BigDecimal balance = new SingleEconomyManager(p).getBankBalance();
                if (balance.equals(new BigDecimal(0))) {
                    BPMessages.send(s, "Bank-Empty", "%player%$" + p.getName());
                    return;
                }
                if (balance.subtract(amount).doubleValue() <= 0) {
                    BPMessages.send(s, "Remove-Message", BPMethods.placeValues(p, balance));
                    new SingleEconomyManager(p).setBankBalance(new BigDecimal(0));
                    return;
                }
                new SingleEconomyManager(p).removeBankBalance(amount);
                BPMessages.send(s, "Remove-Message", BPMethods.placeValues(p, amount));
            }
            break;

            case "saveallbankbalances": {
                if (!BPMethods.hasPermission(s, "bankplus.saveallbankbalances")) return;
                Bukkit.getOnlinePlayers().forEach(p -> new SingleEconomyManager(p).saveBankBalance(true));
                BPMessages.send(s, "Balances-Saved");
                if (Values.CONFIG.isSaveBalancesBroadcast()) BPLogger.info("All player balances have been saved!");
            }
            break;

            default:
                BPMessages.send(s, "Unknown-Command");
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
                        for (String arg : BankPlus.instance().getBanks().keySet()) if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "setlevel": {
                        if (!s.hasPermission("bankplus.setlevel")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : new BanksManager(Values.CONFIG.getMainGuiName()).getLevels())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }
                }
            }
        }
        return null;
    }
}