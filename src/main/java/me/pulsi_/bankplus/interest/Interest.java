package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayerFiles;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.OfflineInterestManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankGuis.BanksManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

public class Interest {

    private long cooldown = 0;
    private boolean wasDisabled = false;

    public void startInterest() {
        long interestSave = 0;

        FileConfiguration config = getInterestSaveConfig();
        if (config != null) interestSave = config.getLong("interest-save");

        File file = getInterestSaveFile();
        if (file != null) file.delete();

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
        File file = new File(BankPlus.INSTANCE.getDataFolder(), "interest-save.yml");
        try {
            file.getParentFile().mkdir();
            file.createNewFile();
        } catch (IOException e) {
            BPLogger.error("Failed to to create the interest-save file! " + e.getMessage());
        }

        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            BPLogger.error("Failed to load the interest-save file! " + e.getMessage());
        }
        config.set("interest-save", getInterestCooldownMillis());

        try {
            config.save(file);
        } catch (IOException e) {
            BPLogger.error("Failed to save the interest-save file! " + e.getMessage());
        }
    }

    private File getInterestSaveFile() {
        File file = new File(BankPlus.INSTANCE.getDataFolder(), "interest-save.yml");
        if (!file.exists()) return null;
        return file;
    }

    private FileConfiguration getInterestSaveConfig() {
        File file = getInterestSaveFile();
        if (file == null || !file.exists()) return null;

        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            BPLogger.error("Failed to load the interest-save file! " + e.getMessage());
        }
        return config;
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
        BigDecimal interestMoney = bankBalance.multiply(Values.CONFIG.getInterestMoneyGiven().divide(BigDecimal.valueOf(100)));
        BigDecimal maxBankCapacity = new BanksManager(Values.CONFIG.getMainGuiName()).getCapacity(p), maxAmount = Values.CONFIG.getInterestMaxAmount();

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
                BPMessages.send(p, Values.MESSAGES.getInterestMoney(), BPMethods.placeValues(p, newAmount), true);
            singleEconomyManager.setBankBalance(maxBankCapacity);
            return;
        }
        if (Values.MESSAGES.isInterestBroadcastEnabled())
            BPMessages.send(p, Values.MESSAGES.getInterestMoney(), BPMethods.placeValues(p, interestMoney), true);
        singleEconomyManager.addBankBalance(interestMoney);
    }

    private void giveMultiInterest(Player p) {
        if (!p.hasPermission("bankplus.receive.interest") || (Values.CONFIG.isIgnoringAfkPlayers() && BankPlus.INSTANCE.getAfkManager().isAFK(p))) return;

        MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);
        BigDecimal interestAmount = new BigDecimal(0);
        for (String bankName : new BanksManager().getAvailableBanks(p)) {
            BigDecimal bankBalance = multiEconomyManager.getBankBalance(bankName);
            BigDecimal interestMoney = bankBalance.multiply(Values.CONFIG.getInterestMoneyGiven().divide(BigDecimal.valueOf(100)));
            BigDecimal maxBankCapacity = new BanksManager(bankName).getCapacity(p), maxAmount = Values.CONFIG.getInterestMaxAmount();

            if (bankBalance.doubleValue() <= 0) continue;
            if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
            if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
                BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
                if (newAmount.doubleValue() <= 0) continue;
                interestAmount = newAmount;
                multiEconomyManager.setBankBalance(maxBankCapacity, bankName);
                continue;
            }
            multiEconomyManager.addBankBalance(interestMoney, bankName);
            interestAmount = interestMoney;
        }
        if (Values.MESSAGES.isInterestBroadcastEnabled())
            BPMessages.send(p, Values.MESSAGES.getMultiInterestMoney(), BPMethods.placeValues(p, interestAmount), true);
    }

    private void giveSingleInterest(OfflinePlayer[] players) {
        Permission permission = BankPlus.INSTANCE.getPermissions();
        if (permission == null) {
            BPLogger.error("Cannot give offline interest, no permission plugin found!");
            return;
        }

        String wName = Bukkit.getWorlds().get(0).getName(), perm = Values.CONFIG.getInterestOfflinePermission();
        for (OfflinePlayer p : players) {
            if (p.isOnline() || !permission.playerHas(wName, p, perm)) continue;

            SingleEconomyManager singleEconomyManager = new SingleEconomyManager(p);
            BigDecimal bankBalance = singleEconomyManager.getBankBalance();
            BigDecimal interestMoney = bankBalance.multiply(Values.CONFIG.getInterestMoneyGiven().divide(BigDecimal.valueOf(100)));
            BigDecimal maxBankCapacity = Values.CONFIG.getMaxBankCapacity(), maxAmount = Values.CONFIG.getInterestMaxAmount();

            if (bankBalance.doubleValue() <= 0) continue;
            if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
            if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
                BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
                if (newAmount.doubleValue() <= 0) continue;
                singleEconomyManager.addBankBalance(newAmount);
                addOfflineInterest(p, newAmount, true);
                continue;
            }
            singleEconomyManager.addBankBalance(interestMoney);
            addOfflineInterest(p, interestMoney, true);
        }
    }

    private void giveMultiInterest(OfflinePlayer[] players) {
        Permission permission = BankPlus.INSTANCE.getPermissions();
        if (permission == null) {
            BPLogger.error("Cannot give offline interest, no permission plugin found!");
            return;
        }

        String wName = Bukkit.getWorlds().get(0).getName(), perm = Values.CONFIG.getInterestOfflinePermission();
        for (OfflinePlayer p : players) {
            boolean hasToSave = false;

            MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);
            for (String bankName : new BanksManager().getAvailableBanks(p)) {
                if (p.isOnline() || !permission.playerHas(wName, p, perm)) continue;

                BigDecimal bankBalance = multiEconomyManager.getBankBalance(bankName);
                BigDecimal interestMoney = bankBalance.multiply(Values.CONFIG.getInterestMoneyGiven().divide(BigDecimal.valueOf(100)));
                BigDecimal maxBankCapacity = new BanksManager(bankName).getCapacity(p), maxAmount = Values.CONFIG.getInterestMaxAmount();

                if (bankBalance.doubleValue() <= 0) continue;
                if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
                if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
                    BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
                    if (newAmount.doubleValue() <= 0) continue;
                    multiEconomyManager.addBankBalance(newAmount, bankName);
                    addOfflineInterest(p, newAmount, false);
                    hasToSave = true;
                    continue;
                }
                multiEconomyManager.addBankBalance(interestMoney, bankName);
                addOfflineInterest(p, interestMoney, false);
                hasToSave = true;
            }
            if (hasToSave) new BankPlusPlayerFiles(p).savePlayerFile(true);
        }
    }

    private void addOfflineInterest(OfflinePlayer p, BigDecimal amount, boolean save) {
        if (!Values.CONFIG.isOfflineInterestEarnedMessageEnabled()) return;
        OfflineInterestManager interestManager = new OfflineInterestManager(p);
        interestManager.setOfflineInterest(interestManager.getOfflineInterest().add(amount), save);
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