package me.pulsi_.bankplus.debt;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.BPPlayerFiles;
import me.pulsi_.bankplus.utils.BPFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class DebtUtils {

    public static void setDebt(OfflinePlayer p, BigDecimal amount) {
        if (p.isOnline()) {
            Player op = Bukkit.getPlayer(p.getUniqueId());
            BPPlayer bp = BankPlus.INSTANCE.getPlayerRegistry().get(op);
            bp.setDebt(amount);
            return;
        }

        BPPlayerFiles files = new BPPlayerFiles(p);
        files.getPlayerConfig().set("Debt", BPFormatter.formatBigDouble(amount));
        files.savePlayerFile(true);
    }

    public static void saveDebt(Player p) {
        BPPlayerFiles files = new BPPlayerFiles(p);
        files.getPlayerConfig().set("Debt", BPFormatter.formatBigDouble(getDebt(p)));
        files.savePlayerFile(true);
    }

    public static BigDecimal getDebt(OfflinePlayer p) {
        if (p.isOnline()) {
            Player op = Bukkit.getPlayer(p.getUniqueId());
            BPPlayer bp = BankPlus.INSTANCE.getPlayerRegistry().get(op);
            BigDecimal debt = bp.getDebt();

            if (debt == null) {
                BPPlayerFiles files = new BPPlayerFiles(p);
                String sDebt = files.getPlayerConfig().getString("Debt");
                bp.setDebt(new BigDecimal(sDebt == null ? "0" : sDebt));
            }
            return bp.getDebt();
        }
        BPPlayerFiles files = new BPPlayerFiles(p);
        String sDebt = files.getPlayerConfig().getString("Debt");
        return new BigDecimal(sDebt == null ? "0" : sDebt);
    }
}