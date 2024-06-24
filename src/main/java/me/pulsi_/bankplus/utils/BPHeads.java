package me.pulsi_.bankplus.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class BPHeads {

    private static ItemStack skull = null;

    /**
     * Get a skull with the head of that player.
     * @param owner The player name.
     * @return A skull with that player head.
     */
    public static ItemStack getNameHead(String owner) {
        ItemStack skull = BPHeads.skull.clone();
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(owner);
        skull.setItemMeta(skullMeta);
        return skull;
    }

    /**
     * Get a skull with the given texture.
     * @param value The textureValue.
     * @return A skull with that texture.
     */
    public static ItemStack getValueHead(String value) {
        ItemStack skull = BPHeads.skull.clone();
        UUID id = new UUID(value.hashCode(), value.hashCode());
        try {
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
            PlayerProfile profile = skullMeta.getPlayerProfile();

            ProfileProperty property = new ProfileProperty("textures", value);
            profile.setProperty(property);

            skullMeta.setPlayerProfile(profile);
            skull.setItemMeta(skullMeta);
            return skull;
        } catch (Exception e) {
            return Bukkit.getUnsafe().modifyItemStack(skull, "{SkullOwner:{Id:\"" + id + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}");
        }
    }

    /**
     * Load the skull ItemStack based on the server version.
     */
    public static void loadSkullBasedOnVersion() {
        try {
            skull = new ItemStack(Material.PLAYER_HEAD);
        } catch (NoSuchFieldError er) {
            skull = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) SkullType.PLAYER.ordinal());
        }
    }
}