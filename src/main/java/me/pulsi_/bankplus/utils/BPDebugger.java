package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.banks.Bank;
import me.pulsi_.bankplus.banks.BanksHolder;
import me.pulsi_.bankplus.banks.BanksManager;
import me.pulsi_.bankplus.interest.Interest;
import me.pulsi_.bankplus.managers.TaskManager;
import me.pulsi_.bankplus.values.Values;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.math.BigDecimal;

public class BPDebugger {

    private static boolean isChatDebuggerEnabled = false, isGuiDebuggerEnabled = false, isDepositDebuggerEnabled = false, isWithdrawDebuggerEnabled = false;

    public static void debugChat(AsyncPlayerChatEvent e) {
        if (!isChatDebuggerEnabled()) return;
        String message = e.getMessage();
        String stripMessage = ChatColor.stripColor(message);
        Player p = e.getPlayer();

        BPLogger.log("");
        BPLogger.log("                     &aBank&9Plus&dDebugger&9: &aPLAYER_CHAT");
        BPLogger.info("PlayerName: &a" + p.getName() + " &9(UUID: &a" + p.getUniqueId() + "&9)");
        if (BPMethods.isDepositing(p)) BPLogger.info("PlayerStatus: &aIS_DEPOSITING");
        else if (BPMethods.isWithdrawing(p)) BPLogger.info("PlayerStatus: &aIS_WITHDRAWING");
        BPLogger.info("PlayerMessage: &a" + message + "&9 (StrippedMessage: &a" + stripMessage + "&9)");

        boolean isExitMessage = stripMessage.equalsIgnoreCase(Values.CONFIG.getExitMessage());
        BPLogger.info("IsExitMessage: &a" + isExitMessage);
        if (isExitMessage) {
            BPLogger.log("");
            return;
        }

        BPLogger.info("MessageIsNumber: &a" + BPMethods.isValidNumber(message));
        if (BPMethods.isValidNumber(message)) {
            BigDecimal mainBalance = BigDecimal.valueOf(BankPlus.instance().getEconomy().getBalance(p));
            BigDecimal bankBalance = Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled() ? new MultiEconomyManager(p).getBankBalance() : new SingleEconomyManager(p).getBankBalance();
            BigDecimal messageNumber = new BigDecimal(message);
            BPLogger.info("PlayerMainBalance: &a" + mainBalance);
            BPLogger.info("PlayerBankBalance: &a" + bankBalance);

            boolean hasEnoughMoneyWithdraw = bankBalance.doubleValue() > 0;
            boolean hasEnoughMoneyDeposit = mainBalance.doubleValue() > 0;
            if (BPMethods.isWithdrawing(p)) BPLogger.info("HasEnoughMoneyToWithdraw: &a" + (bankBalance.doubleValue() > 0));
            else if (BPMethods.isWithdrawing(p)) BPLogger.info("HasEnoughMoneyToDeposit: &a" + (mainBalance.doubleValue() > 0));

            if (!hasEnoughMoneyWithdraw || !hasEnoughMoneyDeposit) {
                BPLogger.log("");
                return;
            }

            BigDecimal newMainBalance = null;
            BigDecimal newBankBalance = null;
            if (BPMethods.isWithdrawing(p)) {
                if (messageNumber.doubleValue() > bankBalance.doubleValue()) messageNumber = bankBalance;
                newMainBalance = mainBalance.add(messageNumber);
                newBankBalance = (bankBalance.subtract(messageNumber)).doubleValue() < 0 ? BigDecimal.valueOf(0) : bankBalance.subtract(messageNumber);
                if (messageNumber.doubleValue() > bankBalance.doubleValue()) BPLogger.info("The player &ahas not &9enough money to withdraw. (NumberInsertedLimited: &a" + messageNumber + "&9, BankBalance: &a" + bankBalance + "&9)");
                else BPLogger.info("The player &ahas &9enough money to withdraw. (NumberInsertedLimited: &a" + messageNumber + "&9, BankBalance: &a" + bankBalance + "&9)");
            }
            else if (BPMethods.isDepositing(p)) {
                if (messageNumber.doubleValue() > mainBalance.doubleValue()) messageNumber = mainBalance;
                newMainBalance = (mainBalance.subtract(messageNumber)).doubleValue() < 0 ? BigDecimal.valueOf(0) : mainBalance.subtract(messageNumber);
                newBankBalance = (bankBalance.add(messageNumber)).doubleValue() > Values.CONFIG.getMaxBankCapacity().doubleValue() ? Values.CONFIG.getMaxBankCapacity() : bankBalance.add(messageNumber);
                if (messageNumber.doubleValue() > mainBalance.doubleValue()) BPLogger.info("The player &ahas not &9enough money to deposit. (NumberInsertedLimited: &a" + messageNumber + "&9, MainBalance: &a" + mainBalance + "&9)");
                else BPLogger.info("The player &ahas &9enough money to deposit. (NumberInsertedLimited: &a" + messageNumber + "&9, MainBalance: &a" + mainBalance + "&9)");
            }
            BPLogger.info("NewBankBalance: &a" + newBankBalance + "&9, NewMainBalance: &a" + newMainBalance + "&9)");
        }
        BPLogger.log("");
    }

