package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.bankSystem.BankListGui;
import me.pulsi_.bankplus.bankSystem.BankUtils;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
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
            if (!BPMethods.hasPermission(s, "bankplus.use")) return true;

            if (!(s instanceof Player)) {
                BPMessages.send(s, "Help-Message");
                return true;
            }
            Player p = (Player) s;

            if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {
                if (Values.CONFIG.isGuiModuleEnabled()) BankUtils.openBank(p, BankListGui.multipleBanksGuiID, false);
                else {
                    BPMessages.send(p, "Multiple-Personal-Bank", BPMethods.placeValues(p, new MultiEconomyManager(p).getBankBalance()));
                    BPMethods.playSound("PERSONAL", p);
                }
            } else {
                if (Values.CONFIG.isGuiModuleEnabled()) BankUtils.openBank(p);
                else {
                    BPMessages.send(p, "Personal-Bank", BPMethods.placeValues(p, new SingleEconomyManager(p).getBankBalance()));
                    BPMethods.playSound("PERSONAL", p);
                }
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
        if (args.length == 1) {
            List<String> cmds = new ArrayList<>();
            for (String identifier : commands.keySet())
                if (s.hasPermission("bankplus." + identifier)) cmds.add(identifier);

            List<String> args0 = new ArrayList<>();
            for (String arg : cmds)
                if (arg.startsWith(args[0].toLowerCase())) args0.add(arg);

            return args0;
        }

        String identifier = args[0].toLowerCase();
        if (!commands.containsKey(identifier)) return null;

        BPCommand cmd = commands.get(identifier);
        return cmd.tabCompletion(s, args);
    }
}