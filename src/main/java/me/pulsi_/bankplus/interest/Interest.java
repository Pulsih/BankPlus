package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.ListUtils;
import me.pulsi_.bankplus.utils.Methods;
import me.pulsi_.bankplus.values.Values;
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
        long interestSave = plugin.players().getLong("Interest-Save");
        long delay = Values.CONFIG.getInterestDelay();

        if (interestSave <= 0) {
            interestCooldown.add(delay);
        } else {
            interestCooldown.add(interestSave);
            plugin.players().set("Interest-Save", null);
            plugin.savePlayers();
        }

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long count = interestCooldown.get(0);
            if (count <= 1) {
                giveInterestToEveryone();
                interestCooldown.set(0, delay);
                return;
            }
            long nextCount = count - 1;
            interestCooldown.set(0, nextCount);
        }, 0, Methods.ticksInMinutes(1));
    }

    public void saveInterest() {
        long interestSave = interestCooldown.get(0);
        if (interestSave <= 0) return;
        plugin.players().set("Interest-Save", interestSave);
        plugin.savePlayers();
    }

    public void giveInterestToEveryone() {
        double moneyPercentage = Values.CONFIG.getInterestMoneyGiven();
        long maxAmount = Values.CONFIG.getInterestMaxAmount();

        if (Values.CONFIG.isGivingInterestToOfflinePlayers())
            for (OfflinePlayer p : Bukkit.getOfflinePlayers())
                giveInterestOffline(p, moneyPercentage, maxAmount);
        for (Player p : Bukkit.getOnlinePlayers())
            if (p.hasPermission("bankplus.receive.interest"))
                giveInterest(p, moneyPercentage, maxAmount);
    }

    private void giveInterest(Player p, double moneyPercentage, long maxAmount) {
        long bankBalance = EconomyManager.getInstance().getBankBalance(p);
        long interestMoney = (long) (bankBalance * moneyPercentage);
        long maxBankCapacity = Values.CONFIG.getMaxBankCapacity();

        if (maxBankCapacity != 0) {
            if (bankBalance + interestMoney >= maxBankCapacity) {
                EconomyManager.getInstance().setPlayerBankBalance(p, maxBankCapacity);
                if (Values.CONFIG.isInterestBroadcastEnabled())
                    MessageManager.interestBroadcastMessageMax(p, maxBankCapacity - bankBalance);
            } else {
                if (bankBalance == 0) {
                    if (Values.CONFIG.isInterestBroadcastEnabled())
                        MessageManager.noMoneyInterest(p);
                    return;
                }
                if (interestMoney == 0) {
                    EconomyManager.getInstance().addPlayerBankBalance(p, 1);
                    if (Values.CONFIG.isInterestBroadcastEnabled())
                        MessageManager.interestBroadcastMessageMax(p, 1);
                } else {
                    if (interestMoney >= maxAmount) {
                        EconomyManager.getInstance().addPlayerBankBalance(p, maxAmount);
                        if (Values.CONFIG.isInterestBroadcastEnabled())
                            MessageManager.interestBroadcastMessageMax(p, maxAmount);
                        return;
                    }
                    EconomyManager.getInstance().addPlayerBankBalance(p, interestMoney);
                    if (Values.CONFIG.isInterestBroadcastEnabled())
                        MessageManager.interestBroadcastMessageMax(p, interestMoney);
                }
            }
        } else {
            if (bankBalance == 0) {
                if (Values.CONFIG.isInterestBroadcastEnabled())
                    MessageManager.noMoneyInterest(p);
                return;
            }
            if (interestMoney == 0) {
                EconomyManager.getInstance().addPlayerBankBalance(p, 1);
                if (Values.CONFIG.isInterestBroadcastEnabled())
                    MessageManager.interestBroadcastMessageMax(p, 1);
            } else {
                if (interestMoney >= maxAmount) {
                    EconomyManager.getInstance().addPlayerBankBalance(p, maxAmount);
                    if (Values.CONFIG.isInterestBroadcastEnabled())
                        MessageManager.interestBroadcastMessageMax(p, maxAmount);
                    return;
                }
                EconomyManager.getInstance().addPlayerBankBalance(p, interestMoney);
                if (Values.CONFIG.isInterestBroadcastEnabled())
                    MessageManager.interestBroadcastMessageMax(p, interestMoney);
            }
        }
    }

    private void giveInterestOffline(OfflinePlayer p, double moneyPercentage, long maxAmount) {
        long bankBalance = EconomyManager.getInstance().getBankBalance(p);
        long interestMoney = (long) (bankBalance * moneyPercentage);
        long maxBankCapacity = Values.CONFIG.getMaxBankCapacity();

        if (maxBankCapacity != 0) {
            if (bankBalance + interestMoney >= maxBankCapacity) {
                EconomyManager.getInstance().setPlayerBankBalance(p, maxBankCapacity);
                addOfflineInterest(p, maxBankCapacity);
            } else {
                if (bankBalance != 0) {
                    if (interestMoney == 0) {
                        EconomyManager.getInstance().addPlayerBankBalance(p, 1);
                        addOfflineInterest(p, 1);
                    } else {
                        if (interestMoney >= maxAmount) {
                            EconomyManager.getInstance().addPlayerBankBalance(p, maxAmount);
                            addOfflineInterest(p, maxAmount);
                            return;
                        }
                        EconomyManager.getInstance().addPlayerBankBalance(p, interestMoney);
                        addOfflineInterest(p, interestMoney);
                    }
                }
            }
        } else {
            if (bankBalance != 0) {
                if (interestMoney == 0) {
                    EconomyManager.getInstance().addPlayerBankBalance(p, 1);
                    addOfflineInterest(p, 1);
                } else {
                    if (interestMoney >= maxAmount) {
                        EconomyManager.getInstance().addPlayerBankBalance(p, maxAmount);
                        addOfflineInterest(p, maxAmount);
                        return;
                    }
                    EconomyManager.getInstance().addPlayerBankBalance(p, interestMoney);
                    addOfflineInterest(p, interestMoney);
                }
            }
        }
    }

    private void addOfflineInterest(OfflinePlayer p, long amount) {
        if (!Values.CONFIG.isOfflineInterestEarnedMessageEnabled()) return;
        long offlineInterest = EconomyManager.getInstance().getOfflineInterest(p);
        EconomyManager.getInstance().setOfflineInterest(p, offlineInterest + amount);
    }

    private void debug(String message) {
        if (ListUtils.INTEREST_DEBUG.get(0).equals("ENABLED"))
            ChatUtils.consoleMessage(message);
    }
}