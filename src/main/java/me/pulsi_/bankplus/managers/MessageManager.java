package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class MessageManager {

    public static void personalBalance(Player p) {
        String message = Values.MESSAGES.getPersonalBank();
        if (isMessageNull(p, message)) return;
        BigDecimal amount = EconomyManager.getBankBalance(p);
        p.sendMessage(addPrefix(message
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void minimumAmountAlert(Player p) {
        String message = Values.MESSAGES.getMinimumAmount();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message
                .replace("%minimum%", "" + Values.CONFIG.getMinimumAmount())
        ));
    }

    public static void cannotUseNegativeNumber(Player p) {
        String message = Values.MESSAGES.getCannotUseNegativeNumber();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message));
    }

    public static void successWithdraw(Player p, BigDecimal amount) {
        String message = Values.MESSAGES.getSuccessWithdraw();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void successDeposit(Player p, BigDecimal amount) {
        String message = Values.MESSAGES.getSuccessDeposit();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void bankOthers(CommandSender s, Player p) {
        String message = Values.MESSAGES.getBankOthers();
        if (isMessageNull(s, message)) return;
        BigDecimal amount = EconomyManager.getBankBalance(p);
        s.sendMessage(addPrefix(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void bankOthers(CommandSender s, OfflinePlayer p) {
        String message = Values.MESSAGES.getBankOthers();
        if (isMessageNull(s, message)) return;
        BigDecimal amount = EconomyManager.getBankBalance(p);
        s.sendMessage(addPrefix(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void setMessage(CommandSender s, Player p, BigDecimal amount) {
        String message = Values.MESSAGES.getSetMessage();
        if (!isMessageNull(p, message)) s.sendMessage(addPrefix(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void setMessage(CommandSender s, OfflinePlayer p, BigDecimal amount) {
        String message = Values.MESSAGES.getSetMessage();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void addMessage(CommandSender s, Player p, BigDecimal amount) {
        String message = Values.MESSAGES.getAddMessage();
        if (!isMessageNull(p, message)) s.sendMessage(addPrefix(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void addMessage(CommandSender s, OfflinePlayer p, BigDecimal amount) {
        String message = Values.MESSAGES.getAddMessage();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void removeMessage(CommandSender s, Player p, BigDecimal amount) {
        String message = Values.MESSAGES.getRemoveMessage();
        if (!isMessageNull(p, message)) s.sendMessage(addPrefix(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void removeMessage(CommandSender s, OfflinePlayer p, BigDecimal amount) {
        String message = Values.MESSAGES.getRemoveMessage();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message
                .replace("%player_name%", p.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void interestBroadcastMessage(Player p, BigDecimal amount) {
        String message = Values.MESSAGES.getInterestBroadcastMessage();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void paymentSent(Player p, Player target, BigDecimal amount) {
        String message = Values.MESSAGES.getPaymentSent();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message
                .replace("%player%", target.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void paymentReceived(Player p, Player target, BigDecimal amount) {
        String message = Values.MESSAGES.getPaymentReceived();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message
                .replace("%player%", target.getName())
                .replace("%amount%", Methods.formatCommas(amount))
                .replace("%amount_long%", "" + amount)
                .replace("%amount_formatted%", Methods.format(amount))
                .replace("%amount_formatted_long%", Methods.formatLong(amount))
        ));
    }

    public static void interestTime(CommandSender s) {
        String message = Values.MESSAGES.getInterestTime();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message.replace("%time%", Methods.formatTime(Interest.getInterestCooldownMillis()))));
    }

    public static void interestTimeMillis(CommandSender s) {
        String message = Values.MESSAGES.getInterestTime();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message.replace("%time%", String.valueOf(Interest.getInterestCooldownMillis()))));
    }

    public static void helpMessage(CommandSender s) {
        for (String helpMessage : Values.MESSAGES.getHelpMessage()) s.sendMessage(addPrefix(helpMessage));
    }

    public static void bankFull(Player p, Player target) {
        String message = Values.MESSAGES.getBankFull();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message.replace("%player%", target.getName())));
    }

    public static void interestBankFull(Player p) {
        String message = Values.MESSAGES.getInterestBankFull();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message));
    }

    public static void banktopDisabled(CommandSender s) {
        String message = Values.MESSAGES.getBanktopDisabled();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message));
    }

    public static void invalidPlayer(CommandSender s) {
        String message = Values.MESSAGES.getInvalidPlayer();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message));
    }

    public static void cannotUseNegativeNumber(CommandSender s) {
        String message = Values.MESSAGES.getCannotUseNegativeNumber();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message));
    }

    public static void chatWithdraw(Player p) {
        String message = Values.MESSAGES.getChatWithdraw();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message));
    }

    public static void chatDeposit(Player p) {
        String message = Values.MESSAGES.getChatDeposit();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message));
    }

    public static void cannotDepositMore(Player p) {
        String message = Values.MESSAGES.getCannotDepositAnymore();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message));
    }

    public static void noMoneyInterest(Player p) {
        String message = Values.MESSAGES.getNoMoneyInterest();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message));
    }

    public static void insufficientMoney(Player p) {
        String message = Values.MESSAGES.getInsufficientMoney();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message));
    }

    public static void noPermission(CommandSender s) {
        String message = Values.MESSAGES.getNoPermission();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message));
    }

    public static void notPlayer(CommandSender s) {
        String message = Values.MESSAGES.getNotPlayer();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message));
    }

    public static void invalidNumber(CommandSender s) {
        String message = Values.MESSAGES.getInvalidNumber();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message));
    }

    public static void invalidNumber(Player p) {
        String message = Values.MESSAGES.getInvalidNumber();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message));
    }

    public static void specifyNumber(CommandSender s) {
        String message = Values.MESSAGES.getSpecifyNumber();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message));
    }

    public static void specifyPlayer(CommandSender s) {
        String message = Values.MESSAGES.getSpecifyPlayer();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message));
    }

    public static void unknownCommand(CommandSender s) {
        String message = Values.MESSAGES.getUnknownCommand();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message));
    }

    public static void interestIsDisabled(CommandSender s) {
        String message = Values.MESSAGES.getInterestDisabled();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message));
    }

    public static void interestRestarted(CommandSender s) {
        String message = Values.MESSAGES.getInterestRestarted();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message));
    }

    public static void reloadMessage(CommandSender s) {
        String message = Values.MESSAGES.getReloadMessage();
        if (!isMessageNull(s, message)) s.sendMessage(addPrefix(message));
    }

    public static void cannotUseBankHere(Player p) {
        String message = Values.MESSAGES.getCannotUseBankHere();
        if (!isMessageNull(p, message)) p.sendMessage(addPrefix(message));
    }

    public static String addPrefix(String mess) {
        String prefix = Values.MESSAGES.getPrefix();
        if (prefix != null) return ChatUtils.color(mess.replace("%prefix%", prefix));
        return ChatUtils.color(mess);
    }

    private static boolean isMessageNull(CommandSender s, String message) {
        if (message != null) return false;
        if (Values.MESSAGES.isAlertMissingMessagePathNull()) {
            s.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &cWarning! This message is missing in the messages file! " +
                    "Reload the server to make the file automatically replace the missing parts!"));
            return true;
        }
        if (Values.MESSAGES.isAlertMissingMessage()) {
            s.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &cWarning! This message is missing in the messages file! " +
                    "Reload the server to make the file automatically replace the missing parts!"));
        }
        return true;
    }

    private static boolean isMessageNull(Player p, String message) {
        if (message != null) return false;
        if (Values.MESSAGES.isAlertMissingMessagePathNull()) {
            p.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &cWarning! This message is missing in the messages file! " +
                    "Reload the server to make the file automatically replace the missing parts!"));
            return true;
        }
        if (Values.MESSAGES.isAlertMissingMessage()) {
            p.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &cWarning! This message is missing in the messages file! " +
                    "Reload the server to make the file automatically replace the missing parts!"));
        }
        return true;
    }
}