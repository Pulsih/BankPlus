package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankTop.BPBankTop;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class BankTopMoneyPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "banktop_money_";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!ConfigValues.isBankTopEnabled()) return bankTopNotEnabled;

        String number = identifier.replace("banktop_money_", "");
        int position;
        try {
            position = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return "Invalid banktop number!";
        }

        if (position > ConfigValues.getBankTopSize())
            return "The banktop limit is " + ConfigValues.getBankTopSize() + "!";

        BigDecimal money = BPBankTop.getBankTopBalancePlayer(position);
        switch (ConfigValues.getBankTopMoneyFormat()) {
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