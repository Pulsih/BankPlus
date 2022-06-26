package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;

public class EconomyManager {

    public static Map<UUID, BigDecimal> playerMoney = new HashMap<>();

    public static List<BigDecimal> getAllPlayerBankBalances() {
        List<BigDecimal> balances = new ArrayList<>();
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        for (String identifier : players.getConfigurationSection("Players").getKeys(false)) {
            String balance = players.getString("Players." + identifier + ".Money");
            if (balance != null) balances.add(new BigDecimal(balance));
        }
        return balances;
    }

    public static void loadBankBalance(Player p) {
        if (playerMoney.containsKey(p.getUniqueId())) return;
        FileConfiguration players = BankPlus.getCm().getConfig("players");

        String path;
        if (Values.CONFIG.isStoringUUIDs()) path = players.getString("Players." + p.getUniqueId() + ".Money");
        else path = players.getString("Players." + p.getName() + ".Money");

        BigDecimal amount;
        if (path == null) amount = new BigDecimal(0);
        else amount = new BigDecimal(path);

        playerMoney.put(p.getUniqueId(), amount);
    }

    public static void saveBankBalance(Player p) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        if (Values.CONFIG.isStoringUUIDs()) players.set("Players." + p.getUniqueId() + ".Money", format(getBankBalance(p)));
        else players.set("Players." + p.getName() + ".Money", format(getBankBalance(p)));
        BankPlus.getCm().savePlayers();
    }

    public static BigDecimal getBankBalance(Player p) {
        loadBankBalance(p);
        return playerMoney.get(p.getUniqueId());
    }

    public static BigDecimal getBankBalance(OfflinePlayer p) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");

        String path;
        if (Values.CONFIG.isStoringUUIDs()) path = players.getString("Players." + p.getUniqueId() + ".Money");
        else path = players.getString("Players." + p.getName() + ".Money");

        BigDecimal balance;
        if (path == null) balance = new BigDecimal(0);
        else balance = new BigDecimal(path);

        return balance;
    }

    public static BigDecimal getOfflineInterest(Player p) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");

        String path;
        if (Values.CONFIG.isStoringUUIDs()) path = players.getString("Players." + p.getUniqueId() + ".Offline-Interest");
        else path = players.getString("Players." + p.getName() + ".Offline-Interest");

        BigDecimal offlineInterest;
        if (path == null) offlineInterest = new BigDecimal(0);
        else offlineInterest = new BigDecimal(path);

        return offlineInterest;
    }

    public static BigDecimal getOfflineInterest(OfflinePlayer p) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");

        String path;
        if (Values.CONFIG.isStoringUUIDs()) path = players.getString("Players." + p.getUniqueId() + ".Offline-Interest");
        else path = players.getString("Players." + p.getName() + ".Offline-Interest");

        BigDecimal offlineInterest;
        if (path == null) offlineInterest = new BigDecimal(0);
        else offlineInterest = new BigDecimal(path);

        return offlineInterest;
    }

    public static void setPlayerBankBalance(Player p, BigDecimal amount) {
        setValue(p, amount);
    }

    public static void setPlayerBankBalance(OfflinePlayer p, BigDecimal amount) {
        setValue(p, amount);
    }

    public static void addPlayerBankBalance(Player p, BigDecimal amount) {
        BigDecimal targetBank = getBankBalance(p);
        setValue(p, targetBank.add(amount));
    }

    public static void addPlayerBankBalance(OfflinePlayer p, BigDecimal amount) {
        BigDecimal targetBank = getBankBalance(p);
        setValue(p, targetBank.add(amount));
    }

    public static void removePlayerBankBalance(Player p, BigDecimal amount) {
        BigDecimal targetBank = getBankBalance(p);
        setValue(p, targetBank.subtract(amount));
    }

    public static void removePlayerBankBalance(OfflinePlayer p, BigDecimal amount) {
        BigDecimal targetBank = getBankBalance(p);
        setValue(p, targetBank.subtract(amount));
    }

    public static void setOfflineInterest(Player p, BigDecimal amount) {
        if (Values.CONFIG.isStoringUUIDs()) BankPlus.getCm().getConfig("players").set("Players." + p.getUniqueId() + ".Offline-Interest", format(amount));
        else BankPlus.getCm().getConfig("players").set("Players." + p.getName() + ".Offline-Interest", format(amount));
        BankPlus.getCm().savePlayers();
    }

    public static void setOfflineInterest(OfflinePlayer p, BigDecimal amount) {
        if (Values.CONFIG.isStoringUUIDs()) BankPlus.getCm().getConfig("players").set("Players." + p.getUniqueId() + ".Offline-Interest", format(amount));
        else BankPlus.getCm().getConfig("players").set("Players." + p.getName() + ".Offline-Interest", format(amount));
        BankPlus.getCm().savePlayers();
    }

    private static void setValue(Player p, BigDecimal amount) {
        playerMoney.put(p.getUniqueId(), amount);
    }

    private static void setValue(OfflinePlayer p, BigDecimal amount) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        if (Values.CONFIG.isStoringUUIDs()) players.set("Players." + p.getUniqueId() + ".Money", format(amount));
        else players.set("Players." + p.getName() + ".Money", format(amount));
        BankPlus.getCm().savePlayers();
    }

    private static String format(BigDecimal bal) {
        String balance = bal.toString();
        if (balance.contains(".")) {
            String decimals = balance.split("\\.")[1];
            if (decimals.length() > Values.CONFIG.getMaxDecimalsAmount()) {
                String correctedDecimals = decimals.substring(0, Values.CONFIG.getMaxDecimalsAmount());
                balance = balance.split("\\.")[0] + "." + correctedDecimals;
            }
        }
        return balance;
    }
}