package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OfflineInterestMethod extends BPInterest.InterestMethod {

    @Override
    public void giveInterest(OfflinePlayer p) {
        if (BPInterest.InterestMethod.offlineTimeExpired(p)) return;

        String offlinePermission = ConfigValues.getInterestOfflinePermission();
        if (!offlinePermission.isEmpty() && !BPUtils.hasOfflinePermission(p, offlinePermission)) return;

        List<Bank> availableBanks = new ArrayList<>(); // List of available banks for the player.
        for (Bank bank : BankRegistry.getBanks().values())
            // If the bank gives interest even if not available, still add it.
            if (bank.isGiveInterestIfNotAvailable() || BankUtils.isAvailable(bank, p)) availableBanks.add(bank);

        if (availableBanks.isEmpty()) return;

        boolean interestToVault = ConfigValues.isGivingInterestOnVaultBalance();
        for (Bank bank : availableBanks) {
            BPEconomy economy = bank.getBankEconomy();
            if (economy.getBankBalance(p).compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal interestMoney = getInterestMoney(bank, p,BankUtils.getOfflineInterestRate(bank, p));

            BigDecimal maxAmount = BankUtils.getMaxInterestAmount(bank, p);
            if (maxAmount.compareTo(BigDecimal.ZERO) > 0) interestMoney = interestMoney.min(maxAmount);

            BigDecimal added = interestMoney;
            if (!interestToVault) added = economy.addBankBalance(p, interestMoney, TransactionType.INTEREST);
            else BankPlus.INSTANCE().getVaultEconomy().depositPlayer(p, interestMoney.doubleValue());

            economy.setOfflineInterest(p, economy.getOfflineInterest(p).add(added));
        }
    }
}
