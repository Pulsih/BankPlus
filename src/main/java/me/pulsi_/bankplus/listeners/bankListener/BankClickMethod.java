package me.pulsi_.bankplus.listeners.bankListener;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.bankSystem.BankHolder;
import me.pulsi_.bankplus.bankSystem.BankListGui;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.MultiEconomyManager;
import me.pulsi_.bankplus.economy.SingleEconomyManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
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
        if (bank == null || bank.getHolder() == null || !(bank.getHolder() instanceof BankHolder)) return;
        e.setCancelled(true);

        BPPlayer player = BankPlus.INSTANCE.getPlayerRegistry().get(p);
        if (player.getOpenedBank() == null) return;

        int slot = e.getSlot();
        String bankName = player.getOpenedBank().getIdentifier();
        if (bankName.equals(BankListGui.multipleBanksGuiID)) {
            HashMap<String, String> slots = player.getPlayerBankClickHolder();
            if (!slots.containsKey(p.getName() + "." + slot)) return;

            p.closeInventory();
            BankUtils.openBank(p, slots.get(p.getName() + "." + slot), false);
            for (int i = 0; i < slots.size(); i++)
                slots.remove(p.getName() + "." + i);

            return;
        }

        BankReader bankReader = new BankReader(bankName);
        ConfigurationSection items = bankReader.getItems();
        if (items == null) return;

        FileConfiguration config = bankReader.getConfig();
        if (config == null) return;

        for (String key : items.getKeys(false)) {
            ConfigurationSection itemValues = config.getConfigurationSection("Items." + key);
            if (itemValues == null || slot + 1 != itemValues.getInt("Slot")) continue;

            List<String> actions = itemValues.getStringList("Actions");
            if (actions.isEmpty()) {
                // Check if they are still using the old methods, still process it and invite them to update it.
                if (processOldClickMethod(itemValues, bankName, p))
                    BPLogger.warn("Warning! You are using an old format for the item action! Check out the BankPlus wiki and update it ");
                continue;
            }

            BigDecimal amount;
            boolean isMulti = Values.MULTIPLE_BANKS.isMultipleBanksEnabled();
            SingleEconomyManager singleEconomyManager = new SingleEconomyManager(p);
            MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);
            Economy economy = BankPlus.INSTANCE.getEconomy();

            for (String actionType : actions) {
                String[] parts = actionType.split(" ");
                int length = parts.length;

                String identifier = length > 0 ? parts[0] : null, value = "";
                if (identifier == null) continue;

                if (identifier.equals("[UPGRADE]")) {
                    bankReader.upgradeBank(p);
                    continue;
                }

                if (length != 1) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 1; i < length; i++) {
                        builder.append(parts[i]);
                        if (i + 1 < length) builder.append(" ");
                    }
                    value = builder.toString();
                }

                if (value.equals("")) {
                    BPLogger.warn("No value specified! Item: " + key + ". File: " + bankName + ".yml. Action: " + identifier + ".");
                    continue;
                }

                switch (identifier) {
                    case "[CONSOLE]":
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value);
                        break;

                    case "[DEPOSIT]":
                        if (value.equals("CUSTOM")) {
                            BPUtils.customDeposit(p, bankName);
                            continue;
                        }
                        try {
                            if (!value.endsWith("%")) amount = new BigDecimal(value);
                            else {
                                BigDecimal percentage = new BigDecimal(value.replace("%", "")).divide(BigDecimal.valueOf(100));
                                amount = BigDecimal.valueOf(economy.getBalance(p)).multiply(percentage);
                            }
                        } catch (NumberFormatException ex) {
                            BPLogger.warn("Invalid deposit number! (Button: " + key + ", Number: " + value + ")");
                            continue;
                        }
                        if (!isMulti) singleEconomyManager.deposit(amount);
                        else multiEconomyManager.deposit(amount, bankName);
                        break;

                    case "[PLAYER]":
                        p.chat(value);
                        break;

                    case "[WITHDRAW]":
                        if (value.equals("CUSTOM")) {
                            BPUtils.customWithdraw(p, bankName);
                            continue;
                        }
                        try {
                            if (!value.endsWith("%")) amount = new BigDecimal(value);
                            else {
                                BigDecimal percentage = new BigDecimal(value.replace("%", "")).divide(BigDecimal.valueOf(100));
                                if (!isMulti) amount = singleEconomyManager.getBankBalance().multiply(percentage);
                                else amount = multiEconomyManager.getBankBalance(bankName).multiply(percentage);
                            }
                        } catch (NumberFormatException ex) {
                            BPLogger.warn("Invalid withdraw number! (Button: " + key + ", Number: " + value + ")");
                            continue;
                        }
                        if (!isMulti) singleEconomyManager.withdraw(amount);
                        else multiEconomyManager.withdraw(amount, bankName);
                }
            }
        }
    }

    private static boolean processOldClickMethod(ConfigurationSection itemValues, String bankName, Player p) {
        String actionType = itemValues.getString("Action.Action-Type");
        if (actionType == null) return false;

        BankReader bankReader = new BankReader(bankName);
        String actionAmount = itemValues.getString("Action.Amount");

        actionType = actionType.toLowerCase();
        actionAmount = actionAmount == null ? "null" : actionAmount.toLowerCase();

        BigDecimal amount;
        boolean isMulti = Values.MULTIPLE_BANKS.isMultipleBanksEnabled();
        SingleEconomyManager singleEconomyManager = new SingleEconomyManager(p);
        MultiEconomyManager multiEconomyManager = new MultiEconomyManager(p);
        Economy economy = BankPlus.INSTANCE.getEconomy();
        switch (actionType) {
            case "deposit":
                switch (actionAmount) {
                    case "custom":
                        BPUtils.customDeposit(p);
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
                            amount = new BigDecimal(actionAmount);
                        } catch (NumberFormatException ex) {
                            BPLogger.error("Invalid deposit number! (Path: " + itemValues + ", Number: " + actionAmount + ")");
                            return true;
                        }
                        if (isMulti) multiEconomyManager.deposit(amount, bankName);
                        else singleEconomyManager.deposit(amount);
                        break;
                }
                break;

            case "withdraw":
                switch (actionAmount) {
                    case "custom":
                        BPUtils.customWithdraw(p);
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
                            amount = new BigDecimal(actionAmount);
                        } catch (NumberFormatException ex) {
                            BPLogger.error("Invalid withdraw number! (Path: " + itemValues + ", Number: " + actionAmount + ")");
                            return true;
                        }
                        if (isMulti) multiEconomyManager.withdraw(amount, bankName);
                        else singleEconomyManager.withdraw(amount);
                        break;
                }
                break;

            case "upgrade":
                bankReader.upgradeBank(p);
                break;
        }
        return true;
    }
}