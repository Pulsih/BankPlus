package me.pulsi_.bankplus.values.configs;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.BPConfigs;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageValues {

    private boolean isTitleCustomAmountEnabled, isInterestBroadcastEnabled;
    private String interestMoney, interestNoMoney, multiInterestMoney, interestBankFull;

    public static MessageValues getInstance() {
        return new MessageValues();
    }

    public void setupValues() {
        FileConfiguration messages = BankPlus.INSTANCE().getConfigs().getConfig(BPConfigs.Type.MESSAGES.name);

        isTitleCustomAmountEnabled = messages.getBoolean("Title-Custom-Transaction.Enabled");
        isInterestBroadcastEnabled = messages.getBoolean("Interest-Broadcast.Enabled");
        interestMoney = messages.getString("Interest-Broadcast.Message");
        multiInterestMoney = messages.getString("Interest-Broadcast.Multi-Message");
        interestNoMoney = messages.getString("Interest-Broadcast.No-Money");
        interestBankFull = messages.getString("Interest-Broadcast.Bank-Full");
    }

    public boolean isTitleCustomAmountEnabled() {
        return isTitleCustomAmountEnabled;
    }

    public boolean isInterestBroadcastEnabled() {
        return isInterestBroadcastEnabled;
    }

    public String getInterestMoney() {
        return interestMoney;
    }

    public String getMultiInterestMoney() {
        return multiInterestMoney;
    }

    public String getInterestNoMoney() {
        return interestNoMoney;
    }

    public String getInterestBankFull() {
        return interestBankFull;
    }
}