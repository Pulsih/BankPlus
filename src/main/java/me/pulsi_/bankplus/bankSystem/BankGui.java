package me.pulsi_.bankplus.bankSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.account.PlayerRegistry;
import me.pulsi_.bankplus.utils.BPHeads;
import me.pulsi_.bankplus.utils.BPLogger;
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
    private final HashMap<Integer, BankItem> bankItems = new HashMap<>();

    private Component title = BankUtils.DISPLAYNAME_NOT_FOUND;
    private int size, updateDelay;
    private BankItem filler, availableBankListItem, unavailableBankListItem;

    public Bank getOriginBank() {
        return originBank;
    }

    public HashMap<Integer, BankItem> getBankItems() {
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

    public String getFillerMaterial() {
        return fillerMaterial;
    }

    public boolean isFillerEnabled() {
        return fillerEnabled;
    }

    public boolean isFillerGlowing() {
        return fillerGlowing;
    }

    public BankItem getAvailableBankListItem() {
        return availableBankListItem;
    }

    public BankItem getUnavailableBankListItem() {
        return unavailableBankListItem;
    }

    public void setBankItem(int slot, BankItem item) {
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

    public void setFillerMaterial(String fillerMaterial) {
        this.fillerMaterial = fillerMaterial;
    }

    public void setFillerEnabled(boolean fillerEnabled) {
        this.fillerEnabled = fillerEnabled;
    }

    public void setFillerGlowing(boolean fillerGlowing) {
        this.fillerGlowing = fillerGlowing;
    }

    public void setAvailableBankListItem(BankItem availableBankListItem) {
        this.availableBankListItem = availableBankListItem;
    }

    public void setUnavailableBankListItem(BankItem unavailableBankListItem) {
        this.unavailableBankListItem = unavailableBankListItem;
    }

    public void openBankGui(Player p) {
        openBankGui(p, false);
    }

    public void openBankGui(Player p, boolean bypass) {
        if (!bypass) {
            if (ConfigValues.isOpeningPermissionsNeeded() && !BPUtils.hasPermission(p, "bankplus.open")) return;

            if (!BankUtils.isAvailable(originBank, p)) {
                BPMessages.send(p, "Cannot-Access-Bank");
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

        player.setOpenedBankGui(this);
        if (ConfigValues.isPersonalSoundEnabled()) {
            if (!BPUtils.playSound(ConfigValues.getPersonalSound(), p))
                BPLogger.warn("Occurred while trying to play PERSONAL sound for player \"" + p.getName() + "\".");
        }
        p.openInventory(bankInventory);
    }

    /**
     * Method to place all the bank items WITHOUT the lore.
     *
     * @param items         The bank items.
     * @param bankInventory The bank inventory.
     * @param p             The player needed for possible skulls.
     */
    public void placeContent(HashMap<Integer, BankItem> items, Inventory bankInventory, Player p) {
        if (fillerEnabled) {
            ItemStack filler = BPItems.getFiller(this);
            int size = bankInventory.getSize();
            for (int i = 0; i < size; i++) bankInventory.setItem(i, filler);
        }

        for (int slot : items.keySet()) {
            BankItem item = items.get(slot);
            if (item.isPlayerHead()) bankInventory.setItem(slot, BPHeads.getNameHead(p.getName()));
            else bankInventory.setItem(slot, items.get(slot).item);
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
        BankItem bankItem = bankItems.get(slot);

        ItemStack inventoryItem = playerOpenedInventory.getItem(slot);
        ItemMeta meta = inventoryItem.getItemMeta();

        List<String> actualLore = new ArrayList<>(), levelLore = bankItem.lore.get(BankUtils.getCurrentLevel(originBank, p));
        if (levelLore == null) levelLore = bankItem.lore.get(0);
        if (levelLore != null) for (String line : levelLore) actualLore.add(BPChat.color(line));

        String displayName = bankItem.getItem().getItemMeta().getDisplayName();
        if (BankPlus.INSTANCE().isPlaceholderApiHooked()) {
            meta.setDisplayName(BPChat.color(PlaceholderAPI.setPlaceholders(p, displayName)));
            meta.setLore(PlaceholderAPI.setPlaceholders(p, actualLore));
        } else {
            meta.setDisplayName(BPChat.color(displayName));
            meta.setLore(actualLore);
        }
        inventoryItem.setItemMeta(meta);
    }
}