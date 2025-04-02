package me.pulsi_.bankplus.external;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.texts.BPChat;
import me.pulsi_.bankplus.values.ConfigValues;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateChecker implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!ConfigValues.isUpdateCheckerEnabled() || (!p.isOp() && !p.hasPermission("bankplus.notify"))) return;

        String message;
        if (!BankPlus.INSTANCE().isUpdated())
            message = BPChat.PREFIX + " <green>A new update is available! " +
                    "<hover:show_text:'<aqua>Click to download it!'>" +
                    "<click:open_url:https://www.spigotmc.org/resources/%E2%9C%A8-bankplus-%E2%9C%A8.93130/>" +
                    "<aqua>[INFO]";
        else if (BankPlus.isAlphaVersion())
            message = BPChat.PREFIX + " <aqua>You are using an alpha " +
                    "version of the plugin, if you find any bug make sure to report it in my discord server. Thanks! :)";
        else message = null;

        if (message != null)
            Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> {
                p.sendMessage(" ");
                p.sendMessage(MiniMessage.miniMessage().deserialize(message));
                p.sendMessage(" ");
            }, 80);
    }
}