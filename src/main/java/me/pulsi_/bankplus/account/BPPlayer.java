package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.bankSystem.BankGui;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class BPPlayer {

    private final Player player;

    private BankGui openedBankGui;
    private BukkitTask bankUpdatingTask, closingTask;

    // Values to check if the player is doing a deposit or withdraw through chat.
    private boolean depositing, withdrawing;

    public BPPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public BankGui getOpenedBankGui() {
        return openedBankGui;
    }

    public BukkitTask getBankUpdatingTask() {
        return bankUpdatingTask;
    }

    public BukkitTask getClosingTask() {
        return closingTask;
    }

    public boolean isDepositing() {
        return depositing;
    }

    public boolean isWithdrawing() {
        return withdrawing;
    }

    public void setOpenedBankGui(BankGui openedBankGui) {
        this.openedBankGui = openedBankGui;
    }

    public void setBankUpdatingTask(BukkitTask bankUpdatingTask) {
        this.bankUpdatingTask = bankUpdatingTask;
    }

    public void setClosingTask(BukkitTask closingTask) {
        this.closingTask = closingTask;
    }

    public void setDepositing(boolean depositing) {
        this.depositing = depositing;
    }

    public void setWithdrawing(boolean withdrawing) {
        this.withdrawing = withdrawing;
    }
}