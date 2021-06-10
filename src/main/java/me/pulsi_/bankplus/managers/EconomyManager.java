package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.entity.Player;

public class EconomyManager {

    private BankPlus plugin;
    public EconomyManager(BankPlus plugin) {
        this.plugin = plugin;
    }

    public int getPersonalBalance(Player p) {

        int personalBalance;

        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            personalBalance = plugin.getPlayers().getInt("Players." + p.getUniqueId() + ".Money");
        } else {
            personalBalance = plugin.getPlayers().getInt("Players." + p.getName() + ".Money");
        }
        return personalBalance;
    }

    public int getOthersBalance(Player target) {

        int othersBalance;

        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            othersBalance = plugin.getPlayers().getInt("Players." + target.getUniqueId() + ".Money");
        } else {
            othersBalance = plugin.getPlayers().getInt("Players." + target.getName() + ".Money");
        }
        return othersBalance;
    }

    public void withdraw(Player p, int withdraw) {

        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {

            double bankMoney = plugin.getPlayers().getDouble("Players." + p.getUniqueId() + ".Money");

            if (bankMoney >= withdraw) {
                plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", bankMoney - withdraw);
                plugin.getEconomy().depositPlayer(p, withdraw);
                p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Success-Withdraw")
                        .replace("%money%", String.valueOf(withdraw))
                        .replace("%money_formatted%", String.valueOf(MethodUtils.formatter(withdraw)))));
                String soundPath = plugin.getConfiguration().getString("General.Withdraw-Sound.Sound");
                boolean soundBoolean = plugin.getConfiguration().getBoolean("General.Withdraw-Sound.Enabled");
                MethodUtils.playSound(soundPath, p, plugin, soundBoolean);
            } else {
                p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Insufficient-Money-Withdraw")));
            }
        } else {

            double bankMoney = plugin.getPlayers().getDouble("Players." + p.getName() + ".Money");

            if (bankMoney >= withdraw) {
                plugin.getPlayers().set("Players." + p.getName() + ".Money", bankMoney - withdraw);
                plugin.getEconomy().depositPlayer(p, withdraw);
                p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Success-Withdraw")
                        .replace("%money%", String.valueOf(withdraw))
                        .replace("%money_formatted%", String.valueOf(MethodUtils.formatter(withdraw)))));
                String soundPath = plugin.getConfiguration().getString("General.Withdraw-Sound.Sound");
                boolean soundBoolean = plugin.getConfiguration().getBoolean("General.Withdraw-Sound.Enabled");
                MethodUtils.playSound(soundPath, p, plugin, soundBoolean);
            } else {
                p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Insufficient-Money-Withdraw")));
            }
        }
    }

    public void deposit(Player p, int deposit) {

        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {

            double bankMoney = plugin.getPlayers().getDouble("Players." + p.getUniqueId() + ".Money");
            double money = plugin.getEconomy().getBalance(p);

            if (money >= deposit) {
                plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", bankMoney + deposit);
                plugin.getEconomy().withdrawPlayer(p, deposit);
                p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Success-Deposit"))
                        .replace("%money%", String.valueOf(deposit))
                        .replace("%money_formatted%", String.valueOf(MethodUtils.formatter(deposit))));
                String soundPath = plugin.getConfiguration().getString("General.Deposit-Sound.Sound");
                boolean soundBoolean = plugin.getConfiguration().getBoolean("General.Deposit-Sound.Enabled");
                MethodUtils.playSound(soundPath, p, plugin, soundBoolean);
            } else {
                p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Insufficient-Money-Deposit")));
            }
        } else {

            double bankMoney = plugin.getPlayers().getDouble("Players." + p.getName() + ".Money");
            double money = plugin.getEconomy().getBalance(p);

            if (money >= deposit) {
                plugin.getPlayers().set("Players." + p.getName() + ".Money", bankMoney + deposit);
                plugin.getEconomy().withdrawPlayer(p, deposit);
                p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Success-Deposit"))
                        .replace("%money%", String.valueOf(deposit))
                        .replace("%money_formatted%", String.valueOf(MethodUtils.formatter(deposit))));
                String soundPath = plugin.getConfiguration().getString("General.Deposit-Sound.Sound");
                boolean soundBoolean = plugin.getConfiguration().getBoolean("General.Deposit-Sound.Enabled");
                MethodUtils.playSound(soundPath, p, plugin, soundBoolean);
            } else {
                p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Insufficient-Money-Deposit")));
            }
        }
    }

    public void setPlayerBankBalance(Player target, int amount) {

        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            plugin.getPlayers().set("Players." + target.getUniqueId() + ".Money", amount);
        } else {
            plugin.getPlayers().set("Players." + target.getName() + ".Money", amount);
        }
    }

    public void addPlayerBankBalance(Player target, int amount) {

        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            int targetBank = plugin.getPlayers().getInt("Players." + target.getUniqueId() + ".Money");
            plugin.getPlayers().set("Players." + target.getUniqueId() + ".Money", targetBank + amount);
        } else {
            int targetBank = plugin.getPlayers().getInt("Players." + target.getName() + ".Money");
            plugin.getPlayers().set("Players." + target.getName() + ".Money", targetBank + amount);
        }
    }

    public void removePlayerBankBalance(Player target, int amount) {

        if (plugin.getConfiguration().getBoolean("General.Use-UUID")) {
            int targetBank = plugin.getPlayers().getInt("Players." + target.getUniqueId() + ".Money");
            plugin.getPlayers().set("Players." + target.getUniqueId() + ".Money", targetBank - amount);
        } else {
            int targetBank = plugin.getPlayers().getInt("Players." + target.getName() + ".Money");
            plugin.getPlayers().set("Players." + target.getName() + ".Money", targetBank - amount);
        }
    }
}