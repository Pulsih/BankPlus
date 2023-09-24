package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.*;

public class BankTopManager {

    private final HashMap<Integer, BankTopPlayer> bankTop = new HashMap<>();

    private final BankPlus plugin;

    public BankTopManager(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void updateBankTop() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            bankTop.clear();

            HashMap<String, BigDecimal> balances = BankPlus.getBPEconomy().getAllBankBalances();

            List<BigDecimal> amounts = new ArrayList<>(balances.values());
            List<String> names = new ArrayList<>(balances.keySet());

            Collections.sort(amounts);

            for (int i = 0; i < Values.CONFIG.getBankTopSize() && i < balances.size(); i++) {
                BigDecimal amount = amounts.get(i);
                for (String name : names) {
                    if (!balances.get(name).equals(amount)) continue;
                    BankTopPlayer player = new BankTopPlayer();
                    player.setBalance(amount);
                    player.setName(name);

                    bankTop.put(i, player);
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
        if (position < 1 || position > bankTop.size()) return new BigDecimal(0);
        return bankTop.get(position - 1).getBalance();
    }

    public String getBankTopNamePlayer(int position) {
        if (position < 1 || position > bankTop.size()) return Values.CONFIG.getBanktopPlayerNotFoundPlaceholder();
        return bankTop.get(position - 1).getName();
    }

    public int getPlayerBankTopPosition(Player p) {
        BPPlayer player = plugin.getPlayerRegistry().get(p);
        if (player.getBanktopPosition() == -1) {
            for (int i = 0; i < bankTop.size(); i++) {
                if (!bankTop.get(i).getName().equals(p.getName())) continue;
                player.setBanktopPosition(i + 1);
                break;
            }
        }
        return player.getBanktopPosition();
    }

    private static class BankTopPlayer {

        private BigDecimal balance;
        private String name;

        public BigDecimal getBalance() {
            return balance;
        }

        public String getName() {
            return name;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}