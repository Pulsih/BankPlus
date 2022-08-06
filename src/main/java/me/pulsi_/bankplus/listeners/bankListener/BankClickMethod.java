package me.pulsi_.bankplus.listeners.bankListener;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.guis.BanksHolder;
import me.pulsi_.bankplus.guis.BanksListGui;
import me.pulsi_.bankplus.guis.BanksManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;

public class BankClickMethod {

    public static void process(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory bank = e.getClickedInventory();
        if (bank == null || bank.getHolder() == null || !(bank.getHolder() instanceof BanksHolder)) return;
        e.setCancelled(true);

        String bankName = BanksHolder.openedBank.getOrDefault(p.getUniqueId(), "");
        if (!BanksHolder.bankGetter.containsKey(bankName)) return;

        if (bankName.equals(BanksListGui.multipleBanksGuiID)) {
            int slot = e.getSlot();
            if (!BanksListGui.getBankFromSlot.containsKey(p.getName() + "." + slot)) return;

            BanksHolder.openBank(p, BanksListGui.getBankFromSlot.get(p.getName() + "." + slot));
            for (int i = 0; i < BanksManager.getBankNames().size(); i++) BanksListGui.getBankFromSlot.remove(p.getName() + "." + i);
            return;
        }

        ConfigurationSection items = BanksManager.getItems(bankName);
        if (items == null) return;

        FileConfiguration config = BanksManager.getConfig(bankName);
        if (config == null) return;

        for (String key : items.getKeys(false)) {
            ConfigurationSection item = config.getConfigurationSection("Items." + key);
            if (item == null) continue;

            String actionType = item.getString("Action.Action-Type");
            String actionAmount = item.getString("Action.Amount");
            if (e.getSlot() + 1 != item.getInt("Slot") || actionType == null) continue;

            actionType = actionType.toLowerCase();
            actionAmount = actionAmount == null ? "null" : actionAmount.toLowerCase();

            BigDecimal amount;
            boolean isMulti = Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled();
            switch (actionType) {
                case "deposit":
                    switch (actionAmount) {
                        case "custom":
                            BPMethods.customDeposit(p);
                            break;

                        case "all":
                            amount = BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p));
                            if (isMulti) MultiEconomyManager.deposit(p, amount, bankName);
                            else SingleEconomyManager.deposit(p, amount);
                            break;

                        case "half":
                            amount = BigDecimal.valueOf(BankPlus.getEconomy().getBalance(p) / 2);
                            if (isMulti) MultiEconomyManager.deposit(p, amount, bankName);
                            else SingleEconomyManager.deposit(p, amount);
                            break;

                        default:
                            try {
                                amount = new BigDecimal(actionAmount);
                            } catch (NumberFormatException ex) {
                                BPLogger.error("Invalid deposit number! (Path: " + item + ", Number: " + actionAmount + ")");
                                return;
                            }
                            if (isMulti) MultiEconomyManager.deposit(p, amount, bankName);
                            else SingleEconomyManager.deposit(p, amount);
                            break;
                    }
                    break;

                case "withdraw":
                    switch (actionAmount) {
                        case "custom":
                            BPMethods.customWithdraw(p);
                            break;

                        case "all":
                            if (isMulti) {
                                amount = MultiEconomyManager.getBankBalance(p, bankName);
                                MultiEconomyManager.withdraw(p, amount, bankName);
                            } else {
                                amount = SingleEconomyManager.getBankBalance(p);
                                SingleEconomyManager.withdraw(p, amount);
                            }
                            break;

                        case "half":
                            if (isMulti) {
                                amount = MultiEconomyManager.getBankBalance(p, bankName).divide(BigDecimal.valueOf(2));
                                MultiEconomyManager.withdraw(p, amount, bankName);
                            } else {
                                amount = SingleEconomyManager.getBankBalance(p).divide(BigDecimal.valueOf(2));
                                SingleEconomyManager.withdraw(p, amount);
                            }
                            break;

                        default:
                            try {
                                amount = new BigDecimal(actionAmount);
                            } catch (NumberFormatException ex) {
                                BPLogger.error("Invalid withdraw number! (Path: " + item + ", Number: " + actionAmount + ")");
                                return;
                            }
                            if (isMulti) MultiEconomyManager.withdraw(p, amount, bankName);
                            else SingleEconomyManager.withdraw(p, amount);
                            break;
                    }
                    break;

                case "upgrade":
                    BanksManager.upgradeBank(p, bankName);
                    break;
            }
        }
    }
}