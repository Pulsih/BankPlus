package me.pulsi_.bankplus.commands.cmdProcessor;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayerFiles;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.bankSystem.BankListGui;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MultiCmdProcessor {

    private static final List<String> confirms = new ArrayList<>();

    public static void processCmd(CommandSender s, String[] args) {

        MultiEconomyManager multiEconomyManager = null;
        if (s instanceof Player) multiEconomyManager = new MultiEconomyManager((Player) s);

        if (args.length == 0) {
            if (!BPMethods.hasPermission(s, "bankplus.use")) return;
            if (!(s instanceof Player)) {
                BPMessages.send(s, "Help-Message");
                return;
            }

            Player p = (Player) s;
            if (Values.CONFIG.isGuiModuleEnabled()) BankUtils.openBank(p, BankListGui.multipleBanksGuiID, false);
            else {
                BPMessages.send(p, "Multiple-Personal-Bank", BPMethods.placeValues(p, multiEconomyManager.getBankBalance()));
                BPMethods.playSound("PERSONAL", p);
            }
            return;
        }

        switch (args[0].toLowerCase()) {
            case "forceupgrade": {
                if (!BPMethods.hasPermission(s, "bankplus.forceupgrade")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }

                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    BPMessages.send(s, "Invalid-Player");
                    return;
                }

                BankReader bankReader = new BankReader(args[2]);
                if (bankReader.exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }

                bankReader.upgradeBank(p);
            }
            break;

            case "upgrade": {
                if (!BPMethods.isPlayer(s) || !BPMethods.hasPermission(s, "bankplus.upgrade")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }

                BankReader bankReader = new BankReader(args[1]);
                if (bankReader.exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }
                bankReader.upgradeBank((Player) s);
            }
            break;

            case "customwithdraw": {
                if (!BPMethods.hasPermission(s, "bankplus.customwithdraw")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    BPMessages.send(s, "Invalid-Player");
                    return;
                }

                String bankName = args[2];
                if (!new BankReader(bankName).exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }

                BPMethods.customWithdraw(p, bankName);
            }
            break;

            case "customdeposit": {
                if (!BPMethods.hasPermission(s, "bankplus.customdeposit")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    BPMessages.send(s, "Invalid-Player");
                    return;
                }

                String bankName = args[2];
                if (!new BankReader(bankName).exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }

                BPMethods.customDeposit(p, bankName);
            }
            break;

            case "forceopen": {
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
                String bankName = args[2];

                if (!new BankReader(bankName).exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }

                BankUtils.openBank(p, bankName, true);
                BPMessages.send(s, "Force-Open", "%player%$" + p.getName(), "%bank%$" + bankName);
            }
            break;

            case "open": {
                if (!BPMethods.isPlayer(s) || !BPMethods.hasPermission(s, "bankplus.open")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }
                BankUtils.openBank((Player) s, args[1], false);
            }
            break;

            case "pay": {
                if (!BPMethods.hasPermission(s, "bankplus.pay") || !BPMethods.isPlayer(s)) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2 || args.length == 3) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }
                if (args.length == 4) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                Player p = (Player) s;
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || target.equals(p)) {
                    BPMessages.send(s, "Invalid-Player");
                    return;
                }

                String fromBank = args[2];
                if (new BankReader(fromBank).exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }
                String toBank = args[3];
                if (new BankReader(toBank).exist()) {
                    BPMessages.send(s, "Invalid-Bank");
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
                    BPMessages.send(s, "Invalid-Player");
                    return;
                }

                if (args.length == 2) {
                    if (s instanceof Player) BPMethods.playSound("VIEW", (Player) s);
                    Player p = Bukkit.getPlayerExact(args[1]);
                    if (p == null) {
                        OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);
                        BPMessages.send(s, "Multiple-Bank-Others", BPMethods.placeValues(oP, new MultiEconomyManager(oP).getBankBalance()));
                        return;
                    }
                    BPMessages.send(s, "Multiple-Bank-Others", BPMethods.placeValues(p, new MultiEconomyManager(p).getBankBalance()));
                } else {
                    String bankName = args[2];
                    BankReader bankReader = new BankReader(bankName);
                    if (!bankReader.exist()) {
                        BPMessages.send(s, "Invalid-Bank");
                        return;
                    }

                    if (s instanceof Player) BPMethods.playSound("VIEW", (Player) s);
                    Player p = Bukkit.getPlayerExact(args[1]);
                    if (p == null) {
                        OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);
                        if (!bankReader.isAvailable(oP)) {
                            BPMessages.send(s, "Cannot-Access-Bank-Others", "%player%$" + oP.getName());
                            return;
                        }
                        BPMessages.send(s, "Bank-Others", BPMethods.placeValues(oP, new MultiEconomyManager(oP).getBankBalance(bankName)));
                        return;
                    }
                    if (!bankReader.isAvailable(p)) {
                        BPMessages.send(s, "Cannot-Access-Bank-Others", "%player%$" + p.getName());
                        return;
                    }
                    BPMessages.send(s, "Bank-Others", BPMethods.placeValues(p, new MultiEconomyManager(p).getBankBalance(bankName)));
                }
            }
            break;

            case "bal":
            case "balance": {
                if (!BPMethods.hasPermission(s, "bankplus.balance") || !BPMethods.isPlayer(s)) return;

                Player p = (Player) s;
                if (args.length == 1)
                    BPMessages.send(p, "Multiple-Personal-Bank", BPMethods.placeValues(p, multiEconomyManager.getBankBalance()));
                else {
                    String bankName = args[1];
                    if (!new BankReader(bankName).exist()) {
                        BPMessages.send(s, "Invalid-Bank");
                        return;
                    }
                    BPMessages.send(p, "Personal-Bank", BPMethods.placeValues(p, multiEconomyManager.getBankBalance(bankName)));
                }
            }
            break;

            case "setlevel": {
                if (!BPMethods.hasPermission(s, "bankplus.setlevel")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }
                if (args.length == 3) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                String bankName = args[2];
                BankReader banksManager = new BankReader(bankName);

                if (!banksManager.exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }

                String level = args[3];
                if (BPMethods.isInvalidNumber(level, s)) return;
                if (!banksManager.getLevels().contains(level)) {
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

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }
                String bankName = args[2];
                if (!new BankReader(bankName).exist()) {
                    BPMessages.send(s, "Invalid-Bank");
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
                    BPMessages.send(s, "Specify-Number");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }
                String bankName = args[2];
                if (!new BankReader(bankName).exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }

                BigDecimal amount;
                switch (args[1]) {
                    case "all":
                        amount = BigDecimal.valueOf(BankPlus.INSTANCE.getEconomy().getBalance(p));
                        multiEconomyManager.deposit(amount, bankName);
                        break;

                    case "half":
                        amount = BigDecimal.valueOf(BankPlus.INSTANCE.getEconomy().getBalance(p) / 2);
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
                    BPMessages.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }
                if (args.length == 3) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                String bankName = args[2];

                BankReader bankReader = new BankReader(bankName);
                if (!bankReader.exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }

                String num = args[3];
                if (BPMethods.isInvalidNumber(num, s)) return;

                BigDecimal amount = new BigDecimal(num);
                Player p = Bukkit.getPlayerExact(args[1]);

                boolean silent = args.length > 4 && args[4].toLowerCase().contains("true");

                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);


                    MultiEconomyManager em = new MultiEconomyManager(oP);
                    BigDecimal capacity = bankReader.getCapacity(oP);
                    if (amount.doubleValue() >= capacity.doubleValue()) {
                        BPMessages.send(s, "Set-Message", BPMethods.placeValues(oP, capacity));
                        if (!silent) em.setBankBalance(capacity, bankName);
                        return;
                    }
                    em.setBankBalance(amount, bankName);
                    if (!silent) BPMessages.send(s, "Set-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                MultiEconomyManager em = new MultiEconomyManager(p);
                BigDecimal capacity = bankReader.getCapacity(p);
                if (amount.doubleValue() >= capacity.doubleValue()) {
                    if (!silent) BPMessages.send(s, "Set-Message", BPMethods.placeValues(p, capacity));
                    em.setBankBalance(capacity, bankName);
                    return;
                }
                em.setBankBalance(amount, bankName);
                if (!silent) BPMessages.send(s, "Set-Message", BPMethods.placeValues(p, amount));
            }
            break;

            case "add": {
                if (!BPMethods.hasPermission(s, "bankplus.add")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }
                if (args.length == 3) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                String bankName = args[2];
                BankReader bankReader = new BankReader(bankName);
                if (!bankReader.exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }

                String num = args[3];
                if (BPMethods.isInvalidNumber(num, s)) return;

                BigDecimal amount = new BigDecimal(num);
                Player p = Bukkit.getPlayerExact(args[1]);

                boolean silent = args.length > 4 && args[4].toLowerCase().contains("true");

                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);

                    MultiEconomyManager em = new MultiEconomyManager(oP);
                    BigDecimal capacity = bankReader.getCapacity(oP), balance = em.getBankBalance(bankName);
                    if (capacity.subtract(balance).doubleValue() <= 0) {
                        if (!silent) BPMessages.send(s, "Bank-Full", "%player%$" + oP.getName());
                        return;
                    }
                    if (balance.add(amount).doubleValue() >= capacity.doubleValue()) {
                        if (!silent) BPMessages.send(s, "Add-Message", BPMethods.placeValues(oP, capacity.subtract(balance)));
                        em.setBankBalance(capacity, bankName);
                        return;
                    }
                    em.addBankBalance(amount, bankName);
                    if (!silent) BPMessages.send(s, "Add-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                MultiEconomyManager em = new MultiEconomyManager(p);
                BigDecimal capacity = bankReader.getCapacity(p), balance = em.getBankBalance(bankName);
                if (capacity.subtract(balance).doubleValue() <= 0) {
                    if (!silent) BPMessages.send(s, "Bank-Full", "%player%$" + p.getName());
                    return;
                }
                if (balance.add(amount).doubleValue() >= capacity.doubleValue()) {
                    if (!silent) BPMessages.send(s, "Add-Message", BPMethods.placeValues(p, capacity.subtract(balance)));
                    em.setBankBalance(capacity, bankName);
                    return;
                }
                em.addBankBalance(amount, bankName);
                if (!silent) BPMessages.send(s, "Add-Message", BPMethods.placeValues(p, amount));
            }
            break;

            case "addall": {
                if (!BPMethods.hasPermission(s, "bankplus.addall")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                String bankName = args[1];
                BankReader reader = new BankReader(bankName);
                if (!reader.exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }

                String num = args[2];
                if (BPMethods.isInvalidNumber(num, s)) return;

                if (!confirms.contains(s.getName())) {
                    confirms.add(s.getName());
                    BPMessages.send(s,
                            BPMessages.getPrefix() + " &cWarning, this command is going to add to every online player (" +
                                    Bukkit.getOnlinePlayers().size() + " players) the specified amount of money in the selected bank if available and" +
                                    " it might require some time, type the command again within 5 seconds to confirm."
                            , true);
                    Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> confirms.remove(s.getName()), 20L * 5);
                    return;
                }

                addAll(s, 0, reader, new BigDecimal(num), bankName);
            }
            break;

            case "remove": {
                if (!BPMethods.hasPermission(s, "bankplus.remove")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Player");
                    return;
                }
                if (args.length == 2) {
                    BPMessages.send(s, "Specify-Bank");
                    return;
                }
                if (args.length == 3) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                String bankName = args[2];
                if (!new BankReader(bankName).exist()) {
                    BPMessages.send(s, "Invalid-Bank");
                    return;
                }

                String num = args[3];
                if (BPMethods.isInvalidNumber(num, s)) return;

                BigDecimal amount = new BigDecimal(num);
                Player p = Bukkit.getPlayerExact(args[1]);

                boolean silent = args.length > 4 && args[4].toLowerCase().contains("true");

                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);

                    MultiEconomyManager em = new MultiEconomyManager(oP);
                    BigDecimal balance = em.getBankBalance(bankName);
                    if (balance.doubleValue() <= 0) {
                        if (!silent) BPMessages.send(s, "Bank-Empty", "%player%$" + oP.getName());
                        return;
                    }
                    if (balance.subtract(amount).doubleValue() <= 0) {
                        if (!silent) BPMessages.send(s, "Remove-Message", BPMethods.placeValues(oP, balance));
                        em.setBankBalance(new BigDecimal(0), bankName);
                        return;
                    }
                    em.removeBankBalance(amount, bankName);
                    if (!silent) BPMessages.send(s, "Remove-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                MultiEconomyManager em = new MultiEconomyManager(p);
                BigDecimal balance = em.getBankBalance(bankName);
                if (balance.doubleValue() <= 0) {
                    if (!silent) BPMessages.send(s, "Bank-Empty", "%player%$" + p.getName());
                    return;
                }
                if (balance.subtract(amount).doubleValue() <= 0) {
                    if (!silent) BPMessages.send(s, "Remove-Message", BPMethods.placeValues(p, balance));
                    em.setBankBalance(new BigDecimal(0), bankName);
                    return;
                }
                em.removeBankBalance(amount, bankName);
                if (!silent) BPMessages.send(s, "Remove-Message", BPMethods.placeValues(p, amount));
            }
            break;

            default:
                BPMessages.send(s, "Unknown-Command");
                break;
        }
    }

    public static List<String> getMultiTabComplete(CommandSender s, String[] args) {
        switch (args.length) {
            case 1: {
                List<String> args1 = new ArrayList<>();
                List<String> listOfArgs = new ArrayList<>();
                if (s.hasPermission("bankplus.add")) listOfArgs.add("add");
                if (s.hasPermission("bankplus.addall")) listOfArgs.add("addAll");
                if (s.hasPermission("bankplus.balance")) {
                    listOfArgs.add("balance");
                    listOfArgs.add("bal");
                }
                if (s.hasPermission("bankplus.deposit")) listOfArgs.add("deposit");
                if (s.hasPermission("bankplus.forceopen")) listOfArgs.add("forceOpen");
                if (s.hasPermission("bankplus.forceupgrade")) listOfArgs.add("forceUpgrade");
                if (s.hasPermission("bankplus.giveinterest")) listOfArgs.add("giveInterest");
                if (s.hasPermission("bankplus.help")) listOfArgs.add("help");
                if (s.hasPermission("bankplus.interest")) listOfArgs.add("interest");
                if (s.hasPermission("bankplus.interestmillis")) listOfArgs.add("interestMillis");
                if (s.hasPermission("bankplus.open")) listOfArgs.add("open");
                if (s.hasPermission("bankplus.pay")) listOfArgs.add("pay");
                if (s.hasPermission("bankplus.reload")) listOfArgs.add("reload");
                if (s.hasPermission("bankplus.remove")) listOfArgs.add("remove");
                if (s.hasPermission("bankplus.resetall")) listOfArgs.add("resetAll");
                if (s.hasPermission("bankplus.restartinterest")) listOfArgs.add("restartInterest");
                if (s.hasPermission("bankplus.saveallbankbalances")) listOfArgs.add("saveAllBankBalances");
                if (s.hasPermission("bankplus.set")) listOfArgs.add("set");
                if (s.hasPermission("bankplus.setlevel")) listOfArgs.add("setlevel");
                if (s.hasPermission("bankplus.updatebanktop")) listOfArgs.add("updateBankTop");
                if (s.hasPermission("bankplus.upgrade")) listOfArgs.add("upgrade");
                if (s.hasPermission("bankplus.view")) listOfArgs.add("view");
                if (s.hasPermission("bankplus.withdraw")) listOfArgs.add("withdraw");

                for (String arg : listOfArgs) if (arg.startsWith(args[0].toLowerCase())) args1.add(arg);
                return args1;
            }

            case 2: {
                switch (args[0].toLowerCase()) {
                    case "bal":
                    case "balance": {
                        if (!s.hasPermission("bankplus.balance")) return null;
                        List<String> args2 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[1].toLowerCase())) args2.add(arg);
                        return args2;
                    }

                    case "addall": {
                        if (!s.hasPermission("bankplus.addall")) return null;
                        List<String> args2 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[1].toLowerCase())) args2.add(arg);
                        return args2;
                    }

                    case "deposit": {
                        if (!s.hasPermission("bankplus.deposit")) return null;
                        List<String> args2 = new ArrayList<>();
                        for (String arg : new String[]{"1", "2", "3", "..."})
                            if (arg.startsWith(args[1].toLowerCase())) args2.add(arg);
                        return args2;
                    }

                    case "open": {
                        if (!s.hasPermission("bankplus.open")) return null;
                        List<String> args2 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[1].toLowerCase())) args2.add(arg);
                        return args2;
                    }

                    case "withdraw": {
                        if (!s.hasPermission("bankplus.withdraw")) return null;
                        List<String> args2 = new ArrayList<>();
                        for (String arg : new String[]{"1", "2", "3", "..."})
                            if (arg.startsWith(args[1].toLowerCase())) args2.add(arg);
                        return args2;
                    }

                    case "resetall": {
                        if (s.hasPermission("bankplus.resetall")) return null;
                        List<String> args2 = new ArrayList<>();
                        for (String arg : new String[]{"remove", "maintain"})
                            if (arg.startsWith(args[1].toLowerCase())) args2.add(arg);
                        return args2;
                    }
                }
            }
            break;

            case 3: {
                switch (args[0].toLowerCase()) {
                    case "add": {
                        if (!s.hasPermission("bankplus.add")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "deposit": {
                        if (!s.hasPermission("bankplus.deposit")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "forceoopen": {
                        if (!s.hasPermission("bankplus.force-open")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "pay": {
                        if (!BPMethods.isPlayer(s) || !s.hasPermission("bankplus.pay")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "remove": {
                        if (!s.hasPermission("bankplus.remove")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "set": {
                        if (!s.hasPermission("bankplus.set")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "setlevel": {
                        if (!s.hasPermission("bankplus.setlevel")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "view": {
                        if (!s.hasPermission("bankplus.view")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }

                    case "withdraw": {
                        if (!s.hasPermission("bankplus.withdraw")) return null;
                        List<String> args3 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                        return args3;
                    }
                }
            }
            break;

            case 4: {
                switch (args[0].toLowerCase()) {
                    case "pay": {
                        if (!BPMethods.isPlayer(s) || !s.hasPermission("bankplus.pay")) return null;
                        List<String> args4 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[3].toLowerCase())) args4.add(arg);
                        return args4;
                    }

                    case "setlevel": {
                        if (!s.hasPermission("bankplus.setlevel")) return null;
                        List<String> args4 = new ArrayList<>();
                        for (String arg : new BankReader(args[2]).getLevels())
                            if (arg.startsWith(args[3].toLowerCase())) args4.add(arg);
                        return args4;
                    }
                }
            }
            break;

            case 5: {
                switch (args[0].toLowerCase()) {
                    case "pay": {
                        if (!BPMethods.isPlayer(s) || !s.hasPermission("bankplus.pay")) return null;
                        List<String> args4 = new ArrayList<>();
                        for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                            if (arg.startsWith(args[3].toLowerCase())) args4.add(arg);
                        return args4;
                    }

                    case "setlevel": {
                        if (!s.hasPermission("bankplus.setlevel")) return null;
                        List<String> args4 = new ArrayList<>();
                        for (String arg : new BankReader(args[2]).getLevels())
                            if (arg.startsWith(args[3].toLowerCase())) args4.add(arg);
                        return args4;
                    }

                    case "add": {
                        if (!s.hasPermission("bankplus.add")) return null;
                        List<String> args5 = new ArrayList<>();
                        String[] listOfArgs = {"silent=true", "silent=false"};
                        for (String arg : listOfArgs)
                            if (arg.toLowerCase().startsWith(args[4].toLowerCase())) args5.add(arg);
                        return args5;
                    }

                    case "remove": {
                        if (!s.hasPermission("bankplus.remove")) return null;
                        List<String> args5 = new ArrayList<>();
                        String[] listOfArgs = {"silent=true", "silent=false"};
                        for (String arg : listOfArgs)
                            if (arg.toLowerCase().startsWith(args[4].toLowerCase())) args5.add(arg);
                        return args5;
                    }

                    case "set": {
                        if (!s.hasPermission("bankplus.set")) return null;
                        List<String> args5 = new ArrayList<>();
                        String[] listOfArgs = {"silent=true", "silent=false"};
                        for (String arg : listOfArgs)
                            if (arg.toLowerCase().startsWith(args[4].toLowerCase())) args5.add(arg);
                        return args5;
                    }
                }
            }
        }
        return null;
    }

    private static void addAll(CommandSender s, int count, BankReader reader, BigDecimal amount, String bankName) {

        int temp = 0;
        Player[] players = (Player[]) Bukkit.getOnlinePlayers().toArray();

        for (int i = 0; i < 60; i++) {
            if (count + temp >= players.length) {
                BPMessages.send(s, BPMessages.getPrefix() + " &2Task finished!", true);
                return;
            }

            Player p = players[count + temp];
            MultiEconomyManager em = new MultiEconomyManager(p);
            BigDecimal capacity = reader.getCapacity(p), balance = em.getBankBalance(bankName);

            if (capacity.subtract(balance).doubleValue() > 0) {
                if (balance.add(amount).doubleValue() < capacity.doubleValue()) em.addBankBalance(amount, bankName);
                else em.setBankBalance(capacity, bankName);
            }
            temp++;
        }

        int finalTemp = temp + 1;
        Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> addAll(s, count + finalTemp, reader, amount, bankName), 2);
    }
}