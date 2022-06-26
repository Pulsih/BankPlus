package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.ChatUtils;
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
        List<BigDecimal> balances = EconomyManager.getAllPlayerBankBalances();
        if (balances.isEmpty()) return;

        Collections.sort(balances);
        Collections.reverse(balances);
        if (balances.size() >= Values.CONFIG.getBankTopSize()) {
            for (int i = 0; i < Values.CONFIG.getBankTopSize(); i++) bankTopBalances.add(balances.get(i));
        } else bankTopBalances.addAll(balances);

        bankTopNames = new ArrayList<>();
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        for (BigDecimal bankTopBalance : bankTopBalances) {
            String topMoney = bankTopBalance.toString();

            for (String identifier : players.getConfigurationSection("Players").getKeys(false)) {
                String money = players.getString("Players." + identifier + ".Money");

                if (topMoney.equals(money)) {
                    String name = players.getString("Players." + identifier + ".Account-Name");
                    if (!bankTopNames.contains(name)) {
                        bankTopNames.add(name);
                        break;
                    }
                }
            }
        }

        if (!Values.CONFIG.isBanktopUpdateBroadcastEnabled()) return;
        String message = MessageManager.addPrefix(Values.CONFIG.getBanktopUpdateBroadcastMessage());
        if (Values.CONFIG.isBanktopUpdateBroadcastOnlyConsole()) ChatUtils.log(message);
        else Bukkit.broadcastMessage(message);
    }

    public static void startUpdateTask() {
        BukkitTask task = TaskManager.getBroadcastTask();
        if (task != null) task.cancel();
        TaskManager.setBroadcastTask(Bukkit.getScheduler().runTaskTimer(BankPlus.getInstance(), BankTopManager::updateBankTop, Values.CONFIG.getUpdateBankTopDelay(), Values.CONFIG.getUpdateBankTopDelay()));
    }

    public static BigDecimal getBankTopBalancePlayer(int position) {
        if (position > bankTopBalances.size()) return new BigDecimal(0);
        return bankTopBalances.get(position - 1);
    }

    public static String getBankTopNamePlayer(int position) {
        if (position > bankTopBalances.size()) return "Not found yet.";
        return bankTopNames.get(position - 1);
    }
}