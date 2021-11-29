package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.ListUtils;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.configs.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Interest {

    private final BankPlus plugin;
    public Interest(BankPlus plugin) {
        this.plugin = plugin;
    }

    public static List<Long> interestCooldown = new ArrayList<>();

    public void startsInterest() {
        final long interestSave = plugin.players().getLong("Interest-Save");
        final long delay = ConfigValues.getInterestDelay();
        if (interestSave <= 0) {
            interestCooldown.add(delay);
        } else {
            interestCooldown.add(interestSave);
            plugin.players().set("Interest-Save", null);
            plugin.savePlayers();
        }

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            debug("&aBANKPLUS &8-> &3INTEREST &8: &fDefault interest delay is: " + delay);
            final long count = interestCooldown.get(0);
            debug("&aBANKPLUS &8-> &3INTEREST &8: &fInterest count: " + count);
            if (count <= 1) {
                debug("&aBANKPLUS &8-> &3INTEREST &8: &fCount has terminated, giving interest...");
                giveInterestToEveryone();
                debug("&aBANKPLUS &8-> &3INTEREST &8: &fResetting interest delay...");
                interestCooldown.set(0, delay);
                return;
            }
            final long nextCount = count - 1;
            debug("&aBANKPLUS &8-> &3INTEREST &8: &f1 Minute passed: COUNT FROM " + count + " TO " + nextCount);
            interestCooldown.set(0, nextCount);
        }, 0, Methods.ticksInMinutes(1));
    }

    public void saveInterest() {
        final long interestSave = interestCooldown.get(0);
        if (interestSave <= 0) return;
        plugin.players().set("Interest-Save", interestSave);
        plugin.savePlayers();
    }

    public void giveInterestToEveryone() {
        double moneyPercentage = ConfigValues.getInterestMoneyGiven();
        long maxAmount = ConfigValues.getInterestMaxAmount();
        if (ConfigValues.isGivingInterestToOfflinePlayers()) {
            for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                giveInterestOffline(p, moneyPercentage, maxAmount);
            }
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("bankplus.receive.interest")) continue;
            giveInterest(p, moneyPercentage, maxAmount);
        }
    }

    private void giveInterest(Player p, double moneyPercentage, long maxAmount) {
        final EconomyManager economy = new EconomyManager(plugin);
        final MessageManager messMan = new MessageManager(plugin);
        final long bankBalance = economy.getBankBalance(p);
        final long interestMoney = (long)(bankBalance * moneyPercentage);
        long maxBankCapacity = ConfigValues.getMaxBankCapacity();

        if (maxBankCapacity != 0) {
            if (bankBalance + interestMoney >= maxBankCapacity) {
                economy.setPlayerBankBalance(p, maxBankCapacity);
                if (ConfigValues.isInterestBroadcastEnabled())
                    messMan.interestBroadcastMessageMax(p, maxBankCapacity - bankBalance);
            } else {
                if (bankBalance == 0) {
                    if (ConfigValues.isInterestBroadcastEnabled())
                        messMan.noMoneyInterest(p);
                    return;
                }
                if (interestMoney == 0) {
                    economy.addPlayerBankBalance(p, 1);
                    if (ConfigValues.isInterestBroadcastEnabled())
                        messMan.interestBroadcastMessageMax(p, 1);
                } else {
                    if (interestMoney >= maxAmount) {
                        economy.addPlayerBankBalance(p, maxAmount);
                        if (ConfigValues.isInterestBroadcastEnabled())
                            messMan.interestBroadcastMessageMax(p, maxAmount);
                        return;
                    }
                    economy.addPlayerBankBalance(p, interestMoney);
                    if (ConfigValues.isInterestBroadcastEnabled())
                        messMan.interestBroadcastMessageMax(p, interestMoney);
                }
            }
        } else {
            if (bankBalance == 0) {
                if (ConfigValues.isInterestBroadcastEnabled())
                    messMan.noMoneyInterest(p);
                return;
            }
            if (interestMoney == 0) {
                economy.addPlayerBankBalance(p, 1);
                if (ConfigValues.isInterestBroadcastEnabled())
                    messMan.interestBroadcastMessageMax(p, 1);
            } else {
                if (interestMoney >= maxAmount) {
                    economy.addPlayerBankBalance(p, maxAmount);
                    if (ConfigValues.isInterestBroadcastEnabled())
                        messMan.interestBroadcastMessageMax(p, maxAmount);
                    return;
                }
                economy.addPlayerBankBalance(p, interestMoney);
                if (ConfigValues.isInterestBroadcastEnabled())
                    messMan.interestBroadcastMessageMax(p, interestMoney);
            }
        }
    }

    private void giveInterestOffline(OfflinePlayer p, double moneyPercentage, long maxAmount) {
        EconomyManager economy = new EconomyManager(plugin);
        final long bankBalance = economy.getBankBalance(p);
        final long interestMoney = (long)(bankBalance * moneyPercentage);
        long maxBankCapacity = plugin.config().getLong("General.Max-Bank-Capacity");

        if (maxBankCapacity != 0) {
            if (bankBalance + interestMoney >= maxBankCapacity) {
                economy.setPlayerBankBalance(p, maxBankCapacity);
                addOfflineInterest(p, maxBankCapacity);
            } else {
                if (bankBalance != 0) {
                    if (interestMoney == 0) {
                        economy.addPlayerBankBalance(p, 1);
                        addOfflineInterest(p, 1);
                    } else {
                        if (interestMoney >= maxAmount) {
                            economy.addPlayerBankBalance(p, maxAmount);
                            addOfflineInterest(p, maxAmount);
                            return;
                        }
                        economy.addPlayerBankBalance(p, interestMoney);
                        addOfflineInterest(p, interestMoney);
                    }
                }
            }
        } else {
            if (bankBalance != 0) {
                if (interestMoney == 0) {
                    economy.addPlayerBankBalance(p, 1);
                    addOfflineInterest(p, 1);
                } else {
                    if (interestMoney >= maxAmount) {
                        economy.addPlayerBankBalance(p, maxAmount);
                        addOfflineInterest(p, maxAmount);
                        return;
                    }
                    economy.addPlayerBankBalance(p, interestMoney);
                    addOfflineInterest(p, interestMoney);
                }
            }
        }
    }

    private void addOfflineInterest(OfflinePlayer p, long amount) {
        if (!ConfigValues.isOfflineInterestEarnedMessageEnabled()) return;
        EconomyManager economy = new EconomyManager(plugin);
        final long offlineInterest = economy.getOfflineInterest(p);
        economy.setOfflineInterest(p, offlineInterest + amount);
    }

    private void debug(String message) {
        if (ListUtils.INTEREST_DEBUG.get(0).equals("ENABLED"))
            ChatUtils.consoleMessage(message);
    }
}