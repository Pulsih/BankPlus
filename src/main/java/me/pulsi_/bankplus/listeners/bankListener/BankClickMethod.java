package me.pulsi_.bankplus.listeners.bankListener;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BankPlusPlayer;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankGuis.BanksHolder;
import me.pulsi_.bankplus.bankGuis.BanksListGui;
import me.pulsi_.bankplus.bankGuis.BanksManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;
import java.util.HashMap;

public class BankClickMethod {

    public static void process(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory bank = e.getClickedInventory();
        if (bank == null || bank.getHolder() == null || !(bank.getHolder() instanceof BanksHolder)) return;
        e.setCancelled(true);

        BankPlusPlayer player = BankPlus.instance().getPlayers().get(p.getUniqueId());
        if (player.getOpenedBank() == null) return;

        String bankName = player.getOpenedBank().getIdentifier();
        if (bankName.equals(BanksListGui.multipleBanksGuiID)) {
            int slot = e.getSlot();
            HashMap<String, String> slots = player.getPlayerBankClickHolder();
            if (!slots.containsKey(p.getName() + "." + slot)) return;

            new BanksHolder().openBank(p, slots.get(p.getName() + "." + slot));
            for (int i = 0; i < slots.size(); i++) slots.remove(p.getName() + "." + i);
            return;
        }

        BanksManager banksManager = new BanksManager(bankName);
        ConfigurationSection items = banksManager.getItems();
        if (items == null) return;

        FileConfiguration config = banksManager.getConfig();
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
            SingleEconomyManager singleEconomyManager = new SingleEconomyManager(p);
            MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);
            Economy economy = BankPlus.instance().getEconomy();

            switch (actionType) {
                case "deposit":
                    switch (actionAmount) {
                        case "custom":
                            BPMethods.customDeposit(p);
                            break;

                        case "all":
                            amount = BigDecimal.valueOf(economy.getBalance(p));
                            if (isMulti) multiEconomyManager.deposit(amount, bankName);
                            else singleEconomyManager.deposit(amount);
                            break;

                        case "half":
                            amount = BigDecimal.valueOf(economy.getBalance(p) / 2);
                            if (isMulti) multiEconomyManager.deposit(amount, bankName);
                            else singleEconomyManager.deposit(amount);
                            break;

                        default:
                            try {
                                if (!actionAmount.endsWith("%")) amount = new BigDecimal(actionAmount);
                                else {
                                    BigDecimal percentage = new BigDecimal(actionAmount.replace("%", "")).divide(BigDecimal.valueOf(100));
                                    amount = BigDecimal.valueOf(economy.getBalance(p)).multiply(percentage);
                                }
                            } catch (NumberFormatException ex) {
                                BPLogger.error("Invalid deposit number! (Path: " + item + ", Number: " + actionAmount + ")");
                                return;
                            }
                            if (isMulti) multiEconomyManager.deposit(amount, bankName);
                            else singleEconomyManager.deposit(amount);
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
                                amount = multiEconomyManager.getBankBalance(bankName);
                                multiEconomyManager.withdraw(amount, bankName);
                            } else {
                                amount = singleEconomyManager.getBankBalance();
                                singleEconomyManager.withdraw(amount);
                            }
                            break;

                        case "half":
                            if (isMulti) {
                                amount = multiEconomyManager.getBankBalance(bankName).divide(BigDecimal.valueOf(2));
                                multiEconomyManager.withdraw(amount, bankName);
                            } else {
                                amount = singleEconomyManager.getBankBalance().divide(BigDecimal.valueOf(2));
                                singleEconomyManager.withdraw(amount);
                            }
                            break;

                        default:
                            try {
                                if (!actionAmount.endsWith("%")) amount = new BigDecimal(actionAmount);
                                else {
                                    BigDecimal bal;
                                    if (isMulti) bal = multiEconomyManager.getBankBalance(bankName);
                                    else bal = singleEconomyManager.getBankBalance();

                                    BigDecimal percentage = new BigDecimal(actionAmount.replace("%", "")).divide(BigDecimal.valueOf(100));
                                    amount = bal.multiply(percentage);
                                }
                            } catch (NumberFormatException ex) {
                                BPLogger.error("Invalid withdraw number! (Path: " + item + ", Number: " + actionAmount + ")");
                                return;
                            }
                            if (isMulti) multiEconomyManager.withdraw(amount, bankName);
                            else singleEconomyManager.withdraw(amount);
                            break;
                    }
                    break;

                case "upgrade":
                    banksManager.upgradeBank(p);
                    break;
            }
        }
    }
}