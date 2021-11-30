package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.guis.GuiBankHolder;
import me.pulsi_.bankplus.interest.Interest;
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

    private final BankPlus plugin;

    public Commands(BankPlus plugin) {
        this.plugin = plugin;
    }

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
            if (!s.hasPermission("bankplus.use")) {
                MessageManager.noPermission(s);
                return false;
            }
            if (!(s instanceof Player)) {
                MessageManager.notPlayer(s);
                return false;
            }
            Player p = (Player) s;
            if (Values.CONFIG.isGuiEnabled()) {
                GuiBankHolder.getEnchanterHolder().openBank(p);
                Methods.playSound("PERSONAL", p, plugin);
            } else {
                MessageManager.personalBalance(p);
            }
            return false;
        }

        switch (args[0]) {
            case "open": {
                if (!s.hasPermission("bankplus.open")) {
                    MessageManager.noPermission(s);
                    return false;
                }
                if (args.length == 1) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    MessageManager.cannotFindPlayer(s);
                    return false;
                }

                GuiBankHolder.getEnchanterHolder().openBank(p);
                s.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &7You have forced &a" + p.getName() + " &7to open their bank."));
            }
            break;

            case "reload": {
                if (!s.hasPermission("bankplus.reload")) {
                    MessageManager.noPermission(s);
                    return false;
                }
                plugin.reloadConfigs();
                Values.CONFIG.setupValues();
                Values.MESSAGES.setupValues();
                MessageManager.reloadMessage(s);
            }
            break;

            case "help": {
                if (!s.hasPermission("bankplus.help")) {
                    MessageManager.noPermission(s);
                    return false;
                }
                for (String helpMessage : plugin.messages().getStringList("Help-Message"))
                    s.sendMessage(ChatUtils.color(helpMessage));
            }
            break;

            case "view": {
                if (!s.hasPermission("bankplus.view")) {
                    MessageManager.noPermission(s);
                    return false;
                }
                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                } else {
                    if (s instanceof Player) {
                        Methods.playSound("VIEW", (Player) s, plugin);
                    }
                    Player p = Bukkit.getPlayerExact(args[1]);
                    if (p == null) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                        MessageManager.bankOthers(s, offlinePlayer);
                        return false;
                    }
                    MessageManager.bankOthers(s, p);
                }
            }
            break;

            case "bal":
            case "balance": {
                if (!s.hasPermission("bankplus.balance")) {
                    MessageManager.noPermission(s);
                    return false;
                }
                if (!(s instanceof Player)) {
                    MessageManager.notPlayer(s);
                    return false;
                }
                MessageManager.personalBalance((Player) s);
            }
            break;

            case "withdraw": {
                if (!s.hasPermission("bankplus.withdraw")) {
                    MessageManager.noPermission(s);
                    return false;
                }
                if (!(s instanceof Player)) {
                    MessageManager.notPlayer(s);
                    return false;
                }
                if (args.length == 1) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                long amount;
                switch (args[1]) {
                    case "all":
                        amount = EconomyManager.getInstance().getBankBalance((Player) s);
                        Methods.withdraw((Player) s, amount, plugin);
                        break;

                    case "half":
                        amount = EconomyManager.getInstance().getBankBalance((Player) s) / 2;
                        Methods.withdraw((Player) s, amount, plugin);
                        break;

                    default:
                        try {
                            amount = Long.parseLong(args[1]);
                            Methods.withdraw((Player) s, amount, plugin);
                        } catch (NumberFormatException e) {
                            MessageManager.invalidNumber(s);
                        }
                }
            }
            break;

            case "deposit": {
                if (!s.hasPermission("bankplus.deposit")) {
                    MessageManager.noPermission(s);
                    return false;
                }
                if (!(s instanceof Player)) {
                    MessageManager.notPlayer(s);
                    return false;
                }
                if (args.length == 1) {
                    MessageManager.specifyNumber(s);
                    return false;
                }

                long amount;
                switch (args[1]) {
                    case "all":
                        amount = (long) plugin.getEconomy().getBalance((Player) s);
                        Methods.deposit((Player) s, amount, plugin);
                        break;

                    case "half":
                        amount = (long) (plugin.getEconomy().getBalance((Player) s) / 2);
                        Methods.deposit((Player) s, amount, plugin);
                        break;

                    default:
                        try {
                            amount = Long.parseLong(args[1]);
                            Methods.deposit((Player) s, amount, plugin);
                        } catch (NumberFormatException ex) {
                            MessageManager.invalidNumber(s);
                        }
                }
            }
            break;

            case "set": {
                if (!s.hasPermission("bankplus.set")) {
                    MessageManager.noPermission(s);
                    return false;
                }
                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }
                if (args.length == 2) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }

                try {
                    long amount = Long.parseLong(args[2]);
                    Player p = Bukkit.getPlayerExact(args[1]);
                    if (p == null) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                        EconomyManager.getInstance().setPlayerBankBalance(offlinePlayer, amount);
                        MessageManager.setMessage(s, offlinePlayer, amount);
                        return false;
                    }
                    EconomyManager.getInstance().setPlayerBankBalance(p, amount);
                    MessageManager.setMessage(s, p, amount);
                } catch (NumberFormatException e) {
                    MessageManager.invalidNumber(s);
                }
            }
            break;

            case "add": {
                if (!s.hasPermission("bankplus.add")) {
                    MessageManager.noPermission(s);
                    return false;
                }
                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }
                if (args.length == 2) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }

                try {
                    long amount = Long.parseLong(args[2]);
                    Player p = Bukkit.getPlayerExact(args[1]);
                    if (p == null) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                        EconomyManager.getInstance().addPlayerBankBalance(offlinePlayer, amount);
                        MessageManager.setMessage(s, offlinePlayer, amount);
                        return false;
                    }
                    EconomyManager.getInstance().addPlayerBankBalance(p, amount);
                    MessageManager.setMessage(s, p, amount);
                } catch (NumberFormatException e) {
                    MessageManager.invalidNumber(s);
                }
            }
            break;

            case "remove": {
                if (!s.hasPermission("bankplus.remove")) {
                    MessageManager.noPermission(s);
                    return false;
                }
                if (args.length == 1) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }
                if (args.length == 2) {
                    MessageManager.specifyPlayer(s);
                    return false;
                }

                try {
                    long amount = Long.parseLong(args[2]);
                    Player p = Bukkit.getPlayerExact(args[1]);
                    if (p == null) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                        EconomyManager.getInstance().removePlayerBankBalance(offlinePlayer, amount);
                        MessageManager.setMessage(s, offlinePlayer, amount);
                        return false;
                    }
                    EconomyManager.getInstance().removePlayerBankBalance(p, amount);
                    MessageManager.setMessage(s, p, amount);
                } catch (NumberFormatException e) {
                    MessageManager.invalidNumber(s);
                }
            }
            break;

            case "interest": {
                if (!s.hasPermission("bankplus.interest.restart")) {
                    MessageManager.noPermission(s);
                    return false;
                }
                if (args.length == 1) {
                    MessageManager.interestUsage(s);
                    return false;
                }
                if (!args[1].equalsIgnoreCase("restart")) return false;

                if (!Values.CONFIG.isInterestEnabled()) {
                    MessageManager.interestIsDisabled(s);
                    return false;
                }
                try {
                    long delay = plugin.config().getLong("Interest.Delay");
                    Interest.interestCooldown.set(0, delay);
                    MessageManager.interestRestarted(s);
                } catch (Error e) {
                    MessageManager.internalError(s);
                }
            }
            break;

            default:
                MessageManager.unknownCommand(s);
                break;
        }
        return true;
    }
}