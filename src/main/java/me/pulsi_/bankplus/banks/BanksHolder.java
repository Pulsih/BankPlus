package me.pulsi_.bankplus.banks;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayer;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class BanksHolder implements InventoryHolder {

    public void openBank(Player p) {
        openBank(p, Values.CONFIG.getMainGuiName());
    }

    public void openBank(Player p, String identifier) {
        HashMap<String, Bank> banks = BankPlus.instance().getBanks();
        BankPlusPlayer player = BankPlus.instance().getPlayers().get(p.getUniqueId());
        if (!banks.containsKey(identifier)) {
            MessageManager.send(p, "Invalid-Bank");
            return;
        }
        BukkitTask task = player.getInventoryUpdateTask();
        if (task != null) task.cancel();

        if (identifier.equals(BanksListGui.multipleBanksGuiID)) {
            new BanksListGui().openMultipleBanksGui(p);
            return;
        }

        if (!new BanksManager(identifier).isAvailable(p)) {
            MessageManager.send(p, "Cannot-Access-Bank");
            return;
        }

        Bank baseBank = banks.get(identifier);
        Inventory bank = Bukkit.createInventory(new BanksHolder(), baseBank.getSize(), BPChat.color(baseBank.getTitle()));
        bank.setContents(baseBank.getContent());
        placeHeads(bank, p, identifier);

        player.setOpenedBank(baseBank);
        BPMethods.playSound("PERSONAL", p);
        p.openInventory(bank);

        long delay = baseBank.getUpdateDelay();
        if (delay == 0) updateMeta(bank, p, identifier);
        else player.setInventoryUpdateTask(Bukkit.getScheduler().runTaskTimer(BankPlus.instance(), () -> updateMeta(bank, p, identifier), 0, delay));
    }

    private void placeHeads(Inventory bank, Player p, String identifier) {
        ConfigurationSection items = new BanksManager(identifier).getItems();
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

    private void updateMeta(Inventory bank, Player p, String identifier) {
        ConfigurationSection items = new BanksManager(identifier).getItems();
        if (items == null) return;

        for (String item : items.getKeys(false)) {
            ConfigurationSection itemValues = items.getConfigurationSection(item);
            if (itemValues == null) continue;

            ItemStack i = bank.getItem(itemValues.getInt("Slot") - 1);
            if (i != null) setMeta(itemValues, i, p);
        }
    }

    private void setMeta(ConfigurationSection itemValues, ItemStack item, Player p) {
        ItemMeta meta = item.getItemMeta();

        String displayName = itemValues.getString("Displayname") == null ? "&c&l*CANNOT FIND DISPLAYNAME*" : itemValues.getString("Displayname");
        List<String> lore = new ArrayList<>();
        for (String lines : itemValues.getStringList("Lore")) lore.add(BPChat.color(lines));

        if (BankPlus.instance().isPlaceholderAPIHooked()) {
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