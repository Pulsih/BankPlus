package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class SetItemNbt {

    static boolean namespacedKeyExist = true;

    static {
        try {
            new NamespacedKey(BankPlus.INSTANCE(), "key");
        } catch (NoClassDefFoundError exception) {
            namespacedKeyExist = false;
        }

    }

    public static void setNbt(ItemStack item, String key, String value) {
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            setNbt(meta, key, value);
            item.setItemMeta(meta);
        }
    }

    public static void setNbt(ItemMeta itemMeta, String key, String value) {
        if (!namespacedKeyExist)
            return;
        try {
            itemMeta.getCustomTagContainer().setCustomTag(new NamespacedKey(BankPlus.INSTANCE(), key), ItemTagType.STRING, value);
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            itemMeta.getPersistentDataContainer().set(new NamespacedKey(BankPlus.INSTANCE(), key), PersistentDataType.STRING, value);
        }
    }

    public static boolean isNamespacedKeyExist() {
        return namespacedKeyExist;
    }

    @Nullable
    public static String getNbt(ItemMeta itemMeta, String key) {
        if (!namespacedKeyExist)
            return null;
        try {
            return itemMeta.getCustomTagContainer().getCustomTag(new NamespacedKey(BankPlus.INSTANCE(), key), ItemTagType.STRING);
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            return itemMeta.getPersistentDataContainer().get(new NamespacedKey(BankPlus.INSTANCE(), key), PersistentDataType.STRING);
        }
    }


}
