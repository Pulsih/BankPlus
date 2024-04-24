package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.bankSystem.BankListGui;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MainCmd implements CommandExecutor, TabCompleter {

    public static final LinkedHashMap<String, BPCommand> commands = new LinkedHashMap<>();

    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {
        if (!Values.CONFIG.getWorldsBlacklist().isEmpty() && s instanceof Player) {
            Player p = (Player) s;
            if (Values.CONFIG.getWorldsBlacklist().contains(p.getWorld().getName()) && !p.hasPermission("bankplus.worlds.blacklist.bypass")) {
                BPMessages.send(p, "Cannot-Use-Bank-Here");
                return true;
            }
        }

        if (args.length == 0) {
            if (!BPUtils.hasPermission(s, "bankplus.use")) return true;

            if (s instanceof Player) {
                if (BankManager.getAvailableBanks((Player) s).isEmpty()) {
                    if (Values.CONFIG.isShowHelpMessageWhenNoAvailableBanks()) {
                        BPMessages.send(s, "Help-Message");
                    } else {
                        BPMessages.send(s, "No-Available-Banks");
                    }
                    return true;
                }
            } else {
                BPMessages.send(s, "Help-Message");
                return true;
            }
            Player p = (Player) s;

            if (Values.CONFIG.isGuiModuleEnabled()) {
                if (!Values.MULTIPLE_BANKS.enableMultipleBanksModule()) BankUtils.openBank(p);
                else {
                    if (!Values.MULTIPLE_BANKS.isDirectlyOpenIf1IsAvailable()) BankUtils.openBank(p, BankListGui.multipleBanksGuiID);
                    else {
                        List<String> availableBanks = BankManager.getAvailableBanks(p);
                        if (availableBanks.size() == 1) BankUtils.openBank(p, availableBanks.get(0));
                        else BankUtils.openBank(p, BankListGui.multipleBanksGuiID);
                    }
                }
            } else {
                BPMessages.send(p, "Multiple-Personal-Bank", BPUtils.placeValues(p, BPEconomy.getBankBalancesSum(p)));
                BPUtils.playSound("PERSONAL", p);
            }
            return true;
        }

        String identifier = args[0].toLowerCase();

        if (!commands.containsKey(identifier)) {
            BPMessages.send(s, "Unknown-Command");
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
                if (s.hasPermission("bankplus." + cmd.getIdentifier().toLowerCase())) cmds.add(cmd.getIdentifier());

            List<String> result = new ArrayList<>();
            for (String arg : cmds)
                if (arg.toLowerCase().startsWith(args0)) result.add(arg);
            return result;
        }

        if (!commands.containsKey(args0) || !s.hasPermission("bankplus." + args0)) return null;

        BPCommand cmd = commands.get(args0);
        if (cmd.playerOnly() && !(s instanceof Player)) return null;

        return cmd.tabCompletion(s, args);
    }
}