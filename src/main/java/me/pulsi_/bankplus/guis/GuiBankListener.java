package me.pulsi_.bankplus.guis;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiBankListener implements Listener {

    private EconomyManager economyManager;
    private BankPlus plugin;

    public GuiBankListener(BankPlus plugin) {
        this.plugin = plugin;
        this.economyManager = new EconomyManager(plugin);
    }

    @EventHandler
    public void guiWithdraw(InventoryClickEvent e) {

        String displayName = plugin.getConfiguration().getString("Gui.Title");

        if (e.getCurrentItem() == null) return;
        if (!e.getView().getTitle().equals(ChatUtils.c(displayName))) return;

        e.setCancelled(true);

        for (String key : plugin.getConfiguration().getConfigurationSection("Gui.Items").getKeys(false)) {
            ConfigurationSection items = plugin.getConfiguration().getConfigurationSection("Gui.Items." + key);

            if (!(e.getSlot() + 1 == items.getInt("Slot"))) continue;

            if (items.getString("Action.Action-Type") == null) continue;

            String actionType = items.getString("Action.Action-Type");
            String amount = items.getString("Action.Amount");
            Player p = (Player) e.getWhoClicked();

            if (actionType.contains("Withdraw")) {
                if (amount.contains("ALL")) {
                    int intAmount = economyManager.getPersonalBalance(p);
                    economyManager.withdraw(p, intAmount);
                } else if (amount.contains("HALF")) {
                    int intAmount = economyManager.getPersonalBalance(p) / 2;
                    economyManager.withdraw(p, intAmount);
                } else {
                    int intAmount = Integer.parseInt(amount);
                    economyManager.withdraw(p, intAmount);
                }
            }
            if (actionType.contains("Deposit")) {
                if (amount.contains("ALL")) {
                    int intAmount = (int) plugin.getEconomy().getBalance(p);
                    economyManager.deposit(p, intAmount);
                } else if (amount.contains("HALF")) {
                    int intAmount = (int) plugin.getEconomy().getBalance(p) / 2;
                    economyManager.deposit(p, intAmount);
                } else {
                    int intAmount = Integer.parseInt(amount);
                    economyManager.deposit(p, intAmount);
                }
            }
        }
    }
}