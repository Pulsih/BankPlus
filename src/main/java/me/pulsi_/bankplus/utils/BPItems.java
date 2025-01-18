package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.bankSystem.BankGui;
import me.pulsi_.bankplus.utils.texts.BPChat;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BPItems {

    public static final ItemStack UNKNOWN_ITEM = new ItemStack(Material.STONE);

    public static ItemStack createItemStack(String material) {
        ItemStack result = UNKNOWN_ITEM.clone();
        if (material != null && !material.isEmpty()) {
            if (material.startsWith("HEAD")) result = getHead(material);
            else {
                if (!material.contains(":")) result = new ItemStack(Material.valueOf(material));
                else {
                    String[] itemData = material.split(":");
                    try {
                        result = new ItemStack(Material.valueOf(itemData[0]), 1, Byte.parseByte(itemData[1]));
                    } catch (IllegalArgumentException e) {
                        BPLogger.warn("Could not update item because \"" + itemData[0] + "\" is not a valid material!");
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get the custom head based on the specified material.
     * @param material The material.
     * @return The custom head if found.
     */
    public static ItemStack getHead(String material) {
        if (material == null) return UNKNOWN_ITEM.clone();

        ItemStack item = UNKNOWN_ITEM.clone();
        if (material.startsWith("HEAD[")) {
            String player = material.replace("HEAD[", "").replace("]", "");
            item = BPHeads.getNameHead(player);
        } else if (material.startsWith("HEAD-<")) {
            String textureValue = material.replace("HEAD-<", "").replace(">", "");
            item = BPHeads.getValueHead(textureValue);
        } // If the head is the player's head, skip it and place it later when opening the bank.
        return item;
    }

    public static ItemStack getFiller(BankGui bankGui) {
        ItemStack item;
        try {
            String material = bankGui.getFillerMaterial();
            if (material.contains(":")) {
                String[] itemData = material.split(":");
                item = new ItemStack(Material.valueOf(itemData[0]), 1, Byte.parseByte(itemData[1]));
            } else item = new ItemStack(Material.valueOf(material));
        } catch (IllegalArgumentException e) {
            item = UNKNOWN_ITEM.clone();
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(BPChat.color("&f"));

        if (bankGui.isFillerGlowing()) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }
}