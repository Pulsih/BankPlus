package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCmdExecution;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class ForceDepositCmd extends BPCommand {

    public ForceDepositCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% &cUsage: &7/bank forceDeposit [player] [amount/half/all/custom] <bankName>");
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
        return false;
    }

    @Override
    public boolean skipUsage() {
        return false;
    }

    @Override
    public BPCmdExecution onExecution(CommandSender s, String[] args) {
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            BPMessages.send(s, "Invalid-Player");
            return BPCmdExecution.invalidExecution();
        }

        if (args.length == 2) {
            BPMessages.send(s, "Specify-Number");
            return BPCmdExecution.invalidExecution();
        }

        String amount = args[2].toLowerCase();
        if (!amount.equals("custom") && !amount.equals("all") && !amount.equals("half") && BPUtils.isInvalidNumber(amount, s))
            return BPCmdExecution.invalidExecution();

        Bank bank = BankUtils.getBank(getPossibleBank(args, 3));
        if (!BankUtils.exist(bank, s)) return BPCmdExecution.invalidExecution();

        if (!BankUtils.isAvailable(bank, target)) {
            BPMessages.send(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
            return BPCmdExecution.invalidExecution();
        }

         return new BPCmdExecution() {
             @Override
             public void execute() {
                 Economy vault = BankPlus.INSTANCE().getVaultEconomy();
                 BPEconomy economy = bank.getBankEconomy();
                 switch (amount) {
                     case "all":
                         economy.deposit(target, BigDecimal.valueOf(vault.getBalance(target)));
                         break;

                     case "half":
                         economy.deposit(target, BigDecimal.valueOf(vault.getBalance(target) / 2));
                         break;

                     case "custom":
                         economy.customDeposit(target);
                         return;

                     default:
                         economy.deposit(target, BPFormatter.getStyledBigDecimal(amount));
                 }
             }
         };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return BPArgs.getOnlinePlayers(args);

        if (args.length == 3)
            return BPArgs.getArgs(args, "1", "2", "3", "half", "all", "custom");

        if (args.length == 4)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames(Bukkit.getPlayer(args[1])));

        return null;
    }
}