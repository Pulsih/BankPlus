package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
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

public class DepositCmd extends BPCommand {

    public DepositCmd(FileConfiguration commandsConfig, String... aliases) {
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
    public boolean skipUsageWarn() {
        return false;
    }

    @Override
    public boolean preCmdChecks(CommandSender s, String[] args) {
        Bank bank = BankUtils.getBank(getPossibleBank(args, 2));
        if (!BankUtils.exist(bank, s)) return false;

        if (!BankUtils.isAvailable(bank, (Player) s)) {
            BPMessages.send(s, "Cannot-Access-Bank");
            return false;
        }

        String arg1 = args[1].toLowerCase();
        return arg1.equals("all") || arg1.equals("half") || !BPUtils.isInvalidNumber(arg1, s);
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        Player p = (Player) s;

        BPEconomy economy = BPEconomy.get(getPossibleBank(args, 2));

        BigDecimal amount;
        String arg1 = args[1].toLowerCase();
        switch (arg1) {
            case "all":
                amount = BigDecimal.valueOf(BankPlus.INSTANCE().getVaultEconomy().getBalance(p));
                break;

            case "half":
                amount = BigDecimal.valueOf(BankPlus.INSTANCE().getVaultEconomy().getBalance(p) / 2);
                break;

            default:
                amount = BPFormatter.getStyledBigDecimal(arg1);
        }
        economy.deposit((Player) s, amount);
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