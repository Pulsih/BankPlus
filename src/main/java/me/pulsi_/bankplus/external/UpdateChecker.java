package me.pulsi_.bankplus.external;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.texts.BPChat;
import me.pulsi_.bankplus.values.Values;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateChecker implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!Values.CONFIG.isUpdateCheckerEnabled() || (!p.isOp() && !p.hasPermission("bankplus.notify")) || BankPlus.INSTANCE().isUpdated()) return;

        TextComponent text = new TextComponent(BPChat.color("&a&lBank&9&lPlus &aNew update available! "));
        TextComponent button = new TextComponent(BPChat.color("&9&l[CLICK HERE]"));
        button.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/%E2%9C%A8-bankplus-%E2%9C%A8.93130/"));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to download it!").color(ChatColor.GRAY).create()));
        text.addExtra(button);

        Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> {
            p.sendMessage("");
            p.spigot().sendMessage(text);
            p.sendMessage("");
        }, 80);
    }
}