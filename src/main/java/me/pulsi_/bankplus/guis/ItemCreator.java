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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemCreator {

    public static ItemStack createItemStack(ConfigurationSection c, Player p, BankPlus plugin) {

        EconomyManager economyManager = new EconomyManager(plugin);

        int cooldown = Integer.parseInt(plugin.getPlayers().getString("Interest-Cooldown"));

        ItemStack item;
        if (c.getString("Material").contains("HEAD-%PLAYER%")) {
            try {
                item = HeadUtils.getOwnerHead(p);
            } catch (NullPointerException exception) {
                item = new ItemStack(Material.BARRIER);
            } catch (IllegalArgumentException exception) {
                item = new ItemStack(Material.BARRIER);
            }
        } else if (c.getString("Material").startsWith("HEAD[")) {
                try {
                    String player = c.getString("Material").replace("HEAD[", "").replace("]", "");
                    item = HeadUtils.getNameHead(player);
                } catch (NullPointerException exception) {
                    item = new ItemStack(Material.BARRIER);
                } catch (IllegalArgumentException exception) {
                    item = new ItemStack(Material.BARRIER);
                }
        } else if (c.getString("Material").startsWith("HEAD-<")) {
            try {
                String textureValue = c.getString("Material").replace("HEAD-<", "").replace(">", "");
                String[] values = textureValue.split(",");
                String value1 = values[0];
                String value2 = values[1];
                item = HeadUtils.getTextureHead(value1, value2);
            } catch (NullPointerException exception) {
                item = new ItemStack(Material.BARRIER);
            } catch (IllegalArgumentException exception) {
                item = new ItemStack(Material.BARRIER);
            }
        } else {
            try {
                item = new ItemStack(Material.valueOf(c.getString("Material")));
            } catch (NullPointerException exception) {
                item = new ItemStack(Material.BARRIER);
            } catch (IllegalArgumentException exception) {
                item = new ItemStack(Material.BARRIER);
            }
        }

        ItemMeta itemMeta = item.getItemMeta();

        try {
            String displayName = c.getString("DisplayName").replace("%player_name%", p.getName())
                    .replace("%balance%", String.valueOf(economyManager.getPersonalBalance(p, plugin)))
                    .replace("%balance_formatted%", MethodUtils.format(economyManager.getPersonalBalance(p, plugin), plugin))
                    .replace("%balance_formatted_long%", MethodUtils.formatLong(economyManager.getPersonalBalance(p, plugin), plugin))
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
                        .replace("%balance%", String.valueOf(economyManager.getPersonalBalance(p, plugin)))
                        .replace("%balance_formatted%", MethodUtils.format(economyManager.getPersonalBalance(p, plugin), plugin))
                        .replace("%balance_formatted_long%", MethodUtils.formatLong(economyManager.getPersonalBalance(p, plugin), plugin))
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
                filler = new ItemStack(Material.valueOf(plugin.getConfiguration().getString("Gui.Filler.Material")));
            } catch (NullPointerException exception) {
                filler = new ItemStack(Material.BARRIER);
            } catch (IllegalArgumentException exception) {
                filler = new ItemStack(Material.BARRIER);
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