package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankGuiRegistry;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.loanSystem.LoanUtils;
import me.pulsi_.bankplus.utils.BPArgs;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LoanCmd extends BPCommand {

    private final BankGuiRegistry registry;

    public LoanCmd(String... aliases) {
        super(aliases);

        registry = BankPlus.INSTANCE().getBankGuiRegistry();
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
        Player sender = (Player) s;

        if (args.length == 1) {
            BPMessages.send(sender, "Specify-Action");
            return false;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "accept":
                LoanUtils.acceptRequest(sender);
                return true;

            case "deny":
                LoanUtils.denyRequest(sender);
                return true;

            case "cancel":
                LoanUtils.cancelRequest(sender);
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
        String targetName = args[2];

        if (args.length == 3) {
            BPMessages.send(sender, "Specify-Number");
            return false;
        }

        String num = args[3];
        if (BPUtils.isInvalidNumber(num, sender)) return false;
        BigDecimal amount = new BigDecimal(num);

        BankManager manager = BankPlus.getBankManager();
        if (action.equals("request") && registry.getBanks().containsKey(targetName)) {
            if (!manager.isAvailable(targetName, sender)) {
                BPMessages.send(sender, "Cannot-Access-Bank");
                return false;
            }
            LoanUtils.sendLoan(sender, targetName, amount);
            return true;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || target.equals(s)) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        String fromBankName = Values.CONFIG.getMainGuiName();
        if (args.length > 4) fromBankName = args[4];

        if (!manager.exist(fromBankName)) {
            BPMessages.send(sender, "Invalid-Bank");
            return false;
        }
        if (!manager.isAvailable(fromBankName, sender)) {
            BPMessages.send(sender, "Cannot-Access-Bank");
            return false;
        }

        String toBankName = Values.CONFIG.getMainGuiName();
        if (args.length > 5) toBankName = args[5];

        if (!manager.exist(toBankName)) {
            BPMessages.send(sender, "Invalid-Bank");
            return false;
        }
        if (!manager.isAvailable(toBankName, target)) {
            BPMessages.send(sender, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
            return false;
        }

        if (!confirm(s)) LoanUtils.sendRequest(sender, target, amount, fromBankName, toBankName, action);
        return true;
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
            if (args[1].equalsIgnoreCase("request")) availableBanks.addAll(BankPlus.getBankManager().getAvailableBanks(p));

            availableBanks.addAll(BPArgs.getOnlinePlayers(args));
            return BPArgs.getArgs(args, availableBanks);
        }

        if (args.length == 4)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 5)
            return BPArgs.getArgs(args, BankPlus.getBankManager().getAvailableBanks(p));

        if (args.length == 6)
            return BPArgs.getArgs(args, BankPlus.getBankManager().getAvailableBanks(Bukkit.getPlayerExact(args[3])));

        return null;
    }
}