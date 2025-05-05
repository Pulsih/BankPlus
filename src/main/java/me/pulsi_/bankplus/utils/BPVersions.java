package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.managers.BPConfigs;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.math.BigDecimal;
import java.util.Scanner;

/**
 * This class will be changed between versions, it will be used to add the compatibility for older versions to the newest versions.
 */
public class BPVersions {

    public static void renameInterestMoneyGiveToRate() {
        File configFile = BankPlus.INSTANCE().getConfigs().getFile("config.yml");
        if (configFile == null || !configFile.exists()) return;

        Scanner scanner;
        try {
            scanner = new Scanner(configFile, "UTF-8");
        } catch (FileNotFoundException e) {
            BPLogger.Console.warn(e, "Could not convert \"" + configFile + "\" interest money-given and offline-money-given paths!");
            return;
        }

        boolean contains = false;
        StringBuilder builder = new StringBuilder();
        while (scanner.hasNext()) {
            String nextLine = scanner.nextLine();
            if (nextLine.contains("Money-Given:") || nextLine.contains("Offline-Money-Given:")) contains = true;
            builder.append(nextLine.replace("Money-Given:", "Rate:").replace("Offline-Money-Given:", "Offline-Rate:")).append("\n");
        }

        if (!contains) return;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            BPLogger.Console.error(e, e.getMessage());
        }
    }

    public static void convertPlayerFilesToNewStyle() {
        if (BPConfigs.isUpdated()) return;

        File dataFolder = new File(BankPlus.INSTANCE().getDataFolder(), "playerdata");
        File[] files = dataFolder.listFiles();
        if (files == null || files.length == 0) return;

        int converted = 0;
        for (File file : files) {
            FileConfiguration oldConfig = new YamlConfiguration(), newConfig = new YamlConfiguration();

            try {
                oldConfig.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                BPLogger.Console.error(e, "An error has occurred while loading a user file (File name: " + file.getName() + "):");
                continue;
            }

            String money = oldConfig.getString("Money"), name = oldConfig.getString("Account-Name"), interest = oldConfig.getString("Offline-Interest"), debt = oldConfig.getString("Debt");
            if (money == null && name == null && interest == null && debt == null) continue;

            newConfig.set("name", name);

            for (String bankName : BPEconomy.nameList()) {
                if (ConfigValues.getMainGuiName().equals(bankName)) {
                    BigDecimal oldMoney;
                    if (money == null) oldMoney = ConfigValues.getStartAmount();
                    else oldMoney = BPFormatter.getStyledBigDecimal(money);
                    newConfig.set("banks." + bankName + ".money", BPFormatter.styleBigDecimal(oldMoney));

                    newConfig.set("banks." + bankName + ".debt", BPFormatter.styleBigDecimal(BPFormatter.getStyledBigDecimal(debt)));
                    newConfig.set("banks." + bankName + ".interest", BPFormatter.styleBigDecimal(BPFormatter.getStyledBigDecimal(interest)));
                } else {
                    BigDecimal oldMoney = BPFormatter.getStyledBigDecimal(oldConfig.getString("Banks." + bankName + ".Money"));
                    newConfig.set("banks." + bankName + ".money", BPFormatter.styleBigDecimal(oldMoney));

                    newConfig.set("banks." + bankName + ".debt", "0");
                    newConfig.set("banks." + bankName + ".interest", "0");
                }

                String levelString = oldConfig.getString("Banks." + bankName + ".Level");
                newConfig.set("banks." + bankName + ".level", levelString == null ? "1" : levelString);
            }

            try {
                newConfig.save(file);
                converted++;
            } catch (IOException e) {
                BPLogger.Console.error(e, "Could not apply changes while converting the player file \"" + file.getName() + "\".");
            }
        }
        if (converted > 0) BPLogger.Console.info("Successfully converted " + converted + " player files to the new format!");
    }

    public static void changeBankUpgradesSection() {
        File[] files = new File(BankPlus.INSTANCE().getDataFolder(), "banks").listFiles();
        if (files == null) return;

        for (File bankFile : files) {
            Scanner scanner;
            try {
                scanner = new Scanner(bankFile, "UTF-8");
            } catch (FileNotFoundException e) {
                BPLogger.Console.warn(e, "Could not find \"" + bankFile + "\" file!");
                continue;
            }

            boolean convertFile = false;
            StringBuilder builder = new StringBuilder();
            while (scanner.hasNext()) {
                String nextLine = scanner.nextLine();
                if (nextLine.contains("Upgrades:")) convertFile = true;

                builder.append(nextLine.replace("Upgrades:", "Levels:")).append("\n");
            }
            if (!convertFile) continue;

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(bankFile));
                writer.write(builder.toString());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                BPLogger.Console.error(e, e.getMessage());
            }
        }
    }
}