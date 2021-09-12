package me.pulsi_.bankplus.guis;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
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

    private static BukkitTask runnable;
    private final BankPlus plugin;
    public GuiBankListener(BankPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void guiListener(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();
        final EconomyManager economy = new EconomyManager(plugin);
        final MessageManager messMan = new MessageManager(plugin);
        final String title = ChatUtils.color(plugin.config().getString("Gui.Title"));

        if (e.getCurrentItem() == null || !e.getView().getTitle().equals(title)) return;
        e.setCancelled(true);

        for (String key : plugin.config().getConfigurationSection("Gui.Items").getKeys(false)) {
            ConfigurationSection items = plugin.config().getConfigurationSection("Gui.Items." + key);

            if (e.getSlot() + 1 != items.getInt("Slot") || items.getString("Action.Action-Type") == null) continue;

            final String actionType = items.getString("Action.Action-Type");
            final String actionAmount = items.getString("Action.Amount");

            long amount;
            switch (actionType) {
                case "Withdraw":
                    switch (actionAmount) {
                        case "CUSTOM":
                            SetUtils.playerWithdrawing.add(p.getUniqueId());
                            if (plugin.messages().getBoolean("Title-Custom-Amount.Enabled"))
                                MethodUtils.sendTitle("Title-Custom-Amount.Title-Withdraw", p, plugin);
                            messMan.chatWithdraw(p);
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
                            try {
                                amount = Long.parseLong(actionAmount);
                                MethodUtils.withdraw(p, amount, plugin);
                            } catch (NumberFormatException ex) {
                                ChatUtils.consoleMessage("&a&lBank&9&lPlus &cInvalid number in the withdraw amount!");
                            }
                            break;
                    }
                    break;

                case "Deposit":
                    switch (actionAmount) {
                        case "CUSTOM":
                            SetUtils.playerDepositing.add(p.getUniqueId());
                            if (plugin.messages().getBoolean("Title-Custom-Amount.Enabled"))
                                MethodUtils.sendTitle("Title-Custom-Amount.Title-Deposit", p, plugin);
                            messMan.chatDeposit(p);
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
                            try {
                                amount = Long.parseLong(actionAmount);
                                MethodUtils.deposit(p, amount, plugin);
                                break;
                            } catch (NumberFormatException ex) {
                                ChatUtils.consoleMessage("&a&lBank&9&lPlus &cInvalid number in the deposit amount!");
                            }
                            break;
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void updateGui(InventoryOpenEvent e) {
        final Player p = (Player) e.getPlayer();
        final String title = ChatUtils.color(plugin.config().getString("Gui.Title"));
        if (!e.getView().getTitle().equals(title) || plugin.config().getInt("Gui.Update-Delay") == 0) return;

        final int delay = plugin.config().getInt("Gui.Update-Delay");
        runnable = Bukkit.getScheduler().runTaskTimer(plugin, () -> new GuiBank(plugin).updateLore(p, title),0, delay * 20L);
    }

    @EventHandler
    public void closeGUI(InventoryCloseEvent e) {
        final String title = ChatUtils.color(plugin.config().getString("Gui.Title"));
        if (runnable != null && e.getView().getTitle().equals(title))
            runnable.cancel();
    }
}