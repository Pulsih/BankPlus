package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerManager;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.events.BPPreTransactionEvent;
import me.pulsi_.bankplus.managers.BPConfigs;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
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

    public void startInterest() {
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

    public void restartInterest() {
        BukkitTask task = BankPlus.INSTANCE().getTaskManager().getInterestTask();
        if (task != null) task.cancel();
        startInterest();
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
        BankPlus.INSTANCE().getTaskManager().setInterestTask(Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), this::loopInterest, 10L));
    }

    public void giveInterest() {
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {

            List<String> availableBanks = new ArrayList<>();
            for (Bank bank : BankManager.getBanks())
                if (bank.isGiveInterestIfNotAvailable() || BankManager.isAvailable(bank.getIdentifier(), p))
                    availableBanks.add(bank.getIdentifier());

            if (availableBanks.isEmpty()) continue;

            Player oP = p.getPlayer();
            if (oP != null) {
                if (!oP.hasPermission(defaultInterestPermission) || BankPlus.INSTANCE().getAfkManager().isAFK(oP)) continue;

                BigDecimal interestAmount = new BigDecimal(0);
                for (String bankName : availableBanks) {
                    BPEconomy economy = BPEconomy.get(bankName);
                    if (economy.getBankBalance(p).compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal interestMoney = getInterestMoney(bankName, p, BankManager.getInterestRate(bankName, p)), maxAmount = Values.CONFIG.getInterestMaxAmount();
                    if (interestMoney.compareTo(maxAmount) > 0) interestMoney = maxAmount;

                    BigDecimal amount = economy.addBankBalance(p, interestMoney, TransactionType.INTEREST);
                    interestAmount = interestAmount.add(amount);
                }
                if (!Values.MESSAGES.isInterestBroadcastEnabled()) continue;

                if (availableBanks.size() > 1) BPMessages.send(p, Values.MESSAGES.getMultiInterestMoney(), BPUtils.placeValues(p, interestAmount), true);
                else BPMessages.send(p, Values.MESSAGES.getInterestMoney(), BPUtils.placeValues(p, interestAmount), true);
            } else {
                if (!isOfflineInterestEnabled || offlineTimeExpired(p) || !BPUtils.hasOfflinePermission(p, Values.CONFIG.getInterestOfflinePermission())) continue;

                for (String bankName : availableBanks) {
                    BPEconomy economy = BPEconomy.get(bankName);
                    BigDecimal bankBalance = economy.getBankBalance(p);
                    if (bankBalance.compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal maxAmount = Values.CONFIG.getInterestMaxAmount(),
                            interestMoney = Values.CONFIG.isOfflineInterestDifferentRate() ?
                                    getInterestMoney(bankName, p, BankManager.getOfflineInterestRate(bankName, p)) :
                                    getInterestMoney(bankName, p, BankManager.getInterestRate(bankName, p));

                    if (interestMoney.compareTo(maxAmount) > 0) interestMoney = maxAmount;

                    economy.addBankBalance(p, interestMoney);
                    economy.setOfflineInterest(p, economy.getOfflineInterest(p).add(interestMoney));
                }
            }
        }
    }

    public BigDecimal getInterestMoney(String bankName, OfflinePlayer p, BigDecimal defaultInterest) {
        BigDecimal playerBalance = BPEconomy.get(bankName).getBankBalance(p);

        if (!Values.CONFIG.enableInterestLimiter() || !Values.CONFIG.accumulateInterestLimiter())
            return playerBalance.multiply(defaultInterest.divide(BigDecimal.valueOf(100)));

        List<String> limiter = BankManager.getInterestLimiter(bankName, BankManager.getCurrentLevel(bankName, p));
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
            BukkitTask task = BankPlus.INSTANCE().getTaskManager().getInterestTask();
            if (task != null) task.cancel();
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