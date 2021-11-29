package me.pulsi_.bankplus.values.configs;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.plugin.java.JavaPlugin;

public class MessagesValues {

    private String chatWithdraw;
    private String chatDeposit;
    private String cannotUseBankHere;
    private String noPermission;
    private String minimumAmount;
    private String reloadMessage;
    private String cannotUseNegativeNumber;
    private String cannotDepositAnymore;
    private String noMoneyInterest;
    private String personalBank;

    public static MessagesValues getInstance() {
        return new MessagesValues();
    }

    public void setupValues() {
        BankPlus plugin = JavaPlugin.getPlugin(BankPlus.class);

        chatWithdraw = plugin.messages().getString("Chat-Withdraw");
        chatDeposit = plugin.messages().getString("Chat-Deposit");
        cannotUseBankHere = plugin.messages().getString("Cannot-Use-Bank-Here");
        noPermission = plugin.messages().getString("No-Permission");
        minimumAmount = plugin.messages().getString("Minimum-Number");
        reloadMessage = plugin.messages().getString("Reload");
        cannotUseNegativeNumber = plugin.messages().getString("Cannot-Use-Negative-Number");
        cannotDepositAnymore = plugin.messages().getString("Cannot-Deposit-Anymore");
        noMoneyInterest = plugin.messages().getString("Interest-Broadcast.No-Money");
        personalBank = plugin.messages().getString("Personal-Bank");
    }

    public String getChatWithdraw() {
        return chatWithdraw;
    }

    public String getChatDeposit() {
        return chatDeposit;
    }

    public String getCannotUseBankHere() {
        return cannotUseBankHere;
    }

    public String getNoPermission() {
        return noPermission;
    }

    public String getMinimumAmount() {
        return minimumAmount;
    }

    public String getReloadMessage() {
        return reloadMessage;
    }

    public String getCannotUseNegativeNumber() {
        return cannotUseNegativeNumber;
    }

    public String getCannotDepositAnymore() {
        return cannotDepositAnymore;
    }

    public String getNoMoneyInterest() {
        return noMoneyInterest;
    }

    public String getPersonalBank() {
        return personalBank;
    }
}