package me.pulsi_.bankplus.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankGuis.BanksManager;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.BankTopManager;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull String getAuthor() {
        return BankPlus.INSTANCE.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bankplus";
    }

    @Override
    public @NotNull String getVersion() {
        return BankPlus.INSTANCE.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String identifier) {
        if (p == null) return "Player not online";

        SingleEconomyManager singleEconomyManager = new SingleEconomyManager(p);
        MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);
        BankTopManager bankTop = BankPlus.INSTANCE.getBankTopManager();

        if (identifier.startsWith("capacity")) {
            BigDecimal capacity = new BanksManager(Values.CONFIG.getMainGuiName()).getCapacity(p);
            if (identifier.endsWith("}") && identifier.contains("{")) {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "The multiple-banks module is disabled.";
                String bankName = identifier.substring(identifier.indexOf("{") + 1, identifier.indexOf("}"));
                if (!new BanksManager(bankName).exist()) return "The selected bank does not exist.";
                capacity = new BanksManager(bankName).getCapacity(p);
            }
            String formatter = identifier.replace("capacity", "");
            if (formatter.contains("_long")) return String.valueOf(capacity);
            if (formatter.contains("_formatted")) return BPMethods.format(capacity);
            if (formatter.contains("_formatted_long")) return BPMethods.formatLong(capacity);
            return BPMethods.formatCommas(capacity);
        }

        if (identifier.startsWith("level")) {
            String level = String.valueOf(new BanksManager(Values.CONFIG.getMainGuiName()).getLevel(p));
            if (identifier.endsWith("}") && identifier.contains("{")) {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "The multiple-banks module is disabled.";
                String bankName = identifier.substring(identifier.indexOf("{") + 1, identifier.indexOf("}"));
                if (!new BanksManager(bankName).exist()) return "The selected bank does not exist.";
                level = String.valueOf(new BanksManager(bankName).getLevel(p));
            }
            return level;
        }

        if (identifier.startsWith("banktop_position")) return bankTop.getPlayerBankTopPosition(p) + "";

        if (identifier.startsWith("next_level_cost")) {
            if (!new BanksManager(Values.CONFIG.getMainGuiName()).hasNextLevel(p)) return BPChat.color(Values.CONFIG.getBankUpgradedMax());
            BigDecimal cost = new BanksManager(Values.CONFIG.getMainGuiName()).getLevelCost(new BanksManager(Values.CONFIG.getMainGuiName()).getLevel(p) + 1);
            if (identifier.endsWith("}") && identifier.contains("{")) {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "The multiple-banks module is disabled.";
                String bankName = identifier.substring(identifier.indexOf("{") + 1, identifier.indexOf("}"));
                if (!new BanksManager(bankName).exist()) return "The selected bank does not exist.";
                if (!new BanksManager(bankName).hasNextLevel(p)) return BPChat.color(Values.CONFIG.getBankUpgradedMax());
                cost = new BanksManager(bankName).getLevelCost(new BanksManager(bankName).getLevel(p) + 1);
            }
            String formatter = identifier.replace("next_level_cost", "");
            if (formatter.contains("_long")) return String.valueOf(cost);
            if (formatter.contains("_formatted")) return BPMethods.format(cost);
            if (formatter.contains("_formatted_long")) return BPMethods.formatLong(cost);
            return BPMethods.formatCommas(cost);
        }

        if (identifier.startsWith("balance")) {
            BigDecimal bal;
            if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) bal = multiEconomyManager.getBankBalance();
            else bal = singleEconomyManager.getBankBalance();
            if (identifier.endsWith("}") && identifier.contains("{")) {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "The multiple-banks module is disabled.";
                String bankName = identifier.substring(identifier.indexOf("{") + 1, identifier.indexOf("}"));
                if (!new BanksManager(bankName).exist()) return "The selected bank does not exist.";
                bal = multiEconomyManager.getBankBalance(bankName);
            }
            String formatter = identifier.replace("balance", "");
            if (formatter.contains("_long")) return String.valueOf(bal);
            if (formatter.contains("_formatted")) return BPMethods.format(bal);
            if (formatter.contains("_formatted_long")) return BPMethods.formatLong(bal);
            return BPMethods.formatCommas(bal);
        }

        if (identifier.startsWith("next_interest")) {
            BigDecimal percentage = Values.CONFIG.getInterestMoneyGiven().divide(BigDecimal.valueOf(100));
            BigDecimal interestMoney = singleEconomyManager.getBankBalance().multiply(percentage);
            if (identifier.endsWith("}") && identifier.contains("{")) {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "The multiple-banks module is disabled.";
                String bankName = identifier.substring(identifier.indexOf("{") + 1, identifier.indexOf("}"));
                if (!new BanksManager(bankName).exist()) return "The selected bank does not exist.";
                interestMoney = multiEconomyManager.getBankBalance(bankName).multiply(percentage);
            }
            String formatter = identifier.replace("next_interest", "");
            if (formatter.contains("_long")) return String.valueOf(interestMoney);
            if (formatter.contains("_formatted")) return BPMethods.format(interestMoney);
            if (formatter.contains("_formatted_long")) return BPMethods.formatLong(interestMoney);
            return BPMethods.formatCommas(interestMoney);
        }

        Interest interest = BankPlus.INSTANCE.getInterest();
        switch (identifier) {
            case "interest_cooldown":
                return BPMethods.formatTime(interest.getInterestCooldownMillis());
            case "interest_cooldown_millis":
                return String.valueOf(interest.getInterestCooldownMillis());
            case "withdraw_taxes":
                return Values.CONFIG.getWithdrawTaxesString();
            case "deposit_taxes":
                return Values.CONFIG.getDepositTaxesString();
            case "interest_rate":
                return Values.CONFIG.getInterestMoneyGivenString();
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

            BigDecimal money = bankTop.getBankTopBalancePlayer(position);
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
            return bankTop.getBankTopNamePlayer(position);
        }
        return null;
    }
}