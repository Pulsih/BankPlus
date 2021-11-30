package me.pulsi_.bankplus.external;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.values.Values;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker implements Listener {

    private final boolean isUpToDate;
    private final BankPlus plugin;

    public UpdateChecker(BankPlus plugin) {
        boolean isUpdated;
        this.plugin = plugin;
        try {
            isUpdated = isPluginUpdated();
        } catch (IOException e) {
            isUpdated = true;
        }
        this.isUpToDate = isUpdated;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if ((!Values.CONFIG.isUpdateCheckerEnabled() || (!e.getPlayer().isOp() && !e.getPlayer().hasPermission("bankplus.notify"))) || isUpToDate) return;

        TextComponent update = new TextComponent(ChatUtils.color("&a&lBank&9&lPlus &aNew update available! &7(CLICK HERE)"));
        update.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/%E2%9C%A8-bankplus-%E2%9C%A8-easy-and-lightweight-bank-plugin.93130/"));
        update.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to download it!").color(ChatColor.GRAY).create()));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            e.getPlayer().sendMessage("");
            e.getPlayer().spigot().sendMessage(update);
            e.getPlayer().sendMessage("");
        }, 80);
    }

    private boolean isPluginUpdated() throws IOException {
        final String currentVersion = new BufferedReader(new InputStreamReader(new URL("https://api.spigotmc.org/legacy/update.php?resource=93130").openConnection().getInputStream())).readLine();
        return plugin.getDescription().getVersion().equals(currentVersion);
    }
}