package me.pulsi_.bankplus.guis;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.MethodUtils;
import me.pulsi_.bankplus.utils.SetUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitTask;

public class GuiBankListener implements Listener {

    BukkitTask runnable;
    private BankPlus plugin;
    public GuiBankListener(BankPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void guiListener(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();
        EconomyManager economy = new EconomyManager(plugin);
        String displayName = plugin.getConfiguration().getString("Gui.Title");

        if (e.getCurrentItem() == null || !e.getView().getTitle().equals(ChatUtils.c(displayName))) return;
        e.setCancelled(true);

        for (String key : plugin.getConfiguration().getConfigurationSection("Gui.Items").getKeys(false)) {
            ConfigurationSection items = plugin.getConfiguration().getConfigurationSection("Gui.Items." + key);

            if (e.getSlot() + 1 != items.getInt("Slot") || items.getString("Action.Action-Type") == null) continue;

            final String actionType = items.getString("Action.Action-Type");
            final String actionAmount = items.getString("Action.Amount");

            long amount;
            switch (actionType) {
                case "Withdraw":
                    switch (actionAmount) {
                        case "CUSTOM":
                            SetUtils.playerWithdrawing.add(p.getUniqueId());
                            if (plugin.getMessages().getBoolean("Title-Custom-Amount.Enabled"))
                                MethodUtils.sendTitle("Title-Custom-Amount.Title-Withdraw", p, plugin);
                            p.closeInventory();
                            break;

                        case "ALL":
                            amount = economy.getBankBalance(p);
                            MethodUtils.withdraw(p, amount, plugin);
                            break;

                        case "HALF":
                            amount = economy.getBankBalance(p) / 2;
                            MethodUtils.withdraw(p, amount, plugin);
                            break;

                        default:
                            amount = Long.parseLong(actionAmount);
                            MethodUtils.withdraw(p, amount, plugin);
                            break;
                    }
                    break;

                case "Deposit":
                    switch (actionAmount) {
                        case "CUSTOM":
                            SetUtils.playerDepositing.add(p.getUniqueId());
                            if (plugin.getMessages().getBoolean("Title-Custom-Amount.Enabled"))
                                MethodUtils.sendTitle("Title-Custom-Amount.Title-Deposit", p, plugin);
                            p.closeInventory();
                            break;

                        case "ALL":
                            amount = (long) plugin.getEconomy().getBalance(p);
                            MethodUtils.deposit(p, amount, plugin);
                            break;

                        case "HALF":
                            amount = (long) (plugin.getEconomy().getBalance(p) / 2);
                            MethodUtils.deposit(p, amount, plugin);
                            break;

                        default:
                            amount = Long.parseLong(actionAmount);
                            MethodUtils.deposit(p, amount, plugin);
                            break;
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void updateGui(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();
        if (!e.getView().getTitle().equalsIgnoreCase(ChatUtils.c(plugin.getConfiguration().getString("Gui.Title")))) return;
        if (plugin.getConfiguration().getInt("Gui.Update-Delay") != 0) {
            runnable = Bukkit.getScheduler().runTaskTimer(plugin, () -> GuiBank.updateLore(p, plugin),
                    plugin.getConfiguration().getInt("Gui.Update-Delay") * 20L, 0);
        }
    }

    @EventHandler
    public void closeGUI(InventoryCloseEvent e) {
        if (runnable == null || !e.getView().getTitle().equalsIgnoreCase(ChatUtils.c(plugin.getConfiguration().getString("Gui.Title")))) return;
        runnable.cancel();
    }
}