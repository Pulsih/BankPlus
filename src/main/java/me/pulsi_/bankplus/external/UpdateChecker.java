package me.pulsi_.bankplus.external;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.ChatUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker implements Listener {

    private BankPlus plugin;
    public UpdateChecker(BankPlus plugin) {
        this.plugin = plugin;
    }

    private final boolean isUpToDate() throws Exception {
        final URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + 93130);
        final String currentVersion = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream())).readLine();
        return plugin.getDescription().getVersion().equals(currentVersion);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) throws Exception {
        if (isUpToDate()) return;
        if (!plugin.getConfig().getBoolean("Update-Checker")) return;
        if (!e.getPlayer().isOp() || !e.getPlayer().hasPermission("bankplus.notify")) return;
        TextComponent update = new TextComponent(ChatUtils.c("&a&lBank&9&lPlus &aNew update available! Click here!"));
        update.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/%E2%9C%A8-bankplus-%E2%9C%A8-easy-and-lightweight-bank-plugin.93130/"));
        update.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to download it!").color(ChatColor.LIGHT_PURPLE).create()));
        e.getPlayer().sendMessage("");
        e.getPlayer().spigot().sendMessage(update);
        e.getPlayer().sendMessage("");
    }
}