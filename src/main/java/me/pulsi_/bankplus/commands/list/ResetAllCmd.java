package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPArgs;
import me.pulsi_.bankplus.utils.BPMessages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ResetAllCmd extends BPCommand {

    private final Economy vaultEconomy;

    public ResetAllCmd(String... aliases) {
        super(aliases);
        vaultEconomy = BankPlus.INSTANCE().getVaultEconomy();
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
        String mode = args[1];
        if (!mode.equalsIgnoreCase("delete") && !mode.equalsIgnoreCase("maintain")) {
            BPMessages.send(s, "%prefix% &cInvalid reset mode! Choose one between &a\"delete\" &cand &a\"maintain\"&c.", true);
            return false;
        }

        if (skipToConfirm(s)) return false;

        BPMessages.send(s, "%prefix% &aSuccessfully reset all players money! &8(&aWith &f" + mode + " &amode&8)", true);
        resetAll(Arrays.asList(Bukkit.getOfflinePlayers()), mode);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return BPArgs.getArgs(args, "delete", "maintain");
        return null;
    }

    private void resetAll(List<OfflinePlayer> offlinePlayers, String mode) {
        List<OfflinePlayer> copy = new ArrayList<>(offlinePlayers);
        Set<String> banks = BankPlus.INSTANCE().getBankRegistry().getBanks().keySet();

        for (int i = 0; i < 80; i++) {
            if (copy.isEmpty()) return;
            OfflinePlayer p = copy.remove(0);

            if (mode.equalsIgnoreCase("maintain")) vaultEconomy.depositPlayer(p, BPEconomy.getBankBalancesSum(p).doubleValue());
            for (String bankName : banks) BPEconomy.get(bankName).setBankBalance(p, BigDecimal.valueOf(0));
        }

        if (!copy.isEmpty())
            Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> resetAll(copy, mode), 1);
    }
}