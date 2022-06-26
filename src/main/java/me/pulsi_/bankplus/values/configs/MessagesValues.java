package me.pulsi_.bankplus.values.configs;

import me.pulsi_.bankplus.BankPlus;
import org.bukkit.configuration.file.FileConfiguration;

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
    private String interestBankFull;
    private String banktopDisabled;
    private boolean isTitleCustomAmountEnabled;
    private boolean isInterestBroadcastEnabled;

    public static MessagesValues getInstance() {
        return new MessagesValues();
    }

    public void setupValues() {
        FileConfiguration messages = BankPlus.getCm().getConfig("messages");

        alertMissingMessage = messages.getBoolean("Alert-Missing-Message");
        alertMissingMessagePathNull = messages.getString("Alert-Missing-Message") == null;
        helpMessage = messages.getStringList("Help-Message");
        prefix = messages.getString("Prefix");
        chatWithdraw = messages.getString("Chat-Withdraw");
        chatDeposit = messages.getString("Chat-Deposit");
        cannotUseBankHere = messages.getString("Cannot-Use-Bank-Here");
        noPermission = messages.getString("No-Permission");
        minimumAmount = messages.getString("Minimum-Number");
        reloadMessage = messages.getString("Reload");
        cannotUseNegativeNumber = messages.getString("Cannot-Use-Negative-Number");
        cannotDepositAnymore = messages.getString("Cannot-Deposit-Anymore");
        noMoneyInterest = messages.getString("Interest-Broadcast.No-Money");
        personalBank = messages.getString("Personal-Bank");
        successWithdraw = messages.getString("Success-Withdraw");
        successDeposit = messages.getString("Success-Deposit");
        bankOthers = messages.getString("Bank-Others");
        setMessage = messages.getString("Set-Message");
        addMessage = messages.getString("Add-Message");
        removeMessage = messages.getString("Remove-Message");
        interestBroadcastMessage = messages.getString("Interest-Broadcast.Message");
        insufficientMoney = messages.getString("Insufficient-Money");
        notPlayer = messages.getString("Not-Player");
        invalidNumber = messages.getString("Invalid-Number");
        specifyNumber = messages.getString("Specify-Number");
        specifyPlayer = messages.getString("Specify-Player");
        unknownCommand = messages.getString("Unknown-Command");
        interestDisabled = messages.getString("Interest-Disabled");
        interestRestarted = messages.getString("Interest-Restarted");
        paymentSent = messages.getString("Payment-Sent");
        paymentReceived = messages.getString("Payment-Received");
        invalidPlayer = messages.getString("Invalid-Player");
        bankFull = messages.getString("Bank-Full");
        interestTime = messages.getString("Interest-Time");
        interestBankFull = messages.getString("Interest-Broadcast.Bank-Full");
        isTitleCustomAmountEnabled = messages.getBoolean("Title-Custom-Amount.Enabled");
        isInterestBroadcastEnabled = messages.getBoolean("Interest-Broadcast.Enabled");
        banktopDisabled = messages.getString("BankTop-Disabled");
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

    public String getInterestBankFull() {
        return interestBankFull;
    }

    public String getBanktopDisabled() {
        return banktopDisabled;
    }

    public boolean isTitleCustomAmountEnabled() {
        return isTitleCustomAmountEnabled;
    }

    public boolean isInterestBroadcastEnabled() {
        return isInterestBroadcastEnabled;
    }
}