package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.gui.GuiHolder;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.DataManager;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

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
            if (!hasPermission(s, "bankplus.use")) return false;
            if (!(s instanceof Player)) {
                MessageManager.helpMessage(s);
                return false;
            }

            Player p = (Player) s;
            if (Values.CONFIG.isGuiEnabled()) {
                new GuiHolder().openBank(p);
                Methods.playSound("PERSONAL", p);
            } else {
                MessageManager.personalBalance(p);
            }
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "customwithdraw": {
                if (!hasPermission(s, "bankplus.customwithdraw")) return false;

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
                if (!hasPermission(s, "bankplus.customdeposit")) return false;

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
                if (!hasPermission(s, "bankplus.open")) return false;

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
                s.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &7You have forced &a" + p.getName() + " &7to open their bank."));
            }
            break;

            case "pay": {
                if (!hasPermission(s, "bankplus.pay")) return false;
                if (!isPlayer(s)) return false;

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
                if (isInvalidNumber(num, s)) return false;
                long amount = Long.parseLong(num);

                Methods.pay(p, target, amount);
            }
            break;

            case "reload": {
                if (!hasPermission(s, "bankplus.reload")) return false;

                DataManager.reloadPlugin();
                MessageManager.reloadMessage(s);
            }
            break;

            case "help":
                if (hasPermission(s, "bankplus.help")) MessageManager.helpMessage(s);
                break;

            case "view": {
                if (!hasPermission(s, "bankplus.view")) return false;

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
                if (!hasPermission(s, "bankplus.balance")) return false;
                if (!isPlayer(s)) return false;

                MessageManager.personalBalance((Player) s);
            }
            break;

            case "withdraw": {
                if (!hasPermission(s, "bankplus.withdraw")) return false;
                if (!isPlayer(s)) return false;

                Player p = (Player) s;
                if (args.length == 1) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                long amount;
                switch (args[1]) {
                    case "all":
                        amount = EconomyManager.getInstance().getBankBalance(p);
                        Methods.withdraw(p, amount);
                        break;

                    case "half":
                        amount = EconomyManager.getInstance().getBankBalance(p) / 2;
                        Methods.withdraw(p, amount);
                        break;

                    default:
                        String num = args[2];
                        if (isInvalidNumber(num, s)) return false;
                        amount = Long.parseLong(num);
                        Methods.withdraw(p, amount);
                }
            }
            break;

            case "deposit": {
                if (!hasPermission(s, "bankplus.deposit")) return false;
                if (!isPlayer(s)) return false;

                Player p = (Player) s;
                if (args.length == 1) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                long amount;
                switch (args[1]) {
                    case "all":
                        amount = (long) BankPlus.getEconomy().getBalance(p);
                        Methods.deposit(p, amount);
                        break;

                    case "half":
                        amount = (long) (BankPlus.getEconomy().getBalance(p) / 2);
                        Methods.deposit(p, amount);
                        break;

                    default:
                        String num = args[2];
                        if (isInvalidNumber(num, s)) return false;
                        amount = Long.parseLong(num);
                        Methods.deposit(p, amount);
                }
            }
            break;

            case "set": {
                if (!hasPermission(s, "bankplus.set")) return false;

                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }
                if (args.length == 2) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                String num = args[2];
                if (isInvalidNumber(num, s)) return false;
                long amount = Long.parseLong(num);

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    EconomyManager.getInstance().setPlayerBankBalance(offlinePlayer, amount);
                    MessageManager.setMessage(s, offlinePlayer, amount);
                    return false;
                }
                EconomyManager.getInstance().setPlayerBankBalance(p, amount);
                MessageManager.setMessage(s, p, amount);
            }
            break;

            case "add": {
                if (!hasPermission(s, "bankplus.add")) return false;

                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }
                if (args.length == 2) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                String num = args[2];
                if (isInvalidNumber(num, s)) return false;
                long amount = Long.parseLong(num);

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    EconomyManager.getInstance().addPlayerBankBalance(offlinePlayer, amount);
                    MessageManager.addMessage(s, offlinePlayer, amount);
                    return false;
                }
                EconomyManager.getInstance().addPlayerBankBalance(p, amount);
                MessageManager.addMessage(s, p, amount);
            }
            break;

            case "remove": {
                if (!hasPermission(s, "bankplus.remove")) return false;

                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }
                if (args.length == 2) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                String num = args[2];
                if (isInvalidNumber(num, s)) return false;
                long amount = Long.parseLong(num);

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    EconomyManager.getInstance().removePlayerBankBalance(offlinePlayer, amount);
                    MessageManager.removeMessage(s, offlinePlayer, amount);
                    return false;
                }
                EconomyManager.getInstance().removePlayerBankBalance(p, amount);
                MessageManager.removeMessage(s, p, amount);
            }
            break;

            case "restartInterest": {
                if (!hasPermission(s, "bankplus.restart-interest")) return false;

                if (!Values.CONFIG.isInterestEnabled()) {
                    MessageManager.interestIsDisabled(s);
                    return false;
                }
                Interest.setInterestCount(Values.CONFIG.getInterestDelay());
                MessageManager.interestRestarted(s);
            }
            break;

            default:
                MessageManager.unknownCommand(s);
                break;
        }
        return true;
    }

    private boolean isInvalidNumber(String number, CommandSender s) {
        try {
            long num = Long.parseLong(number);
            if (num < 0) {
                MessageManager.cannotUseNegativeNumber(s);
                return true;
            }
        } catch (NumberFormatException e) {
            MessageManager.invalidNumber(s);
            return true;
        }
        return false;
    }

    private boolean hasPermission(CommandSender s, String permission) {
        if (s.hasPermission(permission)) return true;
        MessageManager.noPermission(s);
        return false;
    }

    private boolean isPlayer(CommandSender s) {
        if (s instanceof Player) return true;
        MessageManager.notPlayer(s);
        return false;
    }
}