package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.Commands;
import me.pulsi_.bankplus.commands.TabCompletion;
import me.pulsi_.bankplus.listeners.PlayerJoin;
import me.pulsi_.bankplus.listeners.PlayerChat;
import me.pulsi_.bankplus.external.UpdateChecker;
import me.pulsi_.bankplus.guis.GuiBankListener;
import me.pulsi_.bankplus.listeners.PlayerQuit;
import me.pulsi_.bankplus.utils.ChatUtils;

public class DataManager {

    public static void registerEvents(BankPlus plugin) {
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoin(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerQuit(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new GuiBankListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new UpdateChecker(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerChat(plugin), plugin);
    }

    public static void setupCommands(BankPlus plugin) {
        plugin.getCommand("bankplus").setExecutor(new Commands(plugin));
        plugin.getCommand("bankplus").setTabCompleter(new TabCompletion());
    }

    public static void startupMessage(BankPlus plugin) {
        plugin.getServer().getConsoleSender().sendMessage("");
        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.color("&a&lBank&9&lPlus &2Plugin Enabled!"));
        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.color("&aRunning on version &f" + plugin.getDescription().getVersion()));
        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.color("&aMade by &fPulsi_"));
        plugin.getServer().getConsoleSender().sendMessage("");
    }

    public static void shutdownMessage(BankPlus plugin) {
        plugin.getServer().getConsoleSender().sendMessage("");
        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.color("&a&lBank&9&lPlus &cDisabling Plugin!"));
        plugin.getServer().getConsoleSender().sendMessage("");
    }
}