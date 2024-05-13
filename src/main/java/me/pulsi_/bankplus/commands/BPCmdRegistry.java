package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.list.*;
import me.pulsi_.bankplus.managers.BPConfigs;
import me.pulsi_.bankplus.utils.BPLogger;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

public class BPCmdRegistry {

    protected static final LinkedHashMap<String, BPCommand> commands = new LinkedHashMap<>();

    public static void registerPluginCommands() {
        commands.clear();

        BPConfigs configs = BankPlus.INSTANCE().getConfigs();
        File commandsFile = configs.getFile("commands.yml");
        FileConfiguration commandsConfig = configs.getConfig(commandsFile);

        boolean save = new AddAllCmd(commandsConfig, "addAll").register();
        if (new AddCmd(commandsConfig, "add").register()) save = true;
        if (new BalanceCmd(commandsConfig, "balance", "bal").register()) save = true;
        if (new CustomDepositCmd(commandsConfig, "customDeposit").register()) save = true;
        if (new CustomWithdrawCmd(commandsConfig, "customWithdraw").register()) save = true;
        if (new DepositCmd(commandsConfig, "deposit").register()) save = true;
        if (new ForceOpenCmd(commandsConfig, "forceOpen").register()) save = true;
        if (new ForceUpgradeCmd(commandsConfig, "forceUpgrade").register()) save = true;
        if (new GiveInterestCmd(commandsConfig, "giveInterest").register()) save = true;
        if (new HelpCmd(commandsConfig, "help").register()) save = true;
        if (new InterestCmd(commandsConfig, "interest").register()) save = true;
        if (new InterestMillisCmd(commandsConfig, "interestMillis").register()) save = true;
        if (new LoanCmd(commandsConfig, "loan").register()) save = true;
        if (new OpenCmd(commandsConfig, "open").register()) save = true;
        if (new PayCmd(commandsConfig, "pay").register()) save = true;
        if (new PlaceholdersCmd(commandsConfig, "placeholders").register()) save = true;
        if (new ReloadCmd(commandsConfig, "reload").register()) save = true;
        if (new RemoveCmd(commandsConfig, "remove").register()) save = true;
        if (new ResetAllCmd(commandsConfig, "resetAll").register()) save = true;
        if (new RestartInterestCmd(commandsConfig, "restartInterest").register()) save = true;
        if (new SaveAllDataCmd(commandsConfig, "saveAllData").register()) save = true;
        if (new SetCmd(commandsConfig, "set").register()) save = true;
        if (new SetLevelCmd(commandsConfig, "setLevel").register()) save = true;
        if (new TransferCmd(commandsConfig, "transfer").register()) save = true;
        if (new UpdateBankTopCmd(commandsConfig, "updateBankTop").register()) save = true;
        if (new UpgradeCmd(commandsConfig, "upgrade").register()) save = true;
        if (new ViewCmd(commandsConfig, "view").register()) save = true;
        if (new WithdrawCmd(commandsConfig, "withdraw").register()) save = true;

        if (!save) return;
        try {
            commandsConfig.save(commandsFile);
        } catch (IOException e) {
            BPLogger.warn(e, "Could not save \"commands.yml\" config file!");
        }
    }
}