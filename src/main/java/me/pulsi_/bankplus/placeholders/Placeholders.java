package me.pulsi_.bankplus.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.BankTopManager;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return BankPlus.getInstance().getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return "bankplus";
    }

    @Override
    public String getVersion() {
        return BankPlus.getInstance().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        if (p == null) return "Player not online";

        BPLogger.info(identifier);

        BigDecimal balance = EconomyManager.getBankBalance(p);
        double moneyPercentage = Values.CONFIG.getInterestMoneyGiven();
        BigDecimal interestMoney = balance.multiply(BigDecimal.valueOf(moneyPercentage));

        switch (identifier) {
            case "balance":
                return Methods.formatCommas(balance);
            case "balance_long":
                return String.valueOf(balance);
            case "balance_formatted":
                return Methods.format(balance);
            case "balance_formatted_long":
                return Methods.formatLong(balance);

            case "next_interest":
                return Methods.formatCommas(interestMoney);
            case "next_interest_long":
                return String.valueOf(interestMoney);
            case "next_interest_formatted":
                return Methods.format(interestMoney);
            case "next_interest_formatted_long":
                return Methods.formatLong(interestMoney);

            case "interest_cooldown": {
                if (Values.CONFIG.isInterestEnabled()) return Methods.formatTime(Interest.getInterestCooldownMillis());
                else return ChatColor.RED + "Interest is disabled.";
            }
        }

        if (identifier.startsWith("banktop_money_")) {
            String number = identifier.replace("banktop_money_", "");
            int position;
            try {
                position = Integer.parseInt(number);
            } catch (NumberFormatException e) {
                return "Invalid banktop number!";
            }
            if (position > Values.CONFIG.getBankTopSize()) return "Limit of the BankTop: " + Values.CONFIG.getBankTopSize();

            BigDecimal money = BankTopManager.getBankTopBalancePlayer(position);
            switch (Values.CONFIG.getBankTopMoneyFormat()) {
                case "default_amount":
                    return Methods.formatCommas(money);
                case "amount_long":
                    return String.valueOf(money);
                default:
                    return Methods.format(money);
                case "amount_formatted_long":
                    return Methods.formatLong(money);
            }
        }

        if (identifier.startsWith("banktop_name_")) {
            String number = identifier.replace("banktop_name_", "");
            int position;
            try {
                position = Integer.parseInt(number);
            } catch (NumberFormatException e) {
                return "Invalid banktop number!";
            }
            if (position > Values.CONFIG.getBankTopSize()) return "Limit of the BankTop: " + Values.CONFIG.getBankTopSize();
            return BankTopManager.getBankTopNamePlayer(position);
        }
        return null;
    }
}