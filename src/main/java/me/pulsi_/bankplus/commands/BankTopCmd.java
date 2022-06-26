package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.managers.BankTopManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.List;

public class BankTopCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {
        if (!Values.CONFIG.isBanktopEnabled()) MessageManager.banktopDisabled(s);
        if (!Methods.hasPermission(s, "bankplus.banktop")) return false;

        List<String> format = Values.CONFIG.getBankTopFormat();
        for (String line : format) s.sendMessage(ChatUtils.color(placeName(placeMoney(line))));
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
        BigDecimal money = BankTopManager.getBankTopBalancePlayer(position);
        switch (Values.CONFIG.getBankTopMoneyFormat()) {
            case "default_amount":
                stringToReplace = Methods.formatCommas(money);
                break;
            case "amount_long":
                stringToReplace = String.valueOf(money);
                break;
            default:
                stringToReplace = Methods.format(money);
                break;
            case "amount_formatted_long":
                stringToReplace = Methods.formatLong(money);
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

        String name = BankTopManager.getBankTopNamePlayer(position);
        return message.replace("%bankplus_banktop_name_" + position + "%", name);
    }
}