package me.pulsi_.bankplus.bankSystem;

import me.pulsi_.bankplus.utils.BPHeads;
import me.pulsi_.bankplus.utils.BPLogger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BPItems {

    public static final ItemStack UNKNOWN_ITEM = new ItemStack(Material.STONE);

    public static ItemStack createItemStack(String material) {
        ItemStack result = UNKNOWN_ITEM.clone();
        if (material != null && !material.isEmpty()) {
            if (material.startsWith("HEAD")) result = getHead(material);
            else {
                if (!material.contains(":")) result = new ItemStack(Material.valueOf(material));
                else {
                    String[] itemData = material.split(":");
                    try {
                        result = new ItemStack(Material.valueOf(itemData[0]), 1, Byte.parseByte(itemData[1]));
                    } catch (IllegalArgumentException e) {
                        BPLogger.warn("Could not update item because \"" + itemData[0] + "\" is not a valid material!");
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get the custom head based on the specified material.
     *
     * @param material The material.
     * @return The custom head if found.
     */
    public static ItemStack getHead(String material) {
        if (material == null) return UNKNOWN_ITEM.clone();

        ItemStack item = UNKNOWN_ITEM.clone();
        if (material.startsWith("HEAD[")) {
            String player = material.replace("HEAD[", "").replace("]", "");
            item = BPHeads.getNameHead(player);
        } else if (material.startsWith("HEAD-<")) {
            String textureValue = material.replace("HEAD-<", "").replace(">", "");
            item = BPHeads.getValueHead(textureValue);
        } // If the head is the player's head, skip it and place it later when opening the bank.
        return item;
    }

    /**
     * Represent a bankplus item that can also be a head.
     */
    public static class BPItem {

        private ItemStack item; // The item stack containing displayname, material etc..
        private HashMap<Integer, List<String>> lore = new HashMap<>();
        private boolean playerHead;

        public ItemStack getItem() {
            return item;
        }

        public HashMap<Integer, List<String>> getLore() {
            return lore;
        }

        public boolean isPlayerHead() {
            return playerHead;
        }

        public void setItem(ItemStack item) {
            this.item = item;
        }

        public void setLore(HashMap<Integer, List<String>> lore) {
            this.lore = lore;
        }

        public void setPlayerHead(boolean playerHead) {
            this.playerHead = playerHead;
        }

        public static BPItem loadBankItem(ConfigurationSection itemSection) {
            item = BankUtils.getItemStackFromSection(itemSection);
            actions = itemSection.getStringList("Actions");
            lore = BankUtils.getLevelLore(itemSection);

            String material = itemSection.getString(BankUtils.MATERIAL_KEY);
            if (material != null && material.equalsIgnoreCase("head-%player%")) playerHead = true;
            return this;
        }
    }

    /**
     * Represent a bankplus item that has actions to be run when clicked on the gui.
     */
    public static class BPGuiItem extends BPItem {

        private List<String> actions = new ArrayList<>(); // Actions on click.

        public List<String> getActions() {
            return actions;
        }

        public void setActions(List<String> actions) {
            this.actions = actions;
        }

        public static BPGuiItem loadBankItem(ConfigurationSection itemSection) {
            BPGuiItem guiItem = new BPGuiItem();
            guiItem.setItem(BankUtils.getItemStackFromSection(itemSection));
            guiItem.setLore(BankUtils.getLevelLore(itemSection));
            actions = itemSection.getStringList("Actions");

            String material = itemSection.getString(BankUtils.MATERIAL_KEY);
            if (material != null && material.equalsIgnoreCase("head-%player%")) playerHead = true;
            return this;
        }
    }
}