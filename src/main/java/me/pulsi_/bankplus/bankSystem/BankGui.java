package me.pulsi_.bankplus.bankSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPChat;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BankGui {

    private final Bank originBank;

    public BankGui(Bank originBank) {
        this.originBank = originBank;
    }

    // Pseudo inventory content to keep track of items, lore and actions. K = Inventory slot V = BankItem
    private final HashMap<Integer, BPGuiItem> bankItems = new HashMap<>();

    private Component title = BPItems.DISPLAYNAME_NOT_FOUND;
    private int size, updateDelay;
    private BPGuiItem filler, availableBankListItem, unavailableBankListItem;

    public Bank getOriginBank() {
        return originBank;
    }

    public HashMap<Integer, BPGuiItem> getBankItems() {
        return bankItems;
    }

    public Component getTitle() {
        return title;
    }

    public int getSize() {
        return Math.max(9, Math.min(54, size * 9));
    }

    public int getUpdateDelay() {
        return updateDelay;
    }

    public BPGuiItem getFiller() {
        return filler;
    }

    public BPGuiItem getAvailableBankListItem() {
        return availableBankListItem;
    }

    public BPGuiItem getUnavailableBankListItem() {
        return unavailableBankListItem;
    }

    public void setBankItem(int slot, BPGuiItem item) {
        bankItems.put(slot, item);
    }

    public void setTitle(Component title) {
        if (title != null) this.title = title;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setUpdateDelay(int updateDelay) {
        this.updateDelay = updateDelay;
    }

    public void setFiller(BPGuiItem filler) {
        this.filler = filler;
    }

    public void setAvailableBankListItem(BPGuiItem availableBankListItem) {
        this.availableBankListItem = availableBankListItem;
    }

    public void setUnavailableBankListItem(BPGuiItem unavailableBankListItem) {
        this.unavailableBankListItem = unavailableBankListItem;
    }

    public void openBankGui(Player p) {
        openBankGui(p, false);
    }

    public void openBankGui(Player p, boolean bypass) {
        if (!bypass) {
            if (ConfigValues.isOpeningPermissionsNeeded() && !BPUtils.hasPermission(p, "bankplus.open")) return;

            if (!BankUtils.isAvailable(originBank, p)) {
                BPMessages.sendIdentifier(p, "Cannot-Access-Bank");
                return;
            }
        }

        p.closeInventory();
        BPPlayer player = PlayerRegistry.get(p);
        if (player == null) player = PlayerRegistry.loadPlayer(p);

        BukkitTask updating = player.getBankUpdatingTask();
        if (updating != null) updating.cancel();

        Component title = this.title;
        if (BankPlus.INSTANCE().isPlaceholderApiHooked())
            title = BPChat.color(PlaceholderAPI.setPlaceholders(p, MiniMessage.miniMessage().serialize(title)));

        Inventory bankInventory = Bukkit.createInventory(new BankHolder(), getSize(), title);
        placeContent(bankItems, bankInventory, p);
        updateBankGuiMeta(bankInventory, p);

        if (updateDelay >= 0)
            player.setBankUpdatingTask(Bukkit.getScheduler().runTaskTimer(BankPlus.INSTANCE(), () -> updateBankGuiMeta(bankInventory, p), updateDelay, updateDelay));

        player.setOpenedBank(getOriginBank());
        if (ConfigValues.isPersonalSoundEnabled()) BPUtils.playSound(ConfigValues.getPersonalSound(), p);

        p.openInventory(bankInventory);
    }

    /**
     * Method to place all the bank items WITHOUT the lore.
     *
     * @param items         The bank items.
     * @param bankInventory The bank inventory.
     * @param p             The player needed for possible skulls.
     */
    public void placeContent(HashMap<Integer, BPGuiItem> items, Inventory bankInventory, Player p) {
        if (filler != null) {
            int size = bankInventory.getSize();
            for (int i = 0; i < size; i++) bankInventory.setItem(i, filler.item);
        }

        for (int slot : items.keySet()) {
            BPGuiItem item = items.get(slot);
            if (!item.isPlayerHead()) bankInventory.setItem(slot, item.item);
            else bankInventory.setItem(slot, BPItems.getNameHead(p.getName()));
        }
    }

    /**
     * Method to update every item meta in the bank inventory: lore, displayname and placeholders.
     *
     * @param playerOpenedInventory The inventory opened by the player, should check if he has opened a bank.
     * @param p                     The player to update placeholders and lore.
     */
    public void updateBankGuiMeta(Inventory playerOpenedInventory, Player p) {
        for (int slot : bankItems.keySet()) updateBankGuiItemMeta(slot, playerOpenedInventory, p);
    }

    /**
     * Method to update a single item meta: lore, displayname and placeholders.
     *
     * @param slot                  The slot of the item.
     * @param playerOpenedInventory The player inventory where to find the item.
     * @param p                     The player that have the inventory open.
     */
    public void updateBankGuiItemMeta(int slot, Inventory playerOpenedInventory, Player p) {
        BPGuiItem guiItem = bankItems.get(slot);

        ItemStack item = playerOpenedInventory.getItem(slot);
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();

        MiniMessage mm = MiniMessage.miniMessage();
        Component italicRemover = mm.deserialize("<!italic>");

        List<Component> lore = new ArrayList<>(), defaultLore = guiItem.lore.get(0);
        if (defaultLore != null)
            for (Component line : guiItem.lore.getOrDefault(BankUtils.getCurrentLevel(originBank, p), defaultLore))
                // For each lore line, add a reset component to avoid the italic lore bug.
                lore.add(italicRemover.append(line));

        Component displayName = italicRemover.append(guiItem.getItem().getItemMeta().displayName());

        if (BankPlus.INSTANCE().isPlaceholderApiHooked()) {
            meta.displayName(
                    BPChat.color(PlaceholderAPI.setPlaceholders(p, mm.serialize(displayName)))
            );
            meta.lore(
                    BPUtils.stringListToComponentList(
                            PlaceholderAPI.setPlaceholders(p, BPUtils.componentListToStringList(lore))
                    )
            );
        } else {
            meta.displayName(displayName);
            meta.lore(lore);
        }
        item.setItemMeta(meta);
    }

    /**
     * Represent a bankplus item that has no lore (because it is supposed to be updated
     * from internal gui processes and can changes between levels) and has actions to
     * be run when clicked on the gui.
     */
    public static class BPGuiItem {

        private ItemStack item; // The item stack containing displayname, lore, material etc..
        private HashMap<Integer, List<Component>> lore = new HashMap<>(); // Different lore between levels, 0 for default.
        private List<String> actions = new ArrayList<>(); // Actions on click.
        private boolean playerHead; // Boolean to check if it's a HEAD-%player% to place it when opening the gui.

        public ItemStack getItem() {
            return item;
        }

        /**
         * Get the lore of the item, based on the specified level.
         *
         * @return The lore as: K: Level - V: Lore
         */
        public HashMap<Integer, List<Component>> getLore() {
            return lore;
        }

        public List<String> getActions() {
            return actions;
        }

        public boolean isPlayerHead() {
            return playerHead;
        }

        public void setItem(ItemStack item) {
            this.item = item;
        }

        public void setLore(HashMap<Integer, List<Component>> lore) {
            this.lore = lore;
        }

        public void setActions(List<String> actions) {
            this.actions = actions;
        }

        public void setPlayerHead(boolean playerHead) {
            this.playerHead = playerHead;
        }

        public static BPGuiItem loadBankItem(ConfigurationSection itemSection) {
            BPGuiItem guiItem = new BPGuiItem();
            guiItem.setItem(BPItems.getNoLoreItemStackFromSection(itemSection));
            guiItem.setLore(BankUtils.getLevelLore(itemSection));
            guiItem.setActions(itemSection.getStringList("Actions"));
            guiItem.setPlayerHead("head-%player%".equalsIgnoreCase(itemSection.getString(BPItems.MATERIAL_KEY)));
            return guiItem;
        }
    }
}