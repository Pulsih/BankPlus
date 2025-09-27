package me.pulsi_.bankplus.bankSystem;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class about ItemStacks and Material generation.
 */
public class BPItems {

    public static final Component DISPLAYNAME_NOT_FOUND = BPChat.color("<red>Displayname not found.");

    public static final ItemStack UNKNOWN_ITEM = new ItemStack(Material.STONE);

    public static final String MATERIAL_KEY = "Material";
    public static final String AMOUNT_KEY = "Amount";
    public static final String DISPLAYNAME_KEY = "Displayname";
    public static final String LORE_KEY = "Lore";
    public static final String ITEM_FLAGS_KEY = "ItemFlags";
    public static final String GLOWING_KEY = "Glowing";
    public static final String CUSTOM_MODEL_DATA_KEY = "CustomModelData";

    /**
     * Automatically get the item stack with all attributes from the item section. (displayname, lore, glowing, flags etc...)
     *
     * @param itemSection The item section in the config.
     * @return An ItemStack.
     */
    public static ItemStack getItemStackFromSection(ConfigurationSection itemSection) {
        if (itemSection == null) return BPItems.UNKNOWN_ITEM.clone();

        ItemStack item = BPItems.createItemStack(itemSection.getString(MATERIAL_KEY));

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Component italicRemover = MiniMessage.miniMessage().deserialize("<!italic>");

        String displayname = itemSection.getString(DISPLAYNAME_KEY);
        if (displayname != null) meta.displayName(italicRemover.append(BPChat.color(displayname)));

        List<Component> lore = new ArrayList<>();
        for (String lines : itemSection.getStringList(LORE_KEY)) lore.add(italicRemover.append(BPChat.color(lines)));
        meta.lore(lore);

        for (String flag : itemSection.getStringList(ITEM_FLAGS_KEY)) {
            try {
                meta.addItemFlags(ItemFlag.valueOf(flag));
            } catch (IllegalArgumentException e) {
                BPLogger.Console.warn("Could not set item flag \"" + flag + "\" to item \"" + itemSection + "\" because it's not a valid item flag.");
            }
        }

        int modelData = itemSection.getInt(CUSTOM_MODEL_DATA_KEY);
        if (modelData > 0) {
            try {
                meta.setCustomModelData(modelData);
            } catch (NoSuchMethodError e) { // Do that to add support for older minecraft versions.
                BPLogger.Console.warn("Cannot set custom model data to the item: \"" + displayname + "\"&e. Custom model data is only available on 1.14.4+ servers!");
            }
        }
        item.setItemMeta(meta);

        if (itemSection.getBoolean(GLOWING_KEY)) item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return item;
    }

    /**
     * Automatically get the item stack with all attributes, except lore, from the item section. (displayname, glowing, flags etc...)
     * For example the gui items doesn't need to have the lore, since it is got in a different way.
     *
     * @param itemSection The item section in the config.
     * @return An ItemStack.
     */
    public static ItemStack getNoLoreItemStackFromSection(ConfigurationSection itemSection) {
        if (itemSection == null) return BPItems.UNKNOWN_ITEM.clone();

        ItemStack item = BPItems.createItemStack(itemSection.getString(MATERIAL_KEY));

        int amount = itemSection.getInt(AMOUNT_KEY);
        if (amount > 1) item.setAmount(amount);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String displayname = itemSection.getString(DISPLAYNAME_KEY);
        if (displayname != null) meta.displayName(BPChat.color(displayname));

        for (String flag : itemSection.getStringList(ITEM_FLAGS_KEY)) {
            try {
                meta.addItemFlags(ItemFlag.valueOf(flag));
            } catch (IllegalArgumentException e) {
                BPLogger.Console.warn("Could not set item flag \"" + flag + "\" to item \"" + itemSection + "\" because it's not a valid item flag.");
            }
        }

        int modelData = itemSection.getInt(CUSTOM_MODEL_DATA_KEY);
        if (modelData > 0) {
            try {
                meta.setCustomModelData(modelData);
            } catch (NoSuchMethodError e) { // Do that to add support for older minecraft versions.
                BPLogger.Console.warn("Cannot set custom model data to the item: \"" + displayname + "\"&e. Custom model data is only available on 1.14.4+ servers!");
            }
        }
        item.setItemMeta(meta);

        if (itemSection.getBoolean(GLOWING_KEY)) item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return item;
    }

    /**
     * From the material String, process it and create an item stack or head. (only material).
     *
     * @param material The material string.
     * @return An item, or STONE.
     */
    public static ItemStack createItemStack(String material) {
        ItemStack result = UNKNOWN_ITEM.clone();
        if (material == null || material.isEmpty()) return result;

        if (material.startsWith("HEAD")) {
            if (material.startsWith("HEAD[")) { // Head from player name.
                String player = material.replace("HEAD[", "").replace("]", "");
                return getNameHead(player);
            } else if (material.startsWith("HEAD-<")) { // Head from texture value.
                String textureValue = material.replace("HEAD-<", "").replace(">", "");
                return getValueHead(textureValue);
            } // If the head is the player's head, skip it and place it later when opening the bank.
            return result;
        }

        if (!material.contains(":")) result = new ItemStack(Material.valueOf(material));
        else {
            String[] itemData = material.split(":");
            try {
                result = new ItemStack(Material.valueOf(itemData[0]), 1, Byte.parseByte(itemData[1]));
            } catch (IllegalArgumentException e) {
                BPLogger.Console.warn("Could not update item because \"" + itemData[0] + "\" is not a valid material!");
            }
        }
        return result;
    }

    /**
     * Get a skull with the head of that player.
     *
     * @param owner The player name.
     * @return A skull with that player head.
     */
    public static ItemStack getNameHead(String owner) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
        head.setItemMeta(meta);
        return head;
    }

    /**
     * Get a skull with the given texture.
     *
     * @param value The textureValue.
     * @return A skull with that texture.
     */
    public static ItemStack getValueHead(String value) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        UUID id = new UUID(value.hashCode(), value.hashCode());

        try {
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (!skullMeta.hasOwner()) skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer("Pulsi_"));
            PlayerProfile profile = skullMeta.getPlayerProfile();
            ProfileProperty property = new ProfileProperty("textures", value);
            profile.setProperty(property);

            skullMeta.setPlayerProfile(profile);
            head.setItemMeta(skullMeta);
            return head;
        } catch (Error | Exception e) {
            BPLogger.Console.warn(e, "Skull exception");
            return Bukkit.getUnsafe().modifyItemStack(head, "{SkullOwner:{Id:\"" + id + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}");
        }
    }
}