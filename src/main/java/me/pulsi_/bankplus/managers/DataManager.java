package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.Commands;
import me.pulsi_.bankplus.commands.TabCompletion;
import me.pulsi_.bankplus.events.JoinEvent;
import me.pulsi_.bankplus.external.UpdateChecker;
import me.pulsi_.bankplus.guis.GuiBankListener;

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
}