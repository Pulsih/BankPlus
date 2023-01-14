package me.pulsi_.bankplus.account.economy;

import me.pulsi_.bankplus.account.BankPlusPlayerFiles;
import me.pulsi_.bankplus.utils.BPMethods;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class OfflineInterestManager {

    private final Player p;

    public OfflineInterestManager(Player p) {
        this.p = p;
    }

    public OfflineInterestManager(OfflinePlayer p) {
        this.p = p.getPlayer();
    }

    public BigDecimal getOfflineInterest() {
        String interest = new BankPlusPlayerFiles(p).getPlayerConfig().getString("Offline-Interest");
        return new BigDecimal(interest == null ? "0" : interest);
    }

    public void setOfflineInterest(BigDecimal amount, boolean save) {
        BankPlusPlayerFiles files = new BankPlusPlayerFiles(p);
        FileConfiguration config = files.getPlayerConfig();
        config.set("Offline-Interest", BPMethods.formatBigDouble(amount));
        if (save) files.savePlayerFile(config, true);
    }
}