package me.pulsi_.bankplus.guis;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.MethodUtils;
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

    public static ItemStack createItemStack(ConfigurationSection c, Player p, EconomyManager economyManager) {

        ItemStack item = new ItemStack(Material.BARRIER);
        if (c.getString("Material") != null) {
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
            itemMeta.setDisplayName(ChatUtils.c(c.getString("DisplayName")
                    .replace("%player_name%", p.getName())
                    .replace("%balance%", String.valueOf(economyManager.getPersonalBalance(p)))
                    .replace("%balance_formatted%", String.valueOf(MethodUtils.formatter(economyManager.getPersonalBalance(p))))));
        } catch (NullPointerException exception) {
            itemMeta.setDisplayName(ChatUtils.c("&c&l*CANNOT FIND DISPLAYNAME*"));
        }

        List<String> lore = new ArrayList<>();
        if (c.getStringList("Lore") != null) {
            for (String lines : c.getStringList("Lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', lines)
                        .replace("%player_name%", p.getName())
                        .replace("%balance%", String.valueOf(economyManager.getPersonalBalance(p)))
                        .replace("%balance_formatted%", String.valueOf(MethodUtils.formatter(economyManager.getPersonalBalance(p)))));
            }
            itemMeta.setLore(lore);
        }

        if (c.getBoolean("Glowing")) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack guiFiller(BankPlus plugin) {

        ItemStack filler = new ItemStack(Material.BARRIER);
        if (plugin.getConfiguration().getString("Gui.Filler.Material") != null) {
            try {
                filler = new ItemStack(Material.valueOf(plugin.getConfiguration().getString("Gui.Filler.Material")));
            } catch (NullPointerException exception) {
                filler = new ItemStack(Material.BARRIER);
            } catch (IllegalArgumentException exception) {
                filler = new ItemStack(Material.BARRIER);
            }
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