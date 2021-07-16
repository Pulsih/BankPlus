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

import java.io.IOException;

public class GuiBankListener implements Listener {

    private BukkitTask runnable;
    private BankPlus plugin;
    public GuiBankListener(BankPlus plugin) {
        this.plugin = plugin;
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
                if (actionAmount.contains("CUSTOM")) {
                    SetUtils.playerWithdrawing.add(p.getUniqueId());
                    if (plugin.getMessages().getBoolean("Title-Custom-Amount.Enabled")) {
                        MethodUtils.sendTitle("Title-Custom-Amount.Title-Withdraw", p, plugin);
                    }
                    p.closeInventory();
                } else if (actionAmount.contains("ALL")) {
                    long amount = EconomyManager.getPersonalBalance(p, plugin);
                    EconomyManager.withdraw(p, amount, plugin);
                } else if (actionAmount.contains("HALF")) {
                    long amount = EconomyManager.getPersonalBalance(p, plugin) / 2;
                    EconomyManager.withdraw(p, amount, plugin);
                } else {
                    long amount = Long.parseLong(actionAmount);
                    EconomyManager.withdraw(p, amount, plugin);
                }
            }
            if (actionType.contains("Deposit")) {
                if (actionAmount.contains("CUSTOM")) {
                    SetUtils.playerDepositing.add(p.getUniqueId());
                    if (plugin.getMessages().getBoolean("Title-Custom-Amount.Enabled")) {
                        MethodUtils.sendTitle("Title-Custom-Amount.Title-Deposit", p, plugin);
                    }
                    p.closeInventory();
                } else if (actionAmount.contains("ALL")) {
                    long amount = (long) plugin.getEconomy().getBalance(p);
                    EconomyManager.deposit(p, amount, plugin);
                } else if (actionAmount.contains("HALF")) {
                    long amount = (long) (plugin.getEconomy().getBalance(p) / 2);
                    EconomyManager.deposit(p, amount, plugin);
                } else {
                    long amount = Long.parseLong(actionAmount);
                    EconomyManager.deposit(p, amount, plugin);
                }
            }
        }
    }

    @EventHandler
    public void updateGui(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();

        if (!e.getView().getTitle().equalsIgnoreCase(ChatUtils.c(plugin.getConfiguration().getString("Gui.Title")))) return;

        if (plugin.getConfiguration().getInt("Gui.Update-Delay") != 0) {
            runnable = Bukkit.getScheduler().runTaskTimer(plugin, () -> GuiBank.setItems(plugin, p), plugin.getConfiguration().getInt("Gui.Update-Delay") * 20L, 0);
        }
    }

    @EventHandler
    public void closeGUI(InventoryCloseEvent e) {
        try {
            if (!e.getView().getTitle().equalsIgnoreCase(ChatUtils.c(plugin.getConfiguration().getString("Gui.Title")))) return;
            runnable.cancel();
        } catch (NullPointerException ex) {}
    }
}