    public static void debugInterest() {
        BPLogger.log("");
        BPLogger.log("                     &aBank&9Plus&dDebugger&9: &aINTEREST");
        BPLogger.info("IsInterestActive: &a" + Values.CONFIG.isInterestEnabled());
        if (!Values.CONFIG.isInterestEnabled()) {
            BPLogger.log("");
            return;
        }

        String task = TaskManager.getInterestTask().toString();
        if (task == null) task = "null";
        else task = task.replace("org.bukkit.craftbukkit.", "");

        BPLogger.info("InterestTask: &a" + task + " &9(IsNull: &a" + (TaskManager.getInterestTask() == null) + "&9)");
        BPLogger.info("ServerMilliseconds: &a" + System.currentTimeMillis() + "ms");
        BPLogger.info("InterestCooldownMillis: &a" + Interest.getInterestCooldownMillis() + "ms &9(&a" + BPMethods.formatTime(Interest.getInterestCooldownMillis()) + "&9)");
        BPLogger.info("InterestDelay: &a" + Values.CONFIG.getInterestDelay() + "ms &9(&a" + BPMethods.formatTime(Values.CONFIG.getInterestDelay()) + "&9)");
        BPLogger.info("PlayersWaitingInterest: &a" + Bukkit.getOnlinePlayers().size() + " &9(&a" + Bukkit.getOfflinePlayers().length + " Total&9)");
        BPLogger.info("IsOfflineInterest: &a" + Values.CONFIG.isGivingInterestToOfflinePlayers());
        BPLogger.log("");
    }

    public static void debugGui(InventoryClickEvent e) {
        if (!isGuiDebuggerEnabled()) return;

        HumanEntity entity = e.getWhoClicked();
        if (!(entity instanceof Player)) return;
        Player p = (Player) entity;
        int slot = e.getSlot() + 1;

        BPLogger.log("");
        BPLogger.log("                       &aBank&9Plus&dDebugger&9: &aGUI");
        BPLogger.info("PlayerName: &a" + p.getName() + " &9(UUID: &a" + p.getUniqueId() + "&9)");
        BigDecimal mainBalance = BigDecimal.valueOf(BankPlus.instance().getEconomy().getBalance(p));
        BigDecimal bankBalance = Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled() ? new MultiEconomyManager(p).getBankBalance() : new SingleEconomyManager(p).getBankBalance();
        BPLogger.info("PlayerMainBalance: &a" + mainBalance);
        BPLogger.info("PlayerBankBalance: &a" + bankBalance);
        BPLogger.info("ClickedSlot: &a" + slot);

        String itemPath = null, actionType = null, actionNumber = null;
        boolean hasAction = false;
        Bank bank = BankPlus.instance().getPlayers().get(p.getUniqueId()).getOpenedBank();
        for (String key : bank.getItems().getKeys(false)) {
            ConfigurationSection item = bank.getBankConfig().getConfigurationSection("Items." + key);
            if (item == null || slot != item.getInt("Slot")) continue;
            itemPath = item.toString();

            String type = item.getString("Action.Action-Type");
            actionType = type == null ? "Not found." : type;
            String number = item.getString("Action.Amount");
            actionNumber = number == null ? "Not found." : number;
            hasAction = type != null;
        }

        BPLogger.info("ItemPath: &a" + (itemPath == null ? "GuiFiller" : itemPath));
        BPLogger.info("HasAction: &a" + hasAction);
        if (!hasAction) {
            BPLogger.log("");
            return;
        }
        BPLogger.info("ActionType: &a" + actionType);
        boolean isValidNumber = actionNumber.equalsIgnoreCase("ALL") || actionNumber.equalsIgnoreCase("HALF") || BPMethods.isValidNumber(actionNumber);
        BPLogger.info("ActionNumber: &a" + actionNumber + " &9(IsValidNumber: &a" + BPMethods.isValidNumber(actionNumber) + "&9, IsValidAction: &a" + isValidNumber + "&9)");
        BPLogger.log("");
    }

