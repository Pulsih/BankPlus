package me.pulsi_.bankplus.account;

import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

/**
 * Class to access and manage player information easier and faster once loaded on the server.
 * <p>
 * The player will appear as loaded even after he quit, the player will be marked as unloaded
 * once the plugin executes the saving task and will mark all loaded player that left as unloaded.
 */
public class OnlineInfoHolder {

    private static final HashMap<UUID, HashMap<String, BankInfo>> bankInfo = new HashMap<>();

    public static void updatePlayerInfo(OfflinePlayer p, HashMap<String, BankInfo> info) {
        bankInfo.put(p.getUniqueId(), info);
    }

    public static BankInfo getInfo(OfflinePlayer p, String bankName) {
        HashMap<String, BankInfo> info = bankInfo.get(p.getUniqueId());
        return (info == null ? null : info.get(bankName));
    }

    public static void unloadInfo(OfflinePlayer p) {
        unloadInfo(p.getUniqueId());
    }

    public static void unloadInfo(UUID uuid) {
        bankInfo.remove(uuid);
    }

    public static boolean isPlayerLoaded(OfflinePlayer p) {
        return bankInfo.containsKey(p.getUniqueId());
    }

    public static HashMap<UUID, HashMap<String, BankInfo>> getBankInfo() {
        return bankInfo;
    }

    public static class BankInfo {

        private int level;
        private BigDecimal balance, debt, interest;

        public BankInfo(int level, BigDecimal bankBalance, BigDecimal bankDebt, BigDecimal interest) {
            this.level = level;
            this.balance = bankBalance;
            this.debt = bankDebt;
            this.interest = interest;
        }

        public int getLevel() {
            return level;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public BigDecimal getDebt() {
            return debt;
        }

        public BigDecimal getInterest() {
            return interest;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public void setDebt(BigDecimal debt) {
            this.debt = debt;
        }

        public void setInterest(BigDecimal interest) {
            this.interest = interest;
        }
    }
}