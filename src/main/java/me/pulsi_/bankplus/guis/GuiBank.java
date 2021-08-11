package me.pulsi_.bankplus.guis;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GuiBank {

    private final BankPlus plugin;
    public GuiBank(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void openGui(Player p) {

        int lines = guiLines(plugin.getConfiguration().getInt("Gui.Lines"));
        String title;
        try {
            title = plugin.getConfiguration().getString("Gui.Title");
        } catch (NullPointerException | IllegalArgumentException e) {
            title = "&c&l*CANNOT FIND TITLE*";
        }

        Inventory guiBank = Bukkit.createInventory(null, lines, ChatUtils.c(title));

        ConfigurationSection c = plugin.getConfiguration().getConfigurationSection("Gui.Items");
        for (String items : c.getKeys(false)) {
            try {
                guiBank.setItem(c.getConfigurationSection(items).getInt("Slot") - 1, ItemCreator.createItemStack(c.getConfigurationSection(items), p, plugin));
            } catch (ArrayIndexOutOfBoundsException ex) {
                plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&a&lBank&9&lPlus &aSome items go arent set properly! Please fix it in the config!"));
                guiBank.addItem(ItemCreator.createItemStack(c.getConfigurationSection(items), p, plugin));
            }
        }

        if (plugin.getConfiguration().getBoolean("Gui.Filler.Enabled")) {
            for (int i = 0; i < lines; i++) {
                if (guiBank.getItem(i) == null)
                    guiBank.setItem(i, ItemCreator.guiFiller(plugin));
            }
        }

        p.openInventory(guiBank);
    }

    public int guiLines(int number) {
        int lines;
        switch (number) {
            case 1:
                lines = 9;
                break;
            case 2:
                lines = 18;
                break;
            case 3:
                lines = 27;
                break;
            default:
                lines = 36;
                break;
            case 5:
                lines = 45;
                break;
            case 6:
                lines = 54;
                break;
        }
        return lines;
    }

    public static void updateLore(Player p, BankPlus plugin) {
        ConfigurationSection c = plugin.getConfiguration().getConfigurationSection("Gui.Items");
        for (String items : c.getKeys(false)) {
            try {
                ItemStack i = p.getOpenInventory().getItem(c.getConfigurationSection(items).getInt("Slot") - 1);
                i.setItemMeta(ItemCreator.setLore(c.getConfigurationSection(items), i, p));
            } catch (NullPointerException | IllegalArgumentException | ArrayIndexOutOfBoundsException ignored) {}
        }
    }
}