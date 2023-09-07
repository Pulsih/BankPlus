package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.MultiEconomyManager;
import me.pulsi_.bankplus.economy.SingleEconomyManager;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SaveAllBankBalancesCmd extends BPCommand {

    public SaveAllBankBalancesCmd(String... aliases) {
        super(aliases);
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public boolean skipUsageWarn() {
        return true;
    }

    @Override
    public boolean onCommand(CommandSender s, String args[]) {
        if (confirm(s)) return false;

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled())
            Bukkit.getOnlinePlayers().forEach(p -> new MultiEconomyManager(p).saveBankBalance(true));
        else
            Bukkit.getOnlinePlayers().forEach(p -> new SingleEconomyManager(p).saveBankBalance(true));

        BPMessages.send(s, "Balances-Saved");
        if (Values.CONFIG.isSaveBalancesBroadcast()) BPLogger.info("All player balances have been saved!");
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        return null;
    }
}