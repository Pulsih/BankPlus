package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.banks.Bank;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class BankPlusPlayer {

    private BukkitTask inventoryUpdateTask;
    private Bank openedBank;
    private HashMap<String, String> playerBankClickHolder;
    private final Player p;

    public BankPlusPlayer(Player player) {
        this.p = player;
    }

    public BukkitTask getInventoryUpdateTask() {
        return inventoryUpdateTask;
    }

    public Bank getOpenedBank() {
        return openedBank;
    }

    public HashMap<String, String> getPlayerBankClickHolder() {
        return playerBankClickHolder;
    }

    public void setInventoryUpdateTask(BukkitTask inventoryUpdateTask) {
        this.inventoryUpdateTask = inventoryUpdateTask;
    }

    public void setOpenedBank(Bank openedBank) {
        this.openedBank = openedBank;
    }

    public void setPlayerBankClickHolder(HashMap<String, String> playerBankClickHolder) {
        this.playerBankClickHolder = playerBankClickHolder;
    }

    public void updateInstance() {
        BankPlus.instance().getPlayers().put(p.getUniqueId(), this);
    }
}