package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PayCmd extends BPCommand {

    public PayCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Arrays.asList(
                "%prefix% &cUsage: &7/bank pay [player] [amount] <fromBankName> <toBankName>",
                "",
                "&7<fromBankName> The bank from where to take the money.",
                "&7<toBankName>: The bank where to send the money."
        );
    }

    @Override
    public int defaultConfirmCooldown() {
        return 5;
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
        return Collections.singletonList("%prefix% &aType again within 5 seconds to confirm your payment.");
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
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || target.equals(s)) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        if (args.length == 2) {
            BPMessages.send(s, "Specify-Number");
            return false;
        }

        String num = args[2];
        if (BPUtils.isInvalidNumber(num, s)) return false;

        Bank fromBank = BankUtils.getBank(getPossibleBank(args, 3));

        if (!BankUtils.exist(fromBank, s)) return false;
        if (!BankUtils.isAvailable(fromBank, (Player) s)) {
            BPMessages.send(s, "Cannot-Access-Bank");
            return false;
        }

        Bank toBank = BankUtils.getBank(getPossibleBank(args, 4));

        if (!BankUtils.exist(toBank, s)) return false;
        if (!BankUtils.isAvailable(toBank, target)) {
            BPMessages.send(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
            return false;
        }
        return true;
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        BankUtils.getBank(getPossibleBank(args, 3)).getBankEconomy().pay(
                (Player) s,
                Bukkit.getPlayerExact(args[1]),
                new BigDecimal(args[2]),
                BankUtils.getBank(getPossibleBank(args, 4))
        );
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 4)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames((Player) s));

        if (args.length == 5)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames(Bukkit.getPlayerExact(args[1])));
        return null;
    }
}