package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.debt.DebtUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPFormatter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class PlayerRegistry {

    private final HashMap<UUID, BPPlayer> players = new HashMap<>();

    public void put(Player p, BPPlayer player) {
        players.put(p.getUniqueId(), player);
    }

    public BPPlayer get(Player p) {
        return players.get(p.getUniqueId());
    }

    public BPPlayer remove(Player p) {
        return players.remove(p.getUniqueId());
    }

    public boolean contains(Player p) {
        return players.containsKey(p.getUniqueId());
    }

    public void savePlayer(Player p) {
        BPEconomy economy = BankPlus.getBPEconomy();
        BPPlayerFiles files = new BPPlayerFiles(p);

        File file = files.getPlayerFile();
        FileConfiguration config = files.getPlayerConfig(file);

        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
            config.set("banks." + bankName + ".money", BPFormatter.formatBigDouble(economy.getBankBalance(p, bankName)));

        config.set("debt", BPFormatter.formatBigDouble(DebtUtils.getDebt(p)));

        files.savePlayerFile(config, file, false);
    }
}