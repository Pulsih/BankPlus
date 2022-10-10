package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.bankGuis.BankGui;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;

public class BankPlusPlayer {

    private final Player player;
    private final File playerFile;
    private final FileConfiguration playerConfig;
    private BankGui openedBank;
    private HashMap<String, String> playerBankClickHolder;
    private int banktopPosition = -1;

    public BankPlusPlayer(Player player, File playerFile, FileConfiguration playerConfig) {
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

    public BankGui getOpenedBank() {
        return openedBank;
    }

    public HashMap<String, String> getPlayerBankClickHolder() {
        return playerBankClickHolder;
    }

    public int getBanktopPosition() {
        return banktopPosition;
    }

    public void setOpenedBank(BankGui openedBank) {
        this.openedBank = openedBank;
    }

    public void setPlayerBankClickHolder(HashMap<String, String> playerBankClickHolder) {
        this.playerBankClickHolder = playerBankClickHolder;
    }

    public void setBanktopPosition(int banktopPosition) {
        this.banktopPosition = banktopPosition;
    }
}