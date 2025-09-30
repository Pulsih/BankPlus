package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCmdExecution;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.loanSystem.LoanUtils;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoanCmd extends BPCommand {

    public LoanCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    @Override
    public List<String> defaultUsage() {
        return Arrays.asList(
                "%prefix% Usage: /bank loan [action] [playerName] [amount] [fromBankName] [toBankName]",
                "",
                "Possible actions:"
                , " <dark_gray>*</dark_gray> Give"
                , " <dark_gray>*</dark_gray> Request"
                , " <dark_gray>*</dark_gray> Accept"
                , " <dark_gray>*</dark_gray> Deny"
                , " <dark_gray>*</dark_gray> Cancel"
        );
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
        Player sender = (Player) s;

        String action = args[1].toLowerCase();
        switch (action) {
            case "accept":
                return new BPCmdExecution() {
                    @Override
                    public void execute() {
                        LoanUtils.acceptRequest(sender);
                    }
                };

            case "cancel":
                return new BPCmdExecution() {
                    @Override
                    public void execute() {
                        LoanUtils.cancelRequest(sender);
                    }
                };

            case "deny":
                return new BPCmdExecution() {
                    @Override
                    public void execute() {
                        LoanUtils.denyRequest(sender);
                    }
                };
        }

        if (LoanUtils.hasSentRequest(sender)) {
            BPMessages.sendIdentifier(sender, "Loan-Already-Sent");
            return BPCmdExecution.invalidExecution();
        }

        if (!action.equals("give") && !action.equals("request")) {
            BPMessages.sendIdentifier(sender, "Invalid-Action");
            return BPCmdExecution.invalidExecution();
        }

        if (args.length == 2) {
            BPMessages.sendIdentifier(sender, "Specify-Player");
            return BPCmdExecution.invalidExecution();
        }
        if (args.length == 3) {
            BPMessages.sendIdentifier(sender, "Specify-Number");
            return BPCmdExecution.invalidExecution();
        }

        String targetName = args[2], amountString = args[3];

        if (BPUtils.isInvalidNumber(amountString, sender)) return BPCmdExecution.invalidExecution();
        BigDecimal amount = new BigDecimal(amountString);

        // If the target name is the name of a bank, it means that
        // the player is trying to request a loan from a bank.
        if (action.equals("request") && BankUtils.exist(targetName)) {
            if (!BankUtils.isAvailable(targetName, sender)) {
                BPMessages.sendIdentifier(sender, "Cannot-Access-Bank");
                return BPCmdExecution.invalidExecution();
            }

            return new BPCmdExecution() {
                @Override
                public void execute() {
                    LoanUtils.sendLoan(sender, BankRegistry.getBank(targetName), amount);
                }
            };
        } else {
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null || target.equals(s)) {
                BPMessages.sendIdentifier(s, "Invalid-Player");
                return BPCmdExecution.invalidExecution();
            }

            // Check if the bank specified by the request sender is available for him.
            Bank senderBank = BankRegistry.getBank(getPossibleBank(args, 4));

            if (!BankUtils.exist(senderBank, s)) return BPCmdExecution.invalidExecution();
            if (!BankUtils.isAvailable(senderBank, sender)) {
                BPMessages.sendIdentifier(sender, "Cannot-Access-Bank");
                return BPCmdExecution.invalidExecution();
            }

            // Check if the bank specified by the request sender is available for the request target.
            Bank receiverBank = BankRegistry.getBank(getPossibleBank(args, 5));

            if (!BankUtils.exist(receiverBank, s)) return BPCmdExecution.invalidExecution();
            if (!BankUtils.isAvailable(receiverBank, target)) {
                BPMessages.sendIdentifier(sender, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
                return BPCmdExecution.invalidExecution();
            }

            return new BPCmdExecution() {
                @Override
                public void execute() {
                    LoanUtils.sendRequest(sender, target, amount, senderBank, receiverBank, action);
                }
            };
        }

    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        Player p = (Player) s;

        if (args.length == 2) {
            if (LoanUtils.hasSentRequest(p))
                return BPArgs.getArgs(args, "cancel");

            if (LoanUtils.hasRequest(p))
                return BPArgs.getArgs(args, "accept", "deny");

            return BPArgs.getArgs(args, "give", "request");
        }

        if (args.length == 3) {
            List<String> availableBanks = new ArrayList<>();
            if (args[1].equalsIgnoreCase("request")) availableBanks.addAll(BankUtils.getAvailableBankNames(p));

            availableBanks.addAll(BPArgs.getOnlinePlayers(args));
            return BPArgs.getArgs(args, availableBanks);
        }

        if (args.length == 4)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 5)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames(p));

        if (args.length == 6)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames(Bukkit.getPlayerExact(args[3])));

        return null;
    }
}