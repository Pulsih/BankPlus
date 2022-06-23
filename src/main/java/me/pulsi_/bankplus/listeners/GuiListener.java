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

            BigDecimal amount;
            switch (actionType) {
                case "withdraw":
                    switch (actionAmount) {
                        case "custom":
                            Methods.customWithdraw(p);
                            break;

                        case "all":
                            amount = EconomyManager.getBankBalance(p);
                            Methods.withdraw(p, amount);
                            break;

                        case "half":
                            amount = EconomyManager.getBankBalance(p).divide(BigDecimal.valueOf(2));
                            Methods.withdraw(p, amount);
                            break;

                        default:
                            try {
                                amount = new BigDecimal(actionAmount);
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
                            amount = BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p));
                            Methods.deposit(p, amount);
                            break;

                        case "half":
                            amount = BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p) / 2);
                            Methods.deposit(p, amount);
                            break;

                        default:
                            try {
                                amount = new BigDecimal(actionAmount);
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