package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.managers.BPTaskManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import me.pulsi_.bankplus.values.MessageValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static me.pulsi_.bankplus.values.ConfigValues.isOfflineInterestDifferentRate;

public class BPInterest {

    private static final String defaultInterestPermission = "bankplus.receive.interest";
    private static String offlineInterestPermission;

    public static String getDefaultInterestPermission() {
        return defaultInterestPermission;
    }

    public static String getOfflineInterestPermission() {
        return offlineInterestPermission;
    }

    private long cooldown = 0;
    private boolean wasDisabled = true, isOfflineInterestEnabled;

    private final BankPlus plugin;

    public BPInterest(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void restartInterest(boolean start) {
        isOfflineInterestEnabled = ConfigValues.isOfflineInterestEnabled();
        offlineInterestPermission = isOfflineInterestDifferentRate() ? ConfigValues.getInterestOfflinePermission() : defaultInterestPermission;

        long interestSave = 0;

        if (start) {
            FileConfiguration config = plugin.getConfigs().getConfig("saves.yml");
            if (config != null) interestSave = config.getLong("interest-save");
        }

        if (interestSave > 0) cooldown = System.currentTimeMillis() + interestSave;
        else cooldown = System.currentTimeMillis() + ConfigValues.getInterestDelay();
        loopInterest();
    }

    public long getInterestCooldownMillis() {
        return cooldown - System.currentTimeMillis();
    }

    public void giveInterestToEveryone() {
        cooldown = System.currentTimeMillis() + ConfigValues.getInterestDelay();
        Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), this::giveInterest);
    }

    public void saveInterest(FileConfiguration savesConfig) {
        savesConfig.set("interest-save", getInterestCooldownMillis());
    }

    private void loopInterest() {
        if (!isInterestActive()) return;
        if (getInterestCooldownMillis() <= 0) giveInterestToEveryone();
        BPTaskManager.setTask(BPTaskManager.INTEREST_TASK, Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), this::loopInterest, 10L));
    }

    public void giveInterest() {
        boolean interestToVault = ConfigValues.isGivingInterestOnVaultBalance();
        boolean payOfflineToAfk = ConfigValues.isPayingAfkPlayerOfflineAmount();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {

            List<Bank> availableBanks = new ArrayList<>();
            for (Bank bank : BankUtils.getBanks())
                if (bank.isGiveInterestIfNotAvailable() || BankUtils.isAvailable(bank, p)) availableBanks.add(bank);

            if (availableBanks.isEmpty()) continue;

            Player oP = p.getPlayer();
            if (oP != null) {
                boolean isAfk = BankPlus.INSTANCE().getAfkManager().isAFK(oP);
                if (!oP.hasPermission(defaultInterestPermission) || (isAfk && !payOfflineToAfk)) continue;

                BigDecimal interestAmount = BigDecimal.ZERO;
                for (Bank bank : availableBanks) {
                    BPEconomy economy = bank.getBankEconomy();
                    if (economy.getBankBalance(p).compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal interestMoney = getInterestMoney(bank, p, BankUtils.getInterestRate(bank, oP));
                    if (payOfflineToAfk && isAfk) {
                        interestMoney = ConfigValues.isAfkInterestDifferentRate() ? getInterestMoney(bank, p, BankUtils.getAfkInterestRate(bank, oP)) : getInterestMoney(bank, p, BankUtils.getOfflineInterestRate(bank, oP));
                    }

                    BigDecimal maxAmount = BankUtils.getMaxInterestAmount(bank, p);
                    if (maxAmount.compareTo(BigDecimal.ZERO) > 0) interestMoney = interestMoney.min(maxAmount);

                    BigDecimal added;
                    if (!interestToVault) added = economy.addBankBalance(p, interestMoney, TransactionType.INTEREST);
                    else {
                        BankPlus.INSTANCE().getVaultEconomy().depositPlayer(p, interestMoney.doubleValue());
                        added = interestMoney;
                    }
                    interestAmount = interestAmount.add(added);
                }
                if (!MessageValues.isInterestBroadcastEnabled()) continue;

                BigDecimal skipAmount = ConfigValues.getInterestMessageSkipAmount();
                if (skipAmount.compareTo(BigDecimal.ZERO) > 0 && skipAmount.compareTo(interestAmount) >= 0) continue;

                if (availableBanks.size() > 1) BPMessages.send(oP, MessageValues.getMultiInterestMoney(), BPUtils.placeValues(p, interestAmount), true);
                else {
                    if (BankUtils.isFull(availableBanks.get(0), p) && !ConfigValues.isGivingInterestOnVaultBalance()) {
                        BPMessages.send(oP, MessageValues.getInterestBankFull(), BPUtils.placeValues(p, interestAmount), true);
                        continue;
                    }

                    if (interestAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        BPMessages.send(oP, MessageValues.getInterestNoMoney(), BPUtils.placeValues(p, interestAmount), true);
                        continue;
                    }

                    BPMessages.send(oP, MessageValues.getInterestMoney(), BPUtils.placeValues(p, interestAmount), true);
                }
            } else {
                if (!isOfflineInterestEnabled || offlineTimeExpired(p) || !BPUtils.hasOfflinePermission(p, offlineInterestPermission)) continue;

                for (Bank bank : availableBanks) {
                    BPEconomy economy = bank.getBankEconomy();
                    if (economy.getBankBalance(p).compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal interestMoney = isOfflineInterestDifferentRate() ?
                                    getInterestMoney(bank, p, BankUtils.getOfflineInterestRate(bank, p)) :
                                    getInterestMoney(bank, p, BankUtils.getInterestRate(bank, p));

                    BigDecimal maxAmount = BankUtils.getMaxInterestAmount(bank, p);
                    if (maxAmount.compareTo(BigDecimal.ZERO) > 0) interestMoney = interestMoney.min(maxAmount);

                    BigDecimal added;
                    if (!interestToVault) added = economy.addBankBalance(p, interestMoney, TransactionType.INTEREST);
                    else {
                        BankPlus.INSTANCE().getVaultEconomy().depositPlayer(p, interestMoney.doubleValue());
                        added = interestMoney;
                    }
                    economy.setOfflineInterest(p, economy.getOfflineInterest(p).add(added));
                }
            }
        }
    }

    public BigDecimal getInterestMoney(Bank bank, OfflinePlayer p, BigDecimal defaultInterest) {
        BigDecimal playerBalance = bank.getBankEconomy().getBankBalance(p);

        if (!ConfigValues.isInterestLimiterEnabled() || !ConfigValues.isAccumulatingInterestLimiter())
            return playerBalance.multiply(defaultInterest.divide(BigDecimal.valueOf(100)));

        List<String> limiter = BankUtils.getInterestLimiter(bank, BankUtils.getCurrentLevel(bank, p));
        BigDecimal result = BigDecimal.ZERO, count = playerBalance;

        for (String line : limiter) {
            if (!line.contains(":")) continue;

            String[] split1 = line.split(":");
            if (BPUtils.isInvalidNumber(split1[1])) continue;

            String[] split2 = split1[0].split("-");
            if (BPUtils.isInvalidNumber(split2[0]) || BPUtils.isInvalidNumber(split2[1])) continue;

            String interest = split1[1].replace("%", ""), from = split2[0], to = split2[1];
            BigDecimal interestRate = new BigDecimal(interest), fromNumber = new BigDecimal(from), toNumber = new BigDecimal(to);

            if (fromNumber.compareTo(toNumber) > 0) toNumber = fromNumber;

            if (toNumber.compareTo(count) < 0) {
                result = result.add(toNumber.multiply(interestRate).divide(BigDecimal.valueOf(100)));
                count = count.subtract(toNumber);
            } else {
                result = result.add(count.multiply(interestRate).divide(BigDecimal.valueOf(100)));
                return result;
            }
        }
        return result;
    }

    public boolean isInterestActive() {
        if (!ConfigValues.isInterestEnabled()) {
            BPTaskManager.removeTask(BPTaskManager.INTEREST_TASK);
            wasDisabled = true;
            return false;
        }
        wasDisabled = false;
        return true;
    }

    public boolean offlineTimeExpired(OfflinePlayer p) {
        if (ConfigValues.getOfflineInterestLimit() <= 0L) return false;
        long lastSeen;
        try {
            lastSeen = p.getLastSeen();
        } catch (NoSuchMethodError e) {
            lastSeen = p.getLastPlayed();
        }
        return (System.currentTimeMillis() - lastSeen) > ConfigValues.getOfflineInterestLimit();
    }

    public boolean wasDisabled() {
        return wasDisabled;
    }
}