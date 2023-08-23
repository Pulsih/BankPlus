package me.pulsi_.bankplus.logSystem;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPLogger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class BPLogUtils {

    private File logFile = null;
    private String day = null;

    public void setupLoggerFile() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        day = format.format(System.currentTimeMillis());

        logFile = new File(BankPlus.INSTANCE.getDataFolder() + File.separator + "logs", day + ".txt");

        if (logFile.exists()) return;

        logFile.getParentFile().mkdirs();
        try {
            logFile.createNewFile();
        } catch (IOException e) {
            BPLogger.error("Could not create log file: " + e.getMessage());
        }
    }

    public boolean checkDayChanged() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String today = format.format(System.currentTimeMillis());

        if (today.equals(day)) return false;

        day = today;
        return true;
    }

    public File getLogFile() {
        return logFile;
    }
}