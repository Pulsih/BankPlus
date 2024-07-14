package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

public class TaxesPlaceholder extends BPPlaceholder {
    @Override
    public String getIdentifier() {
        return "[deposit/withdraw]_taxes";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        String[] args = getOptions(identifier);
        if (args[0].equals("deposit")) {
            return BPFormatter.styleBigDecimal(ConfigValues.getDepositTaxes());
        } else {
            return BPFormatter.styleBigDecimal(ConfigValues.getWithdrawTaxes());
        }
    }

    @Override
    public boolean hasPlaceholders() {
        return false;
    }
}
