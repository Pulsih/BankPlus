package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageManager {

    public static void chatWithdraw(Player p) {
        String message = Values.MESSAGES.getChatWithdraw();
        if (message != null) p.sendMessage(ChatUtils.color(message));
    }

    public static void chatDeposit(Player p) {
        String message = Values.MESSAGES.getChatDeposit();
        if (message != null) p.sendMessage(ChatUtils.color(message));
    }

    public static void cannotDepositMore(Player p) {
        String message = Values.MESSAGES.getCannotDepositAnymore();
        if (message != null) p.sendMessage(ChatUtils.color(message));
    }

    public static void noMoneyInterest(Player p) {
        String message = Values.MESSAGES.getNoMoneyInterest();
        if (message != null) p.sendMessage(ChatUtils.color(message));
    }

    public static void personalBalance(Player p) {
        String message = Values.MESSAGES.getPersonalBank();
        if (message == null) return;
        long amount = EconomyManager.getBankBalance(p);
        p.sendMessage(ChatUtils.color(message
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void minimumAmountAlert(Player p) {
        String message = Values.MESSAGES.getMinimumAmount();
        if (message != null) p.sendMessage(ChatUtils.color(message));
    }
    
    public static void cannotUseNegativeNumber(Player p) {
        String message = Values.MESSAGES.getCannotUseNegativeNumber();
        if (message != null) p.sendMessage(ChatUtils.color(message));
    }

    public static void cannotUseNegativeNumber(CommandSender s) {
        String message = Values.MESSAGES.getCannotUseNegativeNumber();
        if (message != null) s.sendMessage(ChatUtils.color(message));
    }

    public static void successWithdraw(Player p, long amount) {
        if (plugin.messages().getString("Success-Withdraw") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Success-Withdraw")
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void successDeposit(Player p, long amount) {
        if (plugin.messages().getString("Success-Deposit") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Success-Deposit")
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void bankOthers(CommandSender s, Player p) {
        if (plugin.messages().getString("Bank-Others") == null) return;
        final long amount = EconomyManager.getBankBalance(p);
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Bank-Others")
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void bankOthers(CommandSender s, OfflinePlayer p) {
        if (plugin.messages().getString("Bank-Others") == null) return;
        final long amount = EconomyManager.getBankBalance(p);
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Bank-Others")
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void setMessage(CommandSender s, Player p, long amount) {
        if (plugin.messages().getString("Set-Message") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Set-Message")
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void setMessage(CommandSender s, OfflinePlayer p, long amount) {
        if (plugin.messages().getString("Set-Message") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Set-Message")
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void addMessage(CommandSender s, Player p, long amount) {
        if (plugin.messages().getString("Add-Message") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Add-Message")
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void addMessage(CommandSender s, OfflinePlayer p, long amount) {
        if (plugin.messages().getString("Add-Message") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Add-Message")
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void removeMessage(CommandSender s, Player p, long amount) {
        if (plugin.messages().getString("Remove-Message") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Remove-Message")
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void removeMessage(CommandSender s, OfflinePlayer p, long amount) {
        if (plugin.messages().getString("Remove-Message") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Remove-Message")
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void interestBroadcastMessage(Player p, long bankBalance, double finalMoneyPercentage) {
        if (plugin.messages().getString("Interest-Broadcast.Message") == null) return;
        final long amount = (long) (bankBalance * finalMoneyPercentage);
        if (amount == 0) {
            p.sendMessage(ChatUtils.color(plugin.messages().getString("Interest-Broadcast.Message")
                    .replace("%amount%", "1")
                    .replace("%amount_long%", "1")
                    .replace("%amount_formatted%", "1")
                    .replace("%amount_formatted_long%", "1")
            ));
        } else {
            p.sendMessage(ChatUtils.color(plugin.messages().getString("Interest-Broadcast.Message")
                    .replace("%amount%", Methods.formatCommas(amount))
                    .replace("%amount_long%", "" + amount)
                    .replace("%amount_formatted%", Methods.format(amount))
                    .replace("%amount_formatted_long%", Methods.formatLong(amount))
            ));
        }
    }

    public static void interestBroadcastMessageMax(Player p, long amount) {
        if (plugin.messages().getString("Interest-Broadcast.Message") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Interest-Broadcast.Message")
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void insufficientMoneyWithdraw(Player p) {
        if (plugin.messages().getString("Insufficient-Money-Withdraw") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Insufficient-Money-Withdraw")));
    }

    public static void insufficientMoneyDeposit(Player p) {
        if (plugin.messages().getString("Insufficient-Money-Deposit") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Insufficient-Money-Deposit")));
    }

    public static void noPermission(CommandSender s) {
        String message = Values.MESSAGES.getNoPermission();
        if (message != null) s.sendMessage(ChatUtils.color(message));
    }

    public static void cannotFindPlayer(CommandSender s) {
        if (plugin.messages().getString("Cannot-Find-Player") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Cannot-Find-Player")));
    }

    public static void notPlayer(CommandSender s) {
        if (plugin.messages().getString("Not-Player") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Not-Player")));
    }

    public static void invalidNumber(CommandSender s) {
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Invalid-Number")));
    }

    public static void invalidNumber(Player p) {
        if (plugin.messages().getString("Invalid-Number") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Invalid-Number")));
    }

    public static void specifyNumber(CommandSender s) {
        if (plugin.messages().getString("Specify-Number") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Specify-Number")));
    }

    public static void specifyPlayer(CommandSender s) {
        if (plugin.messages().getString("Specify-Player") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Specify-Player")));
    }

    public static void unknownCommand(CommandSender s) {
        if (plugin.messages().getString("Unknown-Command") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Unknown-Command")));
    }

    public static void interestIsDisabled(CommandSender s) {
        if (plugin.messages().getString("Interest-Disabled") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Interest-Disabled")));
    }

    public static void internalError(CommandSender s) {
        if (plugin.messages().getString("Error") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Error")));
    }

    public static void interestUsage(CommandSender s) {
        if (plugin.messages().getString("Interest-Usage") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Interest-Usage")));
    }

    public static void interestRestarted(CommandSender s) {
        if (plugin.messages().getString("Interest-Restarted") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Interest-Restarted")));
    }

    public static void reloadMessage(CommandSender s) {
        String message = Values.MESSAGES.getReloadMessage();
        if (message != null) s.sendMessage(ChatUtils.color(message));
    }

    public static void cannotUseBankHere(Player p) {
        String message = Values.MESSAGES.getCannotUseBankHere();
        if (message != null) p.sendMessage(ChatUtils.color(message));
    }
}