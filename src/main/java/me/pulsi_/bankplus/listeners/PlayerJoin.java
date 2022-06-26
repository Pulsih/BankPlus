package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.ConfigManager;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.math.BigDecimal;
import java.util.UUID;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        String name = p.getName();

        if (Values.CONFIG.isStoringUUIDs()) registerPlayer(p, uuid.toString());
        else registerPlayer(p, name);
        offlineInterestMessage(p);
    }

    private void registerPlayer(Player p, String identifier) {
        boolean hasChanges = false;

        FileConfiguration players = BankPlus.getCm().getConfig("players");
        String sBalance = players.getString("Players." + identifier + ".Money");
        String sName = players.getString("Players." + identifier + ".Account-Name");
        String sOfflineInterest = players.getString("Players." + identifier + ".Offline-Interest");

        if (sBalance == null) {
            ChatUtils.log("&a&lBank&9&lPlus &2Successfully registered &f" + p.getName() + "&a's account!");
            players.set("Players." + identifier + ".Money", EconomyManager.format(Values.CONFIG.getStartAmount()));
            hasChanges = true;
        }
        if (Values.CONFIG.isNotifyOfflineInterest() && sOfflineInterest == null) {
            players.set("Players." + identifier + ".Offline-Interest", String.valueOf(0));
            hasChanges = true;
        }
        if (sName == null) {
            players.set("Players." + identifier + ".Account-Name", p.getName());
            hasChanges = true;
        }
        if (hasChanges) BankPlus.getCm().savePlayers();

        EconomyManager.loadBankBalance(p);
        notifyAdminGuiPositionChanging(p);
    }

    private void offlineInterestMessage(Player p) {
        if (!Values.CONFIG.isNotifyOfflineInterest()) return;
        BigDecimal offlineInterest = EconomyManager.getOfflineInterest(p);
        if (offlineInterest.doubleValue() <= 0) return;

        long delay = Values.CONFIG.getNotifyOfflineInterestDelay();
        String message = ChatUtils.color(Values.CONFIG.getNotifyOfflineInterestMessage()
                .replace("%amount%", Methods.formatCommas(offlineInterest))
                .replace("%amount_formatted%", Methods.format(offlineInterest))
                .replace("%amount_formatted_long%", Methods.formatLong(offlineInterest)));

        if (delay != 0)
            Bukkit.getScheduler().runTaskLater(BankPlus.getInstance(), () -> p.sendMessage(message), delay * 20L);
        else
            p.sendMessage(message);

        EconomyManager.setOfflineInterest(p, new BigDecimal(0));
    }

    private void notifyAdminGuiPositionChanging(Player p) {
        if (!ConfigManager.guiHasMovedFile || !p.isOp() && !p.hasPermission("bankplus.notify")) return;
        Bukkit.getScheduler().runTaskLater(BankPlus.getInstance(), () ->
                p.sendMessage(ChatUtils.color("&a&lBank&9&lPlus &aHi &f" + p.getName() + "&a! I'm here to notify you that the &fposition &aof the gui has changed from " +
                "the version &fv5.2&a. I already managed to move the gui settings from the file &f\"config.yml\" &ato &f\"bank.yml\"&a!"))
                , 80L);
        ConfigManager.guiHasMovedFile = false;
    }
}