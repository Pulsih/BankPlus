package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankTop.BPBankTop;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.utils.texts.BPChat;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

public class BankTopPositionPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "banktop_position";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!ConfigValues.isBankTopEnabled())
            return BPChat.color("&cThe banktop is not enabled!");

        return BPBankTop.getPlayerBankTopPosition(p) + "";
    }

    @Override
    public boolean hasPlaceholders() {
        return false;
    }

    @Override
    public boolean hasVariables() {
        return false;
    }
}