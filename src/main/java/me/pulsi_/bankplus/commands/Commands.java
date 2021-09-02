package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.guis.GuiBank;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Commands implements CommandExecutor {

    private BankPlus plugin;
    public Commands(BankPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {

        EconomyManager economy = new EconomyManager(plugin);

        List<String> worldBlacklist = plugin.getConfiguration().getStringList("General.Worlds-Blacklist");
        if (!worldBlacklist.isEmpty()) {
            if (s instanceof Player) {
                if (worldBlacklist.contains(((Player)s).getWorld().getName())) {
                    if (!s.hasPermission("bankplus.worlds.blacklist.bypass")) {
                        MessageManager.cannotUseBankHere(s, plugin);
                        return false;
                    }
                }
            }
        }

        if (args.length == 0) {
            if (!(s instanceof Player)) {
                MessageManager.notPlayer(s, plugin);
                return false;
            }
            if (plugin.getConfiguration().getBoolean("Gui.Enabled")) {
                new GuiBank(plugin).openGui((Player) s);
                MethodUtils.playSound("PERSONAL", (Player) s, plugin);
            } else {
                MessageManager.personalBalance((Player) s, plugin);
            }
        }

        if (args.length == 1) {
            switch (args[0]) {
                case "reload":
                    if (!s.hasPermission("bankplus.reload")) {
                        MessageManager.noPermission(s, plugin);
                        return false;
                    }
                    plugin.reloadConfigs();
                    MessageManager.reloadMessage(s, plugin);
                    break;

                case "help":
                    if (!s.hasPermission("bankplus.help")) {
                        MessageManager.noPermission(s, plugin);
                        return false;
                    }
                    for (String helpMessage : plugin.getMessages().getStringList("Help-Message"))
                        s.sendMessage(ChatUtils.c(helpMessage));
                    break;

                case "view":
                    if (!s.hasPermission("bankplus.view")) {
                        MessageManager.noPermission(s, plugin);
                        return false;
                    }
                    MessageManager.specifyPlayer(s, plugin);
                    break;

                case "withdraw":
                case "deposit":
                    if (!(s.hasPermission("bankplus.deposit") || s.hasPermission("bankplus.withdraw"))) {
                        MessageManager.noPermission(s, plugin);
                        return false;
                    }
                    if (!(s instanceof Player)) {
                        MessageManager.notPlayer(s, plugin);
                        return false;
                    }
                    MessageManager.specifyNumber(s, plugin);
                    break;

                case "set":
                case "add":
                case "remove":
                    if (!(s.hasPermission("bankplus.remove") || s.hasPermission("bankplus.add") || s.hasPermission("bankplus.set"))) {
                        MessageManager.noPermission(s, plugin);
                        return false;
                    }
                    MessageManager.specifyPlayer(s, plugin);
                    break;

                case "interest":
                    if (!s.hasPermission("bankplus.interest.restart")) {
                        MessageManager.noPermission(s, plugin);
                        return false;
                    }
                    MessageManager.interestUsage(s, plugin);
                    break;

                default:
                    MessageManager.unknownCommand(s, plugin);
                    break;
            }
        }

        if (args.length == 2) {
            switch (args[0]) {
                case "view":
                    if (!s.hasPermission("bankplus.view")) {
                        MessageManager.noPermission(s, plugin);
                        return false;
                    }
                    if (s instanceof Player) {
                        MethodUtils.playSound("VIEW", (Player) s, plugin);
                    }
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                        MessageManager.bankOthers(s, plugin, p);
                        return false;
                    }
                    MessageManager.bankOthers(s, plugin, Bukkit.getPlayerExact(args[1]));
                    break;

                case "withdraw": {
                    if (!s.hasPermission("bankplus.withdraw")) {
                        MessageManager.noPermission(s, plugin);
                        return false;
                    }
                    if (!(s instanceof Player)) {
                        MessageManager.notPlayer(s, plugin);
                        return false;
                    }
                    long amount;
                    switch (args[1]) {
                        case "all":
                            amount = economy.getBankBalance((Player) s);
                            MethodUtils.withdraw((Player) s, amount, plugin);
                            break;

                        case "half":
                            amount = economy.getBankBalance((Player) s) / 2;
                            MethodUtils.withdraw((Player) s, amount, plugin);
                            break;

                        default:
                            try {
                                amount = Long.parseLong(args[1]);
                                MethodUtils.withdraw((Player) s, amount, plugin);
                            } catch (NumberFormatException e) {
                                MessageManager.invalidNumber(s, plugin);
                            }
                    }
                }
                break;

                case "deposit": {
                    if (!s.hasPermission("bankplus.deposit")) {
                        MessageManager.noPermission(s, plugin);
                        return false;
                    }
                    if (!(s instanceof Player)) {
                        MessageManager.notPlayer(s, plugin);
                        return false;
                    }
                    long amount;
                    switch (args[1]) {
                        case "all":
                            amount = (long) plugin.getEconomy().getBalance((Player) s);
                            MethodUtils.deposit((Player) s, amount, plugin);
                            break;

                        case "half":
                            amount = (long) (plugin.getEconomy().getBalance((Player) s) / 2);
                            MethodUtils.deposit((Player) s, amount, plugin);
                            break;

                        default:
                            try {
                                amount = Long.parseLong(args[1]);
                                MethodUtils.deposit((Player) s, amount, plugin);
                            } catch (NumberFormatException ex) {
                                MessageManager.invalidNumber(s, plugin);
                            }
                    }
                }
                break;

                case "set":
                case "add":
                case "remove":
                    if (!(s.hasPermission("bankplus.set") || s.hasPermission("bankplus.add") || s.hasPermission("bankplus.remove"))) {
                        MessageManager.noPermission(s, plugin);
                        return false;
                    }
                    MessageManager.specifyNumber(s, plugin);
                    break;

                case "interest":
                    if (args[1].equalsIgnoreCase("restart")) {
                        if (!s.hasPermission("bankplus.interest.restart")) {
                            MessageManager.noPermission(s, plugin);
                            return false;
                        }
                        if (!plugin.getConfiguration().getBoolean("Interest.Enabled")) {
                            MessageManager.interestIsDisabled(s, plugin);
                            return false;
                        }
                        try {
                            plugin.getPlayers().set("Interest-Cooldown", plugin.getConfiguration().getLong("Interest.Delay"));
                            MessageManager.interestRestarted(s, plugin);
                            plugin.savePlayers();
                        } catch (Error e) {
                            MessageManager.internalError(s, plugin);
                        }
                    }
                    break;

                default:
                    MessageManager.unknownCommand(s, plugin);
                    break;
            }
        }

        if (args.length == 3) {
            switch (args[0]) {
                case "set":
                    if (!s.hasPermission("bankplus.set")) {
                        MessageManager.noPermission(s, plugin);
                        return false;
                    }
                    try {
                        long amount = Long.parseLong(args[2]);
                        if(Bukkit.getPlayerExact(args[1]) == null) {
                            economy.setPlayerBankBalance(Bukkit.getOfflinePlayer(args[1]), amount);
                            MessageManager.setMessage(s, Bukkit.getOfflinePlayer(args[1]), amount, plugin);
                            return false;
                        }
                        economy.setPlayerBankBalance(Bukkit.getPlayerExact(args[1]), amount);
                        MessageManager.setMessage(s, Bukkit.getPlayerExact(args[1]), amount, plugin);
                    } catch (NumberFormatException ex) {
                        MessageManager.invalidNumber(s, plugin);
                    }
                    break;

                case "add":
                    if (!s.hasPermission("bankplus.add")) {
                        MessageManager.noPermission(s, plugin);
                        return false;
                    }
                    try {
                        long amount = Long.parseLong(args[2]);
                        if(Bukkit.getPlayerExact(args[1]) == null) {
                            economy.addPlayerBankBalance(Bukkit.getOfflinePlayer(args[1]), amount);
                            MessageManager.addMessage(s, Bukkit.getOfflinePlayer(args[1]), amount, plugin);
                            return false;
                        }
                        economy.addPlayerBankBalance(Bukkit.getPlayerExact(args[1]), amount);
                        MessageManager.addMessage(s, Bukkit.getPlayerExact(args[1]), amount, plugin);
                    } catch (NumberFormatException ex) {
                        MessageManager.invalidNumber(s, plugin);
                    }
                    break;

                case "remove":
                    if (!s.hasPermission("bankplus.remove")) {
                        MessageManager.noPermission(s, plugin);
                        return false;
                    }
                    try {
                        long amount = Long.parseLong(args[2]);
                        if(Bukkit.getPlayerExact(args[1]) == null) {
                            economy.removePlayerBankBalance(Bukkit.getOfflinePlayer(args[1]), amount);
                            MessageManager.removeMessage(s, Bukkit.getOfflinePlayer(args[1]), amount, plugin);
                            return false;
                        }
                        economy.removePlayerBankBalance(Bukkit.getPlayerExact(args[1]), amount);
                        MessageManager.removeMessage(s, Bukkit.getPlayerExact(args[1]), amount, plugin);
                    } catch (NumberFormatException ex) {
                        MessageManager.invalidNumber(s, plugin);
                    }
                    break;

                default:
                    MessageManager.unknownCommand(s, plugin);
                    break;
            }
        }
        return true;
    }
}