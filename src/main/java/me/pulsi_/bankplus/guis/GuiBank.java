package me.pulsi_.bankplus.guis;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class GuiBank {

    private final BankPlus plugin;
    public GuiBank(BankPlus plugin) {
        this.plugin = plugin;
    }

    public static void updateLore(Player p, String title) {
        final BankPlus plugin = JavaPlugin.getPlugin(BankPlus.class);
        if (!p.getOpenInventory().getTitle().equals(title)) return;

        final ConfigurationSection c = plugin.config().getConfigurationSection("Gui.Items");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (String items : c.getKeys(false)) {
                try {
                    ItemStack i = p.getOpenInventory().getItem(c.getConfigurationSection(items).getInt("Slot") - 1);
                    i.setItemMeta(ItemCreator.setLore(c.getConfigurationSection(items), i, p));
                } catch (NullPointerException | IllegalArgumentException | ArrayIndexOutOfBoundsException ignored) {
                }
            }
        });
    }

    public final Inventory getBank(Player p) {

        final int lines = guiLines(plugin.config().getInt("Gui.Lines"));
        
        final String s = plugin.config().getString("Gui.Title");
        String title;
        if (s == null) {
            title = "&c&l*CANNOT FIND TITLE*";
        } else {
            title = s;
        }

        Inventory inv = Bukkit.createInventory(null, lines, ChatUtils.color(title));

        final ConfigurationSection c = plugin.config().getConfigurationSection("Gui.Items");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (String items : c.getKeys(false)) {
                try {
                    inv.setItem(c.getConfigurationSection(items).getInt("Slot") - 1, ItemCreator.createItemStack(c.getConfigurationSection(items), p));
                } catch (ArrayIndexOutOfBoundsException ex) {
                    ChatUtils.consoleMessage("&a&lBank&9&lPlus &aSome items go arent set properly! Please fix it in the config!");
                    inv.addItem(ItemCreator.createItemStack(c.getConfigurationSection(items), p));
                }
            }

            if (plugin.config().getBoolean("Gui.Filler.Enabled")) {
                for (int i = 0; i < lines; i++)
                    if (inv.getItem(i) == null)
                        inv.setItem(i, ItemCreator.guiFiller());
            }
        });

        return inv;
    }

    private int guiLines(final int number) {
        switch (number) {
            case 1: return 9;
            case 2: return 18;
            case 3: return 27;
            default: return 36;
            case 5: return 45;
            case 6: return 54;
        }
    }

    public void openGui(Player p) {
        p.openInventory(getBank(p));
    }
}