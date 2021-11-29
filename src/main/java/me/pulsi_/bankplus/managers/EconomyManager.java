package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.values.configs.ConfigValues;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class EconomyManager {

    private static BankPlus plugin;

    public EconomyManager(BankPlus plugin) {
        EconomyManager.plugin = plugin;
    }

    public static long getBankBalance(Player p) {
        if (ConfigValues.isStoringUUIDs())
            return plugin.players().getLong("Players." + p.getUniqueId() + ".Money");
        return plugin.players().getLong("Players." + p.getName() + ".Money");
    }

    public static long getBankBalance(OfflinePlayer p) {
        if (ConfigValues.isStoringUUIDs())
            return plugin.players().getLong("Players." + p.getUniqueId() + ".Money");
        return plugin.players().getLong("Players." + p.getName() + ".Money");
    }

    public static long getOfflineInterest(Player p) {
        if (ConfigValues.isStoringUUIDs())
            return plugin.players().getLong("Players." + p.getUniqueId() + ".Offline-Interest");
        return plugin.players().getLong("Players." + p.getName() + ".Offline-Interest");
    }

    public static long getOfflineInterest(OfflinePlayer p) {
        if (ConfigValues.isStoringUUIDs())
            return plugin.players().getLong("Players." + p.getUniqueId() + ".Offline-Interest");
        return plugin.players().getLong("Players." + p.getName() + ".Offline-Interest");
    }

    public static void withdraw(Player p, long withdraw) {
        long bankMoney = getBankBalance(p);
        plugin.getEconomy().depositPlayer(p, withdraw);
        setValue(p, bankMoney - withdraw);
    }

    public static void deposit(Player p, long deposit) {
        long bankMoney = getBankBalance(p);
        plugin.getEconomy().withdrawPlayer(p, deposit);
        setValue(p, bankMoney + deposit);
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
        plugin.players().set("Players." + p.getUniqueId() + ".Offline-Interest", amount);
        plugin.savePlayers();
    }

    public static void setOfflineInterest(OfflinePlayer p, long amount) {
        plugin.players().set("Players." + p.getUniqueId() + ".Offline-Interest", amount);
        plugin.savePlayers();
    }

    public static void saveBalance(Player p) {
        final long balance = getBankBalance(p);
        setValue(p, balance);
    }

    private static void setValue(Player p, long amount) {
        if (ConfigValues.isStoringUUIDs()) {
            plugin.players().set("Players." + p.getUniqueId() + ".Money", amount);
        } else {
            plugin.players().set("Players." + p.getName() + ".Money", amount);
        }
        plugin.savePlayers();
    }

    private static void setValue(OfflinePlayer p, long amount) {
        if (ConfigValues.isStoringUUIDs()) {
            plugin.players().set("Players." + p.getUniqueId() + ".Money", amount);
        } else {
            plugin.players().set("Players." + p.getName() + ".Money", amount);
        }
        plugin.savePlayers();
    }
}