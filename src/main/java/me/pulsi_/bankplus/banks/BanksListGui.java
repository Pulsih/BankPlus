package me.pulsi_.bankplus.banks;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayer;
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
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BanksListGui {

    public static final String multipleBanksGuiID = "MultipleBanksGui";
    private final Material DEFAULT_MATERIAL = Material.CHEST;

    public void openMultipleBanksGui(Player p) {
        HashMap<String, Bank> banks = BankPlus.instance().getBanks();

        Bank baseBanksListGui = banks.get(multipleBanksGuiID);
        Inventory banksListGui = Bukkit.createInventory(new BanksHolder(), baseBanksListGui.getSize(), baseBanksListGui.getTitle());
        banksListGui.setContents(baseBanksListGui.getContent());
        placeBanks(banksListGui, p);

        BPMethods.playSound("PERSONAL", p);
        p.openInventory(banksListGui);

        BukkitTask task = null;
        long delay = Values.MULTIPLE_BANKS.getUpdateDelay();
        if (delay == 0) updateMeta(p);
        else task = Bukkit.getScheduler().runTaskTimer(BankPlus.instance(), () -> updateMeta(p), 0, delay);

        BankPlusPlayer player = new BankPlusPlayer(p);
        player.setOpenedBank(baseBanksListGui);
        player.setInventoryUpdateTask(task);
        player.updateInstance();
    }

    public void loadMultipleBanksGui() {
        String title = Values.MULTIPLE_BANKS.getBanksGuiTitle() == null ? BPChat.color("&c&l * TITLE NOT FOUND *") : BPChat.color(Values.MULTIPLE_BANKS.getBanksGuiTitle());
        Inventory gui = Bukkit.createInventory(new BanksHolder(), Values.MULTIPLE_BANKS.getBanksGuiLines(), title);

        if (Values.MULTIPLE_BANKS.isFillerEnabled()) {
            ItemStack filler = ItemCreator.getFiller(Values.MULTIPLE_BANKS.getFillerMaterial(), Values.MULTIPLE_BANKS.isFillerGlowing());
            for (int i = 0; i < gui.getSize(); i++) gui.setItem(i, filler);
        }

        Bank multipleBanksGui = new Bank(
                multipleBanksGuiID, title, Values.MULTIPLE_BANKS.getBanksGuiLines(), Values.MULTIPLE_BANKS.getUpdateDelay(), gui.getContents()
        );
        BankPlus.instance().getBanks().put("MultipleBanksGui", multipleBanksGui);
    }

    private void placeBanks(Inventory banksListGui, Player p) {
        BankPlusPlayer player = BankPlus.instance().getPlayers().get(p.getUniqueId());
        HashMap<String, String> banksClickHolder = new HashMap<>();
        int slot = 0;

        for (String bankName : BankPlus.instance().getBanks().keySet()) {
            ItemStack bankItem;
            Material material;

            boolean glow = false;
            BanksManager banksManager = new BanksManager(bankName);
            ConfigurationSection section = banksManager.getBanksGuiItemSection();
            if (section == null) material = DEFAULT_MATERIAL;
            else {
                try {
                    if (banksManager.isAvailable(p)) {
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
            if (glow) glow(bankItem);
            banksListGui.setItem(slot, bankItem);
            banksClickHolder.put(p.getName() + "." + slot, bankName);
            slot++;
        }
        player.setPlayerBankClickHolder(banksClickHolder);
    }

    private void updateMeta(Player p) {
        Inventory bank = p.getOpenInventory().getTopInventory();
        if (bank.getHolder() == null || !(bank.getHolder() instanceof BanksHolder)) return;

        int slot = 0;
        for (String bankName : BankPlus.instance().getBanks().keySet()) {
            ItemStack item = bank.getItem(slot);
            slot++;
            if (item == null) continue;
            ItemMeta meta = item.getItemMeta();

            String displayname = "&c&l* DISPLAYNAME NOT FOUND *";
            List<String> lore = new ArrayList<>();
            int modelData;

            BanksManager banksManager = new BanksManager(bankName);
            ConfigurationSection section = banksManager.getBanksGuiItemSection();
            if (section != null) {
                boolean hasPerm = banksManager.hasPermission();
                String perm = banksManager.getPermission();
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

    private void setMeta(String displayname, List<String> lore, ItemMeta meta, Player p) {
        List<String> newLore = new ArrayList<>();
        for (String lines : lore) newLore.add(BPChat.color(lines));

        if (BankPlus.instance().isPlaceholderAPIHooked()) {
            meta.setDisplayName(PlaceholderAPI.setPlaceholders(p, BPChat.color(displayname)));
            meta.setLore(PlaceholderAPI.setPlaceholders(p, newLore));
        } else {
            meta.setDisplayName(BPChat.color(displayname));
            meta.setLore(newLore);
        }
    }

    private static void glow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }
}