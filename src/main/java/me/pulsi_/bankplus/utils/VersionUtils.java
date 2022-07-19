package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.values.Values;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class will be changed between version, it will be used to add the compatibility
 * for older versions to the newest versions. ( Ex: Automatically moving the player
 * balances from the players.yml to the per-player file )
 */
public class VersionUtils {

    public static void moveBankFileToBanksFolder() {
        File oldBankFile = new File(BankPlus.getInstance().getDataFolder(), "bank.yml");
        if (!oldBankFile.exists()) return;
        File newBankFile = new File(BankPlus.getInstance().getDataFolder(), "banks" + File.separator + Values.CONFIG.getMainGuiName() + ".yml");
        File oldBankFileRenamed = new File(BankPlus.getInstance().getDataFolder(), "old_bank.yml");
        oldBankFileRenamed.getParentFile().mkdir();
        oldBankFile.renameTo(oldBankFileRenamed);

        List<String> linesUnderItems = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(oldBankFileRenamed);
            boolean hasReachedGuiPoint = false;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("Items")) hasReachedGuiPoint = true;
                if (hasReachedGuiPoint) linesUnderItems.add(line);
            }
        } catch (FileNotFoundException e) {
            BPLogger.error(e.getMessage());
        }

        List<String> linesBeforeItems = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(newBankFile);
            boolean hasReachedGuiPoint = false;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("Items")) hasReachedGuiPoint = true;
                if (hasReachedGuiPoint) break;
                linesBeforeItems.add(line);
            }
        } catch (FileNotFoundException e) {
            BPLogger.error(e.getMessage());
        }

        List<String> totalLines = new ArrayList<>(linesBeforeItems);
        totalLines.addAll(linesUnderItems);

        StringBuilder config = new StringBuilder();
        for (String line : totalLines) {
            if (line.contains("DisplayName")) line = line.replace("DisplayName", "Displayname");
            config.append(line).append("\n");
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(newBankFile));
            writer.write(config.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            BPLogger.error(e.getMessage());
        }
    }
}