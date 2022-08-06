package me.pulsi_.bankplus.guis;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BanksHolder implements InventoryHolder {

    public static Map<String, Inventory> bankGetter = new HashMap<>();
    public static Map<UUID, String> openedBank = new HashMap<>();
    public static Map<UUID, BukkitTask> tasks = new HashMap<>();

    public static void openBank(Player p) {
        openBank(p, Values.CONFIG.getMainGuiName());
    }

    public static void openBank(Player p, String identifier) {
        if (!bankGetter.containsKey(identifier)) {
            MessageManager.send(p, "Invalid-Bank");
            return;
        }
        BukkitTask task = tasks.remove(p.getUniqueId());
        if (task != null) task.cancel();

        if (identifier.equals(BanksListGui.multipleBanksGuiID)) {
            BanksListGui.openMultipleBanksGui(p);
            return;
        }

        if (!BanksManager.isAvailable(p, identifier)) {
            MessageManager.send(p, "Cannot-Access-Bank");
            return;
        }

        BPMethods.playSound("PERSONAL", p);
        Inventory baseBank = bankGetter.get(identifier);
        Inventory bank = Bukkit.createInventory(new BanksHolder(), baseBank.getSize(), BanksManager.getTitle(identifier));
        bank.setContents(baseBank.getContents());
        placeHeads(bank, p, identifier);

        openedBank.put(p.getUniqueId(), identifier);
        p.openInventory(bank);

        long delay = BanksManager.getUpdateDelay(identifier);
        if (delay == 0) updateMeta(bank, p, identifier);
        else tasks.put(p.getUniqueId(), Bukkit.getScheduler().runTaskTimer(BankPlus.getInstance(), () -> updateMeta(bank, p, identifier), 0, delay));
    }

    public static void loadBanks() {
        File file = new File(BankPlus.getInstance().getDataFolder(), "banks");
        File[] files = file.listFiles();

        BanksManager.clearBankNames();
        bankGetter.clear();
        List<File> bankFiles;

        if (files == null || files.length == 0) {
            File defaultBankFile = new File(BankPlus.getInstance().getDataFolder(), "banks" + File.separator + Values.CONFIG.getMainGuiName() + ".yml");
            if (!defaultBankFile.exists()) {
                BankPlus.getInstance().saveResource("banks" + File.separator + "bankplus_main_gui_base_file.yml", false);
                File newMainFile = new File(BankPlus.getInstance().getDataFolder(), "banks" + File.separator + "bankplus_main_gui_base_file.yml");
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
                File defaultBankFile = new File(BankPlus.getInstance().getDataFolder(), "banks" + File.separator + Values.CONFIG.getMainGuiName() + ".yml");
                defaultBankFile.getParentFile().mkdir();

                BankPlus.getInstance().saveResource("banks" + File.separator + "bankplus_main_gui_base_file.yml", false);
                File newMainFile = new File(BankPlus.getInstance().getDataFolder(), "banks" + File.separator + "bankplus_main_gui_base_file.yml");
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
            }

            String identifier = bankFile.getName().replace(".yml", "");
            BanksManager.loadValues(identifier);

            Inventory bank = GuiManager.getGuiBank(identifier);
            BanksManager.addBankToBankNames(identifier);
            bankGetter.put(identifier, bank);
        }
        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) BanksListGui.loadMultipleBanksGui();
    }

    private static void placeHeads(Inventory bank, Player p, String identifier) {
        ConfigurationSection items = BanksManager.getItems(identifier);
        if (items == null) return;

        for (String item : items.getKeys(false)) {
            ConfigurationSection itemValues = items.getConfigurationSection(item);
            if (itemValues == null) continue;

            String material = itemValues.getString("Material");
            if (material == null || !material.startsWith("HEAD")) continue;

            try {
                bank.setItem(itemValues.getInt("Slot") - 1, ItemCreator.getHead(material, p));
            } catch (ArrayIndexOutOfBoundsException e) {
                bank.addItem(ItemCreator.getHead(material, p));
            }
        }
    }

    private static void updateMeta(Inventory bank, Player p, String identifier) {
        ConfigurationSection items = BanksManager.getItems(identifier);
        if (items == null) return;

        for (String item : items.getKeys(false)) {
            ConfigurationSection itemValues = items.getConfigurationSection(item);
            if (itemValues == null) continue;

            ItemStack i = bank.getItem(itemValues.getInt("Slot") - 1);
            if (i != null) setMeta(itemValues, i, p);
        }
    }

    private static void setMeta(ConfigurationSection itemValues, ItemStack item, Player p) {
        ItemMeta meta = item.getItemMeta();

        String displayName = itemValues.getString("Displayname") == null ? "&c&l*CANNOT FIND DISPLAYNAME*" : itemValues.getString("Displayname");
        List<String> lore = new ArrayList<>();
        for (String lines : itemValues.getStringList("Lore")) lore.add(BPChat.color(lines));

        if (BankPlus.getInstance().isPlaceholderAPIHooked()) {
            meta.setDisplayName(PlaceholderAPI.setPlaceholders(p, BPChat.color(displayName)));
            meta.setLore(PlaceholderAPI.setPlaceholders(p, lore));
        } else {
            meta.setDisplayName(BPChat.color(displayName));
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}