package me.pulsi_.bankplus.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TabCompletion implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender s, Command command, String alias, String[] args) {

        List<String> args1 = new ArrayList<>();
        if (s.hasPermission("bankplus.add")) args1.add("add");
        if (s.hasPermission("bankplus.balance")) {
            args1.add("balance");
            args1.add("bal");
        }
        if (s.hasPermission("bankplus.deposit")) args1.add("deposit");
        if (s.hasPermission("bankplus.help")) args1.add("help");
        if (s.hasPermission("bankplus.open")) args1.add("open");
        if (s.hasPermission("bankplus.pay")) args1.add("pay");
        if (s.hasPermission("bankplus.reload")) args1.add("reload");
        if (s.hasPermission("bankplus.remove")) args1.add("remove");
        if (s.hasPermission("bankplus.restart-interest")) args1.add("restartInterest");
        if (s.hasPermission("bankplus.set")) args1.add("set");
        if (s.hasPermission("bankplus.view")) args1.add("view");
        if (s.hasPermission("bankplus.withdraw")) args1.add("withdraw");

        List<String> resultArgs1 = new ArrayList<>();
        if (args.length == 1) {
            for (String a : args1) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase()))
                    resultArgs1.add(a);
            }
            return resultArgs1;
        }

        return null;
    }
}