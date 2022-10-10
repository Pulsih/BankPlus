package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.List;

public class BankTopCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {
        if (!Values.CONFIG.isBanktopEnabled()) {
            BPMessages.send(s, "BankTop-Disabled");
            return false;
        }
        if (!BPMethods.hasPermission(s, "bankplus.banktop")) return false;

        List<String> format = Values.CONFIG.getBankTopFormat();
        for (String line : format) s.sendMessage(BPChat.color(placeName(placeMoney(line))));
        return true;
    }

    private String placeMoney(String message) {
        if (!message.contains("%bankplus_banktop_money_")) return message;

        String split = message.split("%bankplus_banktop_money_")[1];
        int i = split.indexOf("%");
        String numbers = split.substring(0, i);
        int position;
        try {
            position = Integer.parseInt(numbers);
        } catch (NumberFormatException e) {
            BPLogger.error("Invalid number for the BankTop money placeholder!");
            BPLogger.error("Message: " + message);
            return message;
        }
        if (position > Values.CONFIG.getBankTopSize()) {
            BPLogger.error("Limit of the BankTop: " + Values.CONFIG.getBankTopSize());
            BPLogger.error("Message: " + message);
            return message;
        }

        String stringToReplace;
        BigDecimal money = BankPlus.INSTANCE.getBankTopManager().getBankTopBalancePlayer(position);
        switch (Values.CONFIG.getBankTopMoneyFormat()) {
            case "default_amount":
                stringToReplace = BPMethods.formatCommas(money);
                break;
            case "amount_long":
                stringToReplace = String.valueOf(money);
                break;
            default:
                stringToReplace = BPMethods.format(money);
                break;
            case "amount_formatted_long":
                stringToReplace = BPMethods.formatLong(money);
                break;
        }
        return message.replace("%bankplus_banktop_money_" + position + "%", stringToReplace);
    }

    private String placeName(String message) {
        if (!message.contains("%bankplus_banktop_name_")) return message;

        String split = message.split("%bankplus_banktop_name_")[1];
        int i = split.indexOf("%");
        String numbers = split.substring(0, i);
        int position;
        try {
            position = Integer.parseInt(numbers);
        } catch (NumberFormatException e) {
            BPLogger.error("Invalid number for the BankTop name placeholder!");
            BPLogger.error("Message: " + message);
            return message;
        }
        if (position > Values.CONFIG.getBankTopSize()) {
            BPLogger.error("Limit of the BankTop: " + Values.CONFIG.getBankTopSize());
            BPLogger.error("Message: " + message);
            return message;
        }

        String name = BankPlus.INSTANCE.getBankTopManager().getBankTopNamePlayer(position);
        return message.replace("%bankplus_banktop_name_" + position + "%", name);
    }
}