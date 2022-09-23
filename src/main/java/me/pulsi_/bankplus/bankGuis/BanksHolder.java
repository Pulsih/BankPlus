package me.pulsi_.bankplus.bankGuis;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayer;
import me.pulsi_.bankplus.utils.*;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BanksHolder implements InventoryHolder {

    public void openBank(Player p) {
        openBank(p, Values.CONFIG.getMainGuiName());
    }

    public void openBank(Player p, String identifier) {
        BankPlusPlayer player = BankPlus.instance().getPlayers().get(p.getUniqueId());
        HashMap<String, BankGui> banks = BankPlus.instance().getBanks();

        if (!banks.containsKey(identifier)) {
            BPMessages.send(p, "Invalid-Bank");
            return;
        }

        if (identifier.equals(BanksListGui.multipleBanksGuiID)) {
            new BanksListGui().openMultipleBanksGui(p);
            return;
        }

        if (!new BanksManager(identifier).isAvailable(p)) {
            BPMessages.send(p, "Cannot-Access-Bank");
            return;
        }

        BukkitTask task = player.getInventoryUpdateTask();
        if (task != null) task.cancel();

        BankGui baseBank = banks.get(identifier);
        Inventory bank = Bukkit.createInventory(new BanksHolder(), baseBank.getSize(), BPChat.color(baseBank.getTitle()));
        bank.setContents(baseBank.getContent());
        placeHeads(bank, p, identifier);
        updateMeta(bank, p, identifier);

        long delay = baseBank.getUpdateDelay();
        if (delay >= 0) player.setInventoryUpdateTask(Bukkit.getScheduler().runTaskTimer(BankPlus.instance(), () -> updateMeta(bank, p, identifier), delay, delay));

        player.setOpenedBank(baseBank);
        BPMethods.playSound("PERSONAL", p);
        p.openInventory(bank);
    }


    private void placeHeads(Inventory bank, Player p, String identifier) {
        ConfigurationSection items = new BanksManager(identifier).getItems();
        if (items == null) return;

        for (String item : items.getKeys(false)) {
            ConfigurationSection itemValues = items.getConfigurationSection(item);
            if (itemValues == null) continue;

            String material = itemValues.getString("Material");
            if (material == null) continue;

            if (material.startsWith("HEAD")){
                try {
                    bank.setItem(itemValues.getInt("Slot") - 1, BPItems.getHead(material, p));
                } catch (ArrayIndexOutOfBoundsException e) {
                    bank.addItem(BPItems.getHead(material, p));
                }
            }

            if(material.startsWith("PLAYER_HEAD")){
                String texture = itemValues.getString("Texture");
                int customModelData = itemValues.getInt("CustomModelData");
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                if(texture != null) {
                    texture = PlaceholderAPI.setPlaceholders(p, texture);
                    UUID hashAsId = new UUID(texture.hashCode(), texture.hashCode());
                    head = Bukkit.getUnsafe().modifyItemStack(head, "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + texture + "\"}]}}}");
                    if(customModelData > 0){
                        ItemMeta headMeta = head.getItemMeta();
                        headMeta.setCustomModelData(customModelData);
                        head.setItemMeta(headMeta);
                    }
                }

                try{
                    bank.setItem(itemValues.getInt("Slot")-1, head);
                }catch (ArrayIndexOutOfBoundsException e){
                    bank.addItem(head);
                }

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