package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.utils.BPArgs;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.List;

public class WithdrawCmd extends BPCommand {

    public WithdrawCmd(String... aliases) {
        super(aliases);
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public boolean skipUsageWarn() {
        return false;
    }

    @Override
    public boolean onCommand(CommandSender s, String[] args) {
        Player p = (Player) s;

        BigDecimal amount;
        switch (args[1]) {
            case "all":
                amount = BigDecimal.valueOf(BankPlus.INSTANCE.getVaultEconomy().getBalance(p));
                break;

            case "half":
                amount = BigDecimal.valueOf(BankPlus.INSTANCE.getVaultEconomy().getBalance(p) / 2);
                break;

            default:
                String num = args[1];
                if (BPUtils.isInvalidNumber(num, s)) return false;
                amount = new BigDecimal(num);
        }

        String bankName = Values.CONFIG.getMainGuiName();
        if (args.length > 2) bankName = args[2];

        if (!new BankManager(bankName).exist()) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }

        if (confirm(s)) return false;
        BankPlus.getBPEconomy().withdraw(p, amount, bankName);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 3)
            return BPArgs.getBanks(args);
        return null;
    }
}