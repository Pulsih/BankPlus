package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankListGui;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import me.pulsi_.bankplus.values.MultipleBanksValues;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static me.pulsi_.bankplus.commands.BPCmdRegistry.commands;

public class MainCmd implements CommandExecutor, TabCompleter {

    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {
        if (!ConfigValues.getWorldsBlacklist().isEmpty() && s instanceof Player p) {
            if (ConfigValues.getWorldsBlacklist().contains(p.getWorld().getName()) && !p.hasPermission("bankplus.worlds.blacklist.bypass")) {
                BPMessages.sendIdentifier(p, "Cannot-Use-Bank-Here");
                return true;
            }
        }

        if (args.length == 0) {
            if (!BPUtils.hasPermission(s, "bankplus.use")) return true;

            if (s instanceof Player p) {
                if (BankUtils.getAvailableBankNames((Player) s).isEmpty()) {
                    if (ConfigValues.isShowingHelpWhenNoBanksAvailable()) BPMessages.sendIdentifier(s, "Help-Message");
                    else BPMessages.sendIdentifier(s, "No-Available-Banks");
                    return true;
                }
            } else {
                BPMessages.sendIdentifier(s, "Help-Message");
                return true;
            }

            if (ConfigValues.isGuiModuleEnabled()) {
                if (!MultipleBanksValues.enableMultipleBanksModule()) BankRegistry.getBank(ConfigValues.getMainGuiName()).getBankGui().openBankGui(p);
                else {
                    if (!MultipleBanksValues.isDirectlyOpenIf1IsAvailable()) new BankListGui().openBankGui(p);
                    else {
                        List<Bank> availableBanks = BankUtils.getAvailableBanks(p);
                        if (availableBanks.size() == 1) availableBanks.getFirst().getBankGui().openBankGui(p);
                        else new BankListGui().openBankGui(p);
                    }
                }
            } else {
                BPMessages.sendIdentifier(p, "Multiple-Personal-Bank", BPUtils.placeValues(p, BPEconomy.getBankBalancesSum(p)));
                if (ConfigValues.isPersonalSoundEnabled()) BPUtils.playSound(ConfigValues.getPersonalSound(), p);
            }
            return true;
        }

        String identifier = args[0].toLowerCase();

        if (!commands.containsKey(identifier)) {
            BPMessages.sendIdentifier(s, "Unknown-Command");
            return true;
        }

        BPCommand cmd = commands.get(identifier);
        cmd.execute(s, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command command, String alias, String[] args) {
        if (args.length == 0) return null;

        String args0 = args[0].toLowerCase();

        if (args.length == 1) {
            List<String> cmds = new ArrayList<>();
            for (BPCommand cmd : commands.values())
                if (s.hasPermission(cmd.permission)) cmds.add(cmd.commandID);

            List<String> result = new ArrayList<>();
            for (String arg : cmds)
                if (arg.toLowerCase().startsWith(args0)) result.add(arg);
            return result;
        }

        BPCommand cmd = commands.get(args0);
        if (cmd == null || !s.hasPermission(cmd.permission) || (cmd.playerOnly() && !(s instanceof Player))) return null;

        return cmd.tabCompletion(s, args);
    }
}