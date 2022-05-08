package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Interest {

    private static int interestCount;

    public static void startsInterest() {
        int interestSave = BankPlus.getInstance().players().getInt("Interest-Save");
        interestCount = Values.CONFIG.getInterestDelay();

        if (interestSave <= 0) {
            loopInterest(interestCount + 1);
        } else {
            loopInterest(interestSave + 1);
            BankPlus.getInstance().players().set("Interest-Save", null);
            BankPlus.getInstance().savePlayers();
        }
    }

    public static void loopInterest(int cooldown) {
        if (!Values.CONFIG.isInterestEnabled()) return;

        if (cooldown <= 1) {
            interestCount = Values.CONFIG.getInterestDelay();
            giveInterestToEveryone();
        } else {
            interestCount = interestCount - 1;
        }
        Bukkit.getScheduler().runTaskLater(BankPlus.getInstance(), () -> loopInterest(interestCount), Methods.ticksInMinutes(1));
    }

    public static void saveInterest() {
        BankPlus.getInstance().players().set("Interest-Save", interestCount);
        BankPlus.getInstance().savePlayers();
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

        for (Player p : Bukkit.getOnlinePlayers())
            if (p.hasPermission("bankplus.receive.interest"))
                giveInterest(p, moneyPercentage, maxAmount);

        if (!Values.CONFIG.isGivingInterestToOfflinePlayers()) return;
        String wName = Bukkit.getWorlds().get(0).getName();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers())
            if (BankPlus.getPermissions().playerHas(wName, p, "bankplus.receive.interest"))
                giveInterest(p, moneyPercentage, maxAmount);
    }

    private static void giveInterest(Player p, double moneyPercentage, long maxAmount) {
        long bankBalance = EconomyManager.getInstance().getBankBalance(p);
        long interestMoney = (long) (bankBalance * moneyPercentage);
        long maxBankCapacity = Values.CONFIG.getMaxBankCapacity();

        if (bankBalance == 0) {
            if (Values.CONFIG.isInterestBroadcastEnabled()) MessageManager.noMoneyInterest(p);
            return;
        }
        if (maxBankCapacity != 0 && (bankBalance + interestMoney >= maxBankCapacity)) {
            EconomyManager.getInstance().setPlayerBankBalance(p, maxBankCapacity);
            if (Values.CONFIG.isInterestBroadcastEnabled())
                MessageManager.interestBroadcastMessage(p, maxBankCapacity - bankBalance);
            return;
        }
        if (interestMoney == 0) {
            EconomyManager.getInstance().addPlayerBankBalance(p, 1);
            if (Values.CONFIG.isInterestBroadcastEnabled()) MessageManager.interestBroadcastMessage(p, 1);
            return;
        }
        if (interestMoney >= maxAmount) {
            EconomyManager.getInstance().addPlayerBankBalance(p, maxAmount);
            if (Values.CONFIG.isInterestBroadcastEnabled()) MessageManager.interestBroadcastMessage(p, maxAmount);
            return;
        }
        EconomyManager.getInstance().addPlayerBankBalance(p, interestMoney);
        if (Values.CONFIG.isInterestBroadcastEnabled()) MessageManager.interestBroadcastMessage(p, interestMoney);
    }

    private static void giveInterest(OfflinePlayer p, double moneyPercentage, long maxAmount) {
        long bankBalance = EconomyManager.getInstance().getBankBalance(p);
        long interestMoney = (long) (bankBalance * moneyPercentage);
        long maxBankCapacity = Values.CONFIG.getMaxBankCapacity();

        if (bankBalance == 0) return;
        if (maxBankCapacity != 0 && (bankBalance + interestMoney >= maxBankCapacity)) {
            EconomyManager.getInstance().setPlayerBankBalance(p, maxBankCapacity);
            addOfflineInterest(p, maxBankCapacity - bankBalance);
            return;
        }
        if (interestMoney == 0) {
            EconomyManager.getInstance().addPlayerBankBalance(p, 1);
            addOfflineInterest(p, 1);
            return;
        }
        if (interestMoney >= maxAmount) {
            EconomyManager.getInstance().addPlayerBankBalance(p, maxAmount);
            addOfflineInterest(p, maxAmount);
            return;
        }
        EconomyManager.getInstance().addPlayerBankBalance(p, interestMoney);
        addOfflineInterest(p, interestMoney);
    }

    private static void addOfflineInterest(OfflinePlayer p, long amount) {
        if (!Values.CONFIG.isOfflineInterestEarnedMessageEnabled()) return;
        long offlineInterest = EconomyManager.getInstance().getOfflineInterest(p);
        EconomyManager.getInstance().setOfflineInterest(p, offlineInterest + amount);
    }
}