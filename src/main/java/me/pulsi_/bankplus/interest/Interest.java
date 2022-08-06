package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.AccountManager;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.OfflineInterestManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.guis.BanksManager;
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
                Bukkit.getScheduler().runTaskAsynchronously(BankPlus.getInstance(), () -> giveMultiInterest(Bukkit.getOfflinePlayers()));
        } else {
            Bukkit.getOnlinePlayers().forEach(Interest::giveSingleInterest);
            if (Values.CONFIG.isGivingInterestToOfflinePlayers())
                Bukkit.getScheduler().runTaskAsynchronously(BankPlus.getInstance(), () -> giveSingleInterest(Bukkit.getOfflinePlayers()));
        }
    }

    public static void saveInterest() {
        File file = new File(BankPlus.getInstance().getDataFolder(), "interest-save.yml");
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
        File file = new File(BankPlus.getInstance().getDataFolder(), "interest-save.yml");
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
        TaskManager.setInterestTask(Bukkit.getScheduler().runTaskLater(BankPlus.getInstance(), Interest::loopInterest, 10L));
    }

    private static void giveSingleInterest(Player p) {
        if (!p.hasPermission("bankplus.receive.interest") || (Values.CONFIG.isIgnoringAfkPlayers() && AFKManager.isAFK(p))) return;

        BigDecimal bankBalance = SingleEconomyManager.getBankBalance(p);
        BigDecimal interestMoney = bankBalance.multiply(Values.CONFIG.getInterestMoneyGiven().divide(BigDecimal.valueOf(100)));
        BigDecimal maxBankCapacity = BanksManager.getCapacity(p, Values.CONFIG.getMainGuiName()), maxAmount = Values.CONFIG.getInterestMaxAmount();

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
            SingleEconomyManager.setBankBalance(p, maxBankCapacity);
            return;
        }
        if (Values.MESSAGES.isInterestBroadcastEnabled())
            MessageManager.send(p, Values.MESSAGES.getInterestMoney(), BPMethods.placeValues(p, interestMoney), true);
        SingleEconomyManager.addBankBalance(p, interestMoney);
    }

    private static void giveMultiInterest(Player p) {
        if (!p.hasPermission("bankplus.receive.interest") || (Values.CONFIG.isIgnoringAfkPlayers() && AFKManager.isAFK(p))) return;
        BigDecimal interestAmount = new BigDecimal(0);
        for (String bankName : BanksManager.getAvailableBanks(p)) {
            BigDecimal bankBalance = MultiEconomyManager.getBankBalance(p, bankName);
            BigDecimal interestMoney = bankBalance.multiply(Values.CONFIG.getInterestMoneyGiven().divide(BigDecimal.valueOf(100)));
            BigDecimal maxBankCapacity = BanksManager.getCapacity(p, bankName), maxAmount = Values.CONFIG.getInterestMaxAmount();

            if (bankBalance.doubleValue() <= 0) continue;
            if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
            if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
                BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
                if (newAmount.doubleValue() <= 0) continue;
                interestAmount = newAmount;
                MultiEconomyManager.setBankBalance(p, maxBankCapacity, bankName);
                continue;
            }
            MultiEconomyManager.addBankBalance(p, interestMoney, bankName);
            interestAmount = interestMoney;
        }
        if (Values.MESSAGES.isInterestBroadcastEnabled())
            MessageManager.send(p, Values.MESSAGES.getMultiInterestMoney(), BPMethods.placeValues(p, interestAmount), true);
    }

    private static void giveSingleInterest(OfflinePlayer[] players) {
        String wName = Bukkit.getWorlds().get(0).getName(), perm = Values.CONFIG.getInterestOfflinePermission();
        for (OfflinePlayer p : players) {
            if (p.isOnline() || !BankPlus.getPermissions().playerHas(wName, p, perm)) continue;

            BigDecimal bankBalance = SingleEconomyManager.getBankBalance(p);
            BigDecimal interestMoney = bankBalance.multiply(Values.CONFIG.getInterestMoneyGiven().divide(BigDecimal.valueOf(100)));
            BigDecimal maxBankCapacity = Values.CONFIG.getMaxBankCapacity(), maxAmount = Values.CONFIG.getInterestMaxAmount();

            if (bankBalance.doubleValue() <= 0) continue;
            if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
            if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
                BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
                if (newAmount.doubleValue() <= 0) continue;
                SingleEconomyManager.addBankBalance(p, newAmount);
                addOfflineInterest(p, newAmount, true);
                continue;
            }
            SingleEconomyManager.addBankBalance(p, interestMoney);
            addOfflineInterest(p, interestMoney, true);
        }
    }

    private static void giveMultiInterest(OfflinePlayer[] players) {
        String wName = Bukkit.getWorlds().get(0).getName(), perm = Values.CONFIG.getInterestOfflinePermission();
        for (OfflinePlayer p : players) {
            boolean hasToSave = false;
            for (String bankName : BanksManager.getAvailableBanks(p)) {
                if (p.isOnline() || !BankPlus.getPermissions().playerHas(wName, p, perm)) continue;

                BigDecimal bankBalance = MultiEconomyManager.getBankBalance(p, bankName);
                BigDecimal interestMoney = bankBalance.multiply(Values.CONFIG.getInterestMoneyGiven().divide(BigDecimal.valueOf(100)));
                BigDecimal maxBankCapacity = BanksManager.getCapacity(p, bankName), maxAmount = Values.CONFIG.getInterestMaxAmount();

                if (bankBalance.doubleValue() <= 0) continue;
                if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
                if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
                    BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
                    if (newAmount.doubleValue() <= 0) continue;
                    MultiEconomyManager.addBankBalance(p, newAmount, bankName);
                    addOfflineInterest(p, newAmount, false);
                    hasToSave = true;
                    continue;
                }
                MultiEconomyManager.addBankBalance(p, interestMoney, bankName);
                addOfflineInterest(p, interestMoney, false);
                hasToSave = true;
            }
            if (hasToSave) AccountManager.savePlayerFile(p, true);
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