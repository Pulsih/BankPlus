package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.managers.BPConfigs;
import me.pulsi_.bankplus.managers.BPTaskManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BPInterest {

    public static final String defaultInterestPermission = "bankplus.receive.interest";

    private long cooldown = 0;
    private boolean wasDisabled = true, isOfflineInterestEnabled;

    private final BankPlus plugin;

    public BPInterest(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void restartInterest() {
        isOfflineInterestEnabled = Values.CONFIG.isGivingInterestToOfflinePlayers();
        long interestSave = 0;

        FileConfiguration config = plugin.getConfigs().getConfig(BPConfigs.Type.SAVES.name);
        if (config != null) interestSave = config.getLong("interest-save");

        if (interestSave > 0) cooldown = System.currentTimeMillis() + interestSave;
        else cooldown = System.currentTimeMillis() + Values.CONFIG.getInterestDelay();
        loopInterest();
    }

    public long getInterestCooldownMillis() {
        return cooldown - System.currentTimeMillis();
    }

    public void giveInterestToEveryone() {
        cooldown = System.currentTimeMillis() + Values.CONFIG.getInterestDelay();
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
        BigDecimal maxAmount = Values.CONFIG.getInterestMaxAmount();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {

            List<String> availableBanks = new ArrayList<>();
            for (Bank bank : BankUtils.getBanks())
                if (bank.isGiveInterestIfNotAvailable() || BankUtils.isAvailable(bank.getIdentifier(), p))
                    availableBanks.add(bank.getIdentifier());

            if (availableBanks.isEmpty()) continue;

            Player oP = p.getPlayer();
            if (oP != null) {
                if (!oP.hasPermission(defaultInterestPermission) || BankPlus.INSTANCE().getAfkManager().isAFK(oP)) continue;

                BigDecimal interestAmount = BigDecimal.ZERO;
                for (String bankName : availableBanks) {
                    BPEconomy economy = BPEconomy.get(bankName);
                    if (economy == null) {
                        BPLogger.warn("Could not add interest to the player " + p.getName() + " because the bank " + bankName + " does not exist.");
                        continue;
                    }
                    if (economy.getBankBalance(p).compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal interestMoney = getInterestMoney(bankName, p, BankUtils.getInterestRate(bankName, p)).min(maxAmount);

                    BigDecimal added = economy.addBankBalance(p, interestMoney, TransactionType.INTEREST);
                    interestAmount = interestAmount.add(added);
                }
                if (!Values.MESSAGES.isInterestBroadcastEnabled()) continue;

                if (availableBanks.size() > 1) BPMessages.send(p, Values.MESSAGES.getMultiInterestMoney(), BPUtils.placeValues(p, interestAmount), true);
                else {
                    if (BankUtils.isFull(availableBanks.get(0), p)) {
                        BPMessages.send(p, Values.MESSAGES.getInterestBankFull(), BPUtils.placeValues(p, interestAmount), true);
                        continue;
                    }

                    if (interestAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        BPMessages.send(p, Values.MESSAGES.getInterestNoMoney(), BPUtils.placeValues(p, interestAmount), true);
                        continue;
                    }

                    BPMessages.send(p, Values.MESSAGES.getInterestMoney(), BPUtils.placeValues(p, interestAmount), true);
                }
            } else {
                if (!isOfflineInterestEnabled || offlineTimeExpired(p) || !BPUtils.hasOfflinePermission(p, Values.CONFIG.getInterestOfflinePermission())) continue;

                for (String bankName : availableBanks) {
                    BPEconomy economy = BPEconomy.get(bankName);
                    if (economy == null) {
                        BPLogger.warn("Could not add interest to the player " + p.getName() + " because the bank " + bankName + " does not exist.");
                        continue;
                    }
                    if (economy.getBankBalance(p).compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal interestMoney = (Values.CONFIG.isOfflineInterestDifferentRate() ?
                                    getInterestMoney(bankName, p, BankUtils.getOfflineInterestRate(bankName, p)) :
                                    getInterestMoney(bankName, p, BankUtils.getInterestRate(bankName, p))).min(maxAmount);

                    BigDecimal added = economy.addBankBalance(p, interestMoney, TransactionType.INTEREST);
                    economy.setOfflineInterest(p, economy.getOfflineInterest(p).add(added));
                }
            }
        }
    }

    public BigDecimal getInterestMoney(String bankName, OfflinePlayer p, BigDecimal defaultInterest) {
        BigDecimal playerBalance = BPEconomy.get(bankName).getBankBalance(p);

        if (!Values.CONFIG.enableInterestLimiter() || !Values.CONFIG.accumulateInterestLimiter())
            return playerBalance.multiply(defaultInterest.divide(BigDecimal.valueOf(100)));

        List<String> limiter = BankUtils.getInterestLimiter(bankName, BankUtils.getCurrentLevel(bankName, p));
        BigDecimal result = new BigDecimal(0), count = playerBalance;
        for (String line : limiter) {
            if (!line.contains(":")) continue;

            String[] split1 = line.split(":");
            if (BPUtils.isInvalidNumber(split1[1])) continue;

            String[] split2 = split1[0].split("-");
            if (BPUtils.isInvalidNumber(split2[0]) || BPUtils.isInvalidNumber(split2[1])) continue;

            String interest = split1[1].replace("%", ""), from = split2[0], to = split2[1];
            BigDecimal interestRate = new BigDecimal(interest), fromNumber = new BigDecimal(from), toNumber = new BigDecimal(to);

            if (fromNumber.doubleValue() > toNumber.doubleValue()) toNumber = fromNumber;

            if (toNumber.doubleValue() < count.doubleValue()) {
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
        if (!Values.CONFIG.isInterestEnabled()) {
            BPTaskManager.removeTask(BPTaskManager.INTEREST_TASK);
            wasDisabled = true;
            return false;
        }
        wasDisabled = false;
        return true;
    }

    public boolean offlineTimeExpired(OfflinePlayer p) {
        if (Values.CONFIG.getOfflineInterestLimit() <= 0L) return false;
        long lastSeen;
        try {
            lastSeen = p.getLastSeen();
        } catch (NoSuchMethodError e) {
            lastSeen = p.getLastPlayed();
        }
        return (System.currentTimeMillis() - lastSeen) > Values.CONFIG.getOfflineInterestLimit();
    }

    public boolean wasDisabled() {
        return wasDisabled;
    }
}