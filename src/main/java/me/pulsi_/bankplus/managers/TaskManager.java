package me.pulsi_.bankplus.managers;

import org.bukkit.scheduler.BukkitTask;

public class TaskManager {

    private BukkitTask interestTask, savingTask, broadcastTask;

    public void setInterestTask(BukkitTask interestTask) {
        this.interestTask = interestTask;
    }

    public void setSavingTask(BukkitTask savingTask) {
        this.savingTask = savingTask;
    }

    public void setBroadcastTask(BukkitTask broadcastTask) {
        this.broadcastTask = broadcastTask;
    }

    public BukkitTask getInterestTask() {
        return interestTask;
    }

    public BukkitTask getSavingTask() {
        return savingTask;
    }

    public BukkitTask getBroadcastTask() {
        return broadcastTask;
    }
}