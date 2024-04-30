package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankTop.BPBankTop;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;

public class BankTopPositionPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "banktop_position";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!Values.CONFIG.isBanktopEnabled())
            return BPChat.color("&cThe banktop is not enabled!");

        return BPBankTop.getPlayerBankTopPosition(p) + "";
    }
}