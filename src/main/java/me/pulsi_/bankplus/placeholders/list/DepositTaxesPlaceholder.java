package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

public class DepositTaxesPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "deposit_taxes";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        return BPFormatter.styleBigDecimal(ConfigValues.getDepositTaxes());
    }
}