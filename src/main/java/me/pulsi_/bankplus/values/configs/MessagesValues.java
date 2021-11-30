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
    private String successWithdraw;
    private String successDeposit;
    private String bankOthers;
    private String setMessage;
    private String addMessage;
    private String removeMessage;
    private String interestBroadcastMessage;
    private String insufficientMoneyWithdraw;
    private String insufficientMoneyDeposit;
    private String cannotFindPlayer;
    private String notPlayer;
    private String invalidNumber;
    private String specifyNumber;
    private String specifyPlayer;
    private String unknownCommand;
    private String interestDisabled;
    private String error;
    private String interestUsage;
    private String interestRestarted;

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
        successWithdraw = plugin.messages().getString("Success-Withdraw");
        successDeposit = plugin.messages().getString("Success-Deposit");
        bankOthers = plugin.messages().getString("Bank-Others");
        setMessage = plugin.messages().getString("Set-Message");
        addMessage = plugin.messages().getString("Add-Message");
        removeMessage = plugin.messages().getString("Remove-Message");
        interestBroadcastMessage = plugin.messages().getString("Interest-Broadcast.Message");
        insufficientMoneyWithdraw = plugin.messages().getString("Insufficient-Money-Withdraw");
        insufficientMoneyDeposit = plugin.messages().getString("Insufficient-Money-Deposit");
        cannotFindPlayer = plugin.messages().getString("Cannot-Find-Player");
        notPlayer = plugin.messages().getString("Not-Player");
        invalidNumber = plugin.messages().getString("Invalid-Number");
        specifyNumber = plugin.messages().getString("Specify-Number");
        specifyPlayer = plugin.messages().getString("Specify-Player");
        unknownCommand = plugin.messages().getString("Unknown-Command");
        interestDisabled = plugin.messages().getString("Interest-Disabled");
        error = plugin.messages().getString("Error");
        interestUsage = plugin.messages().getString("Interest-Usage");
        interestRestarted = plugin.messages().getString("Interest-Restarted");
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

    public String getSuccessWithdraw() {
        return successWithdraw;
    }

    public String getSuccessDeposit() {
        return successDeposit;
    }

    public String getBankOthers() {
        return bankOthers;
    }

    public String getSetMessage() {
        return setMessage;
    }

    public String getAddMessage() {
        return addMessage;
    }

    public String getRemoveMessage() {
        return removeMessage;
    }

    public String getInterestBroadcastMessage() {
        return interestBroadcastMessage;
    }

    public String getInsufficientMoneyWithdraw() {
        return insufficientMoneyWithdraw;
    }

    public String getInsufficientMoneyDeposit() {
        return insufficientMoneyDeposit;
    }

    public String getCannotFindPlayer() {
        return cannotFindPlayer;
    }

    public String getNotPlayer() {
        return notPlayer;
    }

    public String getInvalidNumber() {
        return invalidNumber;
    }

    public String getSpecifyNumber() {
        return specifyNumber;
    }

    public String getSpecifyPlayer() {
        return specifyPlayer;
    }

    public String getUnknownCommand() {
        return unknownCommand;
    }

    public String getInterestDisabled() {
        return interestDisabled;
    }

    public String getError() {
        return error;
    }

    public String getInterestUsage() {
        return interestUsage;
    }

    public String getInterestRestarted() {
        return interestRestarted;
    }
}