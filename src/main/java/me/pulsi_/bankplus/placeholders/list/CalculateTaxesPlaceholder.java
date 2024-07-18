package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class CalculateTaxesPlaceholder extends BPPlaceholder {
    @Override
    public String getIdentifier() {
        return "calculate_[deposit/withdraw]_taxes_[percentage/number]_<amount>";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        String[] args = getSelectedVariantParts(identifier);
        BigDecimal amount = new BigDecimal(args[4]), output;

        if (args[3].equals("percentage")) {
            if (args[1].equals("deposit")) {
                BigDecimal balance = BigDecimal.valueOf(BankPlus.INSTANCE().getVaultEconomy().getBalance(p));
                output = balance.multiply(amount.divide(BigDecimal.valueOf(100)))
                        .multiply(ConfigValues.getDepositTaxes().divide(BigDecimal.valueOf(100)));
            } else { // withdraw percentage
                BigDecimal balance = target == null ? BPEconomy.getBankBalancesSum(p) : BPEconomy.get(target).getBankBalance(p);
                output = balance.multiply(amount.divide(BigDecimal.valueOf(100)))
                        .multiply(ConfigValues.getWithdrawTaxes().divide(BigDecimal.valueOf(100)));
            }
        } else { // number
            if (args[1].equals("deposit")) {
                output = amount.multiply(ConfigValues.getDepositTaxes().divide(BigDecimal.valueOf(100)));
            } else { // withdraw number
                output = amount.multiply(ConfigValues.getWithdrawTaxes().divide(BigDecimal.valueOf(100)));
            }
        }

        return getFormat(identifier, output);
    }

    @Override
    public boolean hasPlaceholders() {
        return true;
    }

    @Override
    public boolean hasVariables() {
        return true;
    }
}
