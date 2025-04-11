package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankTop.BPBankTop;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

public class BankTopPositionPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "banktop_position";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!ConfigValues.isBankTopEnabled()) return bankTopNotEnabled;
        return BPBankTop.getPlayerBankTopPosition(p) + "";
    }
}