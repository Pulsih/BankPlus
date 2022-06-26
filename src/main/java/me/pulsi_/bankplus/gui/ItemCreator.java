package me.pulsi_.bankplus.gui;

import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.HeadUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemCreator {

    private static final ItemStack UNKNOWN_MATERIAL = new ItemStack(Material.BARRIER);

    public static ItemStack createItemStack(ConfigurationSection c) {
        String material = c.getString("Material");
        ItemStack item = getItem(material, c);
        return item;
    }

    public static ItemStack getHead(ConfigurationSection c, Player p) {
        String material = c.getString("Material");
        ItemStack item = null;
        try {
            if (material.contains("HEAD-%PLAYER%")) {
                try {
                    item = HeadUtils.getNameHead(p.getName(), new ItemStack(Material.PLAYER_HEAD));
                } catch (NoSuchFieldError er) {
                    item = HeadUtils.getNameHead(p.getName(), new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal()));
                }
            } else if (material.startsWith("HEAD[")) {
                String player = material.replace("HEAD[", "").replace("]", "");
                try {
                    item = HeadUtils.getNameHead(player, new ItemStack(Material.PLAYER_HEAD));
                } catch (NoSuchFieldError er) {
                    item = HeadUtils.getNameHead(player, new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal()));
                }
            } else if (material.startsWith("HEAD-<")) {
                String textureValue = material.replace("HEAD-<", "").replace(">", "");
                try {
                    item = HeadUtils.getValueHead(new ItemStack(Material.PLAYER_HEAD), textureValue);
                } catch (NoSuchFieldError er) {
                    item = HeadUtils.getValueHead(new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal()), textureValue);
                }
            }
        } catch (IllegalArgumentException e) {
            item = UNKNOWN_MATERIAL;
        }

        return item;
    }

    public static ItemStack getGuiFiller() {
        ItemStack filler;
        try {
            final String material = Values.BANK.getGuiFillerMaterial();
            if (material.contains(":")) {
                String[] itemData = material.split(":");
                filler = new ItemStack(Material.valueOf(itemData[0]), 1, Byte.parseByte(itemData[1]));
            } else {
                filler = new ItemStack(Material.valueOf(material));
            }
        } catch (IllegalArgumentException e) {
            filler = UNKNOWN_MATERIAL;
        }

        ItemMeta fillerMeta = filler.getItemMeta();
        String displayName = Values.BANK.getGuiFillerDisplayname();
        if (displayName == null) {
            fillerMeta.setDisplayName(ChatUtils.color("&c&l*CANNOT FIND DISPLAYNAME*"));
        } else {
            fillerMeta.setDisplayName(ChatUtils.color(displayName));
        }

        if (Values.BANK.isGuiFillerGlowing()) {
            fillerMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            filler.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        filler.setItemMeta(fillerMeta);
        return filler;
    }

    private static ItemStack getItem(String material, ConfigurationSection c) {
        if (material == null) return UNKNOWN_MATERIAL;

        ItemStack item;
        try {
            if (material.contains(":")) {
                String[] itemData = material.split(":");
                item = new ItemStack(Material.valueOf(itemData[0]), 1, Byte.parseByte(itemData[1]));
            } else {
                item = new ItemStack(Material.valueOf(material));
            }
        } catch (IllegalArgumentException e) {
            item = UNKNOWN_MATERIAL;
        }

        int amount = c.getInt("Amount");
        if (amount > 1) item.setAmount(amount);

        return item;
    }

    public static void setLore(ConfigurationSection c, ItemStack i) {
        List<String> lore = new ArrayList<>();
        for (String lines : c.getStringList("Lore")) lore.add(ChatUtils.color(lines));

        ItemMeta meta = i.getItemMeta();
        meta.setLore(lore);

        if (c.getBoolean("Glowing")) {
            meta.addEnchant(Enchantment.DURABILITY, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        i.setItemMeta(meta);
    }

    public static void setDisplayname(ConfigurationSection c, ItemStack i) {
        String displayName = c.getString("DisplayName");
        ItemMeta meta = i.getItemMeta();

        if (displayName == null) {
            meta.setDisplayName(ChatUtils.color("&c&l*CANNOT FIND DISPLAYNAME*"));
        } else {
            meta.setDisplayName(ChatUtils.color(displayName));
        }
        i.setItemMeta(meta);
    }
}