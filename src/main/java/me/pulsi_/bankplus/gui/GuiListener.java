package me.pulsi_.bankplus.gui;

import me.pulsi_.bankplus.BankPlus;
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

public class GuiListener implements Listener {

    @EventHandler
    public void guiListener(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        if (!(p.getOpenInventory().getTopInventory().getHolder() instanceof GuiHolder)) return;
        e.setCancelled(true);

        for (String key : Values.CONFIG.getGuiItems().getKeys(false)) {
            ConfigurationSection items = BankPlus.getInstance().config().getConfigurationSection("Gui.Items." + key);

            if (e.getSlot() + 1 != items.getInt("Slot") || items.getString("Action.Action-Type") == null) continue;

            String actionType = items.getString("Action.Action-Type").toLowerCase();
            String actionAmount = items.getString("Action.Amount").toLowerCase();

            long amount;
            switch (actionType) {
                case "withdraw":
                    switch (actionAmount) {
                        case "custom":
                            Methods.customWithdraw(p);
                            break;

                        case "all":
                            amount = EconomyManager.getInstance().getBankBalance(p);
                            Methods.withdraw(p, amount);
                            break;

                        case "half":
                            amount = EconomyManager.getInstance().getBankBalance(p) / 2;
                            Methods.withdraw(p, amount);
                            break;

                        default:
                            try {
                                amount = Long.parseLong(actionAmount);
                                Methods.withdraw(p, amount);
                            } catch (NumberFormatException ex) {
                                ChatUtils.log("&a&lBank&9&lPlus &cInvalid number in the withdraw amount!");
                            }
                            break;
                    }
                    break;

                case "deposit":
                    switch (actionAmount) {
                        case "custom":
                            Methods.customDeposit(p);
                            break;

                        case "all":
                            amount = (long) BankPlus.getEconomy().getBalance(p);
                            Methods.deposit(p, amount);
                            break;

                        case "half":
                            amount = (long) (BankPlus.getEconomy().getBalance(p) / 2);
                            Methods.deposit(p, amount);
                            break;

                        default:
                            try {
                                amount = Long.parseLong(actionAmount);
                                Methods.deposit(p, amount);
                                break;
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