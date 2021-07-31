package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageManager {

    public static void cannotDepositMore(Player p, BankPlus plugin) {
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Cannot-Deposit-Anymore")));
    }

    public static void personalBalance(Player p, BankPlus plugin) {
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Personal-Bank")
                .replace("%amount%", String.valueOf(EconomyManager.getPersonalBalance(p, plugin)))
                .replace("%amount_formatted%", MethodUtils.format(EconomyManager.getPersonalBalance(p, plugin), plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(EconomyManager.getPersonalBalance(p, plugin), plugin))));
    }

    public static void successWithdraw(Player p, long amount, BankPlus plugin) {
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Success-Withdraw")
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }

    public static void successDeposit(Player p, long amount, BankPlus plugin) {
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Success-Deposit")
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }

    public static void bankOthers(CommandSender s, BankPlus plugin, Player target) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Bank-Others")
                .replace("%player_name%", target.getName())
                .replace("%amount%", String.valueOf(EconomyManager.getOthersBalance(target, plugin)))
                .replace("%amount_formatted%", MethodUtils.format(EconomyManager.getOthersBalance(target, plugin), plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(EconomyManager.getOthersBalance(target, plugin), plugin))));
    }

    public static void bankOthers(CommandSender s, BankPlus plugin, OfflinePlayer target) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Bank-Others")
                .replace("%player_name%", target.getName())
                .replace("%amount%", String.valueOf(EconomyManager.getOthersBalance(target, plugin)))
                .replace("%amount_formatted%", MethodUtils.format(EconomyManager.getOthersBalance(target, plugin), plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(EconomyManager.getOthersBalance(target, plugin), plugin))));
    }

    public static void setMessage(CommandSender s, BankPlus plugin, Player target, long amount) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Set-Message")
                .replace("%player_name%", target.getName())
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }

    public static void addMessage(CommandSender s, BankPlus plugin, Player target, long amount) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Add-Message")
                .replace("%player_name%", target.getName())
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }

    public static void removeMessage(CommandSender s, BankPlus plugin, Player target, long amount) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Remove-Message")
                .replace("%player_name%", target.getName())
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }

    public static void interestBroadcastMessage(Player p, BankPlus plugin, long bankBalance, double finalMoneyPercentage) {

        if ((long) (bankBalance * finalMoneyPercentage) == 0) {
            p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Interest-Broadcast.Message")
                    .replace("%amount%", "1")
                    .replace("%amount_formatted%", "1")
                    .replace("%amount_formatted_long%", "1")));
        } else {
            p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Interest-Broadcast.Message")
                    .replace("%amount%", String.valueOf((long) (bankBalance * finalMoneyPercentage)))
                    .replace("%amount_formatted%", MethodUtils.format((long) (bankBalance * finalMoneyPercentage), plugin))
                    .replace("%amount_formatted_long%", MethodUtils.formatLong((long) (bankBalance * finalMoneyPercentage), plugin))));
        }
    }

    public static void interestBroadcastMessageMax(Player p, BankPlus plugin, long amount) {

            p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Interest-Broadcast.Message")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                    .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }

    public static void insufficientMoneyWithdraw(Player p, BankPlus plugin) {
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Insufficient-Money-Withdraw")));
    }

    public static void insufficientMoneyDeposit(Player p, BankPlus plugin) {
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Insufficient-Money-Deposit")));
    }

    public static void noPermission(CommandSender s, BankPlus plugin) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
    }

    public static void cannotFindPlayer(CommandSender s, BankPlus plugin) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Cannot-Find-Player")));
    }

    public static void notPlayer(CommandSender s, BankPlus plugin) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Not-Player")));
    }

    public static void invalidNumber(CommandSender s, BankPlus plugin) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Invalid-Number")));
    }

    public static void invalidNumber(Player p, BankPlus plugin) {
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Invalid-Number")));
    }

    public static void specifyNumber(CommandSender s, BankPlus plugin) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Specify-Number")));
    }

    public static void unknownCommand(CommandSender s, BankPlus plugin) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Unknown-Command")));
    }

    public static void interestIsDisabled(CommandSender s, BankPlus plugin) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Interest-Disabled")));
    }

    public static void internalError(CommandSender s, BankPlus plugin) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Error")));
    }

    public static void interestRestarted(CommandSender s, BankPlus plugin) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Interest-Restarted")));
    }
}