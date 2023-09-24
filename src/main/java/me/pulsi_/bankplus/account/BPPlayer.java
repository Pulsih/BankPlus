package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.bankSystem.Bank;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;

public class BPPlayer {

    private final Player player;
    private Bank openedBank;
    private HashMap<String, String> playerBankClickHolder;
    private int banktopPosition = -1;
    private BigDecimal debt;
    private BukkitTask closingTask;

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

    public BigDecimal getDebt() {
        return debt;
    }

    public BukkitTask getClosingTask() {
        return closingTask;
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

    public void setDebt(BigDecimal debt) {
        this.debt = debt;
    }

    public void setClosingTask(BukkitTask closingTask) {
        this.closingTask = closingTask;
    }
}