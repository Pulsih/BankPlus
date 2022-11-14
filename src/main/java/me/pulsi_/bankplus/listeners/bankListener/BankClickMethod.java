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
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class BankClickMethod {

    public static void process(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory bank = e.getClickedInventory();
        if (bank == null || bank.getHolder() == null || !(bank.getHolder() instanceof BanksHolder)) return;
        e.setCancelled(true);

        BankPlusPlayer player = BankPlus.INSTANCE.getPlayerRegistry().get(p);
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
            ConfigurationSection itemValues = config.getConfigurationSection("Items." + key);
            if (itemValues == null || e.getSlot() + 1 != itemValues.getInt("Slot")) continue;

            List<String> actions = itemValues.getStringList("Actions");
            if (actions.isEmpty()) continue;

            BigDecimal amount;
            boolean isMulti = Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled();
            SingleEconomyManager singleEconomyManager = new SingleEconomyManager(p);
            MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);
            Economy economy = BankPlus.INSTANCE.getEconomy();

            for (String actionType : actions) {
                String[] parts = actionType.split(" ");
                String identifier = parts.length > 0 ? parts[0] : null, value = null;

                if (identifier == null) continue;
                else {
                    if (parts.length != 1) {
                        for (int i = 1; i < parts.length; i++) {
                            value += parts[i];
                            if (i + 1 < parts.length) value += " ";
                        }
                    }
                }

                switch (identifier) {
                    case "[CONSOLE]":
                        if (value == null) {
                            BPLogger.warn("Cannot dispatch console command! The action " + identifier + " in the bank " + bankName + " does not specify a command!");
                            return;
                        }
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value);
                        break;

                    case "[DEPOSIT]":
                        if (value == null) {
                            BPLogger.warn("Cannot deposit for " + p.getName() + "! The action " + identifier + " in the bank " + bankName + " does not specify a value!");
                            return;
                        }
                        if (value.equals("CUSTOM")) {
                            BPMethods.customDeposit(p, bankName);
                            return;
                        }
                        try {
                            if (!value.endsWith("%")) amount = new BigDecimal(value);
                            else {
                                BigDecimal percentage = new BigDecimal(value.replace("%", "")).divide(BigDecimal.valueOf(100));
                                amount = BigDecimal.valueOf(economy.getBalance(p)).multiply(percentage);
                            }
                        } catch (NumberFormatException ex) {
                            BPLogger.error("Invalid deposit number! (Path: " + itemValues + ", Number: " + value + ")");
                            continue;
                        }
                        if (!isMulti) singleEconomyManager.deposit(amount);
                        else multiEconomyManager.deposit(amount, bankName);
                        break;

                    case "[PLAYER]":
                        if (value == null) {
                            BPLogger.warn("Cannot force chat for " + p.getName() + "! The action " + identifier + " in the bank " + bankName + " does not specify a value!");
                            return;
                        }
                        p.chat(value);
                        break;

                    case "[UPGRADE]":
                        banksManager.upgradeBank(p);
                        break;

                    case "[WITHDRAW]":
                        if (value == null) {
                            BPLogger.warn("Cannot withdraw for " + p.getName() + "! The action " + identifier + " in the bank " + bankName + " does not specify a value!");
                            return;
                        }
                        if (value.equals("CUSTOM")) {
                            BPMethods.customWithdraw(p, bankName);
                            return;
                        }
                        try {
                            if (!value.endsWith("%")) amount = new BigDecimal(value);
                            else {
                                BigDecimal percentage = new BigDecimal(value.replace("%", "")).divide(BigDecimal.valueOf(100));
                                if (!isMulti) amount = singleEconomyManager.getBankBalance().multiply(percentage);
                                else amount = multiEconomyManager.getBankBalance(bankName).multiply(percentage);
                            }
                        } catch (NumberFormatException ex) {
                            BPLogger.error("Invalid withdraw number! (Path: " + itemValues + ", Number: " + value + ")");
                            continue;
                        }
                        if (!isMulti) singleEconomyManager.withdraw(amount);
                        else multiEconomyManager.withdraw(amount, bankName);
                }
            }
        }
    }
}