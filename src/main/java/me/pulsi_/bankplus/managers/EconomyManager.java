package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.MapUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class EconomyManager {

    private BankPlus plugin;
    public EconomyManager(BankPlus plugin) {
        this.plugin = plugin;
    }
    
    public final long getBankBalance(Player p) {
        return MapUtils.BALANCE.get(p.getUniqueId());
    }
    public final long getBankBalance(OfflinePlayer p) {
        return plugin.players().getLong("Players." + p.getUniqueId() + ".Money");
    }

    public final long getOfflineInterest(Player p) {
        return plugin.players().getLong("Players." + p.getUniqueId() + ".Offline-Interest");
    }
    public final long getOfflineInterest(OfflinePlayer p) {
        return plugin.players().getLong("Players." + p.getUniqueId() + ".Offline-Interest");
    }

    public final void withdraw(Player p, long withdraw) {
        final long bankMoney = getBankBalance(p);
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
        plugin.players().set("Players." + p.getUniqueId() + ".Offline-Interest", amount);
        plugin.savePlayers();
    }
    public final void setOfflineInterest(OfflinePlayer p, long amount) {
        plugin.players().set("Players." + p.getUniqueId() + ".Offline-Interest", amount);
        plugin.savePlayers();
    }

    public final void saveBalance(Player p) {
        final long balance = getBankBalance(p);
        plugin.players().set("Players." + p.getUniqueId() + ".Money", balance);
        plugin.savePlayers();
    }

    private void setValue(Player p, long amount) {
        MapUtils.BALANCE.put(p.getUniqueId(), amount);
    }
    private void setValue(OfflinePlayer p, long amount) {
        plugin.players().set("Players." + p.getUniqueId() + ".Money", amount);
        plugin.savePlayers();
    }
}