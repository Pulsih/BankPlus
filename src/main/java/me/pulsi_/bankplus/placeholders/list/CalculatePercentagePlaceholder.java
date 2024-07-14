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
        return "calculate_[deposit/withdraw]_[percentage/number]_<amount>";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        String[] args = getOptions(identifier);
        BigDecimal amount = new BigDecimal(args[3]);
        String percentage;

        if (target == null || p == null) return "Invalid target or player!";

        if (args[1].equals("deposit")) {
            BigDecimal playerBalance = BigDecimal.valueOf(BankPlus.INSTANCE().getVaultEconomy().getBalance(p));
            percentage = String.valueOf(playerBalance.multiply(amount.divide(BigDecimal.valueOf(100), RoundingMode.UP)));
        } else {
            BigDecimal bankBalance = BankUtils.getBank(target).getBankEconomy().getBankBalance(p);
            percentage = String.valueOf(bankBalance.multiply(amount.divide(BigDecimal.valueOf(100), RoundingMode.UP)));
        }

        return percentage;
    }
}
