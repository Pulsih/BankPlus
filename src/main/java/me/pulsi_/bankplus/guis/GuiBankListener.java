package me.pulsi_.bankplus.guis;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.utils.SetUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiBankListener implements Listener {

    private final BankPlus plugin;
    private static Map<UUID, BukkitTask> runnables = new HashMap<>();
    public GuiBankListener(BankPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void guiListener(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        if (!(p.getOpenInventory().getTopInventory().getHolder() instanceof GuiBankHolder)) return;
        e.setCancelled(true);

        for (String key : Values.CONFIG.getGuiItems().getKeys(false)) {
            ConfigurationSection items = plugin.config().getConfigurationSection("Gui.Items." + key);

            if (e.getSlot() + 1 != items.getInt("Slot") || items.getString("Action.Action-Type") == null) continue;

            String actionType = items.getString("Action.Action-Type").toLowerCase();
            String actionAmount = items.getString("Action.Amount").toLowerCase();

            long amount;
            switch (actionType) {
                case "withdraw":
                    switch (actionAmount) {
                        case "custom":
                            SetUtils.playerWithdrawing.add(p.getUniqueId());
                            if (Values.CONFIG.isTitleCustomAmountEnabled())
                                Methods.sendTitle("Title-Custom-Amount.Title-Withdraw", p, plugin);
                            MessageManager.chatWithdraw(p);
                            p.closeInventory();
                            break;

                        case "all":
                            amount = EconomyManager.getInstance().getBankBalance(p);
                            Methods.withdraw(p, amount, plugin);
                            break;

                        case "half":
                            amount = EconomyManager.getInstance().getBankBalance(p) / 2;
                            Methods.withdraw(p, amount, plugin);
                            break;

                        default:
                            try {
                                amount = Long.parseLong(actionAmount);
                                Methods.withdraw(p, amount, plugin);
                            } catch (NumberFormatException ex) {
                                ChatUtils.consoleMessage("&a&lBank&9&lPlus &cInvalid number in the withdraw amount!");
                            }
                            break;
                    }
                    break;

                case "deposit":
                    switch (actionAmount) {
                        case "custom":
                            SetUtils.playerDepositing.add(p.getUniqueId());
                            if (Values.CONFIG.isTitleCustomAmountEnabled())
                                Methods.sendTitle("Title-Custom-Amount.Title-Deposit", p, plugin);
                            MessageManager.chatDeposit(p);
                            p.closeInventory();
                            break;

                        case "all":
                            amount = (long) plugin.getEconomy().getBalance(p);
                            Methods.deposit(p, amount, plugin);
                            break;

                        case "half":
                            amount = (long) (plugin.getEconomy().getBalance(p) / 2);
                            Methods.deposit(p, amount, plugin);
                            break;

                        default:
                            try {
                                amount = Long.parseLong(actionAmount);
                                Methods.deposit(p, amount, plugin);
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
        Player p = (Player) e.getPlayer();
        int delay = Values.CONFIG.getGuiUpdateDelay();
        if (delay != 0) {
            runnables.put(p.getUniqueId(), Bukkit.getScheduler().runTaskTimer(plugin, () -> updateLore(p),0, delay * 20L));
        }
    }

    @EventHandler
    public void closeGUI(InventoryCloseEvent e) {
        if (runnables.containsKey(e.getPlayer().getUniqueId())) {
            runnables.remove(e.getPlayer().getUniqueId()).cancel();
        }
    }

    private void updateLore(Player p) {
        Inventory inventory = p.getOpenInventory().getTopInventory();
        if (!(inventory.getHolder() instanceof GuiBankHolder)) return;

        ConfigurationSection c = Values.CONFIG.getGuiItems();
        for (String items : c.getKeys(false)) {
            ItemStack i = inventory.getItem(c.getConfigurationSection(items).getInt("Slot") - 1);
            if (i != null && i.hasItemMeta())
                i.setItemMeta(ItemUtils.setLore(c.getConfigurationSection(items), i, p));
        }
    }
}