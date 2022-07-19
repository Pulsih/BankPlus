package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.*;

public class BankTopManager {

    public static Map<BigDecimal, String> nameGetter = new HashMap<>();
    private static List<BigDecimal> bankTopBalances;
    private static List<String> bankTopNames;

    public static void updateBankTop() {
        bankTopBalances = new ArrayList<>();
        bankTopNames = new ArrayList<>();

        List<BigDecimal> balances;
        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) balances = MultiEconomyManager.getAllBankBalances();
        else balances = SingleEconomyManager.getAllBankBalances();
        if (balances.isEmpty()) return;

        Collections.sort(balances);
        Collections.reverse(balances);

        int banktopSize = Values.CONFIG.getBankTopSize();
        if (balances.size() >= banktopSize) {
            for (int i = 0; i < banktopSize; i++) bankTopBalances.add(balances.get(i));
        } else bankTopBalances.addAll(balances);

        for (BigDecimal bal : bankTopBalances) {
            String name = nameGetter.get(bal);
            bankTopNames.add(name);
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