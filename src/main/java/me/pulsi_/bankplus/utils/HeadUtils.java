package me.pulsi_.bankplus.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class HeadUtils {

    private static final ItemStack baseSkull = new ItemStack(Material.PLAYER_HEAD);

    public static ItemStack getOwnerHead(OfflinePlayer owner) {
        if(owner.isOnline()) {
            return getPlayerHead(owner.getPlayer().getPlayerProfile());
        } else {
            SkullMeta skullMeta = (SkullMeta) baseSkull.getItemMeta();
            skullMeta.setOwningPlayer(owner);
            baseSkull.setItemMeta(skullMeta);
            return baseSkull;
        }
    }

    public static ItemStack getNameHead(String owner) {
        SkullMeta skullMeta = (SkullMeta) baseSkull.getItemMeta();
        skullMeta.setOwner(owner);
        baseSkull.setItemMeta(skullMeta);
        return baseSkull;
    }

    public static ItemStack getUUIDHead(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) {
            return getPlayerHead(player.getPlayerProfile());
        }
        return getPlayerHead(Bukkit.createProfile(uuid));
    }

    public static ItemStack getTextureHead(String textureValue, String textureSignature) {
        return getPlayerHead(UUID.randomUUID(), textureValue, textureSignature);
    }

    private static ItemStack getPlayerHead(UUID uuid, String textureValue, String textureSignature) {
        PlayerProfile playerProfile = Bukkit.createProfile(uuid);
        playerProfile.setProperty(new ProfileProperty("textures", textureValue, textureSignature));
        return getPlayerHead(playerProfile);
    }

    private static ItemStack getPlayerHead(PlayerProfile playerProfile) {
        SkullMeta skullMeta = (SkullMeta) baseSkull.getItemMeta();
        skullMeta.setPlayerProfile(playerProfile);
        baseSkull.setItemMeta(skullMeta);
        return baseSkull;
    }
}