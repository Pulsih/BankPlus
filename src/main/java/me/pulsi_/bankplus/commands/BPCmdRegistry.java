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
        if (!commandsFile.exists()) {
            try {
                commandsFile.createNewFile();
            } catch (IOException e) {
                BPLogger.Console.warn(e, "Could not create \"commands.yml\" file.");
            }
        }
        FileConfiguration commandsConfig = configs.getConfig(commandsFile);

        new AddAllCmd(commandsConfig, "addAll").register();
        new AddCmd(commandsConfig, "add").register();
        new BalanceCmd(commandsConfig, "balance", "bal").register();
        new DebugCmd(commandsConfig, "debug").register();
        new DepositCmd(commandsConfig, "deposit").register();
        new ForceDepositCmd(commandsConfig, "forceDeposit").register();
        new ForceOpenCmd(commandsConfig, "forceOpen").register();
        new ForceUpgradeCmd(commandsConfig, "forceUpgrade").register();
        new ForceWithdrawCmd(commandsConfig, "forceWithdraw").register();
        new GiveInterestCmd(commandsConfig, "giveInterest").register();
        new GiveRequiredItemsCmd(commandsConfig, "giveRequiredItems").register();
        new HelpCmd(commandsConfig, "help").register();
        new InterestCmd(commandsConfig, "interest").register();
        new InterestMillisCmd(commandsConfig, "interestMillis").register();
        new LoanCmd(commandsConfig, "loan").register();
        new OpenCmd(commandsConfig, "open").register();
        new PayCmd(commandsConfig, "pay").register();
        new PlaceholdersCmd(commandsConfig, "placeholders").register();
        new ReloadCmd(commandsConfig, "reload").register();
        new RemoveCmd(commandsConfig, "remove").register();
        new ResetAllCmd(commandsConfig, "resetAll").register();
        new RestartInterestCmd(commandsConfig, "restartInterest").register();
        new SaveAllDataCmd(commandsConfig, "saveAllData").register();
        new SetCmd(commandsConfig, "set").register();
        new SetLevelCmd(commandsConfig, "setLevel").register();
        new TransferCmd(commandsConfig, "transfer").register();
        new UpdateBankTopCmd(commandsConfig, "updateBankTop").register();
        new UpgradeCmd(commandsConfig, "upgrade").register();
        new ViewCmd(commandsConfig, "view").register();
        new WithdrawCmd(commandsConfig, "withdraw").register();

        try {
            commandsConfig.save(commandsFile);
        } catch (IOException e) {
            BPLogger.Console.warn(e, "Could not save \"commands.yml\" config file!");
        }
    }
}