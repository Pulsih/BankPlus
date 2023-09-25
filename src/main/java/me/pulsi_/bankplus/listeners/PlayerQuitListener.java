package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.BPPlayerFiles;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.debt.DebtUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPFormatter;
import me.pulsi_.bankplus.utils.BPSets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        savePlayer(p);
        BPPlayer player = BankPlus.INSTANCE.getPlayerRegistry().remove(p);

        Bank openedBank = player.getOpenedBank();
        if (openedBank != null) {
            BukkitTask task = openedBank.getInventoryUpdateTask();
            if (task != null) task.cancel();
        }

        BPSets.removePlayerFromDepositing(p);
        BPSets.removePlayerFromWithdrawing(p);
    }

    private void savePlayer(Player p) {
        BPEconomy economy = BankPlus.getBPEconomy();
        BPPlayerFiles files = new BPPlayerFiles(p);

        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig(file);

        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
            config.set("banks." + bankName + ".money", BPFormatter.formatBigDouble(economy.getBankBalance(p, bankName)));
        config.set("debt", BPFormatter.formatBigDouble(DebtUtils.getDebt(p)));
        files.savePlayerFile(config, file, true);

        economy.unloadBankBalance(p);
    }
}