package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.managers.BPTaskManager;
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

    private final String defaultInterestPermission = "bankplus.receive.interest";
    private String offlineInterestPermission;

    private long cooldown = 0;
    private boolean wasDisabled = true, isOfflineInterestEnabled;

    private final BankPlus plugin;

    public BPInterest(BankPlus plugin) {
        this.plugin = plugin;
    }

    /**
     * Restart the interest cooldown.
     *
     * @param loadFromFile Set to true if you want to restart the interest
     *                     cooldown to the one saved in the interest-save file.
     */
    public void restartInterest(boolean loadFromFile) {
        isOfflineInterestEnabled = ConfigValues.isOfflineInterestEnabled();
        offlineInterestPermission = isOfflineInterestDifferentRate() ? ConfigValues.getInterestOfflinePermission() : defaultInterestPermission;

        long interestSave = 0;

        if (loadFromFile) {
            FileConfiguration config = plugin.getConfigs().getConfig("saves.yml");
            if (config != null) interestSave = config.getLong("interest-save");
        }

        if (interestSave > 0) cooldown = System.currentTimeMillis() + interestSave;
        else cooldown = System.currentTimeMillis() + ConfigValues.getInterestDelay();
        loopInterest();
    }

    public String getDefaultInterestPermission() {
        return defaultInterestPermission;
    }

    public String getOfflineInterestPermission() {
        return offlineInterestPermission;
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
        boolean interestToVault = ConfigValues.isGivingInterestOnVaultBalance(), ignoreAfkPlayers = ConfigValues.isIgnoringAfkPlayers();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {

            List<Bank> availableBanks = new ArrayList<>();
            for (Bank bank : BankUtils.getBanks())
                if (bank.isGiveInterestIfNotAvailable() || BankUtils.isAvailable(bank, p)) availableBanks.add(bank);

            if (availableBanks.isEmpty()) continue;

            Player oP = p.getPlayer();
            if (oP != null) { // If the player is online.
                if (!oP.hasPermission(defaultInterestPermission)) continue;

                boolean isAfk = BankPlus.INSTANCE().getAfkManager().isAFK(oP);
                if (ignoreAfkPlayers && isAfk) continue;

                BigDecimal interestAmount = BigDecimal.ZERO;
                for (Bank bank : availableBanks) {
                    BPEconomy economy = bank.getBankEconomy();
                    if (economy.getBankBalance(p).compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal interestMoney;
                    if (isAfk) interestMoney = getInterestMoney(bank, p, BankUtils.getAfkInterestRate(bank, oP));
                    else interestMoney = getInterestMoney(bank, p, BankUtils.getInterestRate(bank, oP));

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

                if (availableBanks.size() > 1)
                    BPMessages.send(oP, MessageValues.getMultiInterestMoney(), BPUtils.placeValues(p, interestAmount));
                else {
                    if (BankUtils.isFull(availableBanks.get(0), p) && !ConfigValues.isGivingInterestOnVaultBalance()) {
                        BPMessages.send(oP, MessageValues.getInterestBankFull(), BPUtils.placeValues(p, interestAmount));
                        continue;
                    }

                    if (interestAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        BPMessages.send(oP, MessageValues.getInterestNoMoney(), BPUtils.placeValues(p, interestAmount));
                        continue;
                    }

                    BPMessages.send(oP, MessageValues.getInterestMoney(), BPUtils.placeValues(p, interestAmount));
                }
            } else { // If the player is offline.
                if (!isOfflineInterestEnabled || offlineTimeExpired(p) || !BPUtils.hasOfflinePermission(p, offlineInterestPermission))
                    continue;

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

    /**
     * Calculate the amount of money that the player will receive from the interest.
     *
     * @param bank            The bank where the money should be deposited.
     * @param p               The player.
     * @param defaultInterest The interest rate.
     * @return The amount of money with all calculations already made (interest limiter).
     */
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