package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.Commands;
import me.pulsi_.bankplus.commands.TabCompletion;
import me.pulsi_.bankplus.events.JoinEvent;
import me.pulsi_.bankplus.external.UpdateChecker;
import me.pulsi_.bankplus.guis.GuiBankListener;
import me.pulsi_.bankplus.utils.ChatUtils;

public class DataManager {

    public static void registerEvents(BankPlus plugin) {
        plugin.getServer().getPluginManager().registerEvents(new JoinEvent(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new GuiBankListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new UpdateChecker(plugin, 93130), plugin);
    }

    public static void setupCommands(BankPlus plugin) {
        plugin.getCommand("bankplus").setExecutor(new Commands(plugin));
        plugin.getCommand("bankplus").setTabCompleter(new TabCompletion());
    }

    public static void startupMessage(BankPlus plugin) {
        plugin.getServer().getConsoleSender().sendMessage("");
        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&a&lBank&9&lPlus &2Plugin Enabled!"));
        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&aRunning on version &f%v%").replace("%v%",plugin.getDescription().getVersion()));
        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&aMade by &fPulsi_"));
        plugin.getServer().getConsoleSender().sendMessage("");
    }

    public static void shutdownMessage(BankPlus plugin) {
        plugin.getServer().getConsoleSender().sendMessage("");
        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&a&lBank&9&lPlus &cDisabling Plugin!"));
        plugin.getServer().getConsoleSender().sendMessage("");
    }
}