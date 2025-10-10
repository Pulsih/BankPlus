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

    /**
     * A hashmap containing as Key the bank top position (always starting from 1 and increasing) and Value the BankTopPlayer object.
     */
    private static final HashMap<Integer, BankTopPlayer> bankTop = new HashMap<>();

    /**
     * Update the bank top.
     */
    public static void updateBankTop() {
        Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
            HashMap<String, BigDecimal> balances = BPEconomy.getAllEconomiesBankBalances();
            List<BankTopPlayer> players = new ArrayList<>();

            for (String name : balances.keySet()) {
                BigDecimal balance = balances.get(name);
                if (balance.compareTo(BigDecimal.ZERO) <= 0) continue;

                BankTopPlayer bankTopPlayer = new BankTopPlayer();
                bankTopPlayer.setName(name);
                bankTopPlayer.setBalance(balance);

                players.add(bankTopPlayer);
            }

            bankTop.clear();
            for (int i = 1; i <= ConfigValues.getBankTopSize(); i++) {
                BankTopPlayer highestPlayerBal = players.getFirst();

                for (BankTopPlayer player : players)
                    if (player.getBalance().compareTo(highestPlayerBal.getBalance()) > 0)
                        highestPlayerBal = player;

                players.remove(highestPlayerBal);
                bankTop.put(i, highestPlayerBal);
            }

            if (!ConfigValues.isBankTopUpdateBroadcastEnabled()) return;

            String message = ConfigValues.getBankTopUpdateBroadcastMessage();
            if (!ConfigValues.isBankTopUpdateBroadcastSilentConsole())
                BPLogger.Console.log(BPMessages.applyMessagesPrefix(message));
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
     *
     * @param position A number between 1 and BankTop size.
     * @return The player balance or 0 if not found.
     */
    public static BigDecimal getBankTopBalancePlayer(int position) {
        BankTopPlayer p = bankTop.get(position);
        return ((p == null || p.getBalance() == null) ? BigDecimal.ZERO : p.getBalance());
    }

    /**
     * Get the name of the player that is in that banktop position.
     *
     * @param position A number between 1 and the BankTop size.
     * @return A player name or "playerNotFound" placeholder if none found.
     */
    public static String getBankTopNamePlayer(int position) {
        BankTopPlayer p = bankTop.get(position);
        return ((p == null || p.getName() == null) ? ConfigValues.getBankTopPlayerNotFoundPlaceholder() : p.getName());
    }

    /**
     * Get the player current banktop position.
     *
     * @param p The player.
     * @return The player current banktop position or -1 if not classified.
     */
    public static int getPlayerBankTopPosition(OfflinePlayer p) {
        return getPlayerBankTopPosition(p.getName());
    }

    /**
     * Get the player current banktop position.
     *
     * @param name The player name.
     * @return The player current banktop position or -1 if not classified.
     */
    public static int getPlayerBankTopPosition(String name) {
        for (int i = 1; i <= bankTop.size(); i++) {
            BankTopPlayer p = bankTop.get(i);
            if (p != null && p.getName() != null && p.getName().equals(name)) return i;
        }
        return -1;
    }
}