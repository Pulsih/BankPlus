package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPArgs;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.List;

public class PayCmd extends BPCommand {

    public PayCmd(String... aliases) {
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

        BigDecimal amount = new BigDecimal(num);
        Player player = (Player) s;

        String fromBankName = Values.CONFIG.getMainGuiName();
        if (args.length > 3) fromBankName = args[3];

        if (!BankManager.exist(fromBankName)) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }
        if (!BankManager.isAvailable(fromBankName, player)) {
            BPMessages.send(s, "Cannot-Access-Bank");
            return false;
        }

        String toBankName = Values.CONFIG.getMainGuiName();
        if (args.length > 4) toBankName = args[4];

        if (!BankManager.exist(toBankName)) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }
        if (!BankManager.isAvailable(toBankName, target)) {
            BPMessages.send(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
            return false;
        }

        if (!skipToConfirm(s)) BPEconomy.get(fromBankName).pay((Player) s, target, amount, toBankName);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        Player p = (Player) s;

        Player target = args.length > 1 ? Bukkit.getPlayerExact(args[1]) : null;

        if (args.length == 3)
            return BPArgs.getArgs(args, "1", "2", "3");

        if (args.length == 4)
            return BPArgs.getArgs(args, BankManager.getAvailableBanks(p));

        if (args.length == 5)
            return BPArgs.getArgs(args, BankManager.getAvailableBanks(target));
        return null;
    }
}