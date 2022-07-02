package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.gui.GuiHolder;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.BPDebugger;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
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
        Inventory inv = e.getClickedInventory();

        if (inv == null || inv.getHolder() == null || !(inv.getHolder() instanceof GuiHolder)) return;
        BPDebugger.debugGui(e);
        e.setCancelled(true);

        for (String key : Values.BANK.getGuiItems().getKeys(false)) {
            ConfigurationSection item = BankPlus.getCm().getConfig("bank").getConfigurationSection("Items." + key);
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
                            Methods.customWithdraw(p);
                            break;
                        case "deposit":
                            Methods.customDeposit(p);
                            break;
                    }
                    break;

                case "all":
                    switch (actionType) {
                        case "withdraw":
                            Methods.withdraw(p, EconomyManager.getBankBalance(p));
                            break;
                        case "deposit":
                            Methods.deposit(p, BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p)));
                            break;
                    }
                    break;

                case "half":
                    switch (actionType) {
                        case "withdraw":
                            Methods.withdraw(p, EconomyManager.getBankBalance(p).divide(BigDecimal.valueOf(2)));
                            break;
                        case "deposit":
                            Methods.deposit(p, BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p) / 2));
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
                            Methods.withdraw(p, amount);
                            break;
                        case "deposit":
                            Methods.deposit(p, amount);
                            break;
                    }
            }
        }
    }

    @EventHandler
    public void closeGUI(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (GuiHolder.tasks.containsKey(p)) GuiHolder.tasks.remove(p).cancel();
    }
}