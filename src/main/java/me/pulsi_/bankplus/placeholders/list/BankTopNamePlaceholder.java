package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class BankTopNamePlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "banktop_name_";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!Values.CONFIG.isBanktopEnabled())
            return BPChat.color("&cThe banktop is not enabled!");

        String number = identifier.replace("banktop_name_", "");
        int position;
        try {
            position = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return "&cInvalid banktop number!";
        }

        if (position > Values.CONFIG.getBankTopSize())
            return "&cThe banktop limit is " + Values.CONFIG.getBankTopSize() + "!";

        return BankPlus.INSTANCE().getBankTopManager().getBankTopNamePlayer(position);
    }
}