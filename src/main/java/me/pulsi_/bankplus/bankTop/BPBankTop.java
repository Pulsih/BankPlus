package me.pulsi_.bankplus.bankTop;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.managers.BPTaskManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;

public class BPBankTop {

    private static final List<BankTopPlayer> bankTop = new ArrayList<>();

    public static void updateBankTop() {
        Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
            bankTop.clear();

            HashMap<String, BigDecimal> balances = BPEconomy.getAllEconomiesBankBalances();
            List<BigDecimal> amounts = new ArrayList<>(balances.values());
            List<String> names = new ArrayList<>(balances.keySet());

            Collections.sort(amounts);

            // i from the end because the balances have been sorted in ascending way, count to not overcome the banktop limit.
            for (int i = balances.size() - 1, count = 0; i >= 0 && count < Values.CONFIG.getBankTopSize(); i--) {
                BigDecimal amount = amounts.remove(i);

                for (String name : names) {
                    if (!balances.get(name).equals(amount)) continue;

                    BankTopPlayer player = new BankTopPlayer();
                    player.setBalance(amount);
                    player.setName(name);
                    bankTop.add(player);

                    names.remove(name);
                    count++;
                    break;
                }
            }

            if (!Values.CONFIG.isBanktopUpdateBroadcastEnabled()) return;

            String message = Values.CONFIG.getBanktopUpdateBroadcastMessage();
            if (!Values.CONFIG.isBanktopUpdateBroadcastSilentConsole()) BPLogger.log(message);
            for (Player p : Bukkit.getOnlinePlayers()) BPMessages.send(p, message, true);
        });
    }

    public static void restartUpdateTask() {
        long delay = Values.CONFIG.getUpdateBankTopDelay();
        BPTaskManager.setTask(BPTaskManager.BANKTOP_BROADCAST_TASK, Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE(), BPBankTop::updateBankTop, delay, delay));
    }

    public static BigDecimal getBankTopBalancePlayer(int position) {
        BankTopPlayer p = bankTop.get(position - 1);
        return ((p == null || p.getBalance() == null) ? BigDecimal.ZERO : p.getBalance());
    }

    public static String getBankTopNamePlayer(int position) {
        BankTopPlayer p = bankTop.get(position - 1);
        return ((p == null || p.getName() == null) ? Values.CONFIG.getBanktopPlayerNotFoundPlaceholder() : p.getName());
    }

    public static int getPlayerBankTopPosition(OfflinePlayer p) {
        return getPlayerBankTopPosition(p.getName());
    }

    public static int getPlayerBankTopPosition(String name) {
        int position = -1;
        for (int i = 0; i <= bankTop.size(); i++) {
            BankTopPlayer p = bankTop.get(i);
            if (p != null && p.getName() != null && p.getName().equals(name)) return i + 1;
        }
        return position;
    }
}