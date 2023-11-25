package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.commands.list.*;

public class CmdRegisterer {

    public void registerCmds() {
        new AddAllCmd("addAll").register();
        new AddCmd("add").register();
        new BalanceCmd("balance", "bal").register();
        new CustomDepositCmd("customDeposit").register();
        new CustomWithdrawCmd("customWithdraw").register();
        new DepositCmd("deposit").register();
        new ForceOpenCmd("forceOpen").register();
        new ForceUpgradeCmd("forceUpgrade").register();
        new GiveInterestCmd("giveInterest").register();
        new HelpCmd("help").register();
        new InterestCmd("interest").register();
        new InterestMillisCmd("interestMillis").register();
        new LoanCmd("loan").register();
        new OpenCmd("open").register();
        new PayCmd("pay").register();
        new ReloadCmd("reload").register();
        new RemoveCmd("remove").register();
        new ResetAllCmd("resetAll").register();
        new RestartInterestCmd("restartInterest").register();
        new SaveAllDataCmd("saveAllData").register();
        new SetCmd("set").register();
        new SetLevelCmd("setLevel").register();
        new TransferCmd("transfer").register();
        new UpdateBankTopCmd("updateBankTop").register();
        new UpgradeCmd("upgrade").register();
        new ViewCmd("view").register();
        new WithdrawCmd("withdraw").register();
    }

    public void resetCmds() {
        MainCmd.commands.clear();
    }
}