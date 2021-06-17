package me.pulsi_.bankplus.guis;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.IOException;

public class GuiBankListener implements Listener {

    private EconomyManager economyManager;
    private BankPlus plugin;
    private GuiBank guiBank;
    public GuiBankListener(BankPlus plugin) {
        this.plugin = plugin;
        this.economyManager = new EconomyManager(plugin);
        this.guiBank = new GuiBank(plugin);
    }

    @EventHandler
    public void guiListener(InventoryClickEvent e) throws IOException {

        Player p = (Player) e.getWhoClicked();
        String displayName = plugin.getConfiguration().getString("Gui.Title");

        if (e.getCurrentItem() == null) return;
        if (!e.getView().getTitle().equals(ChatUtils.c(displayName))) return;

        e.setCancelled(true);

        for (String key : plugin.getConfiguration().getConfigurationSection("Gui.Items").getKeys(false)) {
            ConfigurationSection items = plugin.getConfiguration().getConfigurationSection("Gui.Items." + key);

            if (!(e.getSlot() + 1 == items.getInt("Slot"))) continue;
            if (items.getString("Action.Action-Type") == null) continue;

            String actionType = items.getString("Action.Action-Type");
            String actionAmount = items.getString("Action.Amount");

            if (actionType.contains("Withdraw")) {
                if (actionAmount.contains("ALL")) {
                    long amount = economyManager.getPersonalBalance(p);
                    economyManager.withdraw(p, amount);
                } else if (actionAmount.contains("HALF")) {
                    long amount = economyManager.getPersonalBalance(p) / 2;
                    economyManager.withdraw(p, amount);
                } else {
                    long amount = Long.parseLong(actionAmount);
                    economyManager.withdraw(p, amount);
                }
            }
            if (actionType.contains("Deposit")) {
                if (actionAmount.contains("ALL")) {
                    long amount = (long) plugin.getEconomy().getBalance(p);
                    economyManager.deposit(p, amount);
                } else if (actionAmount.contains("HALF")) {
                    long amount = (long) (plugin.getEconomy().getBalance(p) / 2);
                    economyManager.deposit(p, amount);
                } else {
                    long amount = Long.parseLong(actionAmount);
                    economyManager.deposit(p, amount);
                }
            }
        }
        if (plugin.getConfiguration().getBoolean("Gui.Update-On-Click")) {
            guiBank.openGui(p);
        }
    }
}