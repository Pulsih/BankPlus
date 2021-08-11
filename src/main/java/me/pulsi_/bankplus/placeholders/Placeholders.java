package me.pulsi_.bankplus.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {

    private BankPlus plugin;
    public Placeholders(BankPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier(){
        return "bankplus";
    }

    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        if (p == null) {
            return "Player not online";
        }

        long balance = new EconomyManager(plugin).getBankBalance(p);
        int cooldown = Integer.parseInt(plugin.getPlayers().getString("Interest-Cooldown"));

        switch (identifier) {
            case "balance": return "" + balance;
            case "balance_formatted": return MethodUtils.format(balance, plugin);
            case "balance_formatted_long": return MethodUtils.formatLong(balance, plugin);
            case "interest_cooldown": return MethodUtils.formatTime(cooldown, plugin);
        }
        return null;
    }
}