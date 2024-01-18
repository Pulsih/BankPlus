package me.pulsi_.bankplus.listeners.bankListener;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayer;
import me.pulsi_.bankplus.bankSystem.BankHolder;
import me.pulsi_.bankplus.bankSystem.BankListGui;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
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

        BPPlayer player = BankPlus.INSTANCE().getPlayerRegistry().get(p);
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

        ConfigurationSection items = BankManager.getBank(bankName).getItems();
        if (items == null) return;

        for (String key : items.getKeys(false)) {
            ConfigurationSection itemValues = items.getConfigurationSection(key);
            if (itemValues == null || slot + 1 != itemValues.getInt("Slot")) continue;

            List<String> actions = itemValues.getStringList("Actions");
            if (actions.isEmpty()) {
                // Check if they are still using the old methods, still process it and invite them to update it.
                if (processOldClickMethod(itemValues, bankName, p))
                    BPLogger.warn("Warning! You are using an old format for the item action! Check out the BankPlus wiki and update it!");
                continue;
            }

            Economy vaultEconomy = BankPlus.INSTANCE().getVaultEconomy();

            for (String actionType : actions) {
                String[] parts = actionType.split(" ");
                int length = parts.length;

                String identifier = length > 0 ? parts[0] : null, value = "";
                if (identifier == null) continue;

                if (identifier.equals("[UPGRADE]")) {
                    if (Values.CONFIG.isGuiActionsNeedPermissions() && !BPUtils.hasPermission(p, "bankplus.upgrade")) return;

                    BankManager.upgradeBank(bankName, p);
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
                    case "[CONSOLE]": {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value);
                    }
                    break;

                    case "[DEPOSIT]": {
                        if (Values.CONFIG.isGuiActionsNeedPermissions() && !BPUtils.hasPermission(p, "bankplus.deposit")) return;

                        if (value.equals("CUSTOM")) {
                            BPUtils.customDeposit(p, bankName);
                            continue;
                        }
                        BigDecimal amount;
                        try {
                            if (!value.endsWith("%")) amount = new BigDecimal(value);
                            else {
                                BigDecimal percentage = new BigDecimal(value.replace("%", "")).divide(BigDecimal.valueOf(100));
                                amount = BigDecimal.valueOf(vaultEconomy.getBalance(p)).multiply(percentage);
                            }
                        } catch (NumberFormatException ex) {
                            BPLogger.warn("Invalid deposit number! (Button: " + key + ", Number: " + value + ")");
                            continue;
                        }
                        BPEconomy.deposit(p, amount);
                    }
                    break;

                    case "[PLAYER]": {
                        p.chat(value);
                    }
                    break;

                    case "[WITHDRAW]": {
                        if (Values.CONFIG.isGuiActionsNeedPermissions() && !BPUtils.hasPermission(p, "bankplus.withdraw")) return;

                        if (value.equals("CUSTOM")) {
                            BPUtils.customWithdraw(p, bankName);
                            continue;
                        }
                        BigDecimal amount;
                        try {
                            if (!value.endsWith("%")) amount = new BigDecimal(value);
                            else {
                                BigDecimal percentage = new BigDecimal(value.replace("%", "")).divide(BigDecimal.valueOf(100));
                                amount = BPEconomy.getBankBalance(p, bankName).multiply(percentage);
                            }
                        } catch (NumberFormatException ex) {
                            BPLogger.warn("Invalid withdraw number! (Button: " + key + ", Number: " + value + ")");
                            continue;
                        }
                        BPEconomy.withdraw(p, amount, bankName);
                    }
                }
            }
        }
    }

    private static boolean processOldClickMethod(ConfigurationSection itemValues, String bankName, Player p) {
        String actionType = itemValues.getString("Action.Action-Type");
        if (actionType == null) return false;

        String actionAmount = itemValues.getString("Action.Amount");

        actionType = actionType.toLowerCase();
        actionAmount = actionAmount == null ? "null" : actionAmount.toLowerCase();

        Economy vaultEconomy = BankPlus.INSTANCE().getVaultEconomy();
        switch (actionType) {
            case "deposit":
                switch (actionAmount) {
                    case "custom":
                        BPUtils.customDeposit(p);
                        break;

                    case "all":
                        BPEconomy.deposit(p, BigDecimal.valueOf(vaultEconomy.getBalance(p)));
                        break;

                    case "half":
                        BPEconomy.deposit(p, BigDecimal.valueOf(vaultEconomy.getBalance(p) / 2));
                        break;

                    default:
                        BigDecimal amount;
                        try {
                            amount = new BigDecimal(actionAmount);
                        } catch (NumberFormatException ex) {
                            BPLogger.error("Invalid deposit number! (Path: " + itemValues + ", Number: " + actionAmount + ")");
                            return true;
                        }
                        BPEconomy.deposit(p, amount);
                        break;
                }
                break;

            case "withdraw":
                switch (actionAmount) {
                    case "custom":
                        BPUtils.customWithdraw(p);
                        break;

                    case "all":
                        BPEconomy.withdraw(p, BPEconomy.getBankBalance(p, bankName), bankName);
                        break;

                    case "half":
                        BPEconomy.withdraw(p, BPEconomy.getBankBalance(p, bankName).divide(BigDecimal.valueOf(2)), bankName);
                        break;

                    default:
                        BigDecimal amount;
                        try {
                            amount = new BigDecimal(actionAmount);
                        } catch (NumberFormatException ex) {
                            BPLogger.error("Invalid withdraw number! (Path: " + itemValues + ", Number: " + actionAmount + ")");
                            return true;
                        }
                        BPEconomy.withdraw(p, amount, bankName);
                        break;
                }
                break;

            case "upgrade":
                BankManager.upgradeBank(bankName, p);
                break;
        }
        return true;
    }
}