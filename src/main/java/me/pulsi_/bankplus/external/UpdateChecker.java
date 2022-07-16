package me.pulsi_.bankplus.external;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPChat;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker implements Listener {

    private static boolean isUpToDate;
    private final BankPlus plugin;

    public UpdateChecker(BankPlus plugin) {
        this.plugin = plugin;
        isUpToDate = true;
        if(Values.CONFIG.isUpdateCheckerEnabled()){
            new BukkitRunnable() {
                @Override
                public void run() {
                    boolean isUpdated;
                    try {
                        isUpdated = isPluginUpdated(); // should not in main thread.
                    } catch (IOException e) {
                        isUpdated = true;
                    }
                    isUpToDate = isUpdated;
                }
            }.runTaskAsynchronously(plugin);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!Values.CONFIG.isUpdateCheckerEnabled() || (!p.isOp() && !p.hasPermission("bankplus.notify")) || isUpToDate) return;

        TextComponent text = new TextComponent(BPChat.color("&a&lBank&9&lPlus &aNew update available! "));
        TextComponent button = new TextComponent(BPChat.color("&a&l[CLICK HERE]"));
        button.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/%E2%9C%A8-bankplus-%E2%9C%A8-easy-and-lightweight-bank-plugin.93130/"));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to download it!").color(ChatColor.GRAY).create()));
        text.addExtra(button);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            p.sendMessage("");
            p.spigot().sendMessage(text);
            p.sendMessage("");
        }, 80);
    }

    private boolean isPluginUpdated() throws IOException {
        final String currentVersion = new BufferedReader(new InputStreamReader(new URL("https://api.spigotmc.org/legacy/update.php?resource=93130").openConnection().getInputStream())).readLine();
        return plugin.getDescription().getVersion().equals(currentVersion);
    }
}