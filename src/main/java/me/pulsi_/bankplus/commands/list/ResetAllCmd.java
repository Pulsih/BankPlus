package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPMessages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResetAllCmd extends BPCommand {

    private final String identifier;

    public ResetAllCmd(String... aliases) {
        super(aliases);
        this.identifier = aliases[0];
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
        String mode = args[1];
        if (!mode.equalsIgnoreCase("delete") && !mode.equalsIgnoreCase("maintain")) {
            BPMessages.send(s,
                    BPMessages.getPrefix() + " &cInvalid reset mode! Choose one between &a\"delete\" &cand &a\"maintain\"&c."
                    , true);
            return false;
        }

        if (confirm(s)) return false;
        resetAll(s, 0, mode);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String args[]) {
        if (!s.hasPermission("bankplus." + identifier)) return null;

        if (args.length == 2) {
            List<String> args1 = new ArrayList<>();

            for (String arg : Arrays.asList("delete", "maintain"))
                if (arg.startsWith(args[1].toLowerCase())) args1.add(arg);
            return args1;
        }
        return null;
    }

    private void resetAll(CommandSender s, int count, String mode) {
        BPEconomy economy = BankPlus.getBPEconomy();
        Economy vaultEconomy = BankPlus.INSTANCE.getVaultEconomy();

        int temp = 0;
        for (int i = 0; i < 60; i++) {
            if (count + temp >= Bukkit.getOfflinePlayers().length) {
                BPMessages.send(s, BPMessages.getPrefix() + " &2Task finished!", true);
                return;
            }

            OfflinePlayer p = Bukkit.getOfflinePlayers()[count + temp];
            if (mode.equalsIgnoreCase("maintain")) {
                BigDecimal bal = economy.getBankBalance(p);
                vaultEconomy.depositPlayer(p, bal.doubleValue());
            }

            for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                economy.setBankBalance(p, BigDecimal.valueOf(0), bankName);
            temp++;
        }

        int finalTemp = temp + 1;
        Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> resetAll(s, count + finalTemp, mode), 2);
    }
}