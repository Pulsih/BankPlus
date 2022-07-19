package me.pulsi_.bankplus.guis;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BanksListGui {

    private static final HashMap<Integer, String> getBankFromSlot = new HashMap<>();
    private static final Material DEFAULT_MATERIAL = Material.CHEST;

    public static void openMultipleBanksGui(Player p) {
        String identifier = "MultipleBanksGui";
        Inventory bank = BanksHolder.bankGetter.get(identifier);
        p.openInventory(bank);
        placeBanks(p);

        long delay = Values.MULTIPLE_BANKS.getUpdateDelay();
        if (delay == 0) updateMeta(p);
        else BanksHolder.tasks.put(p, Bukkit.getScheduler().runTaskTimer(BankPlus.getInstance(), () -> updateMeta(p), 0, delay));

        BanksHolder.openedInventory.put(p, identifier);
        BPMethods.playSound("PERSONAL", p);
    }

    public static void loadMultipleBanksGui() {
        String title = Values.MULTIPLE_BANKS.getBanksGuiTitle();
        if (title == null) title = "&c&l * TITLE NOT FOUND *";

        Inventory multipleBanksGui = Bukkit.createInventory(new BanksHolder(), Values.MULTIPLE_BANKS.getBanksGuiLines(), BPChat.color(title));

        if (Values.MULTIPLE_BANKS.isFillerEnabled()) {
            ItemStack filler = ItemCreator.getFiller(Values.MULTIPLE_BANKS.getFillerMaterial(), Values.MULTIPLE_BANKS.isFillerGlowing());
            for (int i = 0; i < multipleBanksGui.getSize(); i++) multipleBanksGui.setItem(i, filler);
        }

        BanksHolder.bankGetter.put("MultipleBanksGui", multipleBanksGui);
    }

    private static void placeBanks(Player p) {
        Inventory bank = p.getOpenInventory().getTopInventory();
        if (!(bank.getHolder() instanceof BanksHolder)) return;

        int slot = 0;
        for (String bankName : BanksManager.getBankNames()) {
            ItemStack bankItem;
            Material material;

            boolean glow = false;
            ConfigurationSection section = BanksManager.getBanksGuiItemSection(bankName);
            if (section == null) material = DEFAULT_MATERIAL;
            else {
                boolean hasPerm = BanksManager.hasPermission(bankName);
                String perm = BanksManager.getPermission(bankName);
                try {
                    if (!hasPerm || p.hasPermission(perm)) {
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
            bank.setItem(slot, bankItem);
            slot++;
        }
    }

    private static void updateMeta(Player p) {
        Inventory bank = p.getOpenInventory().getTopInventory();
        if (!(bank.getHolder() instanceof BanksHolder)) return;

        int slot = 0;
        for (String bankName : BanksManager.getBankNames()) {
            ItemStack item = bank.getItem(slot);
            slot++;
            if (item == null) continue;

            String displayname;
            List<String> lore;

            ConfigurationSection section = BanksManager.getBanksGuiItemSection(bankName);
            if (section == null) {
                displayname = "&c&l* DISPLAYNAME NOT FOUND *";
                lore = new ArrayList<>();
            } else {
                boolean hasPerm = BanksManager.hasPermission(bankName);
                String perm = BanksManager.getPermission(bankName);
                if (!hasPerm || p.hasPermission(perm)) {
                    String dName = section.getString("Available.Displayname");
                    displayname = dName == null ? "&c&l* DISPLAYNAME NOT FOUND *" : dName;
                    lore = section.getStringList("Available.Lore");
                } else {
                    String dName = section.getString("Unavailable.Displayname");
                    displayname = dName == null ? "&c&l* DISPLAYNAME NOT FOUND *" : dName;
                    lore = section.getStringList("Unavailable.Lore");
                }
            }
            setMeta(displayname, lore, item, p);
        }
    }

    private static void setMeta(String displayname, List<String> lore, ItemStack item, Player p) {
        ItemMeta meta = item.getItemMeta();

        List<String> newLore = new ArrayList<>();
        for (String lines : lore) newLore.add(BPChat.color(lines));

        if (BankPlus.getInstance().isPlaceholderAPIHooked()) {
            meta.setDisplayName(PlaceholderAPI.setPlaceholders(p, BPChat.color(displayname)));
            meta.setLore(PlaceholderAPI.setPlaceholders(p, newLore));
        } else {
            meta.setDisplayName(BPChat.color(displayname));
            meta.setLore(newLore);
        }
        item.setItemMeta(meta);
    }

    private static void glow(ItemStack item, boolean glow) {
        if (!glow) return;
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }
}