package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class EconomyManager {

    public static long getBankBalance(Player target, BankPlus plugin) {
        long othersBalance;
        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            othersBalance = plugin.getPlayers().getLong("Players." + target.getUniqueId() + ".Money");
        } else {
            othersBalance = plugin.getPlayers().getLong("Players." + target.getName() + ".Money");
        }
        return othersBalance;
    }
    public static long getBankBalance(OfflinePlayer target, BankPlus plugin) {
        long othersBalance;
        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            othersBalance = plugin.getPlayers().getLong("Players." + target.getUniqueId() + ".Money");
        } else {
            othersBalance = plugin.getPlayers().getLong("Players." + target.getName() + ".Money");
        }
        return othersBalance;
    }

    public static void withdraw(Player p, long withdraw, BankPlus plugin) {
        long bankMoney = getBankBalance(p, plugin);
        plugin.getEconomy().depositPlayer(p, withdraw);
        setValue(p, bankMoney - withdraw, plugin);
    }

    public static void deposit(Player p, long deposit, BankPlus plugin) {
        long bankMoney = getBankBalance(p, plugin);
        plugin.getEconomy().withdrawPlayer(p, deposit);
        setValue(p, bankMoney + deposit, plugin);
    }

    public static void setPlayerBankBalance(Player target, long amount, BankPlus plugin) {
        setValue(target, amount, plugin);
    }
    public static void setPlayerBankBalance(OfflinePlayer target, long amount, BankPlus plugin) {
        setValue(target, amount, plugin);
    }

    public static void addPlayerBankBalance(Player target, long amount, BankPlus plugin) {
        long targetBank = getBankBalance(target, plugin);
        setValue(target, targetBank + amount, plugin);
    }
    public static void addPlayerBankBalance(OfflinePlayer target, long amount, BankPlus plugin) {
        long targetBank = getBankBalance(target, plugin);
        setValue(target, targetBank + amount, plugin);
    }

    public static void removePlayerBankBalance(Player target, long amount, BankPlus plugin) {
        long targetBank = getBankBalance(target, plugin);
        setValue(target, targetBank - amount, plugin);
    }
    public static void removePlayerBankBalance(OfflinePlayer target, long amount, BankPlus plugin) {
        long targetBank = getBankBalance(target, plugin);
        setValue(target, targetBank - amount, plugin);
    }

    private static void setValue(Player p, long amount, BankPlus plugin) {
        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", amount);
        } else {
            plugin.getPlayers().set("Players." + p.getName() + ".Money", amount);
        }
        plugin.savePlayers();
    }
    private static void setValue(OfflinePlayer p, long amount, BankPlus plugin) {
        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", amount);
        } else {
            plugin.getPlayers().set("Players." + p.getName() + ".Money", amount);
        }
        plugin.savePlayers();
    }
}