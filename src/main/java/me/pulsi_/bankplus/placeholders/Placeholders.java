package me.pulsi_.bankplus.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {

    private final BankPlus plugin;

    public Placeholders(BankPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return "bankplus";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        if (p == null) return "Player not online";

        long balance = EconomyManager.getInstance().getBankBalance(p);

        switch (identifier) {
            case "balance":
                return Methods.formatCommas(balance);
            case "balance_long":
                return "" + balance;
            case "balance_formatted":
                return Methods.format(balance);
            case "balance_formatted_long":
                return Methods.formatLong(balance);
            case "interest_cooldown": {
                String interest;
                if (Values.CONFIG.isInterestEnabled()) {
                    long cooldown = Interest.interestCooldown.get(0);
                    if (cooldown <= 0) {
                        interest = "0";
                    } else {
                        interest = Methods.formatTime((int) cooldown);
                    }
                } else {
                    interest = ChatColor.RED + "Interest is disabled.";
                }
                return interest;
            }
        }
        return null;
    }
}