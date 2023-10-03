package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.bankSystem.Bank;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BPItems {

    public static final ItemStack UNKNOWN_ITEM = new ItemStack(Material.STONE);

    public static ItemStack createItemStack(ConfigurationSection values) {
        String material = values.getString("Material");

        ItemStack result = UNKNOWN_ITEM;
        if (material != null) {
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

        int amount = values.getInt("amount");
        if (amount > 1) result.setAmount(amount);
        return result;
    }

    public static ItemStack getHead(String material) {
        if (material == null) return UNKNOWN_ITEM;

        ItemStack item = UNKNOWN_ITEM;
        if (material.startsWith("HEAD[")) {
            String player = material.replace("HEAD[", "").replace("]", "");
            try {
                item = BPHeads.getNameHead(player, new ItemStack(Material.PLAYER_HEAD));
            } catch (NoSuchFieldError er) {
                item = BPHeads.getNameHead(player, new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal()));
            }
        } else if (material.startsWith("HEAD-<")) {
            String textureValue = material.replace("HEAD-<", "").replace(">", "");
            try {
                item = BPHeads.getValueHead(new ItemStack(Material.PLAYER_HEAD), textureValue);
            } catch (NoSuchFieldError er) {
                item = BPHeads.getValueHead(new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal()), textureValue);
            }
        }
        return item;
    }

    public static ItemStack getHead(Player p) {
        ItemStack item;
        try {
            item = BPHeads.getNameHead(p.getName(), new ItemStack(Material.PLAYER_HEAD));
        } catch (NoSuchFieldError er) {
            item = BPHeads.getNameHead(p.getName(), new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal()));
        }
        return item;
    }

    public static ItemStack getFiller(String material, boolean glowing) {
        ItemStack item;
        try {
            if (material.contains(":")) {
                String[] itemData = material.split(":");
                item = new ItemStack(Material.valueOf(itemData[0]), 1, Byte.parseByte(itemData[1]));
            } else item = new ItemStack(Material.valueOf(material));
        } catch (IllegalArgumentException e) {
            item = UNKNOWN_ITEM;
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("");

        if (glowing) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getFiller(Bank bank) {
        ItemStack item;
        try {
            String material = bank.getFillerMaterial();
            if (material.contains(":")) {
                String[] itemData = material.split(":");
                item = new ItemStack(Material.valueOf(itemData[0]), 1, Byte.parseByte(itemData[1]));
            } else item = new ItemStack(Material.valueOf(material));
        } catch (IllegalArgumentException e) {
            item = UNKNOWN_ITEM;
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(BPChat.color("&f"));

        if (bank.isFillerGlowing()) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }
}