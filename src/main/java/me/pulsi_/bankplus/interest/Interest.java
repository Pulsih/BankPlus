package me.pulsi_.bankplus.interest;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.MethodUtils;
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
        final long delay = plugin.config().getLong("Interest.Delay");
        if (interestSave <= 0) {
            interestCooldown.add(delay);
        } else {
            interestCooldown.add(interestSave);
            plugin.players().set("Interest-Save", null);
            plugin.savePlayers();
        }

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            final long count = interestCooldown.get(0);
            if (count <= 1) {
                giveInterestToEveryone();
                interestCooldown.set(0, delay);
                return;
            }
            interestCooldown.set(0, count - 1);
        }, 0, MethodUtils.ticksInMinutes(1));
    }

    public void saveInterest() {
        final long interestSave = interestCooldown.get(0);
        plugin.players().set("Interest-Save", interestSave);
        plugin.savePlayers();
    }

    public void giveInterestToEveryone() {
        double moneyPercentage = plugin.config().getDouble("Interest.Money-Given");
        long maxAmount = plugin.config().getLong("Interest.Max-Amount");
        if (plugin.config().getBoolean("Interest.Give-To-Offline-Players")) {
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
        long maxBankCapacity = plugin.config().getLong("General.Max-Bank-Capacity");

        if (maxBankCapacity != 0) {
            if (bankBalance + interestMoney >= maxBankCapacity) {
                economy.setPlayerBankBalance(p, maxBankCapacity);
                if (plugin.messages().getBoolean("Interest-Broadcast.Enabled"))
                    messMan.interestBroadcastMessageMax(p, maxBankCapacity - bankBalance);
            } else {
                if (bankBalance != 0) {
                    if (interestMoney == 0) {
                        economy.addPlayerBankBalance(p, 1);
                        if (plugin.messages().getBoolean("Interest-Broadcast.Enabled"))
                            messMan.interestBroadcastMessageMax(p, 1);
                    } else {
                        if (interestMoney >= maxAmount) {
                            economy.addPlayerBankBalance(p, maxAmount);
                            if (plugin.messages().getBoolean("Interest-Broadcast.Enabled"))
                                messMan.interestBroadcastMessageMax(p, maxAmount);
                            return;
                        }
                        economy.addPlayerBankBalance(p, interestMoney);
                        if (plugin.messages().getBoolean("Interest-Broadcast.Enabled"))
                            messMan.interestBroadcastMessageMax(p, interestMoney);
                    }
                } else {
                    if (plugin.messages().getBoolean("Interest-Broadcast.Enabled"))
                        messMan.noMoneyInterest(p);
                }
            }
        } else {
            if (bankBalance != 0) {
                if (interestMoney == 0) {
                    economy.addPlayerBankBalance(p, 1);
                    if (plugin.messages().getBoolean("Interest-Broadcast.Enabled"))
                        messMan.interestBroadcastMessageMax(p, 1);
                } else {
                    if (interestMoney >= maxAmount) {
                        economy.addPlayerBankBalance(p, maxAmount);
                        if (plugin.messages().getBoolean("Interest-Broadcast.Enabled"))
                            messMan.interestBroadcastMessageMax(p, maxAmount);
                        return;
                    }
                    economy.addPlayerBankBalance(p, interestMoney);
                    if (plugin.messages().getBoolean("Interest-Broadcast.Enabled"))
                        messMan.interestBroadcastMessageMax(p, interestMoney);
                }
            } else {
                if (plugin.messages().getBoolean("Interest-Broadcast.Enabled"))
                    messMan.noMoneyInterest(p);
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
        if (!plugin.config().getBoolean("General.Offline-Interest-Earned-Message.Enabled")) return;
        EconomyManager economy = new EconomyManager(plugin);
        final long offlineInterest = economy.getOfflineInterest(p);
        economy.setOfflineInterest(p, offlineInterest + amount);
    }
}