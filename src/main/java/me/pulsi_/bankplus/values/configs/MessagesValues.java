package me.pulsi_.bankplus.values.configs;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class MessagesValues {

    private boolean alertMissingMessage;
    private boolean alertMissingMessagePathNull;
    private List<String> helpMessage;
    private String prefix;
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
    private String insufficientMoney;
    private String notPlayer;
    private String invalidNumber;
    private String specifyNumber;
    private String specifyPlayer;
    private String unknownCommand;
    private String interestDisabled;
    private String interestRestarted;
    private String paymentSent;
    private String paymentReceived;
    private String invalidPlayer;
    private String bankFull;
    private String interestTime;

    public static MessagesValues getInstance() {
        return new MessagesValues();
    }

    public void setupValues() {
        BankPlus plugin = JavaPlugin.getPlugin(BankPlus.class);

        alertMissingMessage = plugin.messages().getBoolean("Alert-Missing-Message");
        alertMissingMessagePathNull = plugin.messages().getString("Alert-Missing-Message") == null;
        helpMessage = plugin.messages().getStringList("Help-Message");
        prefix = plugin.messages().getString("Prefix");
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
        insufficientMoney = plugin.messages().getString("Insufficient-Money");
        notPlayer = plugin.messages().getString("Not-Player");
        invalidNumber = plugin.messages().getString("Invalid-Number");
        specifyNumber = plugin.messages().getString("Specify-Number");
        specifyPlayer = plugin.messages().getString("Specify-Player");
        unknownCommand = plugin.messages().getString("Unknown-Command");
        interestDisabled = plugin.messages().getString("Interest-Disabled");
        interestRestarted = plugin.messages().getString("Interest-Restarted");
        paymentSent = plugin.messages().getString("Payment-Sent");
        paymentReceived = plugin.messages().getString("Payment-Received");
        invalidPlayer = plugin.messages().getString("Invalid-Player");
        bankFull = plugin.messages().getString("Bank-Full");
        interestTime = plugin.messages().getString("Interest-Time");
    }

    public boolean isAlertMissingMessage() {
        return alertMissingMessage;
    }

    public boolean isAlertMissingMessagePathNull() {
        return alertMissingMessagePathNull;
    }

    public List<String> getHelpMessage() {
        return helpMessage;
    }

    public String getPrefix() {
        return prefix;
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

    public String getInsufficientMoney() {
        return insufficientMoney;
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

    public String getInterestRestarted() {
        return interestRestarted;
    }

    public String getPaymentSent() {
        return paymentSent;
    }

    public String getPaymentReceived() {
        return paymentReceived;
    }

    public String getInvalidPlayer() {
        return invalidPlayer;
    }

    public String getBankFull() {
        return bankFull;
    }

    public String getInterestTime() {
        return interestTime;
    }
}