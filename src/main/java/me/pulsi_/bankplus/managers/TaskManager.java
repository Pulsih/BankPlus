package me.pulsi_.bankplus.managers;

import org.bukkit.scheduler.BukkitTask;

public class TaskManager {

    private static BukkitTask interestTask, savingTask, broadcastTask;

    public static void setInterestTask(BukkitTask interestTask) {
        TaskManager.interestTask = interestTask;
    }

    public static void setSavingTask(BukkitTask savingTask) {
        TaskManager.savingTask = savingTask;
    }

    public static void setBroadcastTask(BukkitTask broadcastTask) {
        TaskManager.broadcastTask = broadcastTask;
    }

    public static BukkitTask getInterestTask() {
        return interestTask;
    }

    public static BukkitTask getSavingTask() {
        return savingTask;
    }

    public static BukkitTask getBroadcastTask() {
        return broadcastTask;
    }
}