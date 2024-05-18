package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.loanSystem.LoanUtils;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.Values;
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

    private final BankRegistry registry;

    public LoanCmd(FileConfiguration commandsConfig, String... aliases) {
        super(commandsConfig, aliases);

        registry = BankPlus.INSTANCE().getBankRegistry();
    }

    @Override
    public List<String> defaultUsage() {
        return Arrays.asList(
                "%prefix% &cUsage: &7/bank loan [action] [playerName] [amount] <fromBankName> <toBankName>",
                "",
                "&7Possible actions:"
                , " &8* &aGive"
                , " &8* &aRequest"
                , " &8* &aAccept"
                , " &8* &aDeny"
                , " &8* &aCancel"
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
    public boolean skipUsageWarn() {
        return false;
    }

    @Override
    public boolean preCmdChecks(CommandSender s, String[] args) {
        Player sender = (Player) s;

        String action = args[1].toLowerCase();
        switch (action) {
            case "accept":
            case "cancel":
            case "deny":
                return true;
        }

        if (LoanUtils.hasSentRequest(sender)) {
            BPMessages.send(sender, "Loan-Already-Sent");
            return false;
        }

        if (!action.equals("give") && !action.equals("request")) {
            BPMessages.send(sender, "Invalid-Action");
            return false;
        }

        if (args.length == 2) {
            BPMessages.send(sender, "Specify-Player");
            return false;
        }
        String receiverName = args[2];

        if (args.length == 3) {
            BPMessages.send(sender, "Specify-Number");
            return false;
        }

        String num = args[3];
        if (BPUtils.isInvalidNumber(num, sender)) return false;

        // Request bank-to-player, if this passes, the request will be player-to-player.
        if (action.equals("request") && registry.getBanks().containsKey(receiverName)) {
            if (!BankUtils.isAvailable(receiverName, sender)) {
                BPMessages.send(sender, "Cannot-Access-Bank");
                return false;
            }
            return true;
        }

        Player target = Bukkit.getPlayerExact(receiverName);
        if (target == null || target.equals(s)) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        // Check if the bank specified by the request sender is available for him.
        Bank senderBank = BankUtils.getBank(getPossibleBank(args, 4));

        if (!BankUtils.exist(senderBank, s)) return false;
        if (!BankUtils.isAvailable(senderBank, sender)) {
            BPMessages.send(sender, "Cannot-Access-Bank");
            return false;
        }

        // Check if the bank specified by the request sender is available for the request target.
        Bank receiverBank = BankUtils.getBank(getPossibleBank(args, 5));

        if (!BankUtils.exist(receiverBank, s)) return false;
        if (!BankUtils.isAvailable(receiverBank, target)) {
            BPMessages.send(sender, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
            return false;
        }
        return true;
    }

    @Override
    public void onExecution(CommandSender s, String[] args) {
        Player sender = (Player) s;

        String action = args[1].toLowerCase();
        switch (action) {
            case "accept":
                LoanUtils.acceptRequest(sender);
                return;

            case "deny":
                LoanUtils.denyRequest(sender);
                return;

            case "cancel":
                LoanUtils.cancelRequest(sender);
                return;
        }

        String receiverName = args[2];
        BigDecimal amount = new BigDecimal(args[3]);

        if (action.equals("request") && registry.getBanks().containsKey(receiverName)) {
            LoanUtils.sendLoan(sender, BankUtils.getBank(receiverName), amount);
            return;
        }

        Player target = Bukkit.getPlayerExact(receiverName);
        Bank senderBank = BankUtils.getBank(getPossibleBank(args, 4));
        Bank receiverBank = BankUtils.getBank(getPossibleBank(args, 5));
        LoanUtils.sendRequest(sender, target, amount, senderBank, receiverBank, action);
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