package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.guis.GuiBankHolder;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.ConfigValues;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.ListUtils;
import me.pulsi_.bankplus.utils.MethodUtils;
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

        final MessageManager messMan = new MessageManager(plugin);
        final EconomyManager economy = new EconomyManager(plugin);

        if (!ConfigValues.getWorldsBlacklist().isEmpty() && s instanceof Player) {
            Player p = (Player) s;
            if (ConfigValues.getWorldsBlacklist().contains(p.getWorld().getName()) && !p.hasPermission("bankplus.worlds.blacklist.bypass")) {
                messMan.cannotUseBankHere(s);
                return false;
            }
        }

        if (args.length == 0) {
            if (!s.hasPermission("bankplus.use")) {
                messMan.noPermission(s);
                return false;
            }
            if (!(s instanceof Player)) {
                messMan.notPlayer(s);
                return false;
            }
            if (ConfigValues.isGuiEnabled()) {
                GuiBankHolder.getEnchanterHolder().openBank((Player) s);
                MethodUtils.playSound("PERSONAL", (Player) s, plugin);
            } else {
                messMan.personalBalance((Player) s);
            }
        }

        if (args.length == 1) {
            switch (args[0]) {
                case "reload":
                    if (!s.hasPermission("bankplus.reload")) {
                        messMan.noPermission(s);
                        return false;
                    }
                    plugin.reloadConfigs();
                    ConfigValues.setupValues();
                    messMan.reloadMessage(s);
                    break;

                case "help":
                    if (!s.hasPermission("bankplus.help")) {
                        messMan.noPermission(s);
                        return false;
                    }
                    for (String helpMessage : plugin.messages().getStringList("Help-Message"))
                        s.sendMessage(ChatUtils.color(helpMessage));
                    break;

                case "view":
                    if (!s.hasPermission("bankplus.view")) {
                        messMan.noPermission(s);
                        return false;
                    }
                    messMan.specifyPlayer(s);
                    break;

                case "bal":
                case "balance":
                    if (!s.hasPermission("bankplus.balance")) {
                        messMan.noPermission(s);
                        return false;
                    }
                    messMan.personalBalance((Player) s);
                    break;

                case "withdraw":
                case "deposit":
                    if (!(s.hasPermission("bankplus.deposit") || s.hasPermission("bankplus.withdraw"))) {
                        messMan.noPermission(s);
                        return false;
                    }
                    if (!(s instanceof Player)) {
                        messMan.notPlayer(s);
                        return false;
                    }
                    messMan.specifyNumber(s);
                    break;

                case "set":
                case "add":
                case "remove":
                    if (!(s.hasPermission("bankplus.remove") || s.hasPermission("bankplus.add") || s.hasPermission("bankplus.set"))) {
                        messMan.noPermission(s);
                        return false;
                    }
                    messMan.specifyPlayer(s);
                    break;

                case "interest":
                    if (!s.hasPermission("bankplus.interest.restart")) {
                        messMan.noPermission(s);
                        return false;
                    }
                    messMan.interestUsage(s);
                    break;

                case "debug":
                    s.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &aAvailable options: &7playerchat, guibank, interest."));
                    break;

                default:
                    messMan.unknownCommand(s);
                    break;
            }
        }

        if (args.length == 2) {
            switch (args[0]) {
                case "view":
                    if (!s.hasPermission("bankplus.view")) {
                        messMan.noPermission(s);
                        return false;
                    }
                    if (s instanceof Player) {
                        MethodUtils.playSound("VIEW", (Player) s, plugin);
                    }
                    if (Bukkit.getPlayerExact(args[1]) == null) {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                        messMan.bankOthers(s, p);
                        return false;
                    }
                    messMan.bankOthers(s, Bukkit.getPlayerExact(args[1]));
                    break;

                case "withdraw": {
                    if (!s.hasPermission("bankplus.withdraw")) {
                        messMan.noPermission(s);
                        return false;
                    }
                    if (!(s instanceof Player)) {
                        messMan.notPlayer(s);
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
                                messMan.invalidNumber(s);
                            }
                    }
                }
                break;

                case "deposit": {
                    if (!s.hasPermission("bankplus.deposit")) {
                        messMan.noPermission(s);
                        return false;
                    }
                    if (!(s instanceof Player)) {
                        messMan.notPlayer(s);
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
                                messMan.invalidNumber(s);
                            }
                    }
                }
                break;

                case "set":
                case "add":
                case "remove":
                    if (!(s.hasPermission("bankplus.set") || s.hasPermission("bankplus.add") || s.hasPermission("bankplus.remove"))) {
                        messMan.noPermission(s);
                        return false;
                    }
                    messMan.specifyNumber(s);
                    break;

                case "interest":
                    if (args[1].equalsIgnoreCase("restart")) {
                        if (!s.hasPermission("bankplus.interest.restart")) {
                            messMan.noPermission(s);
                            return false;
                        }
                        if (!plugin.config().getBoolean("Interest.Enabled")) {
                            messMan.interestIsDisabled(s);
                            return false;
                        }
                        try {
                            final long delay = plugin.config().getLong("Interest.Delay");
                            Interest.interestCooldown.set(0, delay);
                            messMan.interestRestarted(s);
                        } catch (Error e) {
                            messMan.internalError(s);
                        }
                    }
                    break;

                case "debug":
                    if (args[1].equalsIgnoreCase("playerchat")) {
                        if (ListUtils.PLAYERCHAT_DEBUG.get(0).equals("DISABLED")) {
                            s.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &7Enabled the debug mode for PlayerChat"));
                            ListUtils.PLAYERCHAT_DEBUG.set(0, "ENABLED");
                        } else {
                            ListUtils.PLAYERCHAT_DEBUG.set(0, "DISABLED");
                            s.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &7Disabled the debug mode for PlayerChat"));
                        }
                    }
                    if (args[1].equalsIgnoreCase("guibank")) {
                        if (ListUtils.GUIBANK_DEBUG.get(0).equals("DISABLED")) {
                            s.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &7Enabled the debug mode for GuiBank"));
                            ListUtils.GUIBANK_DEBUG.set(0, "ENABLED");
                        } else {
                            ListUtils.GUIBANK_DEBUG.set(0, "DISABLED");
                            s.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &7Disabled the debug mode for GuiBank"));
                        }
                    }
                    if (args[1].equalsIgnoreCase("interest")) {
                        if (ListUtils.INTEREST_DEBUG.get(0).equals("DISABLED")) {
                            s.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &7Enabled the debug mode for Interest"));
                            ListUtils.INTEREST_DEBUG.set(0, "ENABLED");
                        } else {
                            ListUtils.INTEREST_DEBUG.set(0, "DISABLED");
                            s.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &7Disabled the debug mode for Interest"));
                        }
                    }
                    break;

                default:
                    messMan.unknownCommand(s);
                    break;
            }
        }

        if (args.length == 3) {
            switch (args[0]) {
                case "set":
                    if (!s.hasPermission("bankplus.set")) {
                        messMan.noPermission(s);
                        return false;
                    }
                    try {
                        long amount = Long.parseLong(args[2]);
                        if(Bukkit.getPlayerExact(args[1]) == null) {
                            economy.setPlayerBankBalance(Bukkit.getOfflinePlayer(args[1]), amount);
                            messMan.setMessage(s, Bukkit.getOfflinePlayer(args[1]), amount);
                            return false;
                        }
                        economy.setPlayerBankBalance(Bukkit.getPlayerExact(args[1]), amount);
                        messMan.setMessage(s, Bukkit.getPlayerExact(args[1]), amount);
                    } catch (NumberFormatException ex) {
                        messMan.invalidNumber(s);
                    }
                    break;

                case "add":
                    if (!s.hasPermission("bankplus.add")) {
                        messMan.noPermission(s);
                        return false;
                    }
                    try {
                        long amount = Long.parseLong(args[2]);
                        if(Bukkit.getPlayerExact(args[1]) == null) {
                            economy.addPlayerBankBalance(Bukkit.getOfflinePlayer(args[1]), amount);
                            messMan.addMessage(s, Bukkit.getOfflinePlayer(args[1]), amount);
                            return false;
                        }
                        economy.addPlayerBankBalance(Bukkit.getPlayerExact(args[1]), amount);
                        messMan.addMessage(s, Bukkit.getPlayerExact(args[1]), amount);
                    } catch (NumberFormatException ex) {
                        messMan.invalidNumber(s);
                    }
                    break;

                case "remove":
                    if (!s.hasPermission("bankplus.remove")) {
                        messMan.noPermission(s);
                        return false;
                    }
                    try {
                        long amount = Long.parseLong(args[2]);
                        if(Bukkit.getPlayerExact(args[1]) == null) {
                            economy.removePlayerBankBalance(Bukkit.getOfflinePlayer(args[1]), amount);
                            messMan.removeMessage(s, Bukkit.getOfflinePlayer(args[1]), amount);
                            return false;
                        }
                        economy.removePlayerBankBalance(Bukkit.getPlayerExact(args[1]), amount);
                        messMan.removeMessage(s, Bukkit.getPlayerExact(args[1]), amount);
                    } catch (NumberFormatException ex) {
                        messMan.invalidNumber(s);
                    }
                    break;

                default:
                    messMan.unknownCommand(s);
                    break;
            }
        }
        return true;
    }
}