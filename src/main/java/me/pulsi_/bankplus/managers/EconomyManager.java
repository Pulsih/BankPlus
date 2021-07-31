package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EconomyManager {

    public static long getPersonalBalance(Player p, BankPlus plugin) {
        long personalBalance;
        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            personalBalance = plugin.getPlayers().getLong("Players." + p.getUniqueId() + ".Money");
        } else {
            personalBalance = plugin.getPlayers().getLong("Players." + p.getName() + ".Money");
        }
        return personalBalance;
    }

    public static long getOthersBalance(Player target, BankPlus plugin) {
        long othersBalance;
        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            othersBalance = plugin.getPlayers().getLong("Players." + target.getUniqueId() + ".Money");
        } else {
            othersBalance = plugin.getPlayers().getLong("Players." + target.getName() + ".Money");
        }
        return othersBalance;
    }

    public static long getOthersBalance(OfflinePlayer target, BankPlus plugin) {
        long othersBalance;
        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            othersBalance = plugin.getPlayers().getLong("Players." + target.getUniqueId() + ".Money");
        } else {
            othersBalance = plugin.getPlayers().getLong("Players." + target.getName() + ".Money");
        }
        return othersBalance;
    }

    public static void withdraw(Player p, long withdraw, BankPlus plugin) {
        long bankMoney = getOthersBalance(p, plugin);
        if (bankMoney >= withdraw) {
            if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
                plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", bankMoney - withdraw);
            } else {
                plugin.getPlayers().set("Players." + p.getName() + ".Money", bankMoney - withdraw);
            }
            plugin.getEconomy().depositPlayer(p, withdraw);
            MethodUtils.playSound(plugin.getConfiguration().getString("General.Withdraw-Sound.Sound"), p, plugin, plugin.getConfiguration().getBoolean("General.Withdraw-Sound.Enabled"));
            MessageManager.successWithdraw(p, withdraw, plugin);
            plugin.savePlayers();
        } else {
            MessageManager.insufficientMoneyWithdraw(p, plugin);
        }
    }

    public static void deposit(Player p, long deposit, BankPlus plugin) {
        long money = (long) plugin.getEconomy().getBalance(p);
        long bankMoney = getOthersBalance(p, plugin);
        long maxCapacity = plugin.getConfiguration().getLong("General.Max-Bank-Capacity");

        if (money < deposit) {
            MessageManager.insufficientMoneyDeposit(p, plugin);
            return;
        }

        if (maxCapacity != 0) {
            if (bankMoney >= maxCapacity) {
                MessageManager.cannotDepositMore(p, plugin);
                return;
            }
            if (bankMoney + deposit >= maxCapacity) {
                if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
                    plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", maxCapacity);
                } else {
                    plugin.getPlayers().set("Players." + p.getName() + ".Money", maxCapacity);
                }
                plugin.getEconomy().withdrawPlayer(p, maxCapacity - bankMoney);
                deposit(p, maxCapacity - bankMoney, plugin);
                MessageManager.successDeposit(p, maxCapacity - bankMoney, plugin);
                return;
            }
        } else {
            if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
                plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", bankMoney + deposit);
            } else {
                plugin.getPlayers().set("Players." + p.getName() + ".Money", bankMoney + deposit);
            }
            plugin.getEconomy().withdrawPlayer(p, deposit);
            MessageManager.successDeposit(p, deposit, plugin);
            MethodUtils.playSound(plugin.getConfiguration().getString("General.Deposit-Sound.Sound"), p, plugin, plugin.getConfiguration().getBoolean("General.Deposit-Sound.Enabled"));
            plugin.savePlayers();
        }
    }

    public static void setPlayerBankBalance(CommandSender s, Player target, long amount, BankPlus plugin) {
        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            plugin.getPlayers().set("Players." + target.getUniqueId() + ".Money", amount);
        } else {
            plugin.getPlayers().set("Players." + target.getName() + ".Money", amount);
        }
        MessageManager.setMessage(s, plugin, target, amount);
        plugin.savePlayers();
    }

    public static void addPlayerBankBalance(CommandSender s, Player target, long amount, BankPlus plugin) {
        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            long targetBank = plugin.getPlayers().getLong("Players." + target.getUniqueId() + ".Money");
            plugin.getPlayers().set("Players." + target.getUniqueId() + ".Money", targetBank + amount);
        } else {
            long targetBank = plugin.getPlayers().getLong("Players." + target.getName() + ".Money");
            plugin.getPlayers().set("Players." + target.getName() + ".Money", targetBank + amount);
        }
        MessageManager.addMessage(s, plugin, target, amount);
        plugin.savePlayers();
    }

    public static void removePlayerBankBalance(CommandSender s, Player target, long amount, BankPlus plugin) {
        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            long targetBank = plugin.getPlayers().getLong("Players." + target.getUniqueId() + ".Money");
            if (targetBank >= 0) {
                plugin.getPlayers().set("Players." + target.getUniqueId() + ".Money", targetBank - amount);
            } else {
                plugin.getPlayers().set("Players." + target.getUniqueId() + ".Money", 0);
            }
        } else {
            long targetBank = plugin.getPlayers().getLong("Players." + target.getName() + ".Money");
            if (targetBank >= 0) {
                plugin.getPlayers().set("Players." + target.getName() + ".Money", targetBank - amount);
            } else {
                plugin.getPlayers().set("Players." + target.getName() + ".Money", 0);
            }
        }
        MessageManager.removeMessage(s, plugin, target, amount);
        plugin.savePlayers();
    }
}