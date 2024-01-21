package me.pulsi_.bankplus.bankSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.utils.*;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class BankUtils {

    public static void openBank(Player p) {
        openBank(p, Values.CONFIG.getMainGuiName(), false);
    }

    public static void openBank(Player p, String identifier) {
        openBank(p, identifier, false);
    }

    public static void openBank(Player p, String bankName, boolean bypass) {
        BPPlayer player = PlayerRegistry.get(p);
        BankRegistry registry = BankPlus.INSTANCE().getBankGuiRegistry();

        if (bankName.equals(BankListGui.multipleBanksGuiID)) {
            BankListGui.openMultipleBanksGui(p);
            return;
        }

        if (!registry.getBanks().containsKey(bankName)) {
            BPMessages.send(p, "Invalid-Bank");
            return;
        }

        if (!BankManager.isAvailable(bankName, p) && !bypass) {
            BPMessages.send(p, "Cannot-Access-Bank");
            return;
        }

        BukkitTask updating = player.getBankUpdatingTask();
        if (updating != null) updating.cancel();

        Bank baseBank = registry.getBanks().get(bankName);

        String title = baseBank.getTitle();
        if (!BankPlus.INSTANCE().isPlaceholderApiHooked()) title = BPChat.color(title);
        else title = PlaceholderAPI.setPlaceholders(p, BPChat.color(title));

        Inventory bank = Bukkit.createInventory(new BankHolder(), baseBank.getSize(), title);
        bank.setContents(baseBank.getContent());
        placeHeads(bank, p, baseBank);
        updateMeta(bank, p, baseBank);

        long delay = baseBank.getUpdateDelay();
        if (delay >= 0) player.setBankUpdatingTask(Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE(), () -> updateMeta(bank, p, baseBank), delay, delay));

        player.setOpenedBank(baseBank);
        BPUtils.playSound("PERSONAL", p);
        p.openInventory(bank);
    }

    private static void placeHeads(Inventory bank, Player p, Bank baseBank) {
        ConfigurationSection items = baseBank.getItems();
        if (items == null) return;

        for (String item : items.getKeys(false)) {
            ConfigurationSection itemValues = items.getConfigurationSection(item);
            if (itemValues == null) continue;

            String material = itemValues.getString("Material");
            if (material == null || !material.equals("HEAD-%PLAYER%")) continue;

            try {
                bank.setItem(itemValues.getInt("Slot") - 1, BPItems.getHead(p));
            } catch (ArrayIndexOutOfBoundsException e) {
                bank.addItem(BPItems.getHead(p));
            }
        }
    }

    private static void updateMeta(Inventory bank, Player p, Bank baseBank) {
        ConfigurationSection items = baseBank.getItems();
        if (items == null) return;

        for (String item : items.getKeys(false)) {
            ConfigurationSection itemValues = items.getConfigurationSection(item);
            if (itemValues == null) continue;

            ItemStack i = bank.getItem(itemValues.getInt("Slot") - 1);
            if (i != null) setMeta(itemValues, i, p, baseBank.getIdentifier());
        }
    }

    private static void setMeta(ConfigurationSection itemValues, ItemStack item, Player p, String bankName) {
        ItemMeta meta = item.getItemMeta();

        String displayName = itemValues.getString("Displayname");
        List<String> lore = new ArrayList<>();

        List<String> configLore = itemValues.getStringList("Lore");
        if (!configLore.isEmpty()) for (String line : configLore) lore.add(BPChat.color(line));
        else {
            ConfigurationSection loreSection = itemValues.getConfigurationSection("Lore");
            if (loreSection != null) {
                int level = BankManager.getCurrentLevel(bankName, p);
                List<String> defaultLore = loreSection.getStringList("Default"), leveLore = loreSection.getStringList(level + "");

                if (!leveLore.isEmpty()) for (String line : leveLore) lore.add(BPChat.color(line));
                else if (!defaultLore.isEmpty()) for (String line : defaultLore) lore.add(BPChat.color(line));
            }
        }

        if (BankPlus.INSTANCE().isPlaceholderApiHooked()) {
            meta.setDisplayName(PlaceholderAPI.setPlaceholders(p, BPChat.color(displayName)));
            meta.setLore(PlaceholderAPI.setPlaceholders(p, lore));
        } else {
            meta.setDisplayName(BPChat.color(displayName));
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }

    public static void loadBankValues(Bank bank, FileConfiguration config) {
        bank.setAccessPermission(config.getString("Settings.Permission"));
        bank.setUpgrades(config.getConfigurationSection("Upgrades"));

        if (!Values.CONFIG.isGuiModuleEnabled()) return;

        bank.setTitle(config.getString("Title"));
        bank.setSize(config.getInt("Lines"));
        bank.setUpdateDelay(config.getInt("Update-Delay"));
        bank.setHasFiller(config.getBoolean("Filler.Enabled"));
        bank.setFillerMaterial(config.getString("Filler.Material"));
        bank.setFillerGlowing(config.getBoolean("Filler.Glowing"));

        bank.setItems(config.getConfigurationSection("Items"));
        bank.setBanksListGuiItems(config.getConfigurationSection("Settings.BanksGuiItem"));

        ItemStack[] content = null;
        ConfigurationSection items = bank.getItems();
        if (items != null) {
            Inventory inv = Bukkit.createInventory(null, bank.getSize());

            for (String item : items.getKeys(false)) {
                ConfigurationSection itemValues = items.getConfigurationSection(item);
                if (itemValues == null) continue;

                ItemStack guiItem;
                String material = itemValues.getString("Material");

                if (material.startsWith("HEAD")) guiItem = BPItems.getHead(material);
                else guiItem = BPItems.createItemStack(itemValues);

                ItemMeta meta = guiItem.getItemMeta();
                String displayname = itemValues.getString("Displayname");
                List<String> lore = new ArrayList<>();

                for (String lines : itemValues.getStringList("Lore"))
                    lore.add(BPChat.color(lines));

                meta.setDisplayName(BPChat.color(displayname == null ? "&c&l*CANNOT FIND DISPLAYNAME*" : displayname));
                meta.setLore(lore);

                if (itemValues.getBoolean("Glowing")) {
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                int modelData = itemValues.getInt("CustomModelData");
                if (modelData > 0) {
                    try {
                        meta.setCustomModelData(modelData);
                    } catch (NoSuchMethodError e) {
                        BPLogger.warn("Cannot set custom model data to the item: \"" + displayname + "\"&e. Custom model data is only available on 1.14.4+ servers!");
                    }
                }
                guiItem.setItemMeta(meta);

                try {
                    int slot = itemValues.getInt("Slot") - 1;
                    inv.setItem(slot, guiItem);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    inv.addItem(guiItem);
                }
            }

            if (bank.hasFiller())
                for (int i = 0; i < inv.getSize(); i++)
                    if (inv.getItem(i) == null) inv.setItem(i, BPItems.getFiller(bank));

            content = inv.getContents();
        }
        bank.setContent(content);
    }
}