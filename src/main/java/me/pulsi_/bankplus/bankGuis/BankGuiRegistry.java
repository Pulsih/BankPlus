package me.pulsi_.bankplus.bankGuis;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankGuis.objects.Bank;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPItems;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BankGuiRegistry {

    private final HashMap<String, Bank> banks = new HashMap<>();

    public void put(String identifier, Bank bank) {
        banks.put(identifier, bank);
    }

    public Bank get(String identifier) {
        return banks.get(identifier);
    }

    public Bank remove(String identifier) {
        return banks.remove(identifier);
    }

    public boolean contains(String identifier) {
        return banks.containsKey(identifier);
    }

    public HashMap<String, Bank> getBanks() {
        return banks;
    }

    public boolean loadBanks() {
        HashMap<String, Bank> banks = BankPlus.INSTANCE.getBankGuiRegistry().getBanks();
        File file = new File(BankPlus.INSTANCE.getDataFolder(), "banks");
        File[] files = file.listFiles();

        banks.clear();
        List<File> bankFiles;

        if (files == null || files.length == 0) {
            File defaultBankFile = new File(BankPlus.INSTANCE.getDataFolder(), "banks" + File.separator + Values.CONFIG.getMainGuiName() + ".yml");
            if (!defaultBankFile.exists()) {
                BankPlus.INSTANCE.saveResource("banks" + File.separator + "bankplus_main_gui_base_file.yml", false);
                File newMainFile = new File(BankPlus.INSTANCE.getDataFolder(), "banks" + File.separator + "bankplus_main_gui_base_file.yml");
                newMainFile.renameTo(defaultBankFile);
            }
            bankFiles = new ArrayList<>(Collections.singletonList(defaultBankFile));
        } else {
            bankFiles = new ArrayList<>(Arrays.asList(files));

            boolean theresMainGui = false;
            for (File bankFile : bankFiles) {
                if (!bankFile.getName().replace(".yml", "").equals(Values.CONFIG.getMainGuiName())) continue;
                theresMainGui = true;
                break;
            }
            if (!theresMainGui) {
                BPLogger.warn("The main gui was missing, creating the file...");
                File defaultBankFile = new File(BankPlus.INSTANCE.getDataFolder(), "banks" + File.separator + Values.CONFIG.getMainGuiName() + ".yml");
                defaultBankFile.getParentFile().mkdir();

                BankPlus.INSTANCE.saveResource("banks" + File.separator + "bankplus_main_gui_base_file.yml", false);
                File newMainFile = new File(BankPlus.INSTANCE.getDataFolder(), "banks" + File.separator + "bankplus_main_gui_base_file.yml");
                newMainFile.renameTo(defaultBankFile);

                bankFiles.add(defaultBankFile);
            }
        }

        for (File bankFile : bankFiles) {
            FileConfiguration bankConfig = new YamlConfiguration();
            try {
                bankConfig.load(bankFile);
            } catch (IOException | InvalidConfigurationException e) {
                BPLogger.error("An error has occurred while loading a bank file: " + e.getMessage());
                return false;
            }

            String identifier = bankFile.getName().replace(".yml", "");
            Bank bank = new Bank(identifier);

            ItemStack[] content = null;
            ConfigurationSection items = bankConfig.getConfigurationSection("Items");
            if (items != null) {
                Inventory inv = Bukkit.createInventory(null, bank.getSize(), "");
                for (String item : items.getKeys(false)) {
                    ConfigurationSection itemValues = items.getConfigurationSection(item);
                    if (itemValues == null) continue;

                    String material = itemValues.getString("Material");
                    if (material == null) continue;

                    ItemStack guiItem;
                    if (material.startsWith("HEAD")) guiItem = BPItems.getHead(itemValues);
                    else guiItem = BPItems.createItemStack(itemValues);
                    if (guiItem == null) continue;

                    ItemMeta meta = guiItem.getItemMeta();
                    String displayname = itemValues.getString("Displayname");
                    List<String> lore = new ArrayList<>();
                    for (String lines : itemValues.getStringList("Lore")) lore.add(BPChat.color(lines));

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
            banks.put(identifier, bank);
        }
        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) new BanksListGui().loadMultipleBanksGui();
        return true;
    }
}