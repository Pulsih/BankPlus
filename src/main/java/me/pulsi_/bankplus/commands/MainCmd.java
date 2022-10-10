package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.cmdProcessor.MultiCmdProcessor;
import me.pulsi_.bankplus.commands.cmdProcessor.SingleCmdProcessor;
import me.pulsi_.bankplus.utils.BPChat;
import me.pulsi_.bankplus.utils.BPDebugger;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class MainCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {

        if (!Values.CONFIG.getWorldsBlacklist().isEmpty() && s instanceof Player) {
            Player p = (Player) s;
            if (Values.CONFIG.getWorldsBlacklist().contains(p.getWorld().getName()) && !p.hasPermission("bankplus.worlds.blacklist.bypass")) {
                BPMessages.send(p, "Cannot-Use-Bank-Here");
                return false;
            }
        }

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "reload": {
                    if (!BPMethods.hasPermission(s, "bankplus.reload")) return false;

                    boolean reloaded = BankPlus.INSTANCE.getDataManager().reloadPlugin();
                    if (reloaded) BPMessages.send(s, "Reload");
                    else BPMessages.send(s, "Failed-Reload");
                    return true;
                }

                case "help":
                    if (BPMethods.hasPermission(s, "bankplus.help")) BPMessages.send(s, "Help-Message");
                    break;

                case "restartinterest": {
                    if (!BPMethods.hasPermission(s, "bankplus.restart-interest")) return false;

                    if (!Values.CONFIG.isInterestEnabled()) {
                        BPMessages.send(s, "Interest-Disabled");
                        return false;
                    }
                    BankPlus.INSTANCE.getInterest().restartInterest();
                    BPMessages.send(s, "Interest-Restarted");
                    return true;
                }

                case "giveinterest": {
                    if (!BPMethods.hasPermission(s, "bankplus.give-interest")) return false;

                    if (!Values.CONFIG.isInterestEnabled()) {
                        BPMessages.send(s, "Interest-Disabled");
                        return false;
                    }
                    BankPlus.INSTANCE.getInterest().giveInterestToEveryone();
                    return true;
                }

                case "interest": {
                    if (!BPMethods.hasPermission(s, "bankplus.interest")) return false;

                    if (!Values.CONFIG.isInterestEnabled()) {
                        BPMessages.send(s, "Interest-Disabled");
                        return false;
                    }
                    BPMessages.send(s, "Interest-Time", "%time%$" + BPMethods.formatTime(BankPlus.INSTANCE.getInterest().getInterestCooldownMillis()));
                    return true;
                }

                case "interestmillis": {
                    if (!BPMethods.hasPermission(s, "bankplus.interestmillis")) return false;

                    if (!Values.CONFIG.isInterestEnabled()) {
                        BPMessages.send(s, "Interest-Disabled");
                        return false;
                    }
                    BPMessages.send(s, "Interest-Time", "%time%$" + BankPlus.INSTANCE.getInterest().getInterestCooldownMillis());
                    return true;
                }

                case "updatebanktop": {
                    if (!BPMethods.hasPermission(s, "bankplus.updatebanktop")) return false;
                    BankPlus.INSTANCE.getBankTopManager().updateBankTop();
                    BPMessages.send(s, "BankTop-Updated");
                    return true;
                }

                case "debug": {
                    if (!BPMethods.hasPermission(s, "bankplus.debug")) return false;
                    if (args.length == 1) {
                        s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aChoose a valid option: CHAT, DEPOSIT, INTEREST, GUI, WITHDRAW."));
                        return false;
                    }
                    switch (args[1].toLowerCase()) {
                        case "chat":
                            BPDebugger.toggleChatDebugger(s);
                            break;

                        case "deposit":
                            BPDebugger.toggleDepositDebugger(s);
                            break;

                        case "gui":
                            BPDebugger.toggleGuiDebugger(s);
                            break;

                        case "interest":
                            BPDebugger.debugInterest();
                            if (s instanceof Player)
                                s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aDone! Check the console for the debug report!"));
                            break;

                        case "withdraw":
                            BPDebugger.toggleWithdrawDebugger(s);
                            break;

                        default:
                            s.sendMessage(BPChat.color("&a&lBank&9&lPlus &aChoose a valid option: CHAT, INTEREST, GUI."));
                    }
                    return true;
                }
            }
        }

        if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) MultiCmdProcessor.processCmd(s, args);
        else SingleCmdProcessor.processCmd(s, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command command, String alias, String[] args) {
        return Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled() ? MultiCmdProcessor.getMultiTabComplete(s, args) : SingleCmdProcessor.getSingleTabComplete(s, args);
    }
}