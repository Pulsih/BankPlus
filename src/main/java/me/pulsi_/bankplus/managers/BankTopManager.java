package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BankTopManager {

    private static List<BigDecimal> bankTopBalances;
    private static List<String> bankTopNames;

    public static void updateBankTop() {
        bankTopBalances = new ArrayList<>();
        bankTopNames = new ArrayList<>();
        List<BigDecimal> balances = EconomyManager.getAllPlayerBankBalances();
        if (balances.isEmpty()) return;

        Collections.sort(balances);
        Collections.reverse(balances);

        int banktopSize = Values.CONFIG.getBankTopSize();
        if (balances.size() >= banktopSize) {
            for (int i = 0; i < banktopSize; i++) bankTopBalances.add(balances.get(i));
        } else bankTopBalances.addAll(balances);

        FileConfiguration players = BankPlus.getCm().getConfig("players");
        for (BigDecimal bankTopBalance : bankTopBalances) {
            String topMoney = bankTopBalance.toString();

            for (String identifier : players.getConfigurationSection("Players").getKeys(false)) {
                String money = players.getString("Players." + identifier + ".Money");

                if (!topMoney.equals(money)) continue;
                String name = players.getString("Players." + identifier + ".Account-Name");
                if (name == null || bankTopNames.contains(name)) continue;
                bankTopNames.add(name);
                break;
            }
        }

        if (!Values.CONFIG.isBanktopUpdateBroadcastEnabled()) return;
        String message = MessageManager.addPrefix(Values.CONFIG.getBanktopUpdateBroadcastMessage());
        if (Values.CONFIG.isBanktopUpdateBroadcastOnlyConsole()) BPLogger.log(message);
        else Bukkit.broadcastMessage(message);
    }

    public static void startUpdateTask() {
        BukkitTask task = TaskManager.getBroadcastTask();
        if (task != null) task.cancel();
        TaskManager.setBroadcastTask(Bukkit.getScheduler().runTaskTimer(BankPlus.getInstance(), BankTopManager::updateBankTop, Values.CONFIG.getUpdateBankTopDelay(), Values.CONFIG.getUpdateBankTopDelay()));
    }

    public static BigDecimal getBankTopBalancePlayer(int position) {
        if (position <= 0) return new BigDecimal(0);
        if (position - 1 >= bankTopBalances.size()) return new BigDecimal(0);
        try {
            return bankTopBalances.get(position - 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            return new BigDecimal(0);
        }
    }

    public static String getBankTopNamePlayer(int position) {
        if (position <= 0) return "Invalid number.";
        if (position - 1 >= bankTopNames.size()) return "Not found yet.";
        String name = bankTopNames.get(position - 1);
        if (name == null) return "Not found yet.";
        return name;
    }
}