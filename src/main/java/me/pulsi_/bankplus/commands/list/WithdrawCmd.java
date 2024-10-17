package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCmdExecution;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class WithdrawCmd extends BPCommand {

    public WithdrawCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% &cUsage: &7/bank deposit [amount/half/all] <bankName>");
    }

    @Override
    public int defaultConfirmCooldown() {
        return 0;
    }

    @Override
    public List<String> defaultConfirmMessage() {
        return Collections.emptyList();
    }

    @Override
    public int defaultCooldown() {
        return 0;
    }

    @Override
    public List<String> defaultCooldownMessage() {
        return Collections.emptyList();
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public boolean skipUsage() {
        return false;
    }

    @Override
    public BPCmdExecution onExecution(CommandSender s, String[] args) {
        String amount = args[1].toLowerCase();
        if (!amount.equals("all") && !amount.equals("half") && BPUtils.isInvalidNumber(amount, s)) return BPCmdExecution.invalidExecution();

        Bank bank = BankUtils.getBank(getPossibleBank(args, 2));
        if (!BankUtils.exist(bank, s)) return BPCmdExecution.invalidExecution();

        Player p = (Player) s;
        if (!BankUtils.isAvailable(bank, p)) {
            BPMessages.send(s, "Cannot-Access-Bank");
            return BPCmdExecution.invalidExecution();
        }

         return new BPCmdExecution() {
             @Override
             public void execute() {
                 BPEconomy economy = bank.getBankEconomy();
                 switch (amount) {
                     case "all":
                         economy.withdraw(p, economy.getBankBalance(p));
                         break;

                     case "half":
                         economy.withdraw(p, economy.getBankBalance(p).divide(BigDecimal.valueOf(2)));
                         break;

                     default:
                         economy.withdraw(p, BPFormatter.getStyledBigDecimal(amount));
                 }
             }
         };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 3)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames((Player) s));

        return null;
    }
}