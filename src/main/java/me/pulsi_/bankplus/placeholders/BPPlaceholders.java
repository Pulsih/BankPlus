package me.pulsi_.bankplus.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.debt.DebtUtils;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.BankTopManager;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class BPPlaceholders extends PlaceholderExpansion {

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

        String target = null;
        if (identifier.contains("{") && identifier.endsWith("}"))
            target = identifier.substring(identifier.indexOf("{") + 1, identifier.indexOf("}"));

        if (identifier.startsWith("capacity")) {
            BigDecimal capacity = new BankReader(Values.CONFIG.getMainGuiName()).getCapacity(p);

            if (target != null) {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "&cThe multiple-banks module is disabled!";

                BankReader reader = new BankReader(target);
                if (!reader.exist())
                    return "&cThe selected bank does not exist.";

                capacity = reader.getCapacity(p);
            }
            if (capacity.longValue() <= 0) return Values.CONFIG.getInfiniteCapacityText();

            String formatter = identifier.replace("capacity", "");
            return getFormat(formatter, capacity);
        }

        if (identifier.startsWith("level")) {
            String level = String.valueOf(new BankReader(Values.CONFIG.getMainGuiName()).getCurrentLevel(p));

            if (target != null) {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "The multiple-banks module is disabled.";

                BankReader reader = new BankReader(target);
                if (!reader.exist())
                    return "&cThe selected bank does not exist.";

                level = String.valueOf(reader.getCurrentLevel(p));
            }

            return level;
        }

        if (identifier.startsWith("debt")) {
            String formatter = identifier.replace("debt", "");
            return getFormat(formatter, DebtUtils.getDebt(p));
        }

        if (identifier.startsWith("next_level_cost")) {
            BankReader reader;
            BigDecimal cost;

            if (target == null) reader = new BankReader(Values.CONFIG.getMainGuiName());
            else {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "&cThe multiple-banks module is disabled!";

                reader = new BankReader(target);
                if (!reader.exist())
                    return "&cThe selected bank does not exist!";
            }
            if (!reader.hasNextLevel(p))
                return Values.CONFIG.getBankUpgradedMaxPlaceholder();

            cost = reader.getLevelCost(reader.getCurrentLevel(p) + 1);

            String formatter = identifier.replace("next_level_cost", "");
            return getFormat(formatter, cost);
        }

        if (identifier.startsWith("balance")) {
            BigDecimal bal;
            if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) bal = multiEconomyManager.getBankBalance();
            else bal = singleEconomyManager.getBankBalance();

            if (target != null) {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "&cThe multiple-banks module is disabled!";

                BankReader reader = new BankReader(target);
                if (!reader.exist())
                    return "&cThe selected bank does not exist!";

                bal = multiEconomyManager.getBankBalance(target);
            }

            String formatter = identifier.replace("balance", "");
            return getFormat(formatter, bal);
        }

        if (identifier.startsWith("next_interest")) {
            BankReader reader;
            BigDecimal percentage, interestMoney, balance;

            if (target == null) {
                reader = new BankReader(Values.CONFIG.getMainGuiName());
                percentage = reader.getInterest(p).divide(BigDecimal.valueOf(100));
                balance = singleEconomyManager.getBankBalance();
            } else {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "&cThe multiple-banks module is disabled!";

                reader = new BankReader(target);
                if (!reader.exist())
                    return "The selected bank does not exist.";

                percentage = reader.getInterest(p).divide(BigDecimal.valueOf(100));
                balance = multiEconomyManager.getBankBalance(target);
            }
            interestMoney = new BigDecimal(Math.min(Values.CONFIG.getInterestMaxAmount().doubleValue(), balance.multiply(percentage).doubleValue()));

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
                    return BPFormatter.formatCommas(money);
                case "amount_long":
                    return String.valueOf(money);
                default:
                    return BPFormatter.format(money);
                case "amount_formatted_long":
                    return BPFormatter.formatLong(money);
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
            if (target != null) {
                Player pTarget = Bukkit.getPlayer(target);

                if (pTarget == null)
                    return "&cThe selected player does not exist!";

                result = pTarget;
            }

            return bankTop.getPlayerBankTopPosition(result) + "";
        }

        if (identifier.startsWith("interest_rate")) {
            BankReader reader;
            BigDecimal interestRate;

            if (target == null) reader = new BankReader(Values.CONFIG.getMainGuiName());
            else {
                if (!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                    return "&cThe multiple-banks module is disabled!";

                reader = new BankReader(target);
                if (!reader.exist())
                    return "The selected bank does not exist.";
            }
            interestRate = reader.getInterest(p);

            return interestRate + "";
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
        }

        return null;
    }

    private String getFormat(String formatter, BigDecimal value) {
        if (formatter.contains("_long")) return String.valueOf(value);
        if (formatter.contains("_formatted")) return BPFormatter.format(value);
        if (formatter.contains("_formatted_long")) return BPFormatter.formatLong(value);
        return BPFormatter.formatCommas(value);
    }
}