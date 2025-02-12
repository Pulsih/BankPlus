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

        long interestSave = 0;

        if (loadFromFile) {
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
        OnlineInterestMethod onlineInterestMethod = new OnlineInterestMethod();
        OfflineInterestMethod offlineInterestMethod = new OfflineInterestMethod();

        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            if (p.isOnline()) onlineInterestMethod.giveInterest(p);
            else if (isOfflineInterestEnabled) offlineInterestMethod.giveInterest(p);
        }
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

    public abstract static class InterestMethod {

        /**
         * Method to give the interest to the specified player.
         *
         * @param p The player, as default offline, but could be online, should be checked.
         */
        public abstract void giveInterest(OfflinePlayer p);

        /**
         * Calculate the amount of money that the player will receive from the interest.
         *
         * @param bank            The bank where the money should be deposited.
         * @param p               The player.
         * @param defaultInterest The interest rate as fallback.
         * @return The amount of money with all calculations already made (interest limiter).
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