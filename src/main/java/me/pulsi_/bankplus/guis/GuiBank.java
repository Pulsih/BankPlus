package me.pulsi_.bankplus.guis;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class GuiBank {

    private BankPlus plugin;
    private EconomyManager economyManager;
    public GuiBank(BankPlus plugin) {
        this.plugin = plugin;
        this.economyManager = new EconomyManager(plugin);
    }

    public void openGui(Player p) {

        int lines = guiLines(plugin.getConfiguration().getInt("Gui.Lines"));
        String title;
        try {
            title = plugin.getConfiguration().getString("Gui.Title");
        } catch (NullPointerException ex) {
            title = "&c&l*CANNOT FIND TITLE*";
        }

        Inventory guiBank = Bukkit.createInventory(null, lines, ChatUtils.c(title));

        ConfigurationSection c = plugin.getConfiguration().getConfigurationSection("Gui.Items");

        ItemCreator itemCreator = new ItemCreator();

        for (String items : c.getKeys(false)) {
            try {
                guiBank.setItem(c.getConfigurationSection(items).getInt("Slot") - 1, itemCreator.createItemStack(c.getConfigurationSection(items), p, economyManager, plugin));
            } catch (ArrayIndexOutOfBoundsException ex) {
                plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&a&lBank&9&lPlus &aThere are some items that go out of the gui! Please fix it in the config!"));
                guiBank.addItem(itemCreator.createItemStack(c.getConfigurationSection(items), p, economyManager, plugin));
            }
        }

        if (plugin.getConfiguration().getBoolean("Gui.Filler.Enabled")) {
            for (int i = 0; i < lines; i++) {
                guiBank.getItem(i);
                if (guiBank.getItem(i) == null) {
                    guiBank.setItem(i, ItemCreator.guiFiller(plugin));
                }
            }
        }

        p.openInventory(guiBank);
    }

    public int guiLines(int number) {

        int lines = 27;

        if(number == 1) {
            return 9;
        }
        if(number == 2) {
            return 18;
        }
        if(number == 3) {
            return 27;
        }
        if(number == 4) {
            return 36;
        }
        if(number == 5) {
            return 45;
        }
        if(number == 6) {
            return 54;
        }

        return lines;
    }
}