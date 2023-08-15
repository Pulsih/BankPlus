package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.bankSystem.Bank;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;

public class BPPlayer {

    private final Player player;
    private final File playerFile;
    private final FileConfiguration playerConfig;
    private Bank openedBank;
    private HashMap<String, String> playerBankClickHolder;
    private int banktopPosition = -1;

    private BigDecimal debt;

    public BPPlayer(Player player, File playerFile, FileConfiguration playerConfig) {
        this.player = player;
        this.playerFile = playerFile;
        this.playerConfig = playerConfig;
    }

    public Player getPlayer() {
        return player;
    }

    public File getPlayerFile() {
        return playerFile;
    }

    public FileConfiguration getPlayerConfig() {
        return playerConfig;
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
}