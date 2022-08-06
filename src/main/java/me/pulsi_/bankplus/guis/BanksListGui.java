package me.pulsi_.bankplus.guis;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BanksListGui {

    public static HashMap<String, String> getBankFromSlot = new HashMap<>();
    public static final String multipleBanksGuiID = "MultipleBanksGui";
    public static String multipleBanksGuiName;
    private static final Material DEFAULT_MATERIAL = Material.CHEST;

    public static void openMultipleBanksGui(Player p) {
        BPMethods.playSound("PERSONAL", p);
        Inventory baseBanksListGui = BanksHolder.bankGetter.get(multipleBanksGuiID);
        Inventory banksListGui = Bukkit.createInventory(new BanksHolder(), baseBanksListGui.getSize(), multipleBanksGuiName);
        banksListGui.setContents(baseBanksListGui.getContents());
        placeBanks(banksListGui, p);

        BanksHolder.openedBank.put(p.getUniqueId(), multipleBanksGuiID);
        p.openInventory(banksListGui);

        long delay = Values.MULTIPLE_BANKS.getUpdateDelay();
        if (delay == 0) updateMeta(p);
        else BanksHolder.tasks.put(p.getUniqueId(), Bukkit.getScheduler().runTaskTimer(BankPlus.getInstance(), () -> updateMeta(p), 0, delay));
    }

    public static void loadMultipleBanksGui() {
        String title = Values.MULTIPLE_BANKS.getBanksGuiTitle();
        if (title == null) title = "&c&l * TITLE NOT FOUND *";

        multipleBanksGuiName = BPChat.color(title);
        Inventory multipleBanksGui = Bukkit.createInventory(new BanksHolder(), Values.MULTIPLE_BANKS.getBanksGuiLines(), multipleBanksGuiName);

        if (Values.MULTIPLE_BANKS.isFillerEnabled()) {
            ItemStack filler = ItemCreator.getFiller(Values.MULTIPLE_BANKS.getFillerMaterial(), Values.MULTIPLE_BANKS.isFillerGlowing());
            for (int i = 0; i < multipleBanksGui.getSize(); i++) multipleBanksGui.setItem(i, filler);
        }
        BanksHolder.bankGetter.put("MultipleBanksGui", multipleBanksGui);
    }

    private static void placeBanks(Inventory banksListGui, Player p) {
        int slot = 0;
        for (String bankName : BanksManager.getBankNames()) {
            ItemStack bankItem;
            Material material;

            boolean glow = false;
            ConfigurationSection section = BanksManager.getBanksGuiItemSection(bankName);
            if (section == null) material = DEFAULT_MATERIAL;
            else {
                try {
                    if (BanksManager.isAvailable(p, bankName)) {
                        material = Material.valueOf(section.getString("Available.Material"));
                        glow = section.getBoolean("Available.Glowing");
                    } else {
                        material = Material.valueOf(section.getString("Unavailable.Material"));
                        glow = section.getBoolean("Unavailable.Glowing");
                    }
                } catch (IllegalArgumentException e) {
                    material = ItemCreator.UNKNOWN_MATERIAL;
                }
            }
            bankItem = new ItemStack(material);
            glow(bankItem, glow);
            banksListGui.setItem(slot, bankItem);
            getBankFromSlot.put(p.getName() + "." + slot, bankName);
            slot++;
        }
    }

    private static void updateMeta(Player p) {
        Inventory bank = p.getOpenInventory().getTopInventory();
        if (bank.getHolder() == null || !(bank.getHolder() instanceof BanksHolder)) return;

        int slot = 0;
        for (String bankName : BanksManager.getBankNames()) {
            ItemStack item = bank.getItem(slot);
            slot++;
            if (item == null) continue;
            ItemMeta meta = item.getItemMeta();

            String displayname = "&c&l* DISPLAYNAME NOT FOUND *";
            List<String> lore = new ArrayList<>();
            int modelData;

            ConfigurationSection section = BanksManager.getBanksGuiItemSection(bankName);
            if (section != null) {
                boolean hasPerm = BanksManager.hasPermission(bankName);
                String perm = BanksManager.getPermission(bankName);
                if (!hasPerm || p.hasPermission(perm)) {
                    String dName = section.getString("Available.Displayname");
                    displayname = dName == null ? "&c&l* DISPLAYNAME NOT FOUND *" : dName;
                    lore = section.getStringList("Available.Lore");
                    modelData = section.getInt("Available.CustomModelData");
                } else {
                    String dName = section.getString("Unavailable.Displayname");
                    displayname = dName == null ? "&c&l* DISPLAYNAME NOT FOUND *" : dName;
                    lore = section.getStringList("Unavailable.Lore");
                    modelData = section.getInt("Unavailable.CustomModelData");
                }
                if (modelData > 0) {
                    try {
                        meta.setCustomModelData(modelData);
                    } catch (NoSuchMethodError e) {
                        BPLogger.warn("Cannot set custom model data to the item: \"" + displayname + "\"&e. Custom model data is only available on 1.14.4+ servers!");
                    }
                }
            }
            setMeta(displayname, lore, meta, p);
            item.setItemMeta(meta);
        }
    }

    private static void setMeta(String displayname, List<String> lore, ItemMeta meta, Player p) {
        List<String> newLore = new ArrayList<>();
        for (String lines : lore) newLore.add(BPChat.color(lines));

        if (BankPlus.getInstance().isPlaceholderAPIHooked()) {
            meta.setDisplayName(PlaceholderAPI.setPlaceholders(p, BPChat.color(displayname)));
            meta.setLore(PlaceholderAPI.setPlaceholders(p, newLore));
        } else {
            meta.setDisplayName(BPChat.color(displayname));
            meta.setLore(newLore);
        }
    }

    private static void glow(ItemStack item, boolean glow) {
        if (!glow) return;
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }
}