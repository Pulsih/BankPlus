package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class CalculateWithdrawTaxesPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "calculate_withdraw_taxes_";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        String secondIdentifier = identifier.replace("calculate_withdraw_taxes_", "");

        BigDecimal taxes;
        if (secondIdentifier.startsWith("number_")) {
            String number = secondIdentifier.replace("number_", "");

            BigDecimal amount;
            if (target != null) {
                if (!BankUtils.exist(target))
                    return "The selected bank does not exist.";

                number = number.replace("{" + target + "}", "");
            }

            try {
                amount = new BigDecimal(number.replace("%", ""));
            } catch (NumberFormatException e) {
                return "Invalid Number!";
            }

            taxes = amount.multiply(ConfigValues.getWithdrawTaxes().divide(BigDecimal.valueOf(100)));
        } else {
            String number = secondIdentifier.replace("percentage_", "");

            BigDecimal amount, balance;

            if (target == null) balance = BPEconomy.getBankBalancesSum(p);
            else {
                if (!BankUtils.exist(target))
                    return "The selected bank does not exist.";

                number = number.replace("{" + target + "}", "");
                balance = BPEconomy.get(target).getBankBalance(p);
            }

            try {
                amount = new BigDecimal(number.replace("%", ""));
            } catch (NumberFormatException e) {
                return "Invalid Number!";
            }

            BigDecimal percentageBalance = balance.multiply(amount.divide(BigDecimal.valueOf(100)));
            taxes = percentageBalance.multiply(ConfigValues.getWithdrawTaxes().divide(BigDecimal.valueOf(100)));
        }
        return getFormat(identifier, taxes);
    }
}