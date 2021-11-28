package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.guis.GuiBankHolder;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.ConfigValues;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.ListUtils;
import me.pulsi_.bankplus.utils.Methods;
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

        MessageManager messMan = new MessageManager(plugin);

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
            Player p = (Player) s;
            if (ConfigValues.isGuiEnabled()) {
                GuiBankHolder.getEnchanterHolder().openBank(p);
                Methods.playSound("PERSONAL", p, plugin);
            } else {
                messMan.personalBalance(p);
            }
        }

        switch (args[0]) {
            case "open": {
                if (!s.hasPermission("bankplus.open")) {
                    messMan.noPermission(s);
                    return false;
                }
                if (args[1] == null) {
                    messMan.specifyNumber(s);
                    return false;
                }
                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) {
                    messMan.cannotFindPlayer(s);
                    return false;
                }

                GuiBankHolder.getEnchanterHolder().openBank(p);
                s.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &7You have forced &a" + p.getName() + " &7to open their bank."));
            }
            break;

            case "reload": {
                if (!s.hasPermission("bankplus.reload")) {
                    messMan.noPermission(s);
                    return false;
                }
                plugin.reloadConfigs();
                ConfigValues.setupValues();
                messMan.reloadMessage(s);
            }
            break;

            case "help": {
                if (!s.hasPermission("bankplus.help")) {
                    messMan.noPermission(s);
                    return false;
                }
                for (String helpMessage : plugin.messages().getStringList("Help-Message"))
                    s.sendMessage(ChatUtils.color(helpMessage));
            }
            break;

            case "view": {
                if (!s.hasPermission("bankplus.view")) {
                    messMan.noPermission(s);
                    return false;
                }
                if (args.length == 1) {
                    messMan.specifyPlayer(s);
                    return false;
                } else {
                    if (s instanceof Player) {
                        Methods.playSound("VIEW", (Player) s, plugin);
                    }
                    Player p = Bukkit.getPlayerExact(args[1]);
                    if (p == null) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                        messMan.bankOthers(s, offlinePlayer);
                        return false;
                    }
                    messMan.bankOthers(s, p);
                }
            }
            break;

            case "bal":
            case "balance": {
                if (!s.hasPermission("bankplus.balance")) {
                    messMan.noPermission(s);
                    return false;
                }
                if (!(s instanceof Player)) {
                    messMan.notPlayer(s);
                    return false;
                }
                messMan.personalBalance((Player) s);
            }
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
                if (args[1] == null) {
                    messMan.specifyNumber(s);
                    return false;
                }

                long amount;
                switch (args[1]) {
                    case "all":
                        amount = EconomyManager.getBankBalance((Player) s);
                        Methods.withdraw((Player) s, amount, plugin);
                        break;

                    case "half":
                        amount = EconomyManager.getBankBalance((Player) s) / 2;
                        Methods.withdraw((Player) s, amount, plugin);
                        break;

                    default:
                        try {
                            amount = Long.parseLong(args[1]);
                            Methods.withdraw((Player) s, amount, plugin);
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
                            messMan.invalidNumber(s);
                        }
                }
            }
            break;

            case "set": {
                if (!s.hasPermission("bankplus.set")) {
                    messMan.noPermission(s);
                    return false;
                }
                if (args[1] == null) {
                    messMan.specifyPlayer(s);
                    return false;
                }
                if (args[2] == null) {
                    messMan.specifyNumber(s);
                    return false;
                }

                try {
                    long amount = Long.parseLong(args[2]);
                    Player p = Bukkit.getPlayerExact(args[1]);
                    if (p == null) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                        EconomyManager.setPlayerBankBalance(offlinePlayer, amount);
                        messMan.setMessage(s, offlinePlayer, amount);
                        return false;
                    }
                    EconomyManager.setPlayerBankBalance(p, amount);
                    messMan.setMessage(s, p, amount);
                } catch (NumberFormatException e) {
                    messMan.invalidNumber(s);
                }
            }
            break;

            case "add": {
                if (!s.hasPermission("bankplus.add")) {
                    messMan.noPermission(s);
                    return false;
                }
                if (args[1] == null) {
                    messMan.specifyPlayer(s);
                    return false;
                }
                if (args[2] == null) {
                    messMan.specifyNumber(s);
                    return false;
                }

                try {
                    long amount = Long.parseLong(args[2]);
                    Player p = Bukkit.getPlayerExact(args[1]);
                    if (p == null) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                        EconomyManager.addPlayerBankBalance(offlinePlayer, amount);
                        messMan.setMessage(s, offlinePlayer, amount);
                        return false;
                    }
                    EconomyManager.addPlayerBankBalance(p, amount);
                    messMan.setMessage(s, p, amount);
                } catch (NumberFormatException e) {
                    messMan.invalidNumber(s);
                }
            }
            break;

            case "remove": {
                if (!s.hasPermission("bankplus.remove")) {
                    messMan.noPermission(s);
                    return false;
                }
                if (args[1] == null) {
                    messMan.specifyPlayer(s);
                    return false;
                }
                if (args[2] == null) {
                    messMan.specifyNumber(s);
                    return false;
                }

                try {
                    long amount = Long.parseLong(args[2]);
                    Player p = Bukkit.getPlayerExact(args[1]);
                    if (p == null) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                        EconomyManager.removePlayerBankBalance(offlinePlayer, amount);
                        messMan.setMessage(s, offlinePlayer, amount);
                        return false;
                    }
                    EconomyManager.removePlayerBankBalance(p, amount);
                    messMan.setMessage(s, p, amount);
                } catch (NumberFormatException e) {
                    messMan.invalidNumber(s);
                }
            }
            break;

            case "interest": {
                if (!s.hasPermission("bankplus.interest.restart")) {
                    messMan.noPermission(s);
                    return false;
                }
                if (args[1] == null) {
                    messMan.interestUsage(s);
                    return false;
                }
                if (!args[1].equalsIgnoreCase("restart")) return false;

                if (!ConfigValues.isInterestEnabled()) {
                    messMan.interestIsDisabled(s);
                    return false;
                }
                try {
                    long delay = plugin.config().getLong("Interest.Delay");
                    Interest.interestCooldown.set(0, delay);
                    messMan.interestRestarted(s);
                } catch (Error e) {
                    messMan.internalError(s);
                }
            }
            break;

            case "debug":
                if (!s.hasPermission("bankplus.debug")) {
                    messMan.noPermission(s);
                    return false;
                }
                if (args[1] == null) {
                    s.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &aAvailable options: &7playerchat, guibank, interest."));
                    return false;
                }

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
        return true;
    }
}