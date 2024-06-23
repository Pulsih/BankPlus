package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankTop.BPBankTop;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.utils.texts.BPChat;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

public class BankTopNamePlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "banktop_name_";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!ConfigValues.isBankTopEnabled())
            return BPChat.color("&cThe banktop is not enabled!");

        String number = identifier.replace("banktop_name_", "");
        int position;
        try {
            position = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return "&cInvalid banktop number!";
        }

        if (position > ConfigValues.getBankTopSize())
            return "&cThe banktop limit is " + ConfigValues.getBankTopSize() + "!";

        return BPBankTop.getBankTopNamePlayer(position);
    }
}