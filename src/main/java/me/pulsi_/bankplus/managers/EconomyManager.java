package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class EconomyManager {

    private BankPlus plugin;
    public EconomyManager(BankPlus plugin) {
        this.plugin = plugin;
    }

    public static long getPersonalBalance(Player p, BankPlus plugin) {

        long personalBalance;

        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            personalBalance = plugin.getPlayers().getLong("Players." + p.getUniqueId() + ".Money");
        } else {
            personalBalance = plugin.getPlayers().getLong("Players." + p.getName() + ".Money");
        }
        return personalBalance;
    }

    public long getOthersBalance(Player target) {

        long othersBalance;

        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            othersBalance = plugin.getPlayers().getLong("Players." + target.getUniqueId() + ".Money");
        } else {
            othersBalance = plugin.getPlayers().getLong("Players." + target.getName() + ".Money");
        }
        return othersBalance;
    }

    public void withdraw(Player p, long withdraw) throws IOException {

        long bankMoney;
        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            bankMoney = plugin.getPlayers().getLong("Players." + p.getUniqueId() + ".Money");
        } else {
            bankMoney = plugin.getPlayers().getLong("Players." + p.getName() + ".Money");
        }

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

    public void deposit(Player p, long deposit) throws IOException {

        long money = (long) plugin.getEconomy().getBalance(p);
        long bankMoney;
        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            bankMoney = plugin.getPlayers().getLong("Players." + p.getUniqueId() + ".Money");
        } else {
            bankMoney = plugin.getPlayers().getLong("Players." + p.getName() + ".Money");
        }

        if (money >= deposit) {
            if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
                plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", bankMoney + deposit);
            } else {
                plugin.getPlayers().set("Players." + p.getName() + ".Money", bankMoney + deposit);
            }
            plugin.getEconomy().withdrawPlayer(p, deposit);
            MethodUtils.playSound(plugin.getConfiguration().getString("General.Deposit-Sound.Sound"), p, plugin, plugin.getConfiguration().getBoolean("General.Deposit-Sound.Enabled"));
            MessageManager.successDeposit(p, deposit, plugin);
            MethodUtils.playSound(plugin.getConfiguration().getString("General.Deposit-Sound.Sound"), p, plugin, plugin.getConfiguration().getBoolean("General.Deposit-Sound.Enabled"));
            plugin.savePlayers();
        } else {
            MessageManager.insufficientMoneyDeposit(p, plugin);
        }
    }

    public void setPlayerBankBalance(CommandSender s, Player target, long amount) throws IOException {

        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            plugin.getPlayers().set("Players." + target.getUniqueId() + ".Money", amount);
        } else {
            plugin.getPlayers().set("Players." + target.getName() + ".Money", amount);
        }
        MessageManager.setMessage(s, plugin, target, amount);
        plugin.savePlayers();
    }

    public void addPlayerBankBalance(CommandSender s, Player target, long amount) throws IOException {

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

    public void removePlayerBankBalance(CommandSender s, Player target, long amount) throws IOException {

        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            long targetBank = plugin.getPlayers().getLong("Players." + target.getUniqueId() + ".Money");
            plugin.getPlayers().set("Players." + target.getUniqueId() + ".Money", targetBank - amount);
        } else {
            long targetBank = plugin.getPlayers().getLong("Players." + target.getName() + ".Money");
            plugin.getPlayers().set("Players." + target.getName() + ".Money", targetBank - amount);
        }
        MessageManager.removeMessage(s, plugin, target, amount);
        plugin.savePlayers();
    }
}