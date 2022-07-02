package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;

public class EconomyManager {

    public static Map<UUID, BigDecimal> playerMoney = new HashMap<>();

    public static List<BigDecimal> getAllPlayerBankBalances() {
        List<BigDecimal> balances = new ArrayList<>();
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        ConfigurationSection playerList = players.getConfigurationSection("Players");
        if (playerList == null) return balances;

        for (String identifier : playerList.getKeys(false)) {
            String sBalance = players.getString("Players." + identifier + ".Money");
            if (sBalance != null) {
                BigDecimal balance;
                try {
                    balance = new BigDecimal(sBalance);
                } catch (NumberFormatException e) {
                    balance = new BigDecimal(0);
                }
                balances.add(balance);
            }
        }
        return balances;
    }

    public static void loadBankBalance(Player p) {
        if (playerMoney.containsKey(p.getUniqueId())) return;
        FileConfiguration players = BankPlus.getCm().getConfig("players");

        String bal;
        if (Values.CONFIG.isStoringUUIDs()) bal = players.getString("Players." + p.getUniqueId() + ".Money");
        else bal = players.getString("Players." + p.getName() + ".Money");

        BigDecimal amount;
        if (bal == null) amount = new BigDecimal(0);
        else {
            try {
                amount = new BigDecimal(bal);
            } catch (NumberFormatException e) {
                amount = new BigDecimal(0);
            }
        }
        EconomyManager.setPlayerBankBalance(p, amount);
    }

    public static void saveBankBalance(Player p) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        String path;
        if (Values.CONFIG.isStoringUUIDs()) path = "Players." + p.getUniqueId() + ".Money";
        else path = "Players." + p.getName() + ".Money";
        players.set(path, Methods.formatBigDouble(getBankBalance(p)));
        BankPlus.getCm().savePlayers();
    }

    public static void saveBankBalance(List<Player> listOfPlayers) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        for (Player p : listOfPlayers) {
            String path;
            if (Values.CONFIG.isStoringUUIDs()) path = "Players." + p.getUniqueId() + ".Money";
            else path = "Players." + p.getName() + ".Money";
            players.set(path, Methods.formatBigDouble(getBankBalance(p)));
        }
        BankPlus.getCm().savePlayers();
    }

    public static BigDecimal getBankBalance(Player p) {
        loadBankBalance(p);
        return playerMoney.get(p.getUniqueId());
    }

    public static BigDecimal getBankBalance(OfflinePlayer p) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");

        String bal;
        if (Values.CONFIG.isStoringUUIDs()) bal = players.getString("Players." + p.getUniqueId() + ".Money");
        else bal = players.getString("Players." + p.getName() + ".Money");

        BigDecimal balance;
        if (bal == null) balance = new BigDecimal(0);
        else balance = new BigDecimal(bal);

        return balance;
    }

    public static BigDecimal getOfflineInterest(Player p) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");

        String s;
        if (Values.CONFIG.isStoringUUIDs())
            s = players.getString("Players." + p.getUniqueId() + ".Offline-Interest");
        else s = players.getString("Players." + p.getName() + ".Offline-Interest");

        BigDecimal offlineInterest;
        if (s == null) offlineInterest = new BigDecimal(0);
        else offlineInterest = new BigDecimal(s);

        return offlineInterest;
    }

    public static BigDecimal getOfflineInterest(OfflinePlayer p) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");

        String s;
        if (Values.CONFIG.isStoringUUIDs()) s = players.getString("Players." + p.getUniqueId() + ".Offline-Interest");
        else s = players.getString("Players." + p.getName() + ".Offline-Interest");

        BigDecimal offlineInterest;
        if (s == null) offlineInterest = new BigDecimal(0);
        else offlineInterest = new BigDecimal(s);

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
        if (Values.CONFIG.isStoringUUIDs())
            BankPlus.getCm().getConfig("players").set("Players." + p.getUniqueId() + ".Offline-Interest", Methods.formatBigDouble(amount));
        else
            BankPlus.getCm().getConfig("players").set("Players." + p.getName() + ".Offline-Interest", Methods.formatBigDouble(amount));
        BankPlus.getCm().savePlayers();
    }

    public static void setOfflineInterest(OfflinePlayer p, BigDecimal amount) {
        if (Values.CONFIG.isStoringUUIDs())
            BankPlus.getCm().getConfig("players").set("Players." + p.getUniqueId() + ".Offline-Interest", Methods.formatBigDouble(amount));
        else
            BankPlus.getCm().getConfig("players").set("Players." + p.getName() + ".Offline-Interest", Methods.formatBigDouble(amount));
        BankPlus.getCm().savePlayers();
    }

    private static void setValue(Player p, BigDecimal amount) {
        String amountFormatted = Methods.formatBigDouble(amount);
        playerMoney.put(p.getUniqueId(), new BigDecimal(amountFormatted));
    }

    private static void setValue(OfflinePlayer p, BigDecimal amount) {
        FileConfiguration players = BankPlus.getCm().getConfig("players");
        String path;
        if (Values.CONFIG.isStoringUUIDs()) path = "Players." + p.getUniqueId() + ".Money";
        else path = "Players." + p.getName() + ".Money";
        players.set(path, Methods.formatBigDouble(amount));
        BankPlus.getCm().savePlayers();
    }
}