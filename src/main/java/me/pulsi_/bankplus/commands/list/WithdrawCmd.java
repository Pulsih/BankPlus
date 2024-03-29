package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPArgs;
import me.pulsi_.bankplus.utils.BPFormatter;
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

        String bankName = Values.CONFIG.getMainGuiName();
        if (args.length > 2) bankName = args[2];

        if (!BankManager.exist(bankName)) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }
        BPEconomy economy = BPEconomy.get(bankName);

        BigDecimal amount;
        switch (args[1]) {
            case "all":
                amount = economy.getBankBalance(p);
                break;

            case "half":
                amount = economy.getBankBalance(p).divide(BigDecimal.valueOf(2));
                break;

            default:
                String num = args[1];
                if (BPUtils.isInvalidNumber(num, s)) return false;
                amount = BPFormatter.getBigDecimalFormatted(num);
        }

        if (!skipToConfirm(s)) economy.withdraw(p, amount);
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