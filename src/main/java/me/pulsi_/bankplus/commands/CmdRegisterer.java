package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.commands.list.*;

public class CmdRegisterer {

    public void registerCmds() {
        new AddAllCmd("addall").register();
        new AddCmd("add").register();
        new BalanceCmd("balance", "bal").register();
        new CustomDepositCmd("customdeposit").register();
        new CustomWithdrawCmd("customwithdraw").register();
        new DepositCmd("deposit").register();
        new ForceOpenCmd("forceopen").register();
        new ForceUpgradeCmd("forceupgrade").register();
        new GiveInterestCmd("giveinterest").register();
        new HelpCmd("help").register();
        new InterestCmd("interest").register();
        new InterestMillisCmd("interestmillis").register();
        new OpenCmd("open").register();
        new PayCmd("pay").register();
        new ReloadCmd("reload").register();
        new RemoveCmd("remove").register();
        new ResetAllCmd("resetall").register();
        new RestartInterestCmd("restartinterest").register();
        new SaveAllBankBalancesCmd("saveallbankbalances").register();
        new SetCmd("set").register();
        new SetLevelCmd("setlevel").register();
        new UpdateBankTopCmd("updatebanktop").register();
        new UpgradeCmd("upgrade").register();
        new ViewCmd("view").register();
        new WithdrawCmd("withdraw").register();
    }

    public void resetCmds() {
        MainCmd.commands.clear();
    }
}