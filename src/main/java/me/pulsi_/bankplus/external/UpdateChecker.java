package me.pulsi_.bankplus.external;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.ChatUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker implements Listener {

    private BankPlus plugin;
    private int resourceId;

    public UpdateChecker(BankPlus plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public boolean isUpToDate() throws Exception {
        URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId);
        URLConnection connection = url.openConnection();
        String currentVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
        return plugin.getDescription().getVersion().equals(currentVersion);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) throws Exception {
        if (!plugin.getConfig().getBoolean("Update-Checker")) return;
        Player p = e.getPlayer();
        if (p.isOp() || p.hasPermission("bankplus.notify")) {
            if (!isUpToDate()) {

                TextComponent update = new TextComponent(ChatUtils.c("&8&l<&d&lAdvanced&a&lAuto&c&lSmelt&8&l> &b&lNew update available! Click here!"));
                update.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/advancedautosmelt-smelt-ores-autopickup-items-and-exp.90587/"));
                update.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to download it!").color(ChatColor.LIGHT_PURPLE).create()));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.sendMessage("");
                        p.spigot().sendMessage(update);
                        p.sendMessage("");
                    }
                }.runTaskLater(plugin, 60);
            }
        }
    }
}