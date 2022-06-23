package me.pulsi_.bankplus.gui;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiHolder implements InventoryHolder {

    public static final Map<Player, BukkitTask> tasks = new HashMap<>();
    private static Inventory guiBank;

    public void openBank(Player p) {
        GuiHolder holder = new GuiHolder();
        holder.loadBank();
        p.openInventory(holder.getInventory());
        holder.placeHeads(p);

        int delay = Values.BANK.getGuiUpdateDelay();
        if (delay != 0) {
            tasks.put(p, Bukkit.getScheduler().runTaskTimer(BankPlus.getInstance(), () -> updateLore(p), 0, delay * 20L));
        } else {
            updateLore(p);
        }
    }

    public void loadBank() {
        guiBank = Bukkit.createInventory(new GuiHolder(), guiLines(Values.BANK.getGuiLines()), ChatUtils.color(Values.BANK.getGuiTitle()));

        ConfigurationSection itemsConfiguration = Values.BANK.getGuiItems();
        for (String items : itemsConfiguration.getKeys(false)) {
            ConfigurationSection itemsList = itemsConfiguration.getConfigurationSection(items);

            try {
                guiBank.setItem(itemsList.getInt("Slot") - 1, ItemUtils.createItemStack(itemsList));
            } catch (ArrayIndexOutOfBoundsException ex) {
                guiBank.addItem(ItemUtils.createItemStack(itemsList));
            }
        }

        if (Values.BANK.isGuiFillerEnabled())
            for (int i = 0; i < guiLines(Values.BANK.getGuiLines()); i++)
                if (guiBank.getItem(i) == null) guiBank.setItem(i, ItemUtils.getGuiFiller());
    }

    private void placeHeads(Player p) {
        ConfigurationSection c = Values.BANK.getGuiItems();
        for (String items : c.getKeys(false)) {
            ConfigurationSection itemsList = c.getConfigurationSection(items);

            String material = itemsList.getString("Material");
            if (material == null || !material.startsWith("HEAD")) continue;

            try {
                guiBank.setItem(itemsList.getInt("Slot") - 1, ItemUtils.getHead(itemsList, p));
            } catch (ArrayIndexOutOfBoundsException e) {
                guiBank.addItem(ItemUtils.getHead(itemsList, p));
            }
        }
    }

    private void updateLore(Player p) {
        Inventory bank = p.getOpenInventory().getTopInventory();
        if (!(bank.getHolder() instanceof GuiHolder)) return;

        ConfigurationSection c = Values.BANK.getGuiItems();
        for (String items : c.getKeys(false)) {
            ConfigurationSection itemsList = c.getConfigurationSection(items);

            ItemStack i = bank.getItem(itemsList.getInt("Slot") - 1);
            if (i != null) setLore(itemsList, i, p);
        }
    }

    private void setLore(ConfigurationSection c, ItemStack i, Player p) {
        ItemMeta meta = i.getItemMeta();

        String displayName = c.getString("DisplayName");
        if (displayName == null) displayName = "&c&l*CANNOT FIND DISPLAYNAME*";

        List<String> lore = new ArrayList<>();
        for (String lines : c.getStringList("Lore")) lore.add(ChatUtils.color(lines));

        if (BankPlus.getInstance().isPlaceholderAPIHooked()) {
            meta.setDisplayName(PlaceholderAPI.setPlaceholders(p, ChatUtils.color(displayName)));
            meta.setLore(PlaceholderAPI.setPlaceholders(p, lore));
        } else {
            meta.setDisplayName(ChatUtils.color(displayName));
            meta.setLore(lore);
        }

        i.setItemMeta(meta);
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

    @Override
    public Inventory getInventory() {
        return guiBank;
    }
}