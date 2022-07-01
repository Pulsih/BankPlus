package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.gui.GuiHolder;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.*;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPDebugger;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {

        if (!Values.CONFIG.getWorldsBlacklist().isEmpty() && s instanceof Player) {
            Player p = (Player) s;
            if (Values.CONFIG.getWorldsBlacklist().contains(p.getWorld().getName()) && !p.hasPermission("bankplus.worlds.blacklist.bypass")) {
                MessageManager.cannotUseBankHere(p);
                return false;
            }
        }

        if (args.length == 0) {
            if (!Methods.hasPermission(s, "bankplus.use")) return false;
            if (!(s instanceof Player)) {
                MessageManager.helpMessage(s);
                return false;
            }

            Player p = (Player) s;
            if (Values.BANK.isGuiEnabled()) {
                new GuiHolder().openBank(p);
                Methods.playSound("PERSONAL", p);
            } else {
                MessageManager.personalBalance(p);
            }
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "customwithdraw": {
                if (!Methods.hasPermission(s, "bankplus.customwithdraw")) return false;

                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    MessageManager.invalidPlayer(s);
                    return false;
                }

                Methods.customWithdraw(p);
            }
            break;

            case "customdeposit": {
                if (!Methods.hasPermission(s, "bankplus.customdeposit")) return false;

                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    MessageManager.invalidPlayer(s);
                    return false;
                }

                Methods.customDeposit(p);
            }
            break;

            case "open": {
                if (!Methods.hasPermission(s, "bankplus.open")) return false;

                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    MessageManager.invalidPlayer(s);
                    return false;
                }

                new GuiHolder().openBank(p);
                s.sendMessage(BPChat.color("&a&lBank&9&lPlus &7You have forced &a" + p.getName() + " &7to open their bank."));
            }
            break;

            case "pay": {
                if (!Methods.hasPermission(s, "bankplus.pay")) return false;
                if (!Methods.isPlayer(s)) return false;

                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }

                Player p = (Player) s;
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || target.equals(p)) {
                    MessageManager.invalidPlayer(s);
                    return false;
                }

                if (args.length == 2) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                String num = args[2];
                if (Methods.isInvalidNumber(num, s)) return false;
                BigDecimal amount = new BigDecimal(num);

                Methods.pay(p, target, amount);
            }
            break;

            case "reload": {
                if (!Methods.hasPermission(s, "bankplus.reload")) return false;

                DataManager.reloadPlugin();
                MessageManager.reloadMessage(s);
            }
            break;

            case "help":
                if (Methods.hasPermission(s, "bankplus.help")) MessageManager.helpMessage(s);
                break;

            case "view": {
                if (!Methods.hasPermission(s, "bankplus.view")) return false;

                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }

                if (s instanceof Player) Methods.playSound("VIEW", (Player) s);
                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    MessageManager.bankOthers(s, offlinePlayer);
                    return true;
                }
                MessageManager.bankOthers(s, p);
            }
            break;

            case "bal":
            case "balance": {
                if (!Methods.hasPermission(s, "bankplus.balance")) return false;
                if (!Methods.isPlayer(s)) return false;

                MessageManager.personalBalance((Player) s);
            }
            break;

            case "withdraw": {
                if (!Methods.hasPermission(s, "bankplus.withdraw")) return false;
                if (!Methods.isPlayer(s)) return false;

                Player p = (Player) s;
                if (args.length == 1) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                BigDecimal amount;
                switch (args[1]) {
                    case "all":
                        amount = EconomyManager.getBankBalance(p);
                        Methods.withdraw(p, amount);
                        break;

                    case "half":
                        amount = EconomyManager.getBankBalance(p).divide(BigDecimal.valueOf(2));
                        Methods.withdraw(p, amount);
                        break;

                    default:
                        String num = args[1];
                        if (Methods.isInvalidNumber(num, s)) return false;
                        amount = new BigDecimal(num);
                        Methods.withdraw(p, amount);
                }
            }
            break;

            case "deposit": {
                if (!Methods.hasPermission(s, "bankplus.deposit")) return false;
                if (!Methods.isPlayer(s)) return false;

                Player p = (Player) s;
                if (args.length == 1) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                BigDecimal amount;
                switch (args[1]) {
                    case "all":
                        amount = BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p));
                        Methods.deposit(p, amount);
                        break;

                    case "half":
                        amount = BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p) / 2);
                        Methods.deposit(p, amount);
                        break;

                    default:
                        String num = args[1];
                        if (Methods.isInvalidNumber(num, s)) return false;
                        amount = new BigDecimal(num);
                        Methods.deposit(p, amount);
                }
            }
            break;

            case "set": {
                if (!Methods.hasPermission(s, "bankplus.set")) return false;

                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }
                if (args.length == 2) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                String num = args[2];
                if (Methods.isInvalidNumber(num, s)) return false;
                BigDecimal amount = new BigDecimal(num);

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    EconomyManager.setPlayerBankBalance(offlinePlayer, amount);
                    MessageManager.setMessage(s, offlinePlayer, amount);
                    return false;
                }
                EconomyManager.setPlayerBankBalance(p, amount);
                MessageManager.setMessage(s, p, amount);
            }
            break;

            case "add": {
                if (!Methods.hasPermission(s, "bankplus.add")) return false;

                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }
                if (args.length == 2) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                String num = args[2];
                if (Methods.isInvalidNumber(num, s)) return false;
                BigDecimal amount = new BigDecimal(num);

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    EconomyManager.addPlayerBankBalance(offlinePlayer, amount);
                    MessageManager.addMessage(s, offlinePlayer, amount);
                    return true;
                }
                EconomyManager.addPlayerBankBalance(p, amount);
                MessageManager.addMessage(s, p, amount);
            }
            break;

            case "remove": {
                if (!Methods.hasPermission(s, "bankplus.remove")) return false;

                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }
                if (args.length == 2) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                String num = args[2];
                if (Methods.isInvalidNumber(num, s)) return false;
                BigDecimal amount = new BigDecimal(num);

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    EconomyManager.removePlayerBankBalance(offlinePlayer, amount);
                    MessageManager.removeMessage(s, offlinePlayer, amount);
                    return true;
                }

                if (EconomyManager.getBankBalance(p).subtract(amount).doubleValue() <= 0) {
                    MessageManager.removeMessage(s, p, EconomyManager.getBankBalance(p));
                    EconomyManager.setPlayerBankBalance(p, new BigDecimal(0));
                    return true;
                }
                EconomyManager.removePlayerBankBalance(p, amount);
                MessageManager.removeMessage(s, p, amount);
            }
            break;

            case "restartinterest": {
                if (!Methods.hasPermission(s, "bankplus.restart-interest")) return false;

                if (!Values.CONFIG.isInterestEnabled()) {
                    MessageManager.interestIsDisabled(s);
                    return false;
                }
                Interest.restartInterest();
                MessageManager.interestRestarted(s);
            }
            break;

            case "giveinterest": {
                if (!Methods.hasPermission(s, "bankplus.give-interest")) return false;

                if (!Values.CONFIG.isInterestEnabled()) {
                    MessageManager.interestIsDisabled(s);
                    return false;
                }
                Interest.giveInterestToEveryone();
            }
            break;

            case "interest": {
                if (!Methods.hasPermission(s, "bankplus.interest")) return false;

                if (!Values.CONFIG.isInterestEnabled()) {
                    MessageManager.interestIsDisabled(s);
                    return false;
                }
                MessageManager.interestTime(s);
            }
            break;

            case "interestmillis": {
                if (!Methods.hasPermission(s, "bankplus.interestmillis")) return false;

                if (!Values.CONFIG.isInterestEnabled()) {
                    MessageManager.interestIsDisabled(s);
                    return false;
                }
                MessageManager.interestTime(s);
            }
            break;

            case "saveallbankbalances": {
                if (!Methods.hasPermission(s, "bankplus.saveallbankbalances")) return false;
                for (Player p : Bukkit.getOnlinePlayers()) EconomyManager.saveBankBalance(p);
                s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aSuccessfully saved all player balances to the file!"));
            }
            break;

            case "updatebanktop": {
                if (!Methods.hasPermission(s, "bankplus.updatebanktop")) return false;
                BankTopManager.updateBankTop();
                s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aSuccessfully updated the banktop!"));
            }
            break;

            case "validateallaccounts": {
                if (!Methods.hasPermission(s, "bankplus.validateallaccounts")) return false;
                s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aStarted validation task... Check the console for more info!"));
                AccountManager.validateAllAccounts();
            }
            break;

            case "debug": {
                if (!Methods.hasPermission(s, "bankplus.debug")) return false;
                if (args.length == 1) {
                    s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aChoose a valid option: CHAT, INTEREST, GUI."));
                    return false;
                }
                switch (args[1].toLowerCase()) {
                    case "chat":
                        BPDebugger.toggleChatDebugger(s);
                        break;

                    case "gui":
                        BPDebugger.toggleGuiDebugger(s);
                        break;

                    case "interest":
                        BPDebugger.debugInterest();
                        if (s instanceof Player) s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aDone! Check the console for the debug report!"));
                        break;

                    default:
                        s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aChoose a valid option: CHAT, INTEREST, GUI."));
                }
            }
            break;

            default:
                MessageManager.unknownCommand(s);
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command command, String alias, String[] args) {

        if (args.length == 1) {
            List<String> args1 = new ArrayList<>();
            List<String> listOfArgs = new ArrayList<>();
            if (s.hasPermission("bankplus.add")) listOfArgs.add("add");
            if (s.hasPermission("bankplus.balance")) {
                listOfArgs.add("balance");
                listOfArgs.add("bal");
            }
            if (s.hasPermission("bankplus.debug")) listOfArgs.add("debug");
            if (s.hasPermission("bankplus.deposit")) listOfArgs.add("deposit");
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
            if (s.hasPermission("bankplus.updatebanktop")) listOfArgs.add("updateBankTop");
            if (s.hasPermission("bankplus.validateallaccounts")) listOfArgs.add("validateAllAccounts");
            if (s.hasPermission("bankplus.view")) listOfArgs.add("view");
            if (s.hasPermission("bankplus.withdraw")) listOfArgs.add("withdraw");

            for (String arg : listOfArgs) if (arg.startsWith(args[0].toLowerCase())) args1.add(arg);
            return args1;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            List<String> args2 = new ArrayList<>();
            List<String> listOfArgs = new ArrayList<>();
            if (s.hasPermission("bankplus.debug")) {
                listOfArgs.add("CHAT");
                listOfArgs.add("GUI");
                listOfArgs.add("INTEREST");
            }
            for (String arg : listOfArgs) {
                if (arg.toLowerCase().startsWith(args[1].toLowerCase())) args2.add(arg);
            }
            return args2;
        }

        return null;
    }
}