package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.mySQL.BPSQL;
import me.pulsi_.bankplus.utils.BPLogger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerRegistry {

    private static final HashMap<UUID, BPPlayer> players = new HashMap<>();

    public static boolean isPlayerLoaded(OfflinePlayer p) {
        return players.containsKey(p.getUniqueId());
    }

    /**
     * Load the specified player to all registered banks.
     *
     * @param p The player to load.
     * @return The instance of the loaded player.
     */
    public static BPPlayer loadPlayer(Player p) {
        return loadPlayer(p, true);
    }

    /**
     * Load the specified player to all registered banks.
     *
     * @param p             The player to load.
     * @param wasRegistered Specify if the player was already registered or not.
     * @return The instance of the loaded player.
     */
    public static BPPlayer loadPlayer(Player p, boolean wasRegistered) {
        BPPlayer bpPlayer = new BPPlayer(p);

        for (Bank bank : BankUtils.getBanks())
            bpPlayer = loadPlayer(p, bank, wasRegistered);

        return bpPlayer;
    }

    /**
     * Load the player only to the specified bank (useful when just needing to load it in new registered banks)
     *
     * @param p             The player to load.
     * @param bank          The bank where to load the player.
     * @param wasRegistered Specify if the player was already registered or not.
     * @return The instance of the loaded player.
     */
    public static BPPlayer loadPlayer(Player p, Bank bank, boolean wasRegistered) {
        BPPlayer bpPlayer;

        UUID uuid = p.getUniqueId();
        if (!players.containsKey(uuid)) bpPlayer = new BPPlayer(p);
        else bpPlayer = players.get(uuid);

        if (bank == null) {
            BPLogger.Console.error("Cannot load player " + p.getName() + " because the bank specified is null");
            return bpPlayer;
        }
        BPEconomy economy = bank.getBankEconomy();

        if (BPSQL.isConnected()) {
            // If MySQL is enabled, load the player asynchronously, it may take few more
            // milliseconds but this will ensure to not affect server performance
            // and the player values will be visible after the player has been loaded.
            Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {
                economy.loadPlayerHolder(p, wasRegistered);
                economy.markPlayerAsLoaded(p);
            });
            return bpPlayer;
        }

        economy.loadPlayerHolder(p, wasRegistered);
        economy.markPlayerAsLoaded(p);
        return bpPlayer;
    }

    public static BPPlayer unloadPlayer(UUID uuid) {
        for (BPEconomy economy : BPEconomy.list()) economy.unloadPlayerBalance(uuid);
        return players.remove(uuid);
    }

    public static BPPlayer get(OfflinePlayer p) {
        return get(p.getUniqueId());
    }

    public static BPPlayer get(UUID uuid) {
        return players.get(uuid);
    }
}