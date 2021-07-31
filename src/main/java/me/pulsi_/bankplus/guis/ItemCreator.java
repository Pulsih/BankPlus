package me.pulsi_.bankplus.guis;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.HeadUtils;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    private static final ItemStack barrier = new ItemStack(Material.BARRIER);
    
    public static ItemStack createItemStack(ConfigurationSection c, Player p, BankPlus plugin) {

        int cooldown = Integer.parseInt(plugin.getPlayers().getString("Interest-Cooldown"));

        ItemStack item;
        if (c.getString("Material").contains("HEAD-%PLAYER%")) {
            try {
                item = HeadUtils.getNameHead(p.getName(), new ItemStack(Material.PLAYER_HEAD));
            } catch (NoSuchFieldError er) {
                item = HeadUtils.getNameHead(p.getName(), new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal()));
            } catch (NullPointerException exception) {
                item = barrier;
            } catch (IllegalArgumentException exception) {
                item = barrier;
            }
        } else if (c.getString("Material").startsWith("HEAD[")) {
            String player = c.getString("Material").replace("HEAD[", "").replace("]", "");
            try {
                item = HeadUtils.getNameHead(player, new ItemStack(Material.PLAYER_HEAD));
            } catch (NoSuchFieldError er) {
                item = HeadUtils.getNameHead(player, new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal()));
            } catch (NoSuchMethodError er) {
                item = barrier;
            } catch (NullPointerException exception) {
                item = barrier;
            } catch (IllegalArgumentException exception) {
                item = barrier;
            }
        } else if (c.getString("Material").startsWith("HEAD-<")) {
            String textureValue = c.getString("Material").replace("HEAD-<", "").replace(">", "");
            try {
                item = HeadUtils.getValueHead(new ItemStack(Material.PLAYER_HEAD), textureValue);
            } catch (NoSuchFieldError er) {
                item = HeadUtils.getValueHead(new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal()), textureValue);
            } catch (NullPointerException exception) {
                item = barrier;
            } catch (IllegalArgumentException exception) {
                item = barrier;
            }
        } else {
            try {
                if (c.getString("Material").contains(":")) {
                    String[] itemData = c.getString("Material").split(":");
                    item = new ItemStack(Material.valueOf(itemData[0]), 1, Byte.parseByte(itemData[1]));
                } else {
                    item = new ItemStack(Material.valueOf(c.getString("Material")));
                }
            } catch (NullPointerException exception) {
                item = barrier;
            } catch (IllegalArgumentException exception) {
                item = barrier;
            }
        }

        ItemMeta itemMeta = item.getItemMeta();

        try {
            String displayName = c.getString("DisplayName")
                    .replace("%player_name%", p.getName())
                    .replace("%balance%", String.valueOf(EconomyManager.getPersonalBalance(p, plugin)))
                    .replace("%balance_formatted%", MethodUtils.format(EconomyManager.getPersonalBalance(p, plugin), plugin))
                    .replace("%balance_formatted_long%", MethodUtils.formatLong(EconomyManager.getPersonalBalance(p, plugin), plugin))
                    .replace("%interest_cooldown%", MethodUtils.formatTime(cooldown, plugin));
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                itemMeta.setDisplayName(ChatUtils.c(PlaceholderAPI.setPlaceholders(p, displayName)));
            } else {
                itemMeta.setDisplayName(ChatUtils.c(displayName));
            }
        } catch (NullPointerException exception) {
            itemMeta.setDisplayName(ChatUtils.c("&c&l*CANNOT FIND DISPLAYNAME*"));
        }

        List<String> lore = new ArrayList<>();
        if (c.getStringList("Lore") != null) {
            for (String lines : c.getStringList("Lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', lines)
                        .replace("%player_name%", p.getName())
                        .replace("%balance%", String.valueOf(EconomyManager.getPersonalBalance(p, plugin)))
                        .replace("%balance_formatted%", MethodUtils.format(EconomyManager.getPersonalBalance(p, plugin), plugin))
                        .replace("%balance_formatted_long%", MethodUtils.formatLong(EconomyManager.getPersonalBalance(p, plugin), plugin))
                        .replace("%interest_cooldown%", MethodUtils.formatTime(cooldown, plugin)));
            }
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                itemMeta.setLore(PlaceholderAPI.setPlaceholders(p, lore));
            } else {
                itemMeta.setLore(lore);
            }
        }

        if (c.getBoolean("Glowing")) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack guiFiller(BankPlus plugin) {

        ItemStack filler;
            try {
                if (plugin.getConfiguration().getString("Gui.Filler.Material").contains(":")) {
                    String[] itemData = plugin.getConfiguration().getString("Gui.Filler.Material").split(":");
                    filler = new ItemStack(Material.valueOf(itemData[0]), 1, Byte.parseByte(itemData[1]));
                } else {
                    filler = new ItemStack(Material.valueOf(plugin.getConfiguration().getString("Gui.Filler.Material")));
                }
            } catch (NullPointerException exception) {
                filler = barrier;
            } catch (IllegalArgumentException exception) {
                filler = barrier;
            }

        ItemMeta fillerMeta = filler.getItemMeta();

        try {
            fillerMeta.setDisplayName(ChatUtils.c(plugin.getConfiguration().getString("Gui.Filler.DisplayName")));
        } catch (NullPointerException exception) {
            fillerMeta.setDisplayName(ChatUtils.c("&c&l*CANNOT FIND DISPLAYNAME*"));
        }

        if (plugin.getConfiguration().getBoolean("Gui.Filler.Glowing")) {
            fillerMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            filler.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        filler.setItemMeta(fillerMeta);
        return filler;
    }
}