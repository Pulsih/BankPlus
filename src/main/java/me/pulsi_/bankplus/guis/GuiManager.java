package me.pulsi_.bankplus.guis;

import me.pulsi_.bankplus.utils.BPChat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiManager {

    public static Inventory getGuiBank(String identifier) {
        Inventory bank = Bukkit.createInventory(new BanksHolder(), BanksManager.getLines(identifier), BPChat.color(BanksManager.getTitle(identifier)));

        ConfigurationSection items = BanksManager.getItems(identifier);
        if (items == null) return bank;

        for (String item : items.getKeys(false)) {
            ConfigurationSection itemValues = items.getConfigurationSection(item);
            if (itemValues == null) continue;

            String material = itemValues.getString("Material");
            if (material == null) continue;

            ItemStack itemStack;
            if (material.startsWith("HEAD")) itemStack = ItemCreator.getHead(itemValues);
            else itemStack = ItemCreator.createItemStack(itemValues);
            if (itemStack == null) continue;

            setMeta(itemValues, itemStack);
            try {
                int slot = itemValues.getInt("Slot") - 1;
                bank.setItem(slot, itemStack);
            } catch (ArrayIndexOutOfBoundsException ex) {
                bank.addItem(itemStack);
            }
        }

        if (BanksManager.isFillerEnabled(identifier))
            for (int i = 0; i < bank.getSize(); i++)
                if (bank.getItem(i) == null) bank.setItem(i, ItemCreator.getFiller(identifier));

        return bank;
    }

    private static void setMeta(ConfigurationSection itemValues, ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        String displayName = itemValues.getString("Displayname");
        List<String> lore = new ArrayList<>();
        for (String lines : itemValues.getStringList("Lore")) lore.add(BPChat.color(lines));

        meta.setDisplayName(displayName == null ? "&c&l*CANNOT FIND DISPLAYNAME*" : BPChat.color(displayName));
        meta.setLore(lore);

        if (itemValues.getBoolean("Glowing")) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
    }
}