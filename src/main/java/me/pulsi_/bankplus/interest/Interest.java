package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayerFilesUtils;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.OfflineInterestManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.banks.BanksManager;
import me.pulsi_.bankplus.managers.AFKManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.managers.TaskManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
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

    private static long cooldown = 0;
    public static boolean wasDisabled = false;

    public static void startInterest() {
        long interestSave = 0;

        FileConfiguration config = getInterestSaveConfig();
        if (config != null) interestSave = config.getLong("interest-save");

        File file = getInterestSaveFile();
        if (file != null) file.delete();

        if (interestSave > 0) cooldown = System.currentTimeMillis() + interestSave;
        else cooldown = System.currentTimeMillis() + Values.CONFIG.getInterestDelay();
        loopInterest();
    }

    public static long getInterestCooldownMillis() {
        return cooldown - System.currentTimeMillis();
    }

    public static void restartInterest() {
        BukkitTask task = TaskManager.getInterestTask();
        if (task != null) task.cancel();
        startInterest();
    }

    public static void giveInterestToEveryone() {
        cooldown = System.currentTimeMillis() + Values.CONFIG.getInterestDelay();

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
            Bukkit.getOnlinePlayers().forEach(Interest::giveMultiInterest);
            if (Values.CONFIG.isGivingInterestToOfflinePlayers())
                Bukkit.getScheduler().runTaskAsynchronously(BankPlus.instance(), () -> giveMultiInterest(Bukkit.getOfflinePlayers()));
        } else {
            Bukkit.getOnlinePlayers().forEach(Interest::giveSingleInterest);
            if (Values.CONFIG.isGivingInterestToOfflinePlayers())
                Bukkit.getScheduler().runTaskAsynchronously(BankPlus.instance(), () -> giveSingleInterest(Bukkit.getOfflinePlayers()));
        }
    }

    public static void saveInterest() {
        File file = new File(BankPlus.instance().getDataFolder(), "interest-save.yml");
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

    private static File getInterestSaveFile() {
        File file = new File(BankPlus.instance().getDataFolder(), "interest-save.yml");
        if (!file.exists()) return null;
        return file;
    }

    private static FileConfiguration getInterestSaveConfig() {
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

    private static void loopInterest() {
        if (!isInterestActive()) return;
        if (getInterestCooldownMillis() <= 0) giveInterestToEveryone();
        TaskManager.setInterestTask(Bukkit.getScheduler().runTaskLater(BankPlus.instance(), Interest::loopInterest, 10L));
    }

    private static void giveSingleInterest(Player p) {
        if (!p.hasPermission("bankplus.receive.interest") || (Values.CONFIG.isIgnoringAfkPlayers() && AFKManager.isAFK(p))) return;

        SingleEconomyManager singleEconomyManager = new SingleEconomyManager(p);
        BigDecimal bankBalance = singleEconomyManager.getBankBalance();
        BigDecimal interestMoney = bankBalance.multiply(Values.CONFIG.getInterestMoneyGiven().divide(BigDecimal.valueOf(100)));
        BigDecimal maxBankCapacity = new BanksManager(Values.CONFIG.getMainGuiName()).getCapacity(p), maxAmount = Values.CONFIG.getInterestMaxAmount();

        if (bankBalance.doubleValue() <= 0) {
            if (Values.MESSAGES.isInterestBroadcastEnabled())
                MessageManager.send(p, Values.MESSAGES.getInterestNoMoney(), true);
            return;
        }
        if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
        if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
            BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
            if (newAmount.doubleValue() <= 0) {
                if (Values.MESSAGES.isInterestBroadcastEnabled())
                    MessageManager.send(p, Values.MESSAGES.getInterestBankFull(), true);
                return;
            }
            if (Values.MESSAGES.isInterestBroadcastEnabled())
                MessageManager.send(p, Values.MESSAGES.getInterestMoney(), BPMethods.placeValues(p, newAmount), true);
            singleEconomyManager.setBankBalance(maxBankCapacity);
            return;
        }
        if (Values.MESSAGES.isInterestBroadcastEnabled())
            MessageManager.send(p, Values.MESSAGES.getInterestMoney(), BPMethods.placeValues(p, interestMoney), true);
        singleEconomyManager.addBankBalance(interestMoney);
    }

    private static void giveMultiInterest(Player p) {
        if (!p.hasPermission("bankplus.receive.interest") || (Values.CONFIG.isIgnoringAfkPlayers() && AFKManager.isAFK(p))) return;

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
            MessageManager.send(p, Values.MESSAGES.getMultiInterestMoney(), BPMethods.placeValues(p, interestAmount), true);
    }

    private static void giveSingleInterest(OfflinePlayer[] players) {
        String wName = Bukkit.getWorlds().get(0).getName(), perm = Values.CONFIG.getInterestOfflinePermission();
        for (OfflinePlayer p : players) {
            if (p.isOnline() || !BankPlus.instance().getPermissions().playerHas(wName, p, perm)) continue;

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

    private static void giveMultiInterest(OfflinePlayer[] players) {

        String wName = Bukkit.getWorlds().get(0).getName(), perm = Values.CONFIG.getInterestOfflinePermission();
        for (OfflinePlayer p : players) {
            boolean hasToSave = false;

            MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);
            for (String bankName : new BanksManager().getAvailableBanks(p)) {
                if (p.isOnline() || !BankPlus.instance().getPermissions().playerHas(wName, p, perm)) continue;

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
            if (hasToSave) BankPlusPlayerFilesUtils.savePlayerFile(p, true);
        }
    }

    private static void addOfflineInterest(OfflinePlayer p, BigDecimal amount, boolean save) {
        if (Values.CONFIG.isOfflineInterestEarnedMessageEnabled())
            OfflineInterestManager.setOfflineInterest(p, OfflineInterestManager.getOfflineInterest(p).add(amount), save);
    }

    private static boolean isInterestActive() {
        if (!Values.CONFIG.isInterestEnabled()) {
            BukkitTask task = TaskManager.getInterestTask();
            if (task != null) task.cancel();
            wasDisabled = true;
            return false;
        }
        wasDisabled = false;
        return true;
    }
}