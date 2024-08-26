package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import org.bukkit.entity.Player;

public class NextOfflineInterestPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_offline_interest";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return "&cThe selected bank does not exist.";
        Bank bank = BankUtils.getBank(target);
        return getFormat(identifier, BankPlus.INSTANCE().getInterest().getInterestMoney(bank, p, BankUtils.getOfflineInterestRate(bank, p)));
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