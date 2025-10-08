package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.interest.BPInterest;
import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import org.bukkit.entity.Player;

public class NextInterestPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_interest";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return bankDoesNotExist;
        Bank bank = BankRegistry.getBank(target);
        return getFormat(identifier, BPInterest.InterestMethod.getInterestMoney(bank, p, BankUtils.getOnlineInterestRate(bank, p)));
    }
}