package me.pulsi_.bankplus.guis;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public class GuiBankHolder implements InventoryHolder {

    private final int lines;
    private final Inventory guiBank;
    private final BankPlus plugin = JavaPlugin.getPlugin(BankPlus.class);

    public GuiBankHolder(int lines, String title) {
        this.lines = lines;
        this.guiBank = Bukkit.createInventory(this, guiLines(lines), ChatUtils.color(title));
    }

    public void openBank(Player p) {
        GuiBankHolder gui = getEnchanterHolder();
        gui.buildBank(p);
        p.openInventory(gui.getInventory());
    }

    private void buildBank(Player p) {
        ConfigurationSection c = plugin.config().getConfigurationSection("Gui.Items");
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(BankPlus.class), () -> {
            for (String items : c.getKeys(false)) {
                try {
                    guiBank.setItem(c.getConfigurationSection(items).getInt("Slot") - 1,
                            ItemUtils.createItemStack(c.getConfigurationSection(items), p));
                } catch (ArrayIndexOutOfBoundsException ex) {
                    ChatUtils.consoleMessage("&8[&a&lBank&9&lPlus&8] &cSome items arent set properly in the GUI! Please fix it in the config!");
                    guiBank.addItem(ItemUtils.createItemStack(c.getConfigurationSection(items), p));
                }
            }
            if (Values.CONFIG.isGuiFillerEnabled())
                for (int i = 0; i < guiLines(lines); i++)
                    if (guiBank.getItem(i) == null) guiBank.setItem(i, ItemUtils.guiFiller());
        });
    }

    private int guiLines(int number) {
        switch (number) {
            case 1:
                return 9;
            case 2:
                return 18;
            default:
                return 27;
            case 4:
                return 36;
            case 5:
                return 45;
            case 6:
                return 54;
        }
    }

    public static GuiBankHolder getEnchanterHolder() {
        return new GuiBankHolder(Values.CONFIG.getGuiLines(), Values.CONFIG.getGuiTitle());
    }

    @Override
    public Inventory getInventory() {
        return guiBank;
    }
}