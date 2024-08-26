package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankTop.BPBankTop;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import me.pulsi_.bankplus.utils.texts.BPChat;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class BankTopPlaceholder extends BPPlaceholder {
    @Override
    public String getIdentifier() {
        return "banktop_[name/money]_<position>";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!ConfigValues.isBankTopEnabled()) return BPChat.color("&cThe banktop is not enabled!");
        String[] args = getSelectedVariantParts(identifier);
        if (args.length != 3) return BPChat.color("&cInvalid placeholder!");
        int position;

        try{
            position = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return BPChat.color("&cInvalid banktop number!");
        }

        if (position > ConfigValues.getBankTopSize())
            return BPChat.color("&cThe banktop limit is " + ConfigValues.getBankTopSize() + "!");

        if (args[1].equals("name")) {
            return BPBankTop.getBankTopNamePlayer(position);
        } else {
            BigDecimal money = BPBankTop.getBankTopBalancePlayer(position);
            return switch (ConfigValues.getBankTopMoneyFormat()) {
                case "default_amount" -> BPFormatter.formatCommas(money);
                case "amount_long" -> money.toPlainString();
                case "amount_formatted_long" -> BPFormatter.formatLong(money);
                default -> BPFormatter.formatPrecise(money);
            };
        }
    }

    @Override
    public boolean hasPlaceholders() {
        return true;
    }

    @Override
    public boolean hasVariables() {
        return true;
    }
}
