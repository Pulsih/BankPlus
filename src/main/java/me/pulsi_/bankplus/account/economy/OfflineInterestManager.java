package me.pulsi_.bankplus.account.economy;

import me.pulsi_.bankplus.account.AccountManager;
import me.pulsi_.bankplus.utils.BPMethods;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class OfflineInterestManager {

    public static BigDecimal getOfflineInterest(Player p) {
        BigDecimal amount;
        String interest = AccountManager.getPlayerConfig(p).getString("Offline-Interest");
        if (interest == null) amount = new BigDecimal(0);
        else amount = new BigDecimal(interest);
        return amount;
    }

    public static BigDecimal getOfflineInterest(OfflinePlayer p) {
        BigDecimal amount;
        String interest = AccountManager.getPlayerConfig(p).getString("Offline-Interest");
        if (interest == null) amount = new BigDecimal(0);
        else amount = new BigDecimal(interest);
        return amount;
    }

    public static void setOfflineInterest(Player p, BigDecimal amount, boolean save) {
        AccountManager.getPlayerConfig(p).set("Offline-Interest", BPMethods.formatBigDouble(amount));
        if (save) AccountManager.savePlayerFile(p, true);
    }

    public static void setOfflineInterest(OfflinePlayer p, BigDecimal amount, boolean save) {
        AccountManager.getPlayerConfig(p).set("Offline-Interest", BPMethods.formatBigDouble(amount));
        if (save) AccountManager.savePlayerFile(p, true);
    }
}