package me.pulsi_.bankplus.bankSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPItems;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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

    public static void openBank(Player p, String identifier, boolean bypass) {
        BPPlayer player = BankPlus.INSTANCE.getPlayerRegistry().get(p);
        BankGuiRegistry registry = BankPlus.INSTANCE.getBankGuiRegistry();

        if (identifier.equals(BankListGui.multipleBanksGuiID)) {
            BankListGui.openMultipleBanksGui(p);
            return;
        }

        if (!registry.getBanks().containsKey(identifier)) {
            BPMessages.send(p, "Invalid-Bank");
            return;
        }

        if (!new BankReader(identifier).isAvailable(p) && !bypass) {
            BPMessages.send(p, "Cannot-Access-Bank");
            return;
        }

        BukkitTask updating = player.getBankUpdatingTask();
        if (updating != null) updating.cancel();

        Bank baseBank = registry.getBanks().get(identifier);

        String title = baseBank.getTitle();
        if (!BankPlus.INSTANCE.isPlaceholderAPIHooked()) title = BPChat.color(title);
        else title = PlaceholderAPI.setPlaceholders(p, BPChat.color(title));

        Inventory bank = Bukkit.createInventory(new BankHolder(), baseBank.getSize(), title);
        bank.setContents(baseBank.getContent());
        placeHeads(bank, p, baseBank);
        updateMeta(bank, p, baseBank);

        long delay = baseBank.getUpdateDelay();
        if (delay >= 0) player.setBankUpdatingTask(Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE, () -> updateMeta(bank, p, baseBank), delay, delay));

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
            if (i != null) setMeta(itemValues, i, p, baseBank);
        }
    }

    private static void setMeta(ConfigurationSection itemValues, ItemStack item, Player p, Bank baseBank) {
        ItemMeta meta = item.getItemMeta();

        String displayName = itemValues.getString("Displayname") == null ? "&c&l*CANNOT FIND DISPLAYNAME*" : itemValues.getString("Displayname");
        List<String> lore = new ArrayList<>();

        List<String> configLore = itemValues.getStringList("Lore");
        if (!configLore.isEmpty()) for (String line : configLore) lore.add(BPChat.color(line));
        else {
            ConfigurationSection loreSection = itemValues.getConfigurationSection("Lore");
            if (loreSection != null) {
                int level = new BankReader(baseBank.getIdentifier()).getCurrentLevel(p);
                List<String> defaultLore = loreSection.getStringList("Default"), leveLore = loreSection.getStringList(level + "");

                if (!leveLore.isEmpty()) for (String line : leveLore) lore.add(BPChat.color(line));
                else if (!defaultLore.isEmpty()) for (String line : defaultLore) lore.add(BPChat.color(line));
            }
        }

        if (BankPlus.INSTANCE.isPlaceholderAPIHooked()) {
            meta.setDisplayName(PlaceholderAPI.setPlaceholders(p, BPChat.color(displayName)));
            meta.setLore(PlaceholderAPI.setPlaceholders(p, lore));
        } else {
            meta.setDisplayName(BPChat.color(displayName));
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }
}