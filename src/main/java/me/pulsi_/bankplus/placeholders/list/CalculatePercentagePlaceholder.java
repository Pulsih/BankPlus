package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculatePercentagePlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "calculate_[deposit/withdraw]_percentage_<amount>";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        String[] args = getSelectedVariantParts(identifier);
        BigDecimal amount = new BigDecimal(args[3]), percentage;

        if (target == null || p == null) return "Invalid bank or player!";

        if (args[1].equals("deposit")) {
            BigDecimal playerBalance = BigDecimal.valueOf(BankPlus.INSTANCE().getVaultEconomy().getBalance(p));
            percentage = playerBalance.multiply(amount.divide(BigDecimal.valueOf(100)));
        } else {
            BigDecimal bankBalance = BankUtils.getBank(target).getBankEconomy().getBankBalance(p);
            percentage = bankBalance.multiply(amount.divide(BigDecimal.valueOf(100)));
        }

        return getFormat(identifier, percentage);
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
