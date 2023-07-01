package me.pulsi_.bankplus.commands.cmdProcessor;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayerFiles;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankSystem.BankHolder;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SingleCmdProcessor {

    private static final List<String> confirms = new ArrayList<>();

    public static void processCmd(CommandSender s, String[] args) {

        SingleEconomyManager singleEconomyManager = null;
        if (s instanceof Player) singleEconomyManager = new SingleEconomyManager((Player) s);

        if (args.length == 0) {
            if (!BPMethods.hasPermission(s, "bankplus.use")) return;
            if (!(s instanceof Player)) {
                BPMessages.send(s, "Help-Message");
                return;
            }

            Player p = (Player) s;
            if (Values.CONFIG.isGuiModuleEnabled()) BankUtils.openBank(p);
            else {
                BPMessages.send(p, "Personal-Bank", BPMethods.placeValues(p, singleEconomyManager.getBankBalance()));
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

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    BPMessages.send(s, "Invalid-Player");
                    return;
                }

                new BankReader(Values.CONFIG.getMainGuiName()).upgradeBank(p);
            }
            break;

            case "upgrade": {
                if (BPMethods.isPlayer(s) && BPMethods.hasPermission(s, "bankplus.upgrade"))
                    new BankReader(Values.CONFIG.getMainGuiName()).upgradeBank((Player) s);
            }
            break;

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

            case "forceopen": {
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

                BankUtils.openBank(p, true);
                BPMessages.send(s, "Force-Open", "%player%$" + p.getName(), "%bank%$" + Values.CONFIG.getMainGuiName());
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
                if (!new BankReader(bankName).getLevels().contains(level)) {
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

                BigDecimal amount;
                switch (args[1].toLowerCase()) {
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
                        if (num.contains("%")) {
                            num = num.replace("%", "");
                            if (BPMethods.isInvalidNumber(num, s)) return;

                            amount = (singleEconomyManager.getBankBalance().multiply(new BigDecimal(num))).divide(BigDecimal.valueOf(100));
                            singleEconomyManager.withdraw(amount);

                            return;
                        }
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
                        amount = BigDecimal.valueOf(BankPlus.INSTANCE.getEconomy().getBalance(p));
                        singleEconomyManager.deposit(amount);
                        break;

                    case "half":
                        amount = BigDecimal.valueOf(BankPlus.INSTANCE.getEconomy().getBalance(p) / 2);
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
                BankReader bankReader = new BankReader(bankName);

                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);

                    SingleEconomyManager em = new SingleEconomyManager(oP);
                    BigDecimal capacity = bankReader.getCapacity(oP), balance = em.getBankBalance();
                    if (capacity.subtract(balance).doubleValue() <= 0) {
                        BPMessages.send(s, "Bank-Full", "%player%$" + oP.getName());
                        return;
                    }
                    if (amount.doubleValue() >= capacity.doubleValue()) {
                        BPMessages.send(s, "Set-Message", BPMethods.placeValues(oP, capacity));
                        em.setBankBalance(capacity);
                        return;
                    }
                    em.setBankBalance(amount);
                    BPMessages.send(s, "Set-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                SingleEconomyManager em = new SingleEconomyManager(p);
                BigDecimal capacity = bankReader.getCapacity(p), balance = em.getBankBalance();
                if (capacity.subtract(balance).doubleValue() <= 0) {
                    BPMessages.send(s, "Bank-Full", "%player%$" + p.getName());
                    return;
                }
                if (amount.doubleValue() >= capacity.doubleValue()) {
                    BPMessages.send(s, "Set-Message", BPMethods.placeValues(p, capacity));
                    em.setBankBalance(capacity);
                    return;
                }
                em.setBankBalance(amount);
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
                BankReader bankReader = new BankReader(bankName);

                if (p == null) {
                    OfflinePlayer oP = Bukkit.getOfflinePlayer(args[1]);

                    SingleEconomyManager em = new SingleEconomyManager(oP);
                    BigDecimal capacity = bankReader.getCapacity(oP), balance = em.getBankBalance();
                    if (capacity.equals(balance)) {
                        BPMessages.send(s, "Bank-Full", "%player%$" + oP.getName());
                        return;
                    }
                    if (balance.add(amount).doubleValue() >= capacity.doubleValue()) {
                        BPMessages.send(s, "Add-Message", BPMethods.placeValues(oP, capacity.subtract(balance)));
                        em.setBankBalance(capacity);
                        return;
                    }
                    em.addBankBalance(amount);
                    BPMessages.send(s, "Add-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                SingleEconomyManager em = new SingleEconomyManager(p);
                BigDecimal capacity = bankReader.getCapacity(p), balance = em.getBankBalance();
                if (capacity.subtract(balance).doubleValue() <= 0) {
                    BPMessages.send(s, "Bank-Full", "%player%$" + p.getName());
                    return;
                }
                if (balance.add(amount).doubleValue() >= capacity.doubleValue()) {
                    BPMessages.send(s, "Add-Message", BPMethods.placeValues(p, capacity.subtract(balance)));
                    em.setBankBalance(capacity);
                    return;
                }
                em.addBankBalance(amount);
                BPMessages.send(s, "Add-Message", BPMethods.placeValues(p, amount));
            }
            break;

            case "addall": {
                if (!BPMethods.hasPermission(s, "bankplus.addall")) return;

                if (args.length == 1) {
                    BPMessages.send(s, "Specify-Number");
                    return;
                }

                String num = args[1];
                if (BPMethods.isInvalidNumber(num, s)) return;

                if (!confirms.contains(s.getName())) {
                    confirms.add(s.getName());
                    BPMessages.send(s,
                            BPMessages.getPrefix() + " &cWarning, this command is going to add to every single player that joined the server (" +
                                    Bukkit.getOfflinePlayers().length + " players) the specified amount of money if the bank is available and it might" +
                                    " require some time, type the command again within 3 seconds to confirm."
                            , true);
                    Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> confirms.remove(s.getName()), 20L * 3);
                    return;
                }

                addAll(s, 0, new BankReader(Values.CONFIG.getMainGuiName()), new BigDecimal(num));
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

                    SingleEconomyManager em = new SingleEconomyManager(oP);
                    BigDecimal balance = em.getBankBalance();
                    if (balance.doubleValue() <= 0) {
                        BPMessages.send(s, "Bank-Empty", "%player%$" + oP.getName());
                        return;
                    }
                    if (balance.subtract(amount).doubleValue() <= 0) {
                        BPMessages.send(s, "Remove-Message", BPMethods.placeValues(oP, balance));
                        em.setBankBalance(new BigDecimal(0));
                        return;
                    }
                    em.removeBankBalance(amount);
                    BPMessages.send(s, "Remove-Message", BPMethods.placeValues(oP, amount));
                    return;
                }

                SingleEconomyManager em = new SingleEconomyManager(p);
                BigDecimal balance = em.getBankBalance();
                if (balance.doubleValue() <= 0) {
                    BPMessages.send(s, "Bank-Empty", "%player%$" + p.getName());
                    return;
                }
                if (balance.subtract(amount).doubleValue() <= 0) {
                    BPMessages.send(s, "Remove-Message", BPMethods.placeValues(p, balance));
                    em.setBankBalance(new BigDecimal(0));
                    return;
                }
                em.removeBankBalance(amount);
                BPMessages.send(s, "Remove-Message", BPMethods.placeValues(p, amount));
            }
            break;

            case "saveallbankbalances": {
                if (!BPMethods.hasPermission(s, "bankplus.saveallbankbalances")) return;

                Bukkit.getOnlinePlayers().forEach(p -> new SingleEconomyManager(p).saveBankBalance(true));
                BPMessages.send(s, "Balances-Saved");
                if (Values.CONFIG.isSaveBalancesBroadcast())
                    BPLogger.info("All player balances have been saved!");
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
                if (s.hasPermission("bankplus.addall")) listOfArgs.add("addAll");
                if (s.hasPermission("bankplus.balance")) {
                    listOfArgs.add("balance");
                    listOfArgs.add("bal");
                }
                if (s.hasPermission("bankplus.debug")) listOfArgs.add("debug");
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
                    case "debug": {
                        if (!s.hasPermission("bankplus.debug")) return null;
                        List<String> args2 = new ArrayList<>();
                        List<String> listOfArgs = new ArrayList<>();
                        if (s.hasPermission("bankplus.debug")) listOfArgs.add("TRANSACTIONS");
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

                    case "resetall": {
                        if (s.hasPermission("bankplus.resetall")) return null;
                        List<String> args2 = new ArrayList<>();
                        for (String arg : Arrays.asList("remove", "maintain"))
                            if (arg.startsWith(args[1].toLowerCase())) args2.add(arg);
                        return args2;
                    }
                }
            }
            break;

            case 3: {
                if (args[0].equalsIgnoreCase("setlevel")) {
                    if (!s.hasPermission("bankplus.setlevel")) return null;
                    List<String> args3 = new ArrayList<>();
                    for (String arg : new BankReader(Values.CONFIG.getMainGuiName()).getLevels())
                        if (arg.startsWith(args[2].toLowerCase())) args3.add(arg);
                    return args3;
                }
            }
        }
        return null;
    }

    private static void addAll(CommandSender s, int count, BankReader reader, BigDecimal amount) {

        int temp = 0;
        for (int i = 0; i < 60; i++) {
            if (count + temp >= Bukkit.getOfflinePlayers().length) {
                BPMessages.send(s, BPMessages.getPrefix() + " &2Task finished!", true);
                return;
            }

            OfflinePlayer oP = Bukkit.getOfflinePlayers()[count + temp];
            if (oP.isOnline()) {
                Player p = Bukkit.getPlayer(oP.getUniqueId());

                SingleEconomyManager em = new SingleEconomyManager(p);
                BigDecimal capacity = reader.getCapacity(p), balance = em.getBankBalance();
                if (capacity.subtract(balance).doubleValue() > 0) {
                    if (balance.add(amount).doubleValue() >= capacity.doubleValue()) em.setBankBalance(capacity);
                    else em.addBankBalance(amount);
                }
            } else {
                SingleEconomyManager em = new SingleEconomyManager(oP);
                BigDecimal capacity = reader.getCapacity(oP), balance = em.getBankBalance();
                if (capacity.subtract(balance).doubleValue() > 0) {
                    if (balance.add(amount).doubleValue() >= capacity.doubleValue()) em.setBankBalance(capacity);
                    else em.addBankBalance(amount);
                }
            }
            temp++;
        }
        int finalTemp = temp + 1;
        Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> addAll(s, count + finalTemp, reader, amount), 2);
    }
}