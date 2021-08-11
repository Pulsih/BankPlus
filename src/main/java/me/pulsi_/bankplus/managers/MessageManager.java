package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("all")
public class MessageManager {

    public static void cannotDepositMore(Player p, BankPlus plugin) {
        if (plugin.getMessages().getString("Cannot-Deposit-Anymore") == null) return;
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Cannot-Deposit-Anymore")));
    }

    public static void noMoneyInterest(Player p, BankPlus plugin) {
        if (plugin.getMessages().getString("Interest-Broadcast.No-Money") == null) return;
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Interest-Broadcast.No-Money")));
    }

    public static void personalBalance(Player p, BankPlus plugin) {
        if (plugin.getMessages().getString("Personal-Bank") == null) return;
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Personal-Bank")
                .replace("%amount%", String.valueOf(new EconomyManager(plugin).getBankBalance(p)))
                .replace("%amount_formatted%", MethodUtils.format(new EconomyManager(plugin).getBankBalance(p), plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(new EconomyManager(plugin).getBankBalance(p), plugin))));
    }

    public static void successWithdraw(Player p, long amount, BankPlus plugin) {
        if (plugin.getMessages().getString("Success-Withdraw") == null) return;
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Success-Withdraw")
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }

    public static void successDeposit(Player p, long amount, BankPlus plugin) {
        if (plugin.getMessages().getString("Success-Deposit") == null) return;
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Success-Deposit")
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }

    public static void bankOthers(CommandSender s, BankPlus plugin, Player p) {
        if (plugin.getMessages().getString("Bank-Others") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Bank-Others")
                .replace("%player_name%", p.getName())
                .replace("%amount%", String.valueOf(new EconomyManager(plugin).getBankBalance(p)))
                .replace("%amount_formatted%", MethodUtils.format(new EconomyManager(plugin).getBankBalance(p), plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(new EconomyManager(plugin).getBankBalance(p), plugin))));
    }

    public static void bankOthers(CommandSender s, BankPlus plugin, OfflinePlayer p) {
        if (plugin.getMessages().getString("Bank-Others") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Bank-Others")
                .replace("%player_name%", p.getName())
                .replace("%amount%", String.valueOf(new EconomyManager(plugin).getBankBalance(p)))
                .replace("%amount_formatted%", MethodUtils.format(new EconomyManager(plugin).getBankBalance(p), plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(new EconomyManager(plugin).getBankBalance(p), plugin))));
    }

    public static void setMessage(CommandSender s, Player target, long amount, BankPlus plugin) {
        if (plugin.getMessages().getString("Set-Message") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Set-Message")
                .replace("%player_name%", target.getName())
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }
    public static void setMessage(CommandSender s, OfflinePlayer target, long amount, BankPlus plugin) {
        if (plugin.getMessages().getString("Set-Message") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Set-Message")
                .replace("%player_name%", target.getName())
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }

    public static void addMessage(CommandSender s, Player target, long amount, BankPlus plugin) {
        if (plugin.getMessages().getString("Add-Message") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Add-Message")
                .replace("%player_name%", target.getName())
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }
    public static void addMessage(CommandSender s, OfflinePlayer target, long amount, BankPlus plugin) {
        if (plugin.getMessages().getString("Add-Message") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Add-Message")
                .replace("%player_name%", target.getName())
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }

    public static void removeMessage(CommandSender s, Player target, long amount, BankPlus plugin) {
        if (plugin.getMessages().getString("Remove-Message") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Remove-Message")
                .replace("%player_name%", target.getName())
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }
    public static void removeMessage(CommandSender s, OfflinePlayer target, long amount, BankPlus plugin) {
        if (plugin.getMessages().getString("Remove-Message") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Remove-Message")
                .replace("%player_name%", target.getName())
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }

    public static void interestBroadcastMessage(Player p, BankPlus plugin, long bankBalance, double finalMoneyPercentage) {
        if (plugin.getMessages().getString("Interest-Broadcast.Message") == null) return;
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
        if (plugin.getMessages().getString("Interest-Broadcast.Message") == null) return;
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Interest-Broadcast.Message")
                .replace("%amount%", String.valueOf(amount))
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))));
    }

    public static void insufficientMoneyWithdraw(Player p, BankPlus plugin) {
        if (plugin.getMessages().getString("Insufficient-Money-Withdraw") == null) return;
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Insufficient-Money-Withdraw")));
    }

    public static void insufficientMoneyDeposit(Player p, BankPlus plugin) {
        if (plugin.getMessages().getString("Insufficient-Money-Deposit") == null) return;
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Insufficient-Money-Deposit")));
    }

    public static void noPermission(CommandSender s, BankPlus plugin) {
        if (plugin.getMessages().getString("No-Permission") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
    }

    public static void cannotFindPlayer(CommandSender s, BankPlus plugin) {
        if (plugin.getMessages().getString("Cannot-Find-Player") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Cannot-Find-Player")));
    }

    public static void notPlayer(CommandSender s, BankPlus plugin) {
        if (plugin.getMessages().getString("Not-Player") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Not-Player")));
    }

    public static void invalidNumber(CommandSender s, BankPlus plugin) {
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Invalid-Number")));
    }

    public static void invalidNumber(Player p, BankPlus plugin) {
        if (plugin.getMessages().getString("Invalid-Number") == null) return;
        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Invalid-Number")));
    }

    public static void specifyNumber(CommandSender s, BankPlus plugin) {
        if (plugin.getMessages().getString("Specify-Number") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Specify-Number")));
    }

    public static void specifyPlayer(CommandSender s, BankPlus plugin) {
        if (plugin.getMessages().getString("Specify-Player") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Specify-Player")));
    }

    public static void unknownCommand(CommandSender s, BankPlus plugin) {
        if (plugin.getMessages().getString("Unknown-Command") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Unknown-Command")));
    }

    public static void interestIsDisabled(CommandSender s, BankPlus plugin) {
        if (plugin.getMessages().getString("Interest-Disabled") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Interest-Disabled")));
    }

    public static void internalError(CommandSender s, BankPlus plugin) {
        if (plugin.getMessages().getString("Error") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Error")));
    }

    public static void interestUsage(CommandSender s, BankPlus plugin) {
        if (plugin.getMessages().getString("Interest-Usage") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Interest-Usage")));
    }

    public static void interestRestarted(CommandSender s, BankPlus plugin) {
        if (plugin.getMessages().getString("Interest-Restarted") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Interest-Restarted")));
    }

    public static void reloadMessage(CommandSender s, BankPlus plugin) {
        if (plugin.getMessages().getString("Reload") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Reload")));
    }

    public static void cannotUseBankHere(CommandSender s, BankPlus plugin) {
        if (plugin.getMessages().getString("Cannot-Use-Bank-Here") == null) return;
        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Cannot-Use-Bank-Here")));
    }
}