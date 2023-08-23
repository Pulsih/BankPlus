package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.economy.MultiEconomyManager;
import me.pulsi_.bankplus.economy.SingleEconomyManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
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

    private final List<HashMap<BigDecimal, String>> linkedBalanceName = new ArrayList<>();
    private final List<BigDecimal> bankTopBalances = new ArrayList<>();
    private final List<String> bankTopNames = new ArrayList<>();

    private final BankPlus plugin;

    public BankTopManager(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void updateBankTop() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            linkedBalanceName.clear();
            bankTopBalances.clear();
            bankTopNames.clear();

            List<BigDecimal> balances;
            if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
                balances = MultiEconomyManager.getAllBankBalances();
            else balances = SingleEconomyManager.getAllBankBalances();
            if (balances.isEmpty()) return;

            Collections.sort(balances);
            Collections.reverse(balances);
            bankTopBalances.addAll(balances);

            List<HashMap<BigDecimal, String>> copyOfLinkedBalanceName = new ArrayList<>(linkedBalanceName);
            for (BigDecimal bal : balances) {
                for (HashMap<BigDecimal, String> linkedMap : copyOfLinkedBalanceName) {
                    if (!linkedMap.containsKey(bal)) continue;
                    bankTopNames.add(linkedMap.get(bal));
                    copyOfLinkedBalanceName.remove(linkedMap);
                    break;
                }
            }

            if (!Values.CONFIG.isBanktopUpdateBroadcastEnabled()) return;
            String message = BPMessages.addPrefix(Values.CONFIG.getBanktopUpdateBroadcastMessage());
            if (Values.CONFIG.isBanktopUpdateBroadcastOnlyConsole()) BPLogger.log(message);
            else Bukkit.broadcastMessage(message);
        });
    }

    public void startUpdateTask() {
        TaskManager taskManager = plugin.getTaskManager();
        BukkitTask task = taskManager.getBroadcastTask();
        if (task != null) task.cancel();

        long delay = Values.CONFIG.getUpdateBankTopDelay();
        taskManager.setBroadcastTask(Bukkit.getScheduler().runTaskTimer(plugin, this::updateBankTop, delay, delay));
    }

    public BigDecimal getBankTopBalancePlayer(int position) {
        if (position < 1 || position > bankTopBalances.size()) return new BigDecimal(0);
        return bankTopBalances.get(position - 1);
    }

    public String getBankTopNamePlayer(int position) {
        if (position < 1) return "Invalid number.";
        if (position > bankTopNames.size()) return Values.CONFIG.getBanktopPlayerNotFoundPlaceholder();

        String name = bankTopNames.get(position - 1);
        return name == null ? Values.CONFIG.getBanktopPlayerNotFoundPlaceholder() : name;
    }

    public int getPlayerBankTopPosition(Player p) {
        BPPlayer player = plugin.getPlayerRegistry().get(p);
        if (player.getBanktopPosition() == -1) {
            for (int i = 0; i < bankTopNames.size(); i++) {
                if (!bankTopNames.get(i).equals(p.getName())) continue;
                player.setBanktopPosition(i + 1);
                break;
            }
        }
        return player.getBanktopPosition();
    }

    public List<HashMap<BigDecimal, String>> getLinkedBalanceName() {
        return linkedBalanceName;
    }
}