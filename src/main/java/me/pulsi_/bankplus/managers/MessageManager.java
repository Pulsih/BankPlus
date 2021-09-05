package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageManager {

    private BankPlus plugin;
    public MessageManager(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void cannotDepositMore(Player p) {
        if (plugin.messages().getString("Cannot-Deposit-Anymore") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Cannot-Deposit-Anymore")));
    }

    public void noMoneyInterest(Player p) {
        if (plugin.messages().getString("Interest-Broadcast.No-Money") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Interest-Broadcast.No-Money")));
    }

    public void personalBalance(Player p) {
        if (plugin.messages().getString("Personal-Bank") == null) return;
        final long amount = new EconomyManager(plugin).getBankBalance(p);
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Personal-Bank")
                .replace("%amount%", MethodUtils.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", MethodUtils.format(new EconomyManager(plugin).getBankBalance(p), plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(new EconomyManager(plugin).getBankBalance(p), plugin))
        ));
    }

    public void successWithdraw(Player p, long amount) {
        if (plugin.messages().getString("Success-Withdraw") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Success-Withdraw")
                .replace("%amount%", MethodUtils.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))
        ));
    }

    public void successDeposit(Player p, long amount) {
        if (plugin.messages().getString("Success-Deposit") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Success-Deposit")
                .replace("%amount%", MethodUtils.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))
        ));
    }

    public void bankOthers(CommandSender s, Player p) {
        if (plugin.messages().getString("Bank-Others") == null) return;
        final long amount = new EconomyManager(plugin).getBankBalance(p);
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Bank-Others")
                .replace("%player_name%", p.getName())
                .replace("%amount%", MethodUtils.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))
        ));
    }

    public void bankOthers(CommandSender s, OfflinePlayer p) {
        if (plugin.messages().getString("Bank-Others") == null) return;
        final long amount = new EconomyManager(plugin).getBankBalance(p);
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Bank-Others")
                .replace("%player_name%", p.getName())
                .replace("%amount%", MethodUtils.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))
        ));
    }

    public void setMessage(CommandSender s, Player p, long amount) {
        if (plugin.messages().getString("Set-Message") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Set-Message")
                .replace("%player_name%", p.getName())
                .replace("%amount%", MethodUtils.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))
        ));
    }
    public void setMessage(CommandSender s, OfflinePlayer p, long amount) {
        if (plugin.messages().getString("Set-Message") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Set-Message")
                .replace("%player_name%", p.getName())
                .replace("%amount%", MethodUtils.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))
        ));
    }

    public void addMessage(CommandSender s, Player p, long amount) {
        if (plugin.messages().getString("Add-Message") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Add-Message")
                .replace("%player_name%", p.getName())
                .replace("%amount%", MethodUtils.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))
        ));
    }
    public void addMessage(CommandSender s, OfflinePlayer p, long amount) {
        if (plugin.messages().getString("Add-Message") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Add-Message")
                .replace("%player_name%", p.getName())
                .replace("%amount%", MethodUtils.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))
        ));
    }

    public void removeMessage(CommandSender s, Player p, long amount) {
        if (plugin.messages().getString("Remove-Message") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Remove-Message")
                .replace("%player_name%", p.getName())
                .replace("%amount%", MethodUtils.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))
        ));
    }
    public void removeMessage(CommandSender s, OfflinePlayer p, long amount) {
        if (plugin.messages().getString("Remove-Message") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Remove-Message")
                .replace("%player_name%", p.getName())
                .replace("%amount%", MethodUtils.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))
        ));
    }

    public void interestBroadcastMessage(Player p, long bankBalance, double finalMoneyPercentage) {
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
                    .replace("%amount%", MethodUtils.formatCommas(amount))
                    .replace("%amount_long%", "" + amount)
                    .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                    .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))
            ));
        }
    }

    public void interestBroadcastMessageMax(Player p, long amount) {
        if (plugin.messages().getString("Interest-Broadcast.Message") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Interest-Broadcast.Message")
                .replace("%amount%", MethodUtils.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", MethodUtils.format(amount, plugin))
                .replace("%amount_formatted_long%", MethodUtils.formatLong(amount, plugin))
        ));
    }

    public void insufficientMoneyWithdraw(Player p) {
        if (plugin.messages().getString("Insufficient-Money-Withdraw") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Insufficient-Money-Withdraw")));
    }

    public void insufficientMoneyDeposit(Player p) {
        if (plugin.messages().getString("Insufficient-Money-Deposit") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Insufficient-Money-Deposit")));
    }

    public void noPermission(CommandSender s) {
        if (plugin.messages().getString("No-Permission") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("No-Permission")));
    }

    public void cannotFindPlayer(CommandSender s) {
        if (plugin.messages().getString("Cannot-Find-Player") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Cannot-Find-Player")));
    }

    public void notPlayer(CommandSender s) {
        if (plugin.messages().getString("Not-Player") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Not-Player")));
    }

    public void invalidNumber(CommandSender s) {
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Invalid-Number")));
    }

    public void invalidNumber(Player p) {
        if (plugin.messages().getString("Invalid-Number") == null) return;
        p.sendMessage(ChatUtils.color(plugin.messages().getString("Invalid-Number")));
    }

    public void specifyNumber(CommandSender s) {
        if (plugin.messages().getString("Specify-Number") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Specify-Number")));
    }

    public void specifyPlayer(CommandSender s) {
        if (plugin.messages().getString("Specify-Player") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Specify-Player")));
    }

    public void unknownCommand(CommandSender s) {
        if (plugin.messages().getString("Unknown-Command") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Unknown-Command")));
    }

    public void interestIsDisabled(CommandSender s) {
        if (plugin.messages().getString("Interest-Disabled") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Interest-Disabled")));
    }

    public void internalError(CommandSender s) {
        if (plugin.messages().getString("Error") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Error")));
    }

    public void interestUsage(CommandSender s) {
        if (plugin.messages().getString("Interest-Usage") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Interest-Usage")));
    }

    public void interestRestarted(CommandSender s) {
        if (plugin.messages().getString("Interest-Restarted") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Interest-Restarted")));
    }

    public void reloadMessage(CommandSender s) {
        if (plugin.messages().getString("Reload") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Reload")));
    }

    public void cannotUseBankHere(CommandSender s) {
        if (plugin.messages().getString("Cannot-Use-Bank-Here") == null) return;
        s.sendMessage(ChatUtils.color(plugin.messages().getString("Cannot-Use-Bank-Here")));
    }
}