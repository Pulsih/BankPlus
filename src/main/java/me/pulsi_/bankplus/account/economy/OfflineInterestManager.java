package me.pulsi_.bankplus.account.economy;

import me.pulsi_.bankplus.account.BankPlusPlayerFiles;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMethods;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class OfflineInterestManager {

    private final Player player;
    private final OfflinePlayer offlinePlayer;

    public OfflineInterestManager(Player player) {
        this.player = player;
        this.offlinePlayer = null;
    }

    public OfflineInterestManager(OfflinePlayer offlinePlayer) {
        this.player = null;
        this.offlinePlayer = offlinePlayer;
    }

    public BigDecimal getOfflineInterest() {
        if (player == null && offlinePlayer == null) {
            BPLogger.error("Cannot get offline interest because the player is null!");
            return null;
        }

        String interest;
        if (player != null) interest = new BankPlusPlayerFiles(player).getPlayerConfig().getString("Offline-Interest");
        else interest = new BankPlusPlayerFiles(offlinePlayer).getOfflinePlayerConfig().getString("Offline-Interest");

        return interest == null ? new BigDecimal(0) : new BigDecimal(interest);
    }

    public void setOfflineInterest(BigDecimal amount, boolean save) {
        if (player == null && offlinePlayer == null) {
            BPLogger.error("Cannot set offline interest because the player is null!");
            return;
        }

        if (player != null) {
            BankPlusPlayerFiles files = new BankPlusPlayerFiles(player);
            files.getPlayerConfig().set("Offline-Interest", BPMethods.formatBigDouble(amount));
            if (save) files.savePlayerFile(true);
        } else {
            BankPlusPlayerFiles files = new BankPlusPlayerFiles(offlinePlayer);
            FileConfiguration config = files.getOfflinePlayerConfig();
            config.set("Offline-Interest", BPMethods.formatBigDouble(amount));
            if (save) files.saveOfflinePlayerFile(config, true);
        }
    }
}