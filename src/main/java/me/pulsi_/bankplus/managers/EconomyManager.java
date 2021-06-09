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
        return plugin.getPlayers().getInt("Players." + p.getUniqueId() + ".Money");
    }

    public int getOthersBalance(Player target) {
        return plugin.getPlayers().getInt("Players." + target.getUniqueId() + ".Money");
    }

    public void withdraw(Player p, int withdraw) {

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
    }

    public void deposit(Player p, int deposit) {

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
    }

    public void setPlayerBankBalance(Player target, int amount) {
        plugin.getPlayers().set("Players." + target.getUniqueId() + ".Money", amount);
    }

    public void addPlayerBankBalance(Player target, int amount) {
        int targetBank = plugin.getPlayers().getInt("Players." + target.getUniqueId() + ".Money");
        plugin.getPlayers().set("Players." + target.getUniqueId() + ".Money", targetBank + amount);
    }

    public void removePlayerBankBalance(Player target, int amount) {
        int targetBank = plugin.getPlayers().getInt("Players." + target.getUniqueId() + ".Money");
        plugin.getPlayers().set("Players." + target.getUniqueId() + ".Money", targetBank - amount);
    }
}