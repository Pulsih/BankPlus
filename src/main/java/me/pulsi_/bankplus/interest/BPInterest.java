package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.TransactionType;
import me.pulsi_.bankplus.managers.BPConfigs;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class BPInterest {

    private long cooldown = 0;
    private boolean wasDisabled = true;

    private final BPEconomy economy;

    public BPInterest() {
        economy = BankPlus.getBPEconomy();
    }

    public void startInterest() {
        long interestSave = 0;

        FileConfiguration config = getInterestSaveConfig();
        if (config != null) {
            interestSave = config.getLong("interest-save");
            config.set("interest-save", null);
            saveInterestSaveFile(config);
        }

        if (interestSave > 0) cooldown = System.currentTimeMillis() + interestSave;
        else cooldown = System.currentTimeMillis() + Values.CONFIG.getInterestDelay();
        loopInterest();
    }

    public long getInterestCooldownMillis() {
        return cooldown - System.currentTimeMillis();
    }

    public void restartInterest() {
        BukkitTask task = BankPlus.INSTANCE.getTaskManager().getInterestTask();
        if (task != null) task.cancel();
        startInterest();
    }

    public void giveInterestToEveryone() {
        cooldown = System.currentTimeMillis() + Values.CONFIG.getInterestDelay();
        boolean isOfflineInterestEnabled = Values.CONFIG.isGivingInterestToOfflinePlayers();

        Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE, () -> {
            Bukkit.getOnlinePlayers().forEach(this::giveInterest);
            if (isOfflineInterestEnabled) giveInterest(Bukkit.getOfflinePlayers());
        });
    }

    public void saveInterest() {
        FileConfiguration config = getInterestSaveConfig();
        config.set("interest-save", getInterestCooldownMillis());
        saveInterestSaveFile(config);
    }

    private FileConfiguration getInterestSaveConfig() {
        return BankPlus.INSTANCE.getConfigManager().getConfig(BPConfigs.Type.SAVES.name);
    }

    private void saveInterestSaveFile(FileConfiguration config) {
        File file = BankPlus.INSTANCE.getConfigManager().getFile(BPConfigs.Type.SAVES.name);

        try {
            config.save(file);
        } catch (IOException e) {
            BPLogger.error("Failed to save the interest-save file! " + e.getMessage());
        }
    }

    private void loopInterest() {
        if (!isInterestActive()) return;
        if (getInterestCooldownMillis() <= 0) giveInterestToEveryone();
        BankPlus.INSTANCE.getTaskManager().setInterestTask(Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, this::loopInterest, 10L));
    }

    private void giveInterest(Player p) {
        if (!p.hasPermission("bankplus.receive.interest") || (Values.CONFIG.isIgnoringAfkPlayers() && BankPlus.INSTANCE.getAfkManager().isAFK(p))) return;

        BigDecimal interestAmount = new BigDecimal(0);
        for (String bankName : new BankReader().getAvailableBanks(p)) {

            BigDecimal bankBalance = economy.getBankBalance(p, bankName);
            BankReader reader = new BankReader(bankName);
            BigDecimal interestMoney = getInterestMoney(p, bankBalance, reader.getInterest(p), reader);
            BigDecimal maxBankCapacity = reader.getCapacity(p), maxAmount = Values.CONFIG.getInterestMaxAmount();

            if (bankBalance.doubleValue() <= 0) continue;
            if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
            if (maxBankCapacity.doubleValue() > 0D && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
                BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
                if (newAmount.doubleValue() <= 0) continue;
                interestAmount = newAmount;
                economy.setBankBalance(p, maxBankCapacity, bankName, TransactionType.INTEREST);
                continue;
            }
            economy.addBankBalance(p, interestMoney, bankName, TransactionType.INTEREST);
            interestAmount = interestMoney;
        }
        if (Values.MESSAGES.isInterestBroadcastEnabled())
            BPMessages.send(p, Values.MESSAGES.getMultiInterestMoney(), BPUtils.placeValues(p, interestAmount), true);
    }

    private void giveInterest(OfflinePlayer[] players) {
        Permission permission = BankPlus.INSTANCE.getPermissions();
        if (permission == null) {
            BPLogger.warn("Cannot give offline interest, no permission plugin found!");
            return;
        }

        String perm = Values.CONFIG.getInterestOfflinePermission();
        for (OfflinePlayer p : players) {
            if ((System.currentTimeMillis() - p.getLastSeen()) > Values.CONFIG.getOfflineInterestLimit()) continue;

            boolean hasPermission = false;
            for (World world : Bukkit.getWorlds()) {
                hasPermission = perm == null || perm.isEmpty() || permission.playerHas(world.getName(), p, perm);
                if (hasPermission) break;
            }
            if (!hasPermission) continue;

            for (String bankName : new BankReader().getAvailableBanks(p)) {
                BigDecimal bankBalance = economy.getBankBalance(p, bankName);
                BankReader reader = new BankReader(bankName);
                BigDecimal interestMoney = Values.CONFIG.isOfflineInterestDifferentRate() ?
                        getInterestMoney(p, bankBalance, reader.getOfflineInterest(p), reader) :
                        getInterestMoney(p, bankBalance, reader.getInterest(p), reader);
                BigDecimal maxBankCapacity = reader.getCapacity(p), maxAmount = Values.CONFIG.getInterestMaxAmount();

                if (bankBalance.doubleValue() <= 0) continue;
                if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
                if (maxBankCapacity.doubleValue() > 0D && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
                    BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
                    if (newAmount.doubleValue() <= 0) continue;

                    economy.setOfflineInterest(p, economy.getOfflineInterest(p).add(newAmount), false);
                    economy.addBankBalance(p, newAmount, bankName, TransactionType.INTEREST);
                    continue;
                }
                economy.setOfflineInterest(p, economy.getOfflineInterest(p).add(interestMoney), false);
                economy.addBankBalance(p, interestMoney, bankName, TransactionType.INTEREST);
            }
        }
    }

    private BigDecimal getInterestMoney(OfflinePlayer p, BigDecimal balance, BigDecimal defaultInterest, BankReader reader) {
        if (!Values.CONFIG.enableInterestLimiter() || !Values.CONFIG.accumulateInterestLimiter())
            return balance.multiply(defaultInterest.divide(BigDecimal.valueOf(100)));
        else {
            List<String> limiter = reader.getInterestLimiter(reader.getCurrentLevel(p));
            BigDecimal result = new BigDecimal(0), count = balance;
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
    }

    private boolean isInterestActive() {
        if (!Values.CONFIG.isInterestEnabled()) {
            BukkitTask task = BankPlus.INSTANCE.getTaskManager().getInterestTask();
            if (task != null) task.cancel();
            wasDisabled = true;
            return false;
        }
        wasDisabled = false;
        return true;
    }

    public boolean wasDisabled() {
        return wasDisabled;
    }
}