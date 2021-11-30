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
        long amount = EconomyManager.getInstance().getBankBalance(p);
        p.sendMessage(ChatUtils.color(message
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void minimumAmountAlert(Player p) {
        String message = Values.MESSAGES.getMinimumAmount();
        if (message != null) p.sendMessage(ChatUtils.color(message
                .replace("%minimum%", "" + Values.CONFIG.getMinimumAmount())
        ));
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
        String message = Values.MESSAGES.getSuccessWithdraw();
        if (message != null) p.sendMessage(ChatUtils.color(message
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void successDeposit(Player p, long amount) {
        String message = Values.MESSAGES.getSuccessDeposit();
        if (message != null) p.sendMessage(ChatUtils.color(message
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void bankOthers(CommandSender s, Player p) {
        String message = Values.MESSAGES.getBankOthers();
        if (message == null) return;
        long amount = EconomyManager.getInstance().getBankBalance(p);
        s.sendMessage(ChatUtils.color(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void bankOthers(CommandSender s, OfflinePlayer p) {
        String message = Values.MESSAGES.getBankOthers();
        if (message == null) return;
        long amount = EconomyManager.getInstance().getBankBalance(p);
        s.sendMessage(ChatUtils.color(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void setMessage(CommandSender s, Player p, long amount) {
        String message = Values.MESSAGES.getSetMessage();
        if (message != null) s.sendMessage(ChatUtils.color(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void setMessage(CommandSender s, OfflinePlayer p, long amount) {
        String message = Values.MESSAGES.getSetMessage();
        if (message != null) s.sendMessage(ChatUtils.color(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void addMessage(CommandSender s, Player p, long amount) {
        String message = Values.MESSAGES.getAddMessage();
        if (message != null) s.sendMessage(ChatUtils.color(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void addMessage(CommandSender s, OfflinePlayer p, long amount) {
        String message = Values.MESSAGES.getAddMessage();
        if (message != null) s.sendMessage(ChatUtils.color(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void removeMessage(CommandSender s, Player p, long amount) {
        String message = Values.MESSAGES.getRemoveMessage();
        if (message != null) s.sendMessage(ChatUtils.color(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void removeMessage(CommandSender s, OfflinePlayer p, long amount) {
        String message = Values.MESSAGES.getRemoveMessage();
        if (message != null) s.sendMessage(ChatUtils.color(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void interestBroadcastMessage(Player p, long bankBalance, double finalMoneyPercentage) {
        String message = Values.MESSAGES.getInterestBroadcastMessage();
        if (message == null) return;
        long amount = (long) (bankBalance * finalMoneyPercentage);
        if (amount == 0) {
            p.sendMessage(ChatUtils.color(message
                    .replace("%amount%", "1")
                    .replace("%amount_long%", "1")
                    .replace("%amount_formatted%", "1")
                    .replace("%amount_formatted_long%", "1")
            ));
        } else {
            p.sendMessage(ChatUtils.color(message
                    .replace("%amount%", Methods.formatCommas(amount))
                    .replace("%amount_long%", "" + amount)
                    .replace("%amount_formatted%", Methods.format(amount))
                    .replace("%amount_formatted_long%", Methods.formatLong(amount))
            ));
        }
    }

    public static void interestBroadcastMessageMax(Player p, long amount) {
        String message = Values.MESSAGES.getInterestBroadcastMessage();
        if (message != null) p.sendMessage(ChatUtils.color(message
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void insufficientMoneyWithdraw(Player p) {
        String message = Values.MESSAGES.getInsufficientMoneyWithdraw();
        if (message != null) p.sendMessage(ChatUtils.color(message));
    }

    public static void insufficientMoneyDeposit(Player p) {
        String message = Values.MESSAGES.getInsufficientMoneyDeposit();
        if (message != null) p.sendMessage(ChatUtils.color(message));
    }

    public static void noPermission(CommandSender s) {
        String message = Values.MESSAGES.getNoPermission();
        if (message != null) s.sendMessage(ChatUtils.color(message));
    }

    public static void cannotFindPlayer(CommandSender s) {
        String message = Values.MESSAGES.getCannotFindPlayer();
        if (message != null) s.sendMessage(ChatUtils.color(message));
    }

    public static void notPlayer(CommandSender s) {
        String message = Values.MESSAGES.getNotPlayer();
        if (message != null) s.sendMessage(ChatUtils.color(message));
    }

    public static void invalidNumber(CommandSender s) {
        String message = Values.MESSAGES.getInvalidNumber();
        if (message != null) s.sendMessage(ChatUtils.color(message));
    }

    public static void invalidNumber(Player p) {
        String message = Values.MESSAGES.getInvalidNumber();
        if (message != null) p.sendMessage(ChatUtils.color(message));
    }

    public static void specifyNumber(CommandSender s) {
        String message = Values.MESSAGES.getSpecifyNumber();
        if (message != null) s.sendMessage(ChatUtils.color(message));
    }

    public static void specifyPlayer(CommandSender s) {
        String message = Values.MESSAGES.getSpecifyPlayer();
        if (message != null) s.sendMessage(ChatUtils.color(message));
    }

    public static void unknownCommand(CommandSender s) {
        String message = Values.MESSAGES.getUnknownCommand();
        if (message != null) s.sendMessage(ChatUtils.color(message));
    }

    public static void interestIsDisabled(CommandSender s) {
        String message = Values.MESSAGES.getInterestDisabled();
        if (message != null) s.sendMessage(ChatUtils.color(message));
    }

    public static void internalError(CommandSender s) {
        String message = Values.MESSAGES.getError();
        if (message != null) s.sendMessage(ChatUtils.color(message));
    }

    public static void interestUsage(CommandSender s) {
        String message = Values.MESSAGES.getInterestUsage();
        if (message != null) s.sendMessage(ChatUtils.color(message));
    }

    public static void interestRestarted(CommandSender s) {
        String message = Values.MESSAGES.getInterestRestarted();
        if (message != null) s.sendMessage(ChatUtils.color(message));
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