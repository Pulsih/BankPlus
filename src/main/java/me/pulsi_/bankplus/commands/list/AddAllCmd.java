package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
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
import java.util.ArrayList;
import java.util.List;

public class AddAllCmd extends BPCommand {

    public AddAllCmd(String... aliases) {
        super(aliases);
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public boolean skipUsageWarn() {
        return false;
    }

    @Override
    public boolean onCommand(CommandSender s, String[] args) {
        String num = args[1];
        if (BPUtils.isInvalidNumber(num, s)) return false;

        String bankName = Values.CONFIG.getMainGuiName();
        if (args.length > 2) bankName = args[2];

        if (!BankManager.exist(bankName)) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }

        if (skipToConfirm(s)) return false;

        BPMessages.send(s, "%prefix% &aSuccessfully added &f" + num + " &amoney to all online players!", true);
        addAll(new ArrayList<>(Bukkit.getOnlinePlayers()), new BigDecimal(num), bankName);
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

    private void addAll(List<Player> onlinePlayers, BigDecimal amount, String bankName) {
        List<Player> copy = new ArrayList<>(onlinePlayers);

        BPEconomy economy = BPEconomy.get(bankName);
        for (int i = 0; i < 80; i++) {
            if (copy.isEmpty()) return;
            economy.addBankBalance(copy.remove(0), amount);
        }

        if (!onlinePlayers.isEmpty())
            Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> addAll(copy, amount, bankName), 1);
    }
}