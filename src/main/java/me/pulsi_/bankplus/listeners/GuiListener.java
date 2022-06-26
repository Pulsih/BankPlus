package me.pulsi_.bankplus.listeners;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.gui.GuiHolder;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.math.BigDecimal;

public class GuiListener implements Listener {

    @EventHandler
    public void guiListener(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        if (e.getClickedInventory() == null || e.getClickedInventory().getHolder() == null || !(e.getClickedInventory().getHolder() instanceof GuiHolder)) return;
        e.setCancelled(true);

        for (String key : Values.BANK.getGuiItems().getKeys(false)) {
            ConfigurationSection item = BankPlus.getCm().getConfig("bank").getConfigurationSection("Items." + key);

            if (e.getSlot() + 1 != item.getInt("Slot") || item.getString("Action.Action-Type") == null) continue;

            String actionType = item.getString("Action.Action-Type").toLowerCase();
            String actionAmount = item.getString("Action.Amount").toLowerCase();

            switch (actionType) {
                case "withdraw":
                    switch (actionAmount) {
                        case "custom":
                            Methods.customWithdraw(p);
                            break;

                        case "all":
                            Methods.withdraw(p, EconomyManager.getBankBalance(p));
                            break;

                        case "half":
                            Methods.withdraw(p, EconomyManager.getBankBalance(p).divide(BigDecimal.valueOf(2)));
                            break;

                        default:
                            try {
                                Methods.withdraw(p, new BigDecimal(actionAmount));
                            } catch (NumberFormatException ex) {
                                ChatUtils.log("&a&lBank&9&lPlus &cInvalid number in the withdraw amount!");
                            }
                            break;
                    }
                    break;

                case "deposit":
                    double bal = BankPlus.getEconomy().getBalance(p);
                    switch (actionAmount) {
                        case "custom":
                            Methods.customDeposit(p);
                            break;

                        case "all":
                            Methods.deposit(p, BigDecimal.valueOf(bal));
                            break;

                        case "half":
                            Methods.deposit(p, BigDecimal.valueOf(bal / 2));
                            break;

                        default:
                            try {
                                Methods.deposit(p, new BigDecimal(actionAmount));
                            } catch (NumberFormatException ex) {
                                ChatUtils.log("&a&lBank&9&lPlus &cInvalid number in the deposit amount!");
                            }
                            break;
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void closeGUI(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (GuiHolder.tasks.containsKey(p)) GuiHolder.tasks.remove(p).cancel();
    }
}