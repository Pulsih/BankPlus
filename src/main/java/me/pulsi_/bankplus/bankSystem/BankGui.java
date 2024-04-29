package me.pulsi_.bankplus.bankSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPItems;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BankGui {

    private final String identifier;

    public BankGui(String identifier) {
        this.identifier = identifier;
    }

    // Pseudo inventory content to keep track of items, lore and actions. K = Inventory slot V = BankItem
    private final HashMap<Integer, Bank.BankItem> bankItems = new HashMap<>();

    private String title = "&c&l * TITLE NOT FOUND *";
    private int size, updateDelay;
    private String fillerMaterial, accessPermission;
    private boolean giveInterestIfNotAvailable, fillerEnabled, fillerGlowing;
    private Bank.BankItem availableBankListItem, unavailableBankListItem;

    public HashMap<Integer, Bank.BankItem> getBankItems() {
        return bankItems;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return Math.max(9, Math.min(54, size * 9));
    }

    public int getUpdateDelay() {
        return updateDelay;
    }

    public String getFillerMaterial() {
        return fillerMaterial;
    }

    public String getAccessPermission() {
        return accessPermission;
    }

    public boolean isGiveInterestIfNotAvailable() {
        return giveInterestIfNotAvailable;
    }

    public boolean isFillerEnabled() {
        return fillerEnabled;
    }

    public boolean isFillerGlowing() {
        return fillerGlowing;
    }

    public Bank.BankItem getAvailableBankListItem() {
        return availableBankListItem;
    }

    public Bank.BankItem getUnavailableBankListItem() {
        return unavailableBankListItem;
    }

    public void setBankItem(int slot, BankItem item) {
        bankItems.put(slot, item);
    }

    public void setTitle(String title) {
        if (title != null && !title.isEmpty()) this.title = title;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setUpdateDelay(int updateDelay) {
        this.updateDelay = updateDelay;
    }

    public void setFillerMaterial(String fillerMaterial) {
        this.fillerMaterial = fillerMaterial;
    }

    public void setAccessPermission(String accessPermission) {
        this.accessPermission = accessPermission;
    }

    public void setGiveInterestIfNotAvailable(boolean giveInterestIfNotAvailable) {
        this.giveInterestIfNotAvailable = giveInterestIfNotAvailable;
    }

    public void setFillerEnabled(boolean fillerEnabled) {
        this.fillerEnabled = fillerEnabled;
    }

    public void setFillerGlowing(boolean fillerGlowing) {
        this.fillerGlowing = fillerGlowing;
    }

    public void setAvailableBankListItem(Bank.BankItem availableBankListItem) {
        this.availableBankListItem = availableBankListItem;
    }

    public void setUnavailableBankListItem(Bank.BankItem unavailableBankListItem) {
        this.unavailableBankListItem = unavailableBankListItem;
    }

    public void openBankGui(Player p) {
        openBankGui(p, false);
    }

    public void openBankGui(Player p, boolean bypass) {
        if (!bypass) {
            if (Values.CONFIG.isNeedOpenPermissionToOpen() && !BPUtils.hasPermission(p, "bankplus.open")) return;

            if (!BankUtils.isAvailable(identifier, p)) {
                BPMessages.send(p, "Cannot-Access-Bank");
                return;
            }
        }

        BPPlayer player = PlayerRegistry.get(p);
        if (player == null) player = PlayerRegistry.loadPlayer(p);

        if (!BankUtils.exist(identifier)) {
            BPMessages.send(p, "Invalid-Bank");
            return;
        }

        BukkitTask updating = player.getBankUpdatingTask();
        if (updating != null) updating.cancel();

        Bank baseBank = BankUtils.getBank(identifier);
        String title = baseBank.getTitle();
        if (!BankPlus.INSTANCE().isPlaceholderApiHooked()) title = BPChat.color(title);
        else title = PlaceholderAPI.setPlaceholders(p, BPChat.color(title));

        Inventory bankInventory = Bukkit.createInventory(new BankHolder(), baseBank.getSize(), title);
        placeContent(bankItems, bankInventory, p);
        updateBankGuiMeta(bankInventory, p);

        long delay = baseBank.getUpdateDelay();
        if (delay >= 0) player.setBankUpdatingTask(Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE(), () -> updateBankGuiMeta(bankInventory, p), delay, delay));

        player.setOpenedBankGui(baseBank);
        BPUtils.playSound("PERSONAL", p);
        p.openInventory(bankInventory);
    }

    /**
     * Method to place all the bank items WITHOUT the lore.
     * @param items The bank items.
     * @param bankInventory The bank inventory.
     * @param p The player needed for possible skulls.
     */
    public void placeContent(HashMap<Integer, Bank.BankItem> items, Inventory bankInventory, Player p) {
        if (fillerEnabled) {
            ItemStack filler = BPItems.getFiller(this);
            int size = bankInventory.getSize();
            for (int i = 0; i < size; i++) bankInventory.setItem(i, filler);
        }

        for (int slot : items.keySet()) {
            Bank.BankItem item = items.get(slot);
            if (item.material.equalsIgnoreCase("head-%player%")) bankInventory.setItem(slot,  BPItems.getHead(p));
            else bankInventory.setItem(slot, items.get(slot).item);
        }
    }

    /**
     * Method to update every item meta in the bank inventory: lore, displayname and placeholders.
     * @param playerOpenedInventory The inventory opened by the player, should check if he has opened a bank.
     * @param p The player to update placeholders and lore.
     */
    public void updateBankGuiMeta(Inventory playerOpenedInventory, Player p) {
        for (int slot : bankItems.keySet()) updateBankGuiItemMeta(slot, playerOpenedInventory, p);
    }

    /**
     * Method to update a single item meta: lore, displayname and placeholders.
     * @param slot The slot of the item.
     */
    public void updateBankGuiItemMeta(int slot, Inventory playerOpenedInventory, Player p) {
        Bank.BankItem bankItem = bankItems.get(slot);

        ItemStack inventoryItem = playerOpenedInventory.getItem(slot);
        ItemMeta meta = inventoryItem.getItemMeta();

        List<String> actualLore = new ArrayList<>(), levelLore = bankItem.lore.get(BankUtils.getCurrentLevel(identifier, p));
        if (levelLore == null) levelLore = bankItem.lore.get(0);
        for (String line : levelLore) actualLore.add(BPChat.color(line));

        if (BankPlus.INSTANCE().isPlaceholderApiHooked()) {
            meta.setDisplayName(BPChat.color(PlaceholderAPI.setPlaceholders(p, bankItem.displayname)));
            meta.setLore(PlaceholderAPI.setPlaceholders(p, actualLore));
        } else {
            meta.setDisplayName(BPChat.color(bankItem.displayname));
            meta.setLore(actualLore);
        }
        inventoryItem.setItemMeta(meta);
    }

    /**
     * Class used to keep track of item settings.
     * The ItemStack holds final settings such as material, item flags and enchantments.
     * The lore and displayname are the values that are going to be updated.
     */
    public static class BankItem {

        private ItemStack item;
        private String material = "STONE", displayname = "* DISPLAYNAME NOT FOUND *";
        private List<String> actions = new ArrayList<>();
        private final HashMap<Integer, List<String>> lore = new HashMap<>();

        public ItemStack getItem() {
            return item;
        }

        public String getMaterial() {
            return material;
        }

        public String getDisplayname() {
            return displayname;
        }

        public List<String> getActions() {
            return actions;
        }

        public HashMap<Integer, List<String>> getLore() {
            return lore;
        }

        public void setItem(ItemStack item) {
            this.item = item;
        }

        public void setMaterial(String material) {
            if (material != null) this.material = material;
        }

        public void setDisplayname(String displayname) {
            if (displayname != null) this.displayname = displayname;
        }

        public void setActions(List<String> actions) {
            this.actions = actions;
        }
    }
}