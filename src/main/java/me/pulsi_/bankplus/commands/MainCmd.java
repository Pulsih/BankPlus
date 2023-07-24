package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.account.economy.MultiEconomyManager;
import me.pulsi_.bankplus.account.economy.SingleEconomyManager;
import me.pulsi_.bankplus.commands.cmdProcessor.MultiCmdProcessor;
import me.pulsi_.bankplus.commands.cmdProcessor.SingleCmdProcessor;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainCmd implements CommandExecutor, TabCompleter {

    private static final List<String> confirms = new ArrayList<>();

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
                    return true;

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

                case "resetall": {
                    if (!BPMethods.hasPermission(s, "bankplus.resetall")) return false;

                    if (args.length == 1) {
                        BPMessages.send(s,
                                BPMessages.getPrefix() + " &7Specify a mode between &a\"delete\" &7and &a\"maintain\"&7: &adelete &7will remove all players" +
                                        " money and they will be lost, &amaintain &7will move all players money from their banks to their vault balance."
                                , true);
                        return false;
                    }

                    String mode = args[1];
                    if (!mode.equalsIgnoreCase("delete") && !mode.equalsIgnoreCase("maintain")) {
                        BPMessages.send(s,
                                BPMessages.getPrefix() + " &cInvalid reset mode! Choose one between &a\"delete\" &cand &a\"maintain\"&c."
                                , true);
                        return false;
                    }

                    if (!confirms.contains(s.getName())) {
                        confirms.add(s.getName());
                        BPMessages.send(s,
                                BPMessages.getPrefix() + " &cWarning, this command is going to reset everyone's bank balance, based on the mode" +
                                        " you choose, this action is not reversible and may take few seconds, type the command again within 3 seconds to confirm."
                                , true);
                        Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> confirms.remove(s.getName()), 20L * 3);
                        return false;
                    }

                    resetAll(s, 0, mode);
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

    private static void resetAll(CommandSender s, int count, String mode) {

        int temp = 0;
        for (int i = 0; i < 60; i++) {
            if (count + temp >= Bukkit.getOfflinePlayers().length) {
                BPMessages.send(s, BPMessages.getPrefix() + " &2Task finished!", true);
                return;
            }

            OfflinePlayer oP = Bukkit.getOfflinePlayers()[count + temp];
            if (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled()) {

                MultiEconomyManager em;
                if (oP.isOnline()) em = new MultiEconomyManager(Bukkit.getPlayer(oP.getUniqueId()));
                else em = new MultiEconomyManager(oP);

                if (mode.equalsIgnoreCase("maintain")) {
                    BigDecimal bal = em.getBankBalance();
                    BankPlus.INSTANCE.getEconomy().depositPlayer(oP, bal.doubleValue());
                }

                for (String bankName : BankPlus.INSTANCE.getBankGuiRegistry().getBanks().keySet())
                    em.setBankBalance(BigDecimal.valueOf(0), bankName);
            } else {

                SingleEconomyManager em;
                if (oP.isOnline()) em = new SingleEconomyManager(Bukkit.getPlayer(oP.getUniqueId()));
                else em = new SingleEconomyManager(oP);

                if (mode.equalsIgnoreCase("maintain")) {
                    BigDecimal bal = em.getBankBalance();
                    BankPlus.INSTANCE.getEconomy().depositPlayer(oP, bal.doubleValue());
                }

                em.setBankBalance(BigDecimal.valueOf(0));
            }
            temp++;
        }

        int finalTemp = temp + 1;
        Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> resetAll(s, count + finalTemp, mode), 2);
    }
}