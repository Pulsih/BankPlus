package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankTop.BPBankTop;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.utils.texts.BPChat;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class BankTopMoneyPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "banktop_money_";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!Values.CONFIG.isBanktopEnabled())
            return BPChat.color("&cThe banktop is not enabled!");

        String number = identifier.replace("banktop_money_", "");
        int position;
        try {
            position = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return "&cInvalid banktop number!";
        }

        if (position > Values.CONFIG.getBankTopSize())
            return "&cThe banktop limit is " + Values.CONFIG.getBankTopSize() + "!";

        BigDecimal money = BPBankTop.getBankTopBalancePlayer(position);
        switch (Values.CONFIG.getBankTopMoneyFormat()) {
            case "default_amount":
                return BPFormatter.formatCommas(money);
            case "amount_long":
                return money.toPlainString();
            default:
                return BPFormatter.formatPrecise(money);
            case "amount_formatted_long":
                return BPFormatter.formatLong(money);
        }
    }
}