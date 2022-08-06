package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BankTopManager {

    public static List<HashMap<BigDecimal, String>> linkedBalanceName = new ArrayList<>();
    private static final HashMap<Player, Integer> playerBankTopPosition = new HashMap<>();
    private static final List<BigDecimal> bankTopBalances = new ArrayList<>();
    private static final List<String> bankTopNames = new ArrayList<>();

    public static void updateBankTop() {
        playerBankTopPosition.clear();
        bankTopBalances.clear();
        bankTopNames.clear();

        List<BigDecimal> balances;
        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) balances = MultiEconomyManager.getAllBankBalances();
        else balances = SingleEconomyManager.getAllBankBalances();
        if (balances.isEmpty()) return;

        Collections.sort(balances);
        Collections.reverse(balances);
        bankTopBalances.addAll(balances);

        List<HashMap<BigDecimal, String>> copyOfLinkedBalanceName = new ArrayList<>(linkedBalanceName);
        for (BigDecimal bal : balances) {
            for (HashMap<BigDecimal, String> linkedMap : copyOfLinkedBalanceName) {
                if (linkedMap.containsKey(bal)) {
                    bankTopNames.add(linkedMap.get(bal));
                    copyOfLinkedBalanceName.remove(linkedMap);
                    break;
                }
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

    public static int getPlayerBankTopPosition(Player p) {
        if (!playerBankTopPosition.containsKey(p)) {
            for (int i = 0; i < bankTopNames.size(); i++) {
                if (!bankTopNames.get(i).equals(p.getName())) continue;
                playerBankTopPosition.put(p, i + 1);
                break;
            }
        }
        return playerBankTopPosition.get(p);
    }
}