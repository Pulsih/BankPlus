package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyManager {

    private final BankPlus plugin;

    public EconomyManager(BankPlus plugin) {
        this.plugin = plugin;
    }

    public static EconomyManager getInstance() {
        return new EconomyManager(JavaPlugin.getPlugin(BankPlus.class));
    }
    
    public long getBankBalance(Player p) {
        if (Values.CONFIG.isStoringUUIDs())
            return plugin.players().getLong("Players." + p.getUniqueId() + ".Money");
        return plugin.players().getLong("Players." + p.getName() + ".Money");
    }

    public long getBankBalance(OfflinePlayer p) {
        if (Values.CONFIG.isStoringUUIDs())
            return plugin.players().getLong("Players." + p.getUniqueId() + ".Money");
        return plugin.players().getLong("Players." + p.getName() + ".Money");
    }

    public long getOfflineInterest(Player p) {
        if (Values.CONFIG.isStoringUUIDs())
            return plugin.players().getLong("Players." + p.getUniqueId() + ".Offline-Interest");
        return plugin.players().getLong("Players." + p.getName() + ".Offline-Interest");
    }

    public long getOfflineInterest(OfflinePlayer p) {
        if (Values.CONFIG.isStoringUUIDs())
            return plugin.players().getLong("Players." + p.getUniqueId() + ".Offline-Interest");
        return plugin.players().getLong("Players." + p.getName() + ".Offline-Interest");
    }

    public void withdraw(Player p, long withdraw) {
        long bankMoney = getBankBalance(p);
        plugin.getEconomy().depositPlayer(p, withdraw);
        setValue(p, bankMoney - withdraw);
    }

    public void deposit(Player p, long deposit) {
        long bankMoney = getBankBalance(p);
        plugin.getEconomy().withdrawPlayer(p, deposit);
        setValue(p, bankMoney + deposit);
    }

    public void setPlayerBankBalance(Player p, long amount) {
        setValue(p, amount);
    }

    public void setPlayerBankBalance(OfflinePlayer p, long amount) {
        setValue(p, amount);
    }

    public void addPlayerBankBalance(Player p, long amount) {
        long targetBank = getBankBalance(p);
        setValue(p, targetBank + amount);
    }

    public void addPlayerBankBalance(OfflinePlayer p, long amount) {
        long targetBank = getBankBalance(p);
        setValue(p, targetBank + amount);
    }

    public void removePlayerBankBalance(Player p, long amount) {
        long targetBank = getBankBalance(p);
        setValue(p, targetBank - amount);
    }

    public void removePlayerBankBalance(OfflinePlayer p, long amount) {
        long targetBank = getBankBalance(p);
        setValue(p, targetBank - amount);
    }

    public void setOfflineInterest(Player p, long amount) {
        plugin.players().set("Players." + p.getUniqueId() + ".Offline-Interest", amount);
        plugin.savePlayers();
    }

    public void setOfflineInterest(OfflinePlayer p, long amount) {
        plugin.players().set("Players." + p.getUniqueId() + ".Offline-Interest", amount);
        plugin.savePlayers();
    }

    public void saveBalance(Player p) {
        final long balance = getBankBalance(p);
        setValue(p, balance);
    }

    private void setValue(Player p, long amount) {
        if (Values.CONFIG.isStoringUUIDs()) {
            plugin.players().set("Players." + p.getUniqueId() + ".Money", amount);
        } else {
            plugin.players().set("Players." + p.getName() + ".Money", amount);
        }
        plugin.savePlayers();
    }

    private void setValue(OfflinePlayer p, long amount) {
        if (Values.CONFIG.isStoringUUIDs()) {
            plugin.players().set("Players." + p.getUniqueId() + ".Money", amount);
        } else {
            plugin.players().set("Players." + p.getName() + ".Money", amount);
        }
        plugin.savePlayers();
    }
}