package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCmdExecution;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BalanceCmd extends BPCommand {

    public BalanceCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public BalanceCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank balance [bankName]");
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
        return true;
    }

    public BPCmdExecution onExecution(CommandSender s, String[] args) {
        Player p = (Player) s;

        List<Bank> banks = new ArrayList<>();
        if (args.length == 1) {
            banks.addAll(BankUtils.getAvailableBanks(p));
            if (banks.isEmpty()) {
                BPMessages.sendIdentifier(p, "No-Available-Banks");
                return BPCmdExecution.invalidExecution();
            }

        } else {
            Bank bank = BankRegistry.getBank(args[1]);
            if (!BankUtils.exist(bank, s)) return BPCmdExecution.invalidExecution();

            if (!BankUtils.isAvailable(bank, p)) {
                BPMessages.sendIdentifier(s, "Cannot-Access-Bank");
                return BPCmdExecution.invalidExecution();
            }
            banks.add(bank);
        }

        return new BPCmdExecution() {
            @Override
            public void execute() {
                if (banks.size() > 1)
                    BPMessages.sendIdentifier(
                            p,
                            "Multiple-Personal-Bank",
                            BPUtils.placeValues(p, BPEconomy.getBankBalancesSum(p))
                    );
                else {
                    Bank bank = banks.getFirst();
                    BPMessages.sendIdentifier(
                            p,
                            "Personal-Bank",
                            BPUtils.placeValues(p, bank.getBankEconomy().getBankBalance(p), BankUtils.getCurrentLevel(bank, p))
                    );
                }

                if (ConfigValues.isViewSoundEnabled()) BPUtils.playSound(ConfigValues.getPersonalSound(), p);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2) return BPArgs.getArgs(args, BankUtils.getAvailableBankNames((Player) s));
        return null;
    }
}