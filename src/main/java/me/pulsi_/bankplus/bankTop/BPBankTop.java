package me.pulsi_.bankplus.bankTop;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.managers.TaskManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BPBankTop {

    private final HashMap<Integer, BankTopPlayer> bankTop = new HashMap<>();

    private final BankPlus plugin;

    public BPBankTop(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void updateBankTop() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            bankTop.clear();

            HashMap<String, BigDecimal> balances = BPEconomy.getAllEconomiesBankBalances();
            List<BigDecimal> amounts = new ArrayList<>(balances.values());
            List<String> names = new ArrayList<>(balances.keySet());

            Collections.sort(amounts);

            for (int i = balances.size() - 1, a = 0; i >= 0 && a < Values.CONFIG.getBankTopSize(); i--, a++) {
                BigDecimal amount = amounts.get(i);

                for (String name : names) {
                    if (!balances.get(name).equals(amount)) continue;

                    BankTopPlayer player = new BankTopPlayer();
                    player.setBalance(amount);
                    player.setName(name);
                    bankTop.put(a + 1, player);

                    amounts.remove(amount);
                    names.remove(name);
                    break;
                }
            }

            if (!Values.CONFIG.isBanktopUpdateBroadcastEnabled()) return;

            String message = Values.CONFIG.getBanktopUpdateBroadcastMessage();
            if (!Values.CONFIG.isBanktopUpdateBroadcastSilentConsole()) BPLogger.log(message);
            for (Player p : Bukkit.getOnlinePlayers()) BPMessages.send(p, message, true);
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
        BankTopPlayer p = bankTop.get(Math.min(bankTop.size(), Math.max(1, position)));
        return (p == null || p.getBalance() == null ? new BigDecimal(0) : p.getBalance());
    }

    public String getBankTopNamePlayer(int position) {
        BankTopPlayer p = bankTop.get(Math.min(bankTop.size(), Math.max(1, position)));
        return (p == null || p.getName() == null ? Values.CONFIG.getBanktopPlayerNotFoundPlaceholder() : p.getName());
    }

    public int getPlayerBankTopPosition(OfflinePlayer p) {
        BPPlayer player = PlayerRegistry.get(p);
        if (player != null) {
            if (player.getBanktopPosition() == -1)
                player.setBanktopPosition(getPlayerBankTopPosition(p.getName()));
            return player.getBanktopPosition();
        }
        return getPlayerBankTopPosition(p.getName());
    }

    public int getPlayerBankTopPosition(String name) {
        int position = -1;
        for (int i = 1; i <= bankTop.size(); i++) {
            BankTopPlayer p = bankTop.get(i);
            if (p != null && p.getName() != null && p.getName().equals(name)) return i;
        }
        return position;
    }
}