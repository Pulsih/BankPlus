package me.pulsi_.bankplus.debt;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.BPPlayerFiles;
import me.pulsi_.bankplus.utils.BPFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.math.BigDecimal;

public class DebtUtils {

    public static void setDebt(OfflinePlayer p, BigDecimal amount) {
        if (p.isOnline()) {
            BPPlayer bp = BankPlus.INSTANCE.getPlayerRegistry().get(p.getPlayer());
            bp.setDebt(amount);
            return;
        }

        BPPlayerFiles files = new BPPlayerFiles(p);
        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig(file);
        config.set("debt", BPFormatter.formatBigDouble(amount));
        files.savePlayerFile(config, file, true);
    }

    public static void saveDebt(Player p) {
        BPPlayerFiles files = new BPPlayerFiles(p);
        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig(file);
        config.set("debt", BPFormatter.formatBigDouble(getDebt(p)));
        files.savePlayerFile(config, file, true);
    }

    public static BigDecimal getDebt(OfflinePlayer p) {
        if (p.isOnline()) {
            BPPlayer bp = BankPlus.INSTANCE.getPlayerRegistry().get(p.getPlayer());
            BigDecimal debt = bp.getDebt();

            if (debt == null) {
                BPPlayerFiles files = new BPPlayerFiles(p);
                String sDebt = files.getPlayerConfig().getString("debt");
                bp.setDebt(new BigDecimal(sDebt == null ? "0" : sDebt));
            }
            return bp.getDebt();
        }
        BPPlayerFiles files = new BPPlayerFiles(p);
        String sDebt = files.getPlayerConfig().getString("debt");
        return new BigDecimal(sDebt == null ? "0" : sDebt);
    }
}