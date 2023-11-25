package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.bankSystem.Bank;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.HashMap;

public class BPPlayer {

    private final Player player;

    private Bank openedBank;
    private HashMap<String, String> playerBankClickHolder;
    private int banktopPosition = -1;
    private BukkitTask bankUpdatingTask, closingTask;
    private HashMap<String, PlayerBank> bankInformation = new HashMap<>();

    public BPPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public Bank getOpenedBank() {
        return openedBank;
    }

    public HashMap<String, String> getPlayerBankClickHolder() {
        return playerBankClickHolder;
    }

    public int getBanktopPosition() {
        return banktopPosition;
    }

    public BukkitTask getBankUpdatingTask() {
        return bankUpdatingTask;
    }

    public BukkitTask getClosingTask() {
        return closingTask;
    }

    public HashMap<String, PlayerBank> getBankInformation() {
        return bankInformation;
    }

    public void setOpenedBank(Bank openedBank) {
        this.openedBank = openedBank;
    }

    public void setPlayerBankClickHolder(HashMap<String, String> playerBankClickHolder) {
        this.playerBankClickHolder = playerBankClickHolder;
    }

    public void setBanktopPosition(int banktopPosition) {
        this.banktopPosition = banktopPosition;
    }

    public void setBankUpdatingTask(BukkitTask bankUpdatingTask) {
        this.bankUpdatingTask = bankUpdatingTask;
    }

    public void setClosingTask(BukkitTask closingTask) {
        this.closingTask = closingTask;
    }

    public void setBankInformation(HashMap<String, PlayerBank> bankInformation) {
        this.bankInformation = bankInformation;
    }

    public static class PlayerBank {
        private int level;
        private BigDecimal balance, debt, interest;

        public PlayerBank(int level, BigDecimal bankBalance, BigDecimal bankDebt, BigDecimal interest) {
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