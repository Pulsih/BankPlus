package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountManager {

    private static final int maxValidationPerTask = 640;

    public static void validateAllAccounts() {
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        ConfigurationSection playerList = players.getConfigurationSection("Players");
        if (playerList == null) return;

        List<String> identifiers = new ArrayList<>(playerList.getKeys(false));
        long time = System.currentTimeMillis();
        int size = identifiers.size();

        if (size >= maxValidationPerTask) {
            int tasks = size / maxValidationPerTask;
            if (size % maxValidationPerTask != 0) tasks++;
            BPLogger.info("Started validation task! &8(&9Tasks amount: &f" + tasks + "&9, Accounts to validate: &f" + size + "&8)");
            validateAccountInTasks(identifiers, 0, time);
        } else {
            for (String identifier : identifiers) validateAccount(identifier);
            BankPlus.getCm().savePlayers();
            BPLogger.info("Successfully validated " + size + " player accounts! &8(&9Took " + (System.currentTimeMillis() - time) + "ms&8)");
        }
    }

    public static void validateAccount(String identifier) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");

        OfflinePlayer p;
        try {
            UUID uuid = UUID.fromString(identifier);
            p = Bukkit.getOfflinePlayer(uuid);
        } catch (IllegalArgumentException e) {
            p = Bukkit.getOfflinePlayer(identifier);
        }

        String sBalance = players.getString("Players." + identifier + ".Money");
        String sOfflineInterest = players.getString("Players." + identifier + ".Offline-Interest");
        String sName = players.getString("Players." + identifier + ".Account-Name");

        if (sBalance == null) players.set("Players." + p.getUniqueId() + ".Money", Methods.formatBigDouble(Values.CONFIG.getStartAmount()));
        else {
            BigDecimal bal;
            try {
                bal = new BigDecimal(sBalance);
            } catch (NumberFormatException e) {
                bal = new BigDecimal(0);
            }
            if (bal.doubleValue() < 0) bal = new BigDecimal(0);
            players.set("Players." + p.getUniqueId() + ".Money", Methods.formatBigDouble(bal));
        }

        if (Values.CONFIG.isNotifyOfflineInterest()) {
            if (sOfflineInterest == null) players.set("Players." + p.getUniqueId() + ".Offline-Interest", Methods.formatBigDouble(new BigDecimal(0)));
            else {
                BigDecimal bal;
                try {
                    bal = new BigDecimal(sOfflineInterest);
                } catch (NumberFormatException e) {
                    bal = new BigDecimal(0);
                }
                if (bal.doubleValue() < 0) bal = new BigDecimal(0);
                players.set("Players." + p.getUniqueId() + ".Offline-Interest", Methods.formatBigDouble(bal));
            }
        }

        if (sName == null) players.set("Players." + identifier + ".Account-Name", p.getName());
    }

    public static void validateAccountInTasks(List<String> identifiers, int point, long time) {
        List<String> listOfIdentifiers = new ArrayList<>(identifiers);
        for (int i = 0; i < maxValidationPerTask; i++) {
            if (point >= listOfIdentifiers.size()) {
                BankPlus.getCm().savePlayers();
                BPLogger.info("Successfully validated " + listOfIdentifiers.size() + " player accounts! &8(&9Took " + (System.currentTimeMillis() - time) + "ms&8)");
                return;
            }
            validateAccount(listOfIdentifiers.get(point));
            point++;
        }

        int finalPoint = point;
        BPLogger.info("Validated " + finalPoint + " player accounts!");
        Bukkit.getScheduler().runTaskLater(BankPlus.getInstance(), () -> validateAccountInTasks(listOfIdentifiers, finalPoint, time), 10L);
    }
}