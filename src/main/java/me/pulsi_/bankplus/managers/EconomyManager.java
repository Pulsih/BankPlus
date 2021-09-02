package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class EconomyManager {

    private final boolean isUUIDStorage;
    private BankPlus plugin;
    public EconomyManager(BankPlus plugin) {
        this.plugin = plugin;
        this.isUUIDStorage = plugin.getConfiguration().getBoolean("General.Use-UUID");
    }
    
    public final long getBankBalance(Player p) {
        long othersBalance;
        if (isUUIDStorage) {
            othersBalance = plugin.getPlayers().getLong("Players." + p.getUniqueId() + ".Money");
        } else {
            othersBalance = plugin.getPlayers().getLong("Players." + p.getName() + ".Money");
        }
        return othersBalance;
    }
    public final long getBankBalance(OfflinePlayer p) {
        long othersBalance;
        if (isUUIDStorage) {
            othersBalance = plugin.getPlayers().getLong("Players." + p.getUniqueId() + ".Money");
        } else {
            othersBalance = plugin.getPlayers().getLong("Players." + p.getName() + ".Money");
        }
        return othersBalance;
    }

    public final long getOfflineInterest(Player p) {
        long othersBalance;
        if (isUUIDStorage) {
            othersBalance = plugin.getPlayers().getLong("Players." + p.getUniqueId() + ".Offline-Interest");
        } else {
            othersBalance = plugin.getPlayers().getLong("Players." + p.getName() + ".Offline-Interest");
        }
        return othersBalance;
    }
    public final long getOfflineInterest(OfflinePlayer p) {
        long othersBalance;
        if (isUUIDStorage) {
            othersBalance = plugin.getPlayers().getLong("Players." + p.getUniqueId() + ".Offline-Interest");
        } else {
            othersBalance = plugin.getPlayers().getLong("Players." + p.getName() + ".Offline-Interest");
        }
        return othersBalance;
    }

    public final void withdraw(Player p, long withdraw) {
        long bankMoney = getBankBalance(p);
        plugin.getEconomy().depositPlayer(p, withdraw);
        setValue(p, bankMoney - withdraw);
    }

    public final void deposit(Player p, long deposit) {
        long bankMoney = getBankBalance(p);
        plugin.getEconomy().withdrawPlayer(p, deposit);
        setValue(p, bankMoney + deposit);
    }

    public final void setPlayerBankBalance(Player p, long amount) {
        setValue(p, amount);
    }
    public final void setPlayerBankBalance(OfflinePlayer p, long amount) {
        setValue(p, amount);
    }

    public final void addPlayerBankBalance(Player p, long amount) {
        long targetBank = getBankBalance(p);
        setValue(p, targetBank + amount);
    }
    public final void addPlayerBankBalance(OfflinePlayer p, long amount) {
        long targetBank = getBankBalance(p);
        setValue(p, targetBank + amount);
    }

    public final void removePlayerBankBalance(Player p, long amount) {
        long targetBank = getBankBalance(p);
        setValue(p, targetBank - amount);
    }
    public final void removePlayerBankBalance(OfflinePlayer p, long amount) {
        long targetBank = getBankBalance(p);
        setValue(p, targetBank - amount);
    }

    public final void setOfflineInterest(Player p, long amount) {
        if (isUUIDStorage) {
            plugin.getPlayers().set("Players." + p.getUniqueId() + ".Offline-Interest", amount);
        } else {
            plugin.getPlayers().set("Players." + p.getName() + ".Offline-Interest", amount);
        }
        plugin.savePlayers();
    }
    public final void setOfflineInterest(OfflinePlayer p, long amount) {
        if (isUUIDStorage) {
            plugin.getPlayers().set("Players." + p.getUniqueId() + ".Offline-Interest", amount);
        } else {
            plugin.getPlayers().set("Players." + p.getName() + ".Offline-Interest", amount);
        }
        plugin.savePlayers();
    }

    private void setValue(Player p, long amount) {
        if (isUUIDStorage) {
            plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", amount);
        } else {
            plugin.getPlayers().set("Players." + p.getName() + ".Money", amount);
        }
        plugin.savePlayers();
    }
    private void setValue(OfflinePlayer p, long amount) {
        if (isUUIDStorage) {
            plugin.getPlayers().set("Players." + p.getUniqueId() + ".Money", amount);
        } else {
            plugin.getPlayers().set("Players." + p.getName() + ".Money", amount);
        }
        plugin.savePlayers();
    }
}