package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.guis.BanksHolder;
import me.pulsi_.bankplus.guis.BanksManager;
import me.pulsi_.bankplus.utils.BPDebugger;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMethods;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;

public class GuiListener implements Listener {

    @EventHandler
    public void guiListener(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();
        if (!BanksHolder.openedInventory.containsKey(p)) return;

        String identifier = BanksHolder.openedInventory.get(p);
        if (!BanksHolder.bankGetter.containsKey(identifier)) return;

        Inventory inv = e.getClickedInventory();

        if (inv == null || inv.getHolder() == null || !(inv.getHolder() instanceof BanksHolder)) return;
        BPDebugger.debugGui(e);
        e.setCancelled(true);

        ConfigurationSection items = BanksManager.getItems(identifier);
        if (items == null) return;

        for (String key : items.getKeys(false)) {
            ConfigurationSection item = BanksManager.getConfig(identifier).getConfigurationSection("Items." + key);
            if (item == null) continue;

            String actionType = item.getString("Action.Action-Type");
            String actionAmount = item.getString("Action.Amount");
            if (e.getSlot() + 1 != item.getInt("Slot") || actionType == null || actionAmount == null) continue;

            actionType = actionType.toLowerCase();
            actionAmount = actionAmount.toLowerCase();

            switch (actionAmount) {
                case "custom":
                    switch (actionType) {
                        case "withdraw":
                            BPMethods.customWithdraw(p);
                            break;
                        case "deposit":
                            BPMethods.customDeposit(p);
                            break;
                    }
                    break;

                case "all":
                    switch (actionType) {
                        case "withdraw":
                            BPMethods.withdraw(p, SingleEconomyManager.getBankBalance(p));
                            break;
                        case "deposit":
                            BPMethods.deposit(p, BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p)));
                            break;
                    }
                    break;

                case "half":
                    switch (actionType) {
                        case "withdraw":
                            BPMethods.withdraw(p, SingleEconomyManager.getBankBalance(p).divide(BigDecimal.valueOf(2)));
                            break;
                        case "deposit":
                            BPMethods.deposit(p, BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p) / 2));
                            break;
                    }
                    break;

                case "upgrade":
                    switch (actionType) {
                        case "withdraw":
                            BPMethods.withdraw(p, SingleEconomyManager.getBankBalance(p).divide(BigDecimal.valueOf(2)));
                            break;
                        case "deposit":
                            BPMethods.deposit(p, BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p) / 2));
                            break;
                    }
                    break;

                default:
                    BigDecimal amount;
                    try {
                        amount = new BigDecimal(actionAmount);
                    } catch (NumberFormatException ex) {
                        BPLogger.error("Invalid withdraw number! (Item-Path: " + item + ", Action-Type: " + actionType + ", Action-Number: " + actionAmount + ")");
                        return;
                    }
                    switch (actionType) {
                        case "withdraw":
                            BPMethods.withdraw(p, amount);
                            break;
                        case "deposit":
                            BPMethods.deposit(p, amount);
                            break;
                    }
            }
        }
    }

    @EventHandler
    public void closeGUI(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (BanksHolder.tasks.containsKey(p)) BanksHolder.tasks.remove(p).cancel();
    }
}