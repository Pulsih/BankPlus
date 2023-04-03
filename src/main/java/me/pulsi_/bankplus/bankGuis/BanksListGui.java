package me.pulsi_.bankplus.bankGuis;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayer;
import me.pulsi_.bankplus.bankGuis.objects.Bank;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPItems;
import me.pulsi_.bankplus.utils.BPLogger;
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
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BanksListGui {

    public static final String multipleBanksGuiID = "MultipleBanksGui";
    private final Material DEFAULT_MATERIAL = Material.CHEST;

    public void openMultipleBanksGui(Player p) {
        BankPlusPlayer player = BankPlus.INSTANCE.getPlayerRegistry().get(p);

        Bank openedBank = player.getOpenedBank();
        if (openedBank != null) {
            BukkitTask task = openedBank.getInventoryUpdateTask();
            if (task != null) task.cancel();
        }

        Bank baseBanksListGui = BankPlus.INSTANCE.getBankGuiRegistry().get(multipleBanksGuiID);

        String title = baseBanksListGui.getTitle();
        if (!BankPlus.INSTANCE.isPlaceholderAPIHooked()) title = BPChat.color(title);
        else title = PlaceholderAPI.setPlaceholders(p, BPChat.color(title));

        Inventory banksListGui = Bukkit.createInventory(new BanksHolder(), baseBanksListGui.getSize(), title);
        banksListGui.setContents(baseBanksListGui.getContent());
        placeBanks(banksListGui, p);
        updateMeta(banksListGui, p);

        long delay = Values.MULTIPLE_BANKS.getUpdateDelay();
        if (delay >= 0) baseBanksListGui.setInventoryUpdateTask(Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE, () -> updateMeta(banksListGui, p), delay, delay));

        player.setOpenedBank(baseBanksListGui);
        BPMethods.playSound("PERSONAL", p);
        p.openInventory(banksListGui);
    }

    public void loadMultipleBanksGui() {
        String title = BPChat.color(Values.MULTIPLE_BANKS.getBanksGuiTitle() == null ? "&c&l * TITLE NOT FOUND *" : Values.MULTIPLE_BANKS.getBanksGuiTitle());
        Inventory gui = Bukkit.createInventory(new BanksHolder(), Values.MULTIPLE_BANKS.getBanksGuiLines(), title);

        if (Values.MULTIPLE_BANKS.isFillerEnabled()) {
            ItemStack filler = BPItems.getFiller(Values.MULTIPLE_BANKS.getFillerMaterial(), Values.MULTIPLE_BANKS.isFillerGlowing());
            for (int i = 0; i < gui.getSize(); i++) gui.setItem(i, filler);
        }

        Bank multipleBanksGui = new Bank(
                multipleBanksGuiID, title, Values.MULTIPLE_BANKS.getBanksGuiLines(), getSize(Values.MULTIPLE_BANKS.getUpdateDelay()), gui.getContents()
        );
        BankPlus.INSTANCE.getBankGuiRegistry().put("MultipleBanksGui", multipleBanksGui);
    }

    private void placeBanks(Inventory banksListGui, Player p) {
        BankPlusPlayer player = BankPlus.INSTANCE.getPlayerRegistry().get(p);
        HashMap<String, String> banksClickHolder = new HashMap<>();
        int slot = 0;

        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
            if (bankName.equals(multipleBanksGuiID)) continue;

            ItemStack bankItem;
            Material material;

            boolean glow = false;
            BankReader banksManager = new BankReader(bankName);
            ConfigurationSection section = banksManager.getBanksGuiItemSection();
            if (section == null) material = DEFAULT_MATERIAL;
            else {
                try {
                    String path = banksManager.isAvailable(p) ? "Available." : "Unavailable.";
                    material = Material.valueOf(section.getString(path + "Material"));
                    glow = section.getBoolean(path + "Glowing");
                } catch (IllegalArgumentException e) {
                    material = BPItems.UNKNOWN_MATERIAL;
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

    private void updateMeta(Inventory banksList, Player p) {
        int slot = 0;
        for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet()) {
            ItemStack item = banksList.getItem(slot);
            slot++;
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
            String displayname = "&c&l* DISPLAYNAME NOT FOUND *";
            List<String> lore = new ArrayList<>();

            BankReader banksManager = new BankReader(bankName);
            ConfigurationSection section = banksManager.getBanksGuiItemSection();
            if (section != null) {
                String path = (!banksManager.hasPermissionSection() || p.hasPermission(banksManager.getPermission())) ? "Available." : "Unavailable.";

                String dName = section.getString(path + "Displayname");
                displayname = dName == null ? "&c&l* DISPLAYNAME NOT FOUND *" : dName;
                lore = section.getStringList(path + "Lore");
                int modelData = section.getInt(path + "CustomModelData");

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

        if (BankPlus.INSTANCE.isPlaceholderAPIHooked()) {
            meta.setDisplayName(PlaceholderAPI.setPlaceholders(p, BPChat.color(displayname)));
            meta.setLore(PlaceholderAPI.setPlaceholders(p, newLore));
        } else {
            meta.setDisplayName(BPChat.color(displayname));
            meta.setLore(newLore);
        }
    }

    public int getSize(int size) {
        if (size < 2) return 9;
        switch (size) {
            case 2:
                return 18;
            case 3:
                return 27;
            case 4:
                return 36;
            case 5:
                return 45;
            default:
                return 54;
        }
    }

    private static void glow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }
}