package me.pulsi_.bankplus.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.BankTopManager;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
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

        BigDecimal balance = SingleEconomyManager.getBankBalance(p);
        BigDecimal interestMoney = balance.multiply(Values.CONFIG.getInterestMoneyGiven().divide(BigDecimal.valueOf(100)));

        switch (identifier) {
            case "balance":
                return BPMethods.formatCommas(balance);
            case "balance_long":
                return String.valueOf(balance);
            case "balance_formatted":
                return BPMethods.format(balance);
            case "balance_formatted_long":
                return BPMethods.formatLong(balance);

            case "next_interest":
                return BPMethods.formatCommas(interestMoney);
            case "next_interest_long":
                return String.valueOf(interestMoney);
            case "next_interest_formatted":
                return BPMethods.format(interestMoney);
            case "next_interest_formatted_long":
                return BPMethods.formatLong(interestMoney);

            case "interest_cooldown":
                return BPMethods.formatTime(Interest.getInterestCooldownMillis());
        }

        if (identifier.startsWith("banktop_money_")) {
            String number = identifier.replace("banktop_money_", "");
            int position;
            try {
                position = Integer.parseInt(number);
            } catch (NumberFormatException e) {
                return "Invalid banktop number!";
            }
            if (position > Values.CONFIG.getBankTopSize())
                return "Limit of the BankTop: " + Values.CONFIG.getBankTopSize();

            BigDecimal money = BankTopManager.getBankTopBalancePlayer(position);
            switch (Values.CONFIG.getBankTopMoneyFormat()) {
                case "default_amount":
                    return BPMethods.formatCommas(money);
                case "amount_long":
                    return String.valueOf(money);
                default:
                    return BPMethods.format(money);
                case "amount_formatted_long":
                    return BPMethods.formatLong(money);
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
            if (position > Values.CONFIG.getBankTopSize())
                return "Limit of the BankTop: " + Values.CONFIG.getBankTopSize();
            return BankTopManager.getBankTopNamePlayer(position);
        }
        return null;
    }
}