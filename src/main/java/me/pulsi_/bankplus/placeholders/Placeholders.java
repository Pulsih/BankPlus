package me.pulsi_.bankplus.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.BankTopManager;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
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
        return "Pulsi_";
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

        String placeholderBankNameTarget = null;
        if (identifier.contains("{") && identifier.endsWith("}"))
            placeholderBankNameTarget = identifier.substring(identifier.indexOf("{") + 1, identifier.indexOf("}"));

        if (identifier.startsWith("capacity")) {
            BigDecimal capacity = new BankReader(Values.CONFIG.getMainGuiName()).getCapacity(p);

            if (placeholderBankNameTarget != null) {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "&cThe multiple-banks module is disabled!";

                BankReader reader = new BankReader(placeholderBankNameTarget);
                if (!reader.exist())
                    return "&cThe selected bank does not exist.";

                capacity = reader.getCapacity(p);
            }

            String formatter = identifier.replace("capacity", "");
            return getFormat(formatter, capacity);
        }

        if (identifier.startsWith("level")) {
            String level = String.valueOf(new BankReader(Values.CONFIG.getMainGuiName()).getCurrentLevel(p));

            if (placeholderBankNameTarget != null) {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "The multiple-banks module is disabled.";

                BankReader reader = new BankReader(placeholderBankNameTarget);
                if (!reader.exist())
                    return "&cThe selected bank does not exist.";

                level = String.valueOf(reader.getCurrentLevel(p));
            }

            return level;
        }

        if (identifier.startsWith("next_level_cost")) {
            BankReader mainReader = new BankReader(Values.CONFIG.getMainGuiName());
            if (!mainReader.hasNextLevel(p))
                return BPChat.color(Values.CONFIG.getBankUpgradedMaxPlaceholder());

            BigDecimal cost = mainReader.getLevelCost(mainReader.getCurrentLevel(p) + 1);

            if (identifier.contains("{") && identifier.endsWith("}")) {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "&cThe multiple-banks module is disabled!";

                BankReader reader = new BankReader(placeholderBankNameTarget);
                if (!reader.exist())
                    return "&cThe selected bank does not exist!";

                if (!reader.hasNextLevel(p))
                    return Values.CONFIG.getBankUpgradedMaxPlaceholder();

                cost = reader.getLevelCost(reader.getCurrentLevel(p) + 1);
            }

            String formatter = identifier.replace("next_level_cost", "");
            return getFormat(formatter, cost);
        }

        if (identifier.startsWith("balance")) {
            BigDecimal bal;
            if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) bal = multiEconomyManager.getBankBalance();
            else bal = singleEconomyManager.getBankBalance();

            if (placeholderBankNameTarget != null) {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "&cThe multiple-banks module is disabled!";

                BankReader reader = new BankReader(placeholderBankNameTarget);
                if (!reader.exist())
                    return "&cThe selected bank does not exist!";

                bal = multiEconomyManager.getBankBalance(placeholderBankNameTarget);
            }

            String formatter = identifier.replace("balance", "");
            return getFormat(formatter, bal);
        }

        if (identifier.startsWith("next_interest")) {
            BigDecimal percentage = Values.CONFIG.getInterestMoneyGiven().divide(BigDecimal.valueOf(100));
            BigDecimal interestMoney = singleEconomyManager.getBankBalance().multiply(percentage);

            if (placeholderBankNameTarget != null) {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "&cThe multiple-banks module is disabled!";

                BankReader reader = new BankReader(placeholderBankNameTarget);
                if (!reader.exist())
                    return "The selected bank does not exist.";

                interestMoney = multiEconomyManager.getBankBalance(placeholderBankNameTarget).multiply(percentage);
            }

            String formatter = identifier.replace("next_interest", "");
            return getFormat(formatter, interestMoney);
        }

        if (identifier.startsWith("banktop_money_")) {
            if (!Values.CONFIG.isBanktopEnabled())
                return BPChat.color("&cThe banktop is not enabled!");

            String number = identifier.replace("banktop_money_", "");
            int position;
            try {
                position = Integer.parseInt(number);
            } catch (NumberFormatException e) {
                return "&cInvalid banktop number!";
            }

            if (position > Values.CONFIG.getBankTopSize())
                return "&cThe banktop limit is " + Values.CONFIG.getBankTopSize() + "!";

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
            if (!Values.CONFIG.isBanktopEnabled())
                return BPChat.color("&cThe banktop is not enabled!");

            String number = identifier.replace("banktop_name_", "");
            int position;
            try {
                position = Integer.parseInt(number);
            } catch (NumberFormatException e) {
                return "&cInvalid banktop number!";
            }

            if (position > Values.CONFIG.getBankTopSize())
                return "&cThe banktop limit is " + Values.CONFIG.getBankTopSize() + "!";

            return bankTop.getBankTopNamePlayer(position);
        }

        if (identifier.startsWith("banktop_position")) {
            if (!Values.CONFIG.isBanktopEnabled())
                return BPChat.color("&cThe banktop is not enabled!");

            Player result = p;
            if (identifier.contains("{") && identifier.endsWith("}")) {
                Player pTarget = Bukkit.getPlayer(placeholderBankNameTarget);

                if (pTarget == null)
                    return "&cThe selected player does not exist!";

                result = pTarget;
            }

            return bankTop.getPlayerBankTopPosition(result) + "";
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

        return null;
    }

    private String getFormat(String formatter, BigDecimal value) {
        if (formatter.contains("_long")) return String.valueOf(value);
        if (formatter.contains("_formatted")) return BPMethods.format(value);
        if (formatter.contains("_formatted_long")) return BPMethods.formatLong(value);
        return BPMethods.formatCommas(value);
    }
}