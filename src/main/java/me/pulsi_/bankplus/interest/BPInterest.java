package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.managers.BPTaskManager;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.SavesFile;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.List;

public class BPInterest {

    public static final String INTEREST_SAVE_PATH = "interest-save";

    private long cooldown = 0;
    private boolean wasDisabled = true, isOfflineInterestEnabled;

    /**
     * Get the time left, in milliseconds, before the interest will be given.
     * @return Time left in milliseconds.
     */
    public long getInterestCooldownMillis() {
        return cooldown - System.currentTimeMillis();
    }

    /**
     * Restart the interest cooldown.
     *
     * @param loadFromFile Set to true if you want to restart the interest
     *                     cooldown to the one saved in the interest-save file.
     */
    public void restartInterest(boolean loadFromFile) {
        isOfflineInterestEnabled = ConfigValues.isOfflineInterestEnabled();

        long interestSave = 0;
        if (loadFromFile) interestSave = SavesFile.getLong(INTEREST_SAVE_PATH);

        if (interestSave > 0) cooldown = System.currentTimeMillis() + interestSave;
        else cooldown = System.currentTimeMillis() + ConfigValues.getInterestDelay();
        loopInterest();
    }

    /**
     * Give interest to every player, based on the settings in the config file.
     */
    public void giveInterest() {
        cooldown = System.currentTimeMillis() + ConfigValues.getInterestDelay();

        Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
            OnlineInterestMethod onlineInterestMethod = new OnlineInterestMethod();
            OfflineInterestMethod offlineInterestMethod = new OfflineInterestMethod();

            for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                if (p.isOnline()) onlineInterestMethod.giveInterest(p);
                else if (isOfflineInterestEnabled) offlineInterestMethod.giveInterest(p);
            }
        });
    }

    /**
     * Save the current interest time left to the saves file.
     */
    public void saveInterest() {
        SavesFile.set(INTEREST_SAVE_PATH, getInterestCooldownMillis());
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

    public boolean wasDisabled() {
        return wasDisabled;
    }

    private void loopInterest() {
        if (!isInterestActive()) return;
        if (getInterestCooldownMillis() <= 0) giveInterest();
        BPTaskManager.setTask(BPTaskManager.INTEREST_TASK, Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), this::loopInterest, 10L));
    }

    public abstract static class InterestMethod {

        /**
         * Method to give the interest to the specified player.
         *
         * @param p The player, as default offline, but could be online, should be checked.
         */
        public abstract void giveInterest(OfflinePlayer p);

        /**
         * Calculate the amount of money that the player would receive with the current interest rate.
         *
         * @param bank            The bank where to calculate the interest.
         * @param p               The player to retrieve bank level and other information.
         * @param defaultInterest The interest rate as fallback.
         * @return The amount of money with all calculations (interest limiter) already made.
         */
        public static BigDecimal getInterestMoney(Bank bank, OfflinePlayer p, BigDecimal defaultInterest) {
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

        /**
         * Check if the specified player is offline from more than the limit set in the config file.
         *
         * @param p The player to check.
         * @return true if it's offline for more than the limit.
         */
        public static boolean offlineTimeExpired(OfflinePlayer p) {
            if (ConfigValues.getOfflineInterestLimit() <= 0L) return false;
            long lastSeen;
            try {
                lastSeen = p.getLastSeen();
            } catch (NoSuchMethodError e) {
                lastSeen = p.getLastPlayed();
            }
            return (System.currentTimeMillis() - lastSeen) > ConfigValues.getOfflineInterestLimit();
        }

    }
}