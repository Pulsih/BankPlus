package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    public static Map<UUID, Long> playerMoney = new HashMap<>();

    public static void loadBankBalance(Player p) {
        if (playerMoney.containsKey(p.getUniqueId())) return;

        long amount;
        FileConfiguration players = BankPlus.getCm().getConfig("players");

        if (Values.CONFIG.isStoringUUIDs())
            amount = players.getLong("Players." + p.getUniqueId() + ".Money");
        else
            amount = players.getLong("Players." + p.getName() + ".Money");

        playerMoney.put(p.getUniqueId(), amount);
    }

    public static void saveBankBalance(Player p) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        if (Values.CONFIG.isStoringUUIDs())
            players.set("Players." + p.getUniqueId() + ".Money", getBankBalance(p));
        else
            players.set("Players." + p.getName() + ".Money", getBankBalance(p));
        BankPlus.getCm().savePlayers();
    }

    public static long getBankBalance(Player p) {
        loadBankBalance(p);
        return playerMoney.get(p.getUniqueId());
    }

    public static long getBankBalance(OfflinePlayer p) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        if (Values.CONFIG.isStoringUUIDs())
            return players.getLong("Players." + p.getUniqueId() + ".Money");
        return players.getLong("Players." + p.getName() + ".Money");
    }

    public static long getOfflineInterest(Player p) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        if (Values.CONFIG.isStoringUUIDs())
            return players.getLong("Players." + p.getUniqueId() + ".Offline-Interest");
        return players.getLong("Players." + p.getName() + ".Offline-Interest");
    }

    public static long getOfflineInterest(OfflinePlayer p) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        if (Values.CONFIG.isStoringUUIDs())
            return players.getLong("Players." + p.getUniqueId() + ".Offline-Interest");
        return players.getLong("Players." + p.getName() + ".Offline-Interest");
    }

    public static void withdraw(Player p, long amount) {
        long bankMoney = getBankBalance(p);
        BankPlus.getEconomy().depositPlayer(p, amount);
        setValue(p, bankMoney - amount);
    }

    public static void deposit(Player p, long amount) {
        long bankMoney = getBankBalance(p);
        BankPlus.getEconomy().withdrawPlayer(p, amount);
        setValue(p, bankMoney + amount);
    }

    public static void setPlayerBankBalance(Player p, long amount) {
        setValue(p, amount);
    }

    public static void setPlayerBankBalance(OfflinePlayer p, long amount) {
        setValue(p, amount);
    }

    public static void addPlayerBankBalance(Player p, long amount) {
        long targetBank = getBankBalance(p);
        setValue(p, targetBank + amount);
    }

    public static void addPlayerBankBalance(OfflinePlayer p, long amount) {
        long targetBank = getBankBalance(p);
        setValue(p, targetBank + amount);
    }

    public static void removePlayerBankBalance(Player p, long amount) {
        long targetBank = getBankBalance(p);
        setValue(p, targetBank - amount);
    }

    public static void removePlayerBankBalance(OfflinePlayer p, long amount) {
        long targetBank = getBankBalance(p);
        setValue(p, targetBank - amount);
    }

    public static void setOfflineInterest(Player p, long amount) {
        if (Values.CONFIG.isStoringUUIDs())
            BankPlus.getCm().getConfig("players").set("Players." + p.getUniqueId() + ".Offline-Interest", amount);
        else
            BankPlus.getCm().getConfig("players").set("Players." + p.getName() + ".Offline-Interest", amount);
        BankPlus.getCm().savePlayers();
    }

    public static void setOfflineInterest(OfflinePlayer p, long amount) {
        if (Values.CONFIG.isStoringUUIDs())
            BankPlus.getCm().getConfig("players").set("Players." + p.getUniqueId() + ".Offline-Interest", amount);
        else
            BankPlus.getCm().getConfig("players").set("Players." + p.getName() + ".Offline-Interest", amount);
        BankPlus.getCm().savePlayers();
    }

    private static void setValue(Player p, long amount) {
        playerMoney.put(p.getUniqueId(), amount);
    }

    private static void setValue(OfflinePlayer p, long amount) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        if (Values.CONFIG.isStoringUUIDs())
            players.set("Players." + p.getUniqueId() + ".Money", amount);
        else
            players.set("Players." + p.getName() + ".Money", amount);
        BankPlus.getCm().savePlayers();
    }
}