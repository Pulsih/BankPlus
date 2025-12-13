package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.BPCmdExecution;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.economy.EconomyUtils;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ResetAllCmd extends BPCommand {

    private final Economy vaultEconomy;

    public ResetAllCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
        vaultEconomy = BankPlus.INSTANCE().getVaultEconomy();
    }

    public ResetAllCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
        vaultEconomy = BankPlus.INSTANCE().getVaultEconomy();
    }

    @Override
    public List<String> defaultUsage() {
        return Arrays.asList(
                "%prefix% Usage: /bank resetall [mode]",
                "",
                "Specify a mode between <aqua>\"delete\"</aqua> and <aqua>\"maintain\"</aqua>.",
                "- delete: Will remove all players money and they will be lost",
                "- maintain: Will move all players money from their banks to their vault balance."
        );
    }

    @Override
    public int defaultConfirmCooldown() {
        return 5;
    }

    @Override
    public List<String> defaultConfirmMessage() {
        return Collections.singletonList(
                "%prefix% <red>Warning, this command is going to reset everyone's bank balance," +
                        " based on the mode you choose, this action is not reversible, type the" +
                        " command again within 5 seconds to confirm."
        );
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
        return false;
    }

    @Override
    public boolean skipUsage() {
        return false;
    }

    @Override
    public BPCmdExecution onExecution(CommandSender s, String[] args) {
        String mode = args[1];
        if (!mode.equalsIgnoreCase("delete") && !mode.equalsIgnoreCase("maintain")) {
            BPMessages.sendMessage(s, "%prefix% Invalid reset mode! Choose one between <aqua>\"delete\"</aqua> and <aqua>\"maintain\"</aqua>.");
            return BPCmdExecution.invalidExecution();
        }

        return new BPCmdExecution() {
            @Override
            public void execute() {
                BPMessages.sendMessage(s, "%prefix% Successfully reset all players money! <dark_gray>(</dark_gray>With <white>" + mode + "</white> mode<dark_gray>)");
                resetAll(Arrays.asList(Bukkit.getOfflinePlayers()), mode);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return BPArgs.getArgs(args, "delete", "maintain");
        return null;
    }

    private void resetAll(List<OfflinePlayer> offlinePlayers, String mode) {
        List<OfflinePlayer> copy = new ArrayList<>(offlinePlayers);
        List<BPEconomy> economies = BPEconomy.list();

        for (int i = 0; i < 50; i++) {
            if (copy.isEmpty()) {
                EconomyUtils.saveEveryone(true);
                return;
            }
            OfflinePlayer p = copy.removeFirst();

            if (mode.equalsIgnoreCase("maintain")) vaultEconomy.depositPlayer(p, BPEconomy.getBankBalancesSum(p).doubleValue());

            for (BPEconomy economy : economies) {
                economy.setBankBalance(p, BigDecimal.ZERO);
                economy.setDebt(p, BigDecimal.ZERO);
                economy.setBankLevel(p, 1);
            }
        }

        Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> resetAll(copy, mode), 1);
    }
}