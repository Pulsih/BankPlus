package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.AFKManager;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;

public class Interest {

    private static long cooldown = 0;
    public static boolean isInterestActive = false;
    private static BukkitTask task = null;

    public static void startsInterest() {
        long interestSave = BankPlus.getCm().getConfig("players").getLong("Interest-Save");

        if (interestSave <= 0) {
            cooldown = System.currentTimeMillis() + (Values.CONFIG.getInterestDelay() + Methods.secondsInMilliseconds(1));
            loopInterest();
        } else {
            cooldown = System.currentTimeMillis() + (interestSave + Methods.secondsInMilliseconds(1));
            loopInterest();
            BankPlus.getCm().getConfig("players").set("Interest-Save", null);
            BankPlus.getCm().savePlayers();
        }
    }

    public static void loopInterest() {
        if (!Values.CONFIG.isInterestEnabled()) return;
        if (getInterestCooldownMillis() <= 1) giveInterestToEveryone();
        task = Bukkit.getScheduler().runTaskLater(BankPlus.getInstance(), Interest::loopInterest, 5L);
    }

    public static void saveInterest() {
        BankPlus.getCm().getConfig("players").set("Interest-Save", cooldown);
        BankPlus.getCm().savePlayers();
    }

    public static long getInterestCooldownMillis() {
        return cooldown - System.currentTimeMillis();
    }

    public static void restartInterest() {
        if (task != null) task.cancel();
        startsInterest();
    }

    public static void giveInterestToEveryone() {
        cooldown = System.currentTimeMillis() + Values.CONFIG.getInterestDelay();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("bankplus.receive.interest")) continue;

            if (Values.CONFIG.isIgnoringAfkPlayers()) {
                if (!AFKManager.isAFK(p)) giveInterest(p);
                continue;
            }
            giveInterest(p);
        }

        if (!Values.CONFIG.isGivingInterestToOfflinePlayers()) return;
        Bukkit.getScheduler().runTaskAsynchronously(BankPlus.getInstance(), () -> {
            String wName = Bukkit.getWorlds().get(0).getName();
            for (OfflinePlayer p : Bukkit.getOfflinePlayers())
                if (!p.isOnline() && BankPlus.getPermissions().playerHas(wName, p, "bankplus.receive.interest"))
                    giveInterest(p);
        });
    }

    private static void giveInterest(Player p) {
        double moneyPercentage = Values.CONFIG.getInterestMoneyGiven();
        BigDecimal bankBalance = EconomyManager.getBankBalance(p);
        BigDecimal interestMoney = bankBalance.multiply(BigDecimal.valueOf(moneyPercentage));
        BigDecimal maxBankCapacity = Values.CONFIG.getMaxBankCapacity();
        BigDecimal maxAmount = Values.CONFIG.getInterestMaxAmount();

        if (bankBalance.doubleValue() <= 0) {
            if (Values.MESSAGES.isInterestBroadcastEnabled()) MessageManager.noMoneyInterest(p);
            return;
        }
        if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
        if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
            BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
            if (newAmount.doubleValue() <= 0) {
                if (Values.MESSAGES.isInterestBroadcastEnabled()) MessageManager.interestBankFull(p);
                return;
            }
            if (Values.MESSAGES.isInterestBroadcastEnabled()) MessageManager.interestBroadcastMessage(p, newAmount);
            EconomyManager.setPlayerBankBalance(p, maxBankCapacity);
            return;
        }
        if (Values.MESSAGES.isInterestBroadcastEnabled()) MessageManager.interestBroadcastMessage(p, interestMoney);
        EconomyManager.addPlayerBankBalance(p, interestMoney);
    }

    private static void giveInterest(OfflinePlayer p) {
        double moneyPercentage = Values.CONFIG.getInterestMoneyGiven();
        BigDecimal bankBalance = EconomyManager.getBankBalance(p);
        BigDecimal interestMoney = bankBalance.multiply(BigDecimal.valueOf(moneyPercentage));
        BigDecimal maxBankCapacity = Values.CONFIG.getMaxBankCapacity();
        BigDecimal maxAmount = Values.CONFIG.getInterestMaxAmount();

        if (bankBalance.doubleValue() <= 0) return;
        if (interestMoney.doubleValue() >= maxAmount.doubleValue()) interestMoney = maxAmount;
        if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
            BigDecimal newAmount = maxBankCapacity.subtract(bankBalance);
            if (newAmount.doubleValue() <= 0) return;
            EconomyManager.addPlayerBankBalance(p, newAmount);
            addOfflineInterest(p, maxBankCapacity.subtract(bankBalance));
            return;
        }
        EconomyManager.addPlayerBankBalance(p, interestMoney);
        addOfflineInterest(p, interestMoney);
    }

    private static void addOfflineInterest(OfflinePlayer p, BigDecimal amount) {
        if (!Values.CONFIG.isOfflineInterestEarnedMessageEnabled()) return;
        BigDecimal offlineInterest = EconomyManager.getOfflineInterest(p);
        EconomyManager.setOfflineInterest(p, offlineInterest.add(amount));
    }
}