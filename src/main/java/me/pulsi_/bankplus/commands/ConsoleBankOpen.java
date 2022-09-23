package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.bankGuis.BanksHolder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ConsoleBankOpen implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof ConsoleCommandSender){
            if(args.length == 0){
                sender.sendMessage("[BankPlus]Error! Use /bankopen <player>");
                return false;
            }
            if(args.length == 1){
                String target = args[0];
                Player targetPlayer = Bukkit.getPlayerExact(target);
                if(targetPlayer == null){
                    sender.sendMessage("[BankPlus]Error! Target not found");
                    return false;
                }else{
                    BanksHolder banksHolder = new BanksHolder();
                    banksHolder.openBank(targetPlayer);
                }
            }
        }
        return false;
    }
}
