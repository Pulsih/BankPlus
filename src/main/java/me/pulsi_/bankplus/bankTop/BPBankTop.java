package me.pulsi_.bankplus.bankTop;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.managers.BPTaskManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BPBankTop {

    private static final List<BankTopPlayer> bankTop = new ArrayList<>();

    /**
     * Update the bank top.
     */
    public static void updateBankTop() {
        Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
            bankTop.clear();

            HashMap<String, BigDecimal> balances = BPEconomy.getAllEconomiesBankBalances();
            List<BigDecimal> amounts = new ArrayList<>(balances.values());
            List<String> names = new ArrayList<>(balances.keySet());
            Collections.sort(amounts);

            // i from the end because the balances have been sorted in ascending way, count to not overcome the banktop limit.
            for (int i = balances.size() - 1, count = 0; i >= 0 && count < ConfigValues.getBankTopSize(); i--) {
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

            if (!ConfigValues.isBankTopUpdateBroadcastEnabled()) return;

            String message = ConfigValues.getBankTopUpdateBroadcastMessage();
            if (!ConfigValues.isBankTopUpdateBroadcastSilentConsole()) BPLogger.Console.log(BPMessages.applyMessagesPrefix(message));
            for (Player p : Bukkit.getOnlinePlayers()) BPMessages.sendMessage(p, message);
        });
    }

    /**
     * Restart the banktop update count down without updating it.
     */
    public static void restartBankTopUpdateTask() {
        long delay = ConfigValues.getUpdateBankTopDelay();
        BPTaskManager.setTask(BPTaskManager.BANKTOP_BROADCAST_TASK, Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE(), BPBankTop::updateBankTop, delay, delay));
    }

    /**
     * Get the balance of the player that is in that banktop position.
     * @param position A number between 1 and BankTop size.
     * @return The player balance or 0 if not found.
     */
    public static BigDecimal getBankTopBalancePlayer(int position) {
        if (position <= 0 || position > bankTop.size()) return BigDecimal.ZERO;
        BankTopPlayer p = bankTop.get(position - 1);
        return ((p == null || p.getBalance() == null) ? BigDecimal.ZERO : p.getBalance());
    }

    /**
     * Get the name of the player that is in that banktop position.
     * @param position A number between 1 and the BankTop size.
     * @return A player name or "playerNotFound" placeholder if none found.
     */
    public static String getBankTopNamePlayer(int position) {
        if (position <= 0 || position > bankTop.size()) return ConfigValues.getBankTopPlayerNotFoundPlaceholder();
        BankTopPlayer p = bankTop.get(position - 1);
        return ((p == null || p.getName() == null) ? ConfigValues.getBankTopPlayerNotFoundPlaceholder() : p.getName());
    }

    /**
     * Get the player current banktop position.
     * @param p The player.
     * @return The player current banktop position or -1 if not classified.
     */
    public static int getPlayerBankTopPosition(OfflinePlayer p) {
        return getPlayerBankTopPosition(p.getName());
    }

    /**
     * Get the player current banktop position.
     * @param name The player name.
     * @return The player current banktop position or -1 if not classified.
     */
    public static int getPlayerBankTopPosition(String name) {
        int position = -1;
        for (int i = 0; i <= bankTop.size(); i++) {
            BankTopPlayer p = bankTop.get(i);
            if (p != null && p.getName() != null && p.getName().equals(name)) return i + 1;
        }
        return position;
    }
}