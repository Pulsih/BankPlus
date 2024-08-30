package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class CalculateDepositTaxesPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "calculate_deposit_taxes_";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        String secondIdentifier = identifier.replace("calculate_deposit_taxes_", "");

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

            taxes = amount.multiply(ConfigValues.getDepositTaxes().divide(BigDecimal.valueOf(100)));
        } else {
            String number = secondIdentifier.replace("percentage_", "");

            BigDecimal amount, balance;
            try {
                balance = BigDecimal.valueOf(BankPlus.INSTANCE().getVaultEconomy().getBalance(p));
            } catch (NumberFormatException e) {
                return "Invalid vault balance!";
            }

            try {
                amount = new BigDecimal(number.replace("%", ""));
            } catch (NumberFormatException e) {
                return "Invalid Number!";
            }

            BigDecimal percentageBalance = balance.multiply(amount.divide(BigDecimal.valueOf(100)));
            taxes = percentageBalance.multiply(ConfigValues.getDepositTaxes().divide(BigDecimal.valueOf(100)));
        }
        return getFormat(identifier, taxes);
    }
}