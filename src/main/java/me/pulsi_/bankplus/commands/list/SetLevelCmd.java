package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.BPPlayerFiles;
import me.pulsi_.bankplus.bankSystem.BankReader;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.commands.MainCmd;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetLevelCmd extends BPCommand {

    private final String identifier;

    public SetLevelCmd(String... aliases) {
        super(aliases);
        this.identifier = aliases[0];
    }

    @Override
    public void register() {
        MainCmd.commands.put(identifier, this);
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
    public boolean onCommand(CommandSender s, String args[]) {
        OfflinePlayer p = Bukkit.getPlayerExact(args[1]);
        if (!p.hasPlayedBefore()) {
            BPMessages.send(s, "Invalid-Player");
            return false;
        }

        if (args.length == 2) {
            BPMessages.send(s, "Specify-Number");
            return false;
        }

        String level = args[2];
        if (BPUtils.isInvalidNumber(level, s)) return false;

        String bankName;
        if (Values.MULTIPLE_BANKS.isMultipleBanksEnabled()) {
            if (args.length == 3) {
                BPMessages.send(s, "Specify-Bank");
                return false;
            }

            bankName = args[3];
        } else bankName = Values.CONFIG.getMainGuiName();

        BankReader reader = new BankReader(bankName);
        if (!reader.exist()) {
            BPMessages.send(s, "Invalid-Bank");
            return false;
        }
        if (!reader.getLevels().contains(level)) {
            BPMessages.send(s, "Invalid-Bank-Level");
            return false;
        }
        if (confirm(s)) return false;

        BPPlayerFiles files = new BPPlayerFiles(p);
        files.getPlayerConfig().set("Banks." + bankName + ".Level", Integer.valueOf(level));
        files.savePlayerFile(true);

        BPMessages.send(s, "Set-Level-Message", "%player%$" + p.getName(), "%level%$" + level);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!s.hasPermission("bankplus." + identifier)) return null;

        if (args.length == 3) {
            List<String> args2 = new ArrayList<>();
            for (String arg : Arrays.asList("1", "2", "3"))
                if (arg.startsWith(args[2].toLowerCase())) args2.add(arg);
            return args2;
        }

        if (Values.MULTIPLE_BANKS.isMultipleBanksEnabled() && args.length == 4) {
            List<String> args3 = new ArrayList<>();
            for (String arg : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                if (arg.startsWith(args[3].toLowerCase())) args3.add(arg);
            return args3;
        }
        return null;
    }
}