package me.pulsi_.bankplus.guis;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.HeadUtils;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ItemUtils {

    private static final ItemStack UNKNOWN_MATERIAL = new ItemStack(Material.BARRIER);
    
    public static ItemStack createItemStack(ConfigurationSection c, Player p) {

        BankPlus plugin = JavaPlugin.getPlugin(BankPlus.class);

        long cooldown = 0;
        if (Values.CONFIG.isInterestEnabled()) cooldown = Interest.interestCooldown.get(0);

        String material = c.getString("Material");
        ItemStack item = getItem(material, c, p);
        ItemMeta meta = item.getItemMeta();

        String displayName = c.getString("DisplayName");
        setDisplayname(displayName, meta, plugin.isPlaceholderAPIHooked(), p);

        setLore(c, plugin.isPlaceholderAPIHooked(), meta, p, cooldown);

        if (c.getBoolean("Glowing")) {
            meta.addEnchant(Enchantment.DURABILITY, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack guiFiller() {
        ItemStack filler;
        try {
            final String material = Values.CONFIG.getGuiFillerMaterial();
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

        String displayName = Values.CONFIG.getGuiFillerDisplayname();
        if (displayName == null) {
            fillerMeta.setDisplayName(ChatUtils.color("&c&l*CANNOT FIND DISPLAYNAME*"));
        } else {
            fillerMeta.setDisplayName(ChatUtils.color(displayName));
        }

        if (Values.CONFIG.isGuiFillerGlowing()) {
            fillerMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            filler.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        filler.setItemMeta(fillerMeta);
        return filler;
    }

    public static ItemMeta setLore(ConfigurationSection c, ItemStack i, Player p) {
        BankPlus plugin = JavaPlugin.getPlugin(BankPlus.class);
        ItemMeta itemMeta = i.getItemMeta();

        List<String> lore = new ArrayList<>();
        for (String lines : c.getStringList("Lore"))
            lore.add(ChatUtils.color(lines));

        if (plugin.isPlaceholderAPIHooked()) {
            itemMeta.setLore(PlaceholderAPI.setPlaceholders(p, lore));
        } else {
            itemMeta.setLore(lore);
        }
        return itemMeta;
    }

    private static ItemStack getItem(String material, ConfigurationSection c, Player p) {
        ItemStack item;
        if (material == null) return UNKNOWN_MATERIAL;
        if (material.contains("HEAD-%PLAYER%")) {
            try {
                item = HeadUtils.getNameHead(p.getName(), new ItemStack(Material.PLAYER_HEAD));
            } catch (NoSuchFieldError er) {
                item = HeadUtils.getNameHead(p.getName(), new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal()));
            } catch (IllegalArgumentException e) {
                item = UNKNOWN_MATERIAL;
            }
        } else if (material.startsWith("HEAD[")) {
            final String player = c.getString("Material").replace("HEAD[", "").replace("]", "");
            try {
                item = HeadUtils.getNameHead(player, new ItemStack(Material.PLAYER_HEAD));
            } catch (NoSuchFieldError er) {
                item = HeadUtils.getNameHead(player, new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal()));
            } catch (IllegalArgumentException e) {
                item = UNKNOWN_MATERIAL;
            }
        } else if (material.startsWith("HEAD-<")) {
            final String textureValue = c.getString("Material").replace("HEAD-<", "").replace(">", "");
            try {
                item = HeadUtils.getValueHead(new ItemStack(Material.PLAYER_HEAD), textureValue);
            } catch (NoSuchFieldError er) {
                item = HeadUtils.getValueHead(new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal()), textureValue);
            } catch (IllegalArgumentException e) {
                item = UNKNOWN_MATERIAL;
            }
        } else {
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
        }
        int amount = c.getInt("Amount");
        if (amount > 1) item.setAmount(amount);

        return item;
    }

    private static void setDisplayname(String displayName, ItemMeta itemMeta, boolean isPlaceholderApiHooked, Player p) {
        if (displayName == null) {
            itemMeta.setDisplayName(ChatUtils.color("&c&l*CANNOT FIND DISPLAYNAME*"));
        } else {
            if (isPlaceholderApiHooked) {
                itemMeta.setDisplayName(ChatUtils.color(PlaceholderAPI.setPlaceholders(p, displayName)));
            } else {
                itemMeta.setDisplayName(ChatUtils.color(displayName));
            }
        }
    }

    private static void setLore(ConfigurationSection c, boolean isPlaceholderApiHooked, ItemMeta meta, Player p, long cooldown) {
        List<String> lore = new ArrayList<>();
        for (String lines : c.getStringList("Lore")) {
            lore.add(ChatUtils.color(lines
                    .replace("%player_name%", p.getName())
                    .replace("%balance%", Methods.formatCommas(EconomyManager.getInstance().getBankBalance(p)))
                    .replace("%balance_formatted%", Methods.format(EconomyManager.getInstance().getBankBalance(p)))
                    .replace("%balance_formatted_long%", Methods.formatLong(EconomyManager.getInstance().getBankBalance(p)))
                    .replace("%interest_cooldown%", Methods.formatTime((int) cooldown))
            ));
        }
        if (isPlaceholderApiHooked) {
            meta.setLore(PlaceholderAPI.setPlaceholders(p, lore));
        } else {
            meta.setLore(lore);
        }
    }
}