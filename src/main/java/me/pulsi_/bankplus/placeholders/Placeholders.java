package me.pulsi_.bankplus.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
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
    public String onPlaceholderRequest(Player p, String identifier){
        if (p == null) {
            return "The players is not Online!";
        }

        EconomyManager economyManager = new EconomyManager(plugin);
        int balance = economyManager.getPersonalBalance(p);

        if (identifier.equals("balance")) {
            return "" + balance;
        }

        if (identifier.equals("balance_formatted")) {
            if (balance < 1000) {
                return String.valueOf(balance);
            }
            if (balance >= 1000 && balance < 1000000) {
                return Math.round(balance / 1000) + "K";
            }
            if (balance >= 1000000 && balance < 1000000000) {
                return Math.round(balance / 1000000) + "M";
            }
            if (balance >= 1000000000) {
                return Math.round(balance / 1000000000) + "B";
            }
        }

        return null;
    }
}