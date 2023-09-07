package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerFiles;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.economy.MultiEconomyManager;
import me.pulsi_.bankplus.economy.SingleEconomyManager;
import me.pulsi_.bankplus.managers.BPConfigs;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.TransactionType;
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

public class Interest {

    private long cooldown = 0;
    private boolean wasDisabled = true;

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
        boolean offInterest = Values.CONFIG.isGivingInterestToOfflinePlayers();

        Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE, () -> {
            if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
                Bukkit.getOnlinePlayers().forEach(this::giveMultiInterest);
                if (offInterest) giveMultiInterest(Bukkit.getOfflinePlayers());
            } else {
                Bukkit.getOnlinePlayers().forEach(this::giveSingleInterest);
                if (offInterest) giveSingleInterest(Bukkit.getOfflinePlayers());
            }
        });
    }

    public void saveInterest() {
        FileConfiguration config = getInterestSaveConfig();
        config.set("interest-save", getInterestCooldownMillis());
        saveInterestSaveFile(config);
    }

    private FileConfiguration getInterestSaveConfig() {
        return BankPlus.INSTANCE.getConfigManager().getConfig(BPConfigs.Type.SAVES);
    }

    private void saveInterestSaveFile(FileConfiguration config) {
        File file = BankPlus.INSTANCE.getConfigManager().getFile(BPConfigs.Type.SAVES);

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

    private void giveSingleInterest(Player p) {
        if (!p.hasPermission("bankplus.receive.interest") || (Values.CONFIG.isIgnoringAfkPlayers() && BankPlus.INSTANCE.getAfkManager().isAFK(p))) return;

        SingleEconomyManager singleEconomyManager = new SingleEconomyManager(p);
        BigDecimal bankBalance = singleEconomyManager.getBankBalance();

        BankReader bankReader = new BankReader(Values.CONFIG.getMainGuiName());
        BigDecimal interestMoney = bankBalance.multiply(bankReader.getInterest(p).divide(BigDecimal.valueOf(100)));
        BigDecimal maxBankCapacity = bankReader.getCapacity(p), maxAmount = Values.CONFIG.getInterestMaxAmount();

        if (bankBalance.doubleValue() <= 0) {
            if (Values.MESSAGES.isInterestBroadcastEnabled())
                BPMessages.send(p, Values.MESSAGES.getInterestNoMoney(), true);
            return;
        }
        if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
        if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
            BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
            if (newAmount.doubleValue() <= 0) {
                if (Values.MESSAGES.isInterestBroadcastEnabled())
                    BPMessages.send(p, Values.MESSAGES.getInterestBankFull(), true);
                return;
            }
            if (Values.MESSAGES.isInterestBroadcastEnabled())
                BPMessages.send(p, Values.MESSAGES.getInterestMoney(), BPUtils.placeValues(p, newAmount), true);
            singleEconomyManager.setBankBalance(maxBankCapacity, TransactionType.INTEREST);
            return;
        }
        if (Values.MESSAGES.isInterestBroadcastEnabled())
            BPMessages.send(p, Values.MESSAGES.getInterestMoney(), BPUtils.placeValues(p, interestMoney), true);
        singleEconomyManager.addBankBalance(interestMoney, TransactionType.INTEREST);
    }

    private void giveMultiInterest(Player p) {
        if (!p.hasPermission("bankplus.receive.interest") || (Values.CONFIG.isIgnoringAfkPlayers() && BankPlus.INSTANCE.getAfkManager().isAFK(p))) return;

        MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);
        BigDecimal interestAmount = new BigDecimal(0);
        for (String bankName : new BankReader().getAvailableBanks(p)) {

            BigDecimal bankBalance = multiEconomyManager.getBankBalance(bankName);
            BankReader bankReader = new BankReader(bankName);
            BigDecimal interestMoney = bankBalance.multiply(bankReader.getInterest(p).divide(BigDecimal.valueOf(100)));
            BigDecimal maxBankCapacity = bankReader.getCapacity(p), maxAmount = Values.CONFIG.getInterestMaxAmount();

            if (bankBalance.doubleValue() <= 0) continue;
            if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
            if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
                BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
                if (newAmount.doubleValue() <= 0) continue;
                interestAmount = newAmount;
                multiEconomyManager.setBankBalance(maxBankCapacity, bankName, TransactionType.INTEREST);
                continue;
            }
            multiEconomyManager.addBankBalance(interestMoney, bankName, TransactionType.INTEREST);
            interestAmount = interestMoney;
        }
        if (Values.MESSAGES.isInterestBroadcastEnabled())
            BPMessages.send(p, Values.MESSAGES.getMultiInterestMoney(), BPUtils.placeValues(p, interestAmount), true);
    }

    private void giveSingleInterest(OfflinePlayer[] players) {
        Permission permission = BankPlus.INSTANCE.getPermissions();
        if (permission == null) {
            BPLogger.error("Cannot give offline interest, no permission plugin found!");
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

            SingleEconomyManager singleEconomyManager = new SingleEconomyManager(p);
            BigDecimal bankBalance = singleEconomyManager.getBankBalance();
            BankReader reader = new BankReader(Values.CONFIG.getMainGuiName());
            BigDecimal interest = Values.CONFIG.isOfflineInterestDifferentRate() ? reader.getOfflineInterest(p) : reader.getInterest(p);
            BigDecimal interestMoney = bankBalance.multiply(interest.divide(BigDecimal.valueOf(100)));
            BigDecimal maxBankCapacity = reader.getCapacity(p), maxAmount = Values.CONFIG.getInterestMaxAmount();

            if (bankBalance.doubleValue() <= 0) continue;
            if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
            if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
                BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
                if (newAmount.doubleValue() <= 0) continue;
                singleEconomyManager.addBankBalance(newAmount, true, false, TransactionType.INTEREST);
                continue;
            }
            singleEconomyManager.addBankBalance(interestMoney, true, false, TransactionType.INTEREST);
        }
    }

    private void giveMultiInterest(OfflinePlayer[] players) {
        Permission permission = BankPlus.INSTANCE.getPermissions();
        if (permission == null) {
            BPLogger.error("Cannot give offline interest, no permission plugin found!");
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

            boolean hasToSave = false;
            for (String bankName : new BankReader().getAvailableBanks(p)) {
                MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);
                BigDecimal bankBalance = multiEconomyManager.getBankBalance(bankName);
                BankReader reader = new BankReader(bankName);
                BigDecimal interest = Values.CONFIG.isOfflineInterestDifferentRate() ? reader.getOfflineInterest(p) : reader.getInterest(p);
                BigDecimal interestMoney = bankBalance.multiply(interest.divide(BigDecimal.valueOf(100)));
                BigDecimal maxBankCapacity = reader.getCapacity(p), maxAmount = Values.CONFIG.getInterestMaxAmount();

                if (bankBalance.doubleValue() <= 0) continue;
                if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
                if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
                    BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
                    if (newAmount.doubleValue() <= 0) continue;
                    multiEconomyManager.addBankBalance(newAmount, bankName, true, false, TransactionType.INTEREST);
                    hasToSave = true;
                    continue;
                }
                multiEconomyManager.addBankBalance(interestMoney, bankName, true, false, TransactionType.INTEREST);
                hasToSave = true;
            }
            if (hasToSave) new BPPlayerFiles(p).savePlayerFile(true);
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