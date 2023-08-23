package me.pulsi_.bankplus.economy;

import me.pulsi_.bankplus.account.BPPlayerFiles;
import me.pulsi_.bankplus.utils.BPFormatter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class OfflineInterestManager {

    private final Player p;

    public OfflineInterestManager(Player p) {
        this.p = p;
    }

    public BigDecimal getOfflineInterest() {
        String interest = new BPPlayerFiles(p).getPlayerConfig().getString("Offline-Interest");
        return new BigDecimal(interest == null ? "0" : interest);
    }

    public void setOfflineInterest(BigDecimal amount, boolean save) {
        BPPlayerFiles files = new BPPlayerFiles(p);
        FileConfiguration config = files.getPlayerConfig();
        config.set("Offline-Interest", BPFormatter.formatBigDouble(amount));
        if (save) files.savePlayerFile(config, true);
    }
}