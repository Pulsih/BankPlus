package me.pulsi_.bankplus.debug;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankTop.BankTopPlayer;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Debug {

    public static void debugBankTop(Object debugReceiver) {
        BPMessages.sendMessage(debugReceiver, "Simulating a banktop update:");
        BPMessages.sendMessage(debugReceiver, " ");

        Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
            HashMap<String, BigDecimal> balances = BPEconomy.getAllEconomiesBankBalances();
            BPMessages.sendMessage(debugReceiver, "|  All player balances: " + balances.size());

            List<BankTopPlayer> players = new ArrayList<>();

            BPMessages.sendMessage(debugReceiver, "|  Players with 0 balance skipped.");
            for (String name : balances.keySet()) {
                BigDecimal balance = balances.get(name);
                if (balance.compareTo(BigDecimal.ZERO) <= 0) continue;

                BankTopPlayer bankTopPlayer = new BankTopPlayer();
                bankTopPlayer.setName(name);
                bankTopPlayer.setBalance(balance);

                players.add(bankTopPlayer);

                BPMessages.sendMessage(debugReceiver, "|    Player \"" + name + "\" loaded with \"" + balance + "\" total balance.");
            }

            BPMessages.sendMessage(debugReceiver, "|  BankTopPlayer instances loaded: " + players.size());
            BPMessages.sendMessage(debugReceiver, " ");
            BPMessages.sendMessage(debugReceiver, "|  Sorting the first " + ConfigValues.getBankTopSize() + " players with the highest balance.");
            for (int i = 1; i <= ConfigValues.getBankTopSize(); i++) {
                BankTopPlayer highestPlayerBal = players.getFirst();

                for (BankTopPlayer player : players)
                    if (player.getBalance().compareTo(highestPlayerBal.getBalance()) > 0)
                        highestPlayerBal = player;

                players.remove(highestPlayerBal);
                BPMessages.sendMessage(debugReceiver, "|    BankTop Position #" + i + ": " + highestPlayerBal.getName() + " with " + highestPlayerBal.getBalance() + " balance.");
            }

            BPMessages.sendMessage(debugReceiver, " ");
            BPMessages.sendMessage(debugReceiver, "BankTop simulation ended.");
        });
    }
}
