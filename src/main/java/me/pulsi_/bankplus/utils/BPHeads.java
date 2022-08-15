package me.pulsi_.bankplus.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class BPHeads {

    public static ItemStack getNameHead(String owner, ItemStack baseSkull) {
        SkullMeta skullMeta = (SkullMeta) baseSkull.getItemMeta();
        skullMeta.setOwner(owner);
        baseSkull.setItemMeta(skullMeta);
        return baseSkull;
    }

    public static ItemStack getValueHead(ItemStack skull, String value) {
        UUID hashAsId = new UUID(value.hashCode(), value.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(skull, "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}");
    }
}