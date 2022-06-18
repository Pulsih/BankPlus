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

    private static int interestCount;
    private static long cooldown;
    public static boolean isInterestActive = false;
    private static BukkitTask task = null;

    public static void startsInterest() {
        int interestSave = BankPlus.getCm().getConfig("players").getInt("Interest-Save");
        interestCount = Values.CONFIG.getInterestDelay();

        if (interestSave <= 0) {
            cooldown = System.currentTimeMillis() + Methods.millisecondsInMinutes(interestCount);
            loopInterest(interestCount + 1);
        } else {
            cooldown = System.currentTimeMillis() + Methods.millisecondsInMinutes(interestSave);
            loopInterest(interestSave + 1);
            BankPlus.getCm().getConfig("players").set("Interest-Save", null);
            BankPlus.getCm().savePlayers();
        }
    }

    public static void loopInterest(int count) {
        if (!Values.CONFIG.isInterestEnabled()) {
            isInterestActive = false;
            return;
        }
        isInterestActive = true;

        if (count <= 1) {
            interestCount = Values.CONFIG.getInterestDelay();
            cooldown = System.currentTimeMillis() + Methods.millisecondsInMinutes(interestCount);
            giveInterestToEveryone();
        } else {
            interestCount = interestCount - 1;
        }
        task = Bukkit.getScheduler().runTaskLater(BankPlus.getInstance(), () -> loopInterest(interestCount), Methods.ticksInMinutes(1));
    }

    public static void saveInterest() {
        BankPlus.getCm().getConfig("players").set("Interest-Save", interestCount);
        BankPlus.getCm().savePlayers();
    }

    public static long getInterestCooldownMillis() {
        return cooldown - System.currentTimeMillis();
    }

    public static void setInterestCount(int time) {
        cooldown = System.currentTimeMillis() + Methods.millisecondsInMinutes(time);
        if (task != null) task.cancel();
        interestCount = time;
        loopInterest(time + 1);
    }

    public static void giveInterestToEveryone() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("bankplus.receive.interest")) continue;

            if (Values.CONFIG.isIgnoringAfkPlayers()) {
                if (!AFKManager.isAFK(p)) giveInterest(p);
            } else {
                giveInterest(p);
            }
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

        if (bankBalance.doubleValue() == 0) {
            if (Values.MESSAGES.isInterestBroadcastEnabled()) MessageManager.noMoneyInterest(p);
            return;
        }
        if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
            if (Values.MESSAGES.isInterestBroadcastEnabled())
                MessageManager.interestBroadcastMessage(p, maxBankCapacity.subtract(bankBalance));
            EconomyManager.setPlayerBankBalance(p, maxBankCapacity);
            return;
        }
        if (interestMoney.doubleValue() >= maxAmount.doubleValue()) {
            if (Values.MESSAGES.isInterestBroadcastEnabled()) MessageManager.interestBroadcastMessage(p, maxAmount);
            EconomyManager.addPlayerBankBalance(p, maxAmount);
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

        if (bankBalance.doubleValue() == 0) return;
        if (maxBankCapacity.doubleValue() != 0 && (bankBalance.add(interestMoney).doubleValue() >= maxBankCapacity.doubleValue())) {
            EconomyManager.addPlayerBankBalance(p, maxBankCapacity.subtract(bankBalance));
            addOfflineInterest(p, maxBankCapacity.subtract(bankBalance));
            return;
        }
        if (interestMoney.doubleValue() >= maxAmount.doubleValue()) {
            EconomyManager.addPlayerBankBalance(p, maxAmount);
            addOfflineInterest(p, maxAmount);
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