    public static void debugDeposit(Player p, BigDecimal amount, EconomyResponse response) {
        if (!isDepositDebuggerEnabled()) return;

        BPLogger.log("");
        BPLogger.log("                       &aBank&9Plus&dDebugger&9: &aDEPOSIT");
        BPLogger.info("PlayerName: &a" + p.getName() + " &9(UUID: &a" + p.getUniqueId() + "&9)");
        BigDecimal mainBalance = BigDecimal.valueOf(BankPlus.instance().getEconomy().getBalance(p));
        BigDecimal bankBalance = Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled() ? new MultiEconomyManager(p).getBankBalance() : new SingleEconomyManager(p).getBankBalance();
        BPLogger.info("PlayerMainBalance: &a" + mainBalance);
        BPLogger.info("PlayerBankBalance: &a" + bankBalance);
        BPLogger.info("AmountInserted: &a" + amount);

        EconomyResponse.ResponseType economyResponseType = response.type;
        BPLogger.info("DepositEconomyResponse: &a" + economyResponseType);
        if (economyResponseType != EconomyResponse.ResponseType.SUCCESS) {
            BPLogger.info("&cVault failed withdraw task for " + p.getName());
            BPLogger.info("Error message: &c" + response.errorMessage);
        }
        BPLogger.log("");
    }

    public static void debugWithdraw(Player p, BigDecimal amount, EconomyResponse response) {
        if (!isWithdrawDebuggerEnabled()) return;

        BPLogger.log("");
        BPLogger.log("                       &aBank&9Plus&dDebugger&9: &aWITHDRAW");
        BPLogger.info("PlayerName: &a" + p.getName() + " &9(UUID: &a" + p.getUniqueId() + "&9)");
        BigDecimal mainBalance = BigDecimal.valueOf(BankPlus.instance().getEconomy().getBalance(p));
        BigDecimal bankBalance = Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled() ? new MultiEconomyManager(p).getBankBalance() : new SingleEconomyManager(p).getBankBalance();
        BPLogger.info("PlayerMainBalance: &a" + mainBalance);
        BPLogger.info("PlayerBankBalance: &a" + bankBalance);
        BPLogger.info("AmountInserted: &a" + amount);

        EconomyResponse.ResponseType economyResponseType = response.type;
        BPLogger.info("DepositEconomyResponse: &a" + economyResponseType);
        if (economyResponseType != EconomyResponse.ResponseType.SUCCESS) {
            BPLogger.info("&cVault failed deposit task for " + p.getName());
            BPLogger.info("Error message: &c" + response.errorMessage);
        }
        BPLogger.log("");
    }

    public static void toggleDepositDebugger(CommandSender s) {
        if (isDepositDebuggerEnabled) s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aYou have &cdeactivated &athe DEPOSIT debugger!"));
        else s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aYou have &2activated &athe DEPOSIT debugger!"));
        isDepositDebuggerEnabled = !isDepositDebuggerEnabled;
    }

    public static void toggleWithdrawDebugger(CommandSender s) {
        if (isWithdrawDebuggerEnabled) s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aYou have &cdeactivated &athe WITHDRAW debugger!"));
        else s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aYou have &2activated &athe WITHDRAW debugger!"));
        isWithdrawDebuggerEnabled = !isWithdrawDebuggerEnabled;
    }

    public static void toggleChatDebugger(CommandSender s) {
        if (isChatDebuggerEnabled) s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aYou have &cdeactivated &athe CHAT debugger!"));
        else s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aYou have &2activated &athe CHAT debugger!"));
        isChatDebuggerEnabled = !isChatDebuggerEnabled;
    }

    public static void toggleGuiDebugger(CommandSender s) {
        if (isGuiDebuggerEnabled) s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aYou have &cdeactivated &athe GUI debugger!"));
        else s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aYou have &2activated &athe GUI debugger!"));
        isGuiDebuggerEnabled = !isGuiDebuggerEnabled;
    }

    public static boolean isDepositDebuggerEnabled() {
        return isDepositDebuggerEnabled;
    }

    public static boolean isWithdrawDebuggerEnabled() {
        return isWithdrawDebuggerEnabled;
    }

    public static boolean isChatDebuggerEnabled() {
        return isChatDebuggerEnabled;
    }

    public static boolean isGuiDebuggerEnabled() {
        return isGuiDebuggerEnabled;
    }
}