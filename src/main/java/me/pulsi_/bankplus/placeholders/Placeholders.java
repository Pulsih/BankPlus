package me.pulsi_.bankplus.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.EconomyManager;
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

        BigDecimal balance = EconomyManager.getBankBalance(p);

        double moneyPercentage = Values.CONFIG.getInterestMoneyGiven();
        BigDecimal interestMoney = balance.multiply(BigDecimal.valueOf(moneyPercentage));

        switch (identifier) {
            case "balance":
                return Methods.formatCommas(balance);
            case "balance_long":
                return "" + balance;
            case "balance_formatted":
                return Methods.format(balance);
            case "balance_formatted_long":
                return Methods.formatLong(balance);
            case "next_interest":
                return Methods.formatCommas(interestMoney);
            case "next_interest_long":
                return "" + interestMoney;
            case "next_interest_formatted":
                return Methods.format(interestMoney);
            case "next_interest_formatted_long":
                return Methods.formatLong(interestMoney);

            case "interest_cooldown": {
                String interest;
                if (Values.CONFIG.isInterestEnabled())
                    interest = Methods.formatTime(Interest.getInterestCooldownMillis());
                else
                    interest = ChatColor.RED + "Interest is disabled.";

                return interest;
            }
        }
        return null;
    }
}