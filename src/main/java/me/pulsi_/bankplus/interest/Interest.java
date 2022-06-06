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

public class Interest {

    private static int interestCount;

    public static boolean isInterestActive = false;

    public static void startsInterest() {
        int interestSave = BankPlus.getCm().getConfig("players").getInt("Interest-Save");
        interestCount = Values.CONFIG.getInterestDelay();

        if (interestSave <= 0) {
            loopInterest(interestCount + 1);
        } else {
            loopInterest(interestSave + 1);
            BankPlus.getCm().getConfig("players").set("Interest-Save", null);
            BankPlus.getCm().savePlayers();
        }
    }

    public static void loopInterest(int cooldown) {
        if (!Values.CONFIG.isInterestEnabled()) {
            isInterestActive = false;
            return;
        }
        isInterestActive = true;

        if (cooldown <= 1) {
            interestCount = Values.CONFIG.getInterestDelay();
            giveInterestToEveryone();
        } else {
            interestCount = interestCount - 1;
        }
        Bukkit.getScheduler().runTaskLater(BankPlus.getInstance(), () -> loopInterest(interestCount), Methods.ticksInMinutes(1));
    }

    public static void saveInterest() {
        BankPlus.getCm().getConfig("players").set("Interest-Save", interestCount);
        BankPlus.getCm().savePlayers();
    }

    public static int getInterestCount() {
        return interestCount;
    }

    public static void setInterestCount(int cooldown) {
        interestCount = cooldown;
    }

    public static void giveInterestToEveryone() {
        double moneyPercentage = Values.CONFIG.getInterestMoneyGiven();
        long maxAmount = Values.CONFIG.getInterestMaxAmount();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("bankplus.receive.interest")) continue;

            if (Values.CONFIG.isIgnoringAfkPlayers()) {
                if (!AFKManager.isAFK(p)) giveInterest(p, moneyPercentage, maxAmount);
            } else {
                giveInterest(p, moneyPercentage, maxAmount);
            }
        }

        if (!Values.CONFIG.isGivingInterestToOfflinePlayers()) return;
        Bukkit.getScheduler().runTaskAsynchronously(BankPlus.getInstance(), () -> {
            String wName = Bukkit.getWorlds().get(0).getName();
            for (OfflinePlayer p : Bukkit.getOfflinePlayers())
                if (!p.isOnline() && BankPlus.getPermissions().playerHas(wName, p, "bankplus.receive.interest"))
                    giveInterest(p, moneyPercentage, maxAmount);
        });
    }

    private static void giveInterest(Player p, double moneyPercentage, long maxAmount) {
        long bankBalance = EconomyManager.getBankBalance(p);
        long interestMoney = (long) (bankBalance * moneyPercentage);
        long maxBankCapacity = Values.CONFIG.getMaxBankCapacity();

        if (bankBalance == 0) {
            if (Values.MESSAGES.isInterestBroadcastEnabled()) MessageManager.noMoneyInterest(p);
            return;
        }
        if (maxBankCapacity != 0 && (bankBalance + interestMoney >= maxBankCapacity)) {
            EconomyManager.setPlayerBankBalance(p, maxBankCapacity);
            if (Values.MESSAGES.isInterestBroadcastEnabled())
                MessageManager.interestBroadcastMessage(p, maxBankCapacity - bankBalance);
            return;
        }
        if (interestMoney == 0) {
            EconomyManager.addPlayerBankBalance(p, 1);
            if (Values.MESSAGES.isInterestBroadcastEnabled()) MessageManager.interestBroadcastMessage(p, 1);
            return;
        }
        if (interestMoney >= maxAmount) {
            EconomyManager.addPlayerBankBalance(p, maxAmount);
            if (Values.MESSAGES.isInterestBroadcastEnabled()) MessageManager.interestBroadcastMessage(p, maxAmount);
            return;
        }
        EconomyManager.addPlayerBankBalance(p, interestMoney);
        if (Values.MESSAGES.isInterestBroadcastEnabled()) MessageManager.interestBroadcastMessage(p, interestMoney);
    }

    private static void giveInterest(OfflinePlayer p, double moneyPercentage, long maxAmount) {
        long bankBalance = EconomyManager.getBankBalance(p);
        long interestMoney = (long) (bankBalance * moneyPercentage);
        long maxBankCapacity = Values.CONFIG.getMaxBankCapacity();

        if (bankBalance == 0) return;
        if (maxBankCapacity != 0 && (bankBalance + interestMoney >= maxBankCapacity)) {
            EconomyManager.addPlayerBankBalance(p, maxBankCapacity - bankBalance);
            addOfflineInterest(p, maxBankCapacity - bankBalance);
            return;
        }
        if (interestMoney == 0) {
            EconomyManager.addPlayerBankBalance(p, 1);
            addOfflineInterest(p, 1);
            return;
        }
        if (interestMoney >= maxAmount) {
            EconomyManager.addPlayerBankBalance(p, maxAmount);
            addOfflineInterest(p, maxAmount);
            return;
        }
        EconomyManager.addPlayerBankBalance(p, interestMoney);
        addOfflineInterest(p, interestMoney);
    }

    private static void addOfflineInterest(OfflinePlayer p, long amount) {
        if (!Values.CONFIG.isOfflineInterestEarnedMessageEnabled()) return;
        long offlineInterest = EconomyManager.getOfflineInterest(p);
        EconomyManager.setOfflineInterest(p, offlineInterest + amount);
    }
}