package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.ConfigValues;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class MethodUtils {

    public static String formatTime(int cooldown) {
        if (cooldown < 60) {
            if (cooldown == 1) {
                return cooldown + ConfigValues.getMinute();
            } else {
                return cooldown + ConfigValues.getMinutes();
            }
        }
        if (cooldown < 1440) {
            if (cooldown == 60) {
                return cooldown / 60 + ConfigValues.getHour();
            } else {
                return cooldown / 60 + ConfigValues.getHours();
            }
        }
        if (cooldown == 1440) {
            return cooldown / 1440 + ConfigValues.getDay();
        } else {
            return cooldown / 1440 + ConfigValues.getDays();
        }
    }

    public static String formatLong(long balance) {
        if (balance < 1000L) return "" + balance;
        if (balance < 1000000L) return Math.round(balance / 1000L) + ConfigValues.getK();
        if (balance < 1000000000L) return Math.round(balance / 1000000L) + ConfigValues.getM();
        if (balance < 1000000000000L) return Math.round(balance / 1000000000L) + ConfigValues.getB();
        if (balance < 1000000000000000L) return Math.round(balance / 1000000000000L) + ConfigValues.getT();
        if (balance < 1000000000000000000L) return Math.round(balance / 1000000000000000L) + ConfigValues.getQ();
        return "0";
    }

    public static String format(double balance) {
        if (balance < 1000L) return formatString(balance);
        if (balance >= 1000L && balance < 1000000L) return formatString(balance / 1000L) + ConfigValues.getK();
        if (balance >= 1000000L && balance < 1000000000L) return formatString(balance / 1000000L) + ConfigValues.getM();
        if (balance >= 1000000000L && balance < 1000000000000L) return formatString(balance / 1000000000L) + ConfigValues.getB();
        if (balance >= 1000000000000L && balance < 1000000000000000L) return formatString(balance / 1000000000000L) + ConfigValues.getT();
        if (balance >= 1000000000000000L && balance < 1000000000000000000L) return formatString(balance / 1000000000000000L) + ConfigValues.getQ();
        return "0";
    }

    private static String formatString(double balance) {
        NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(0);
        return format.format(balance);
    }

    public static String formatCommas(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }

    public static void sendTitle(String path, Player p, BankPlus plugin) {
        try {
            String[] pathSlitted = plugin.messages().getString(path).split(",");
            String title1 = pathSlitted[0];
            String title2 = pathSlitted[1];
            p.sendTitle(ChatUtils.color(title1), ChatUtils.color(title2));
        } catch (NullPointerException | IllegalArgumentException e) {
            plugin.getServer().getConsoleSender().sendMessage(ChatUtils.color("&a&lBank&9&lPlus &cInvalid Title at: &f" + path));
        }
    }

    public static void playSound(String sound, Player p, BankPlus plugin) {
        switch (sound) {
            case "WITHDRAW":
                if (ConfigValues.isWithdrawSoundEnabled()) {
                    try {
                        String[] pathSlitted = ConfigValues.getWithdrawSound().split(",");
                        String soundType = pathSlitted[0];
                        int volume = Integer.parseInt(pathSlitted[1]);
                        int pitch = Integer.parseInt(pathSlitted[2]);
                        p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
                    } catch (NullPointerException | IllegalArgumentException exception) {
                        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.color("&a&lBank&9&lPlus &cInvalid SoundType at: &fGeneral.Withdraw-Sound.Sound"));
                    }
                }
                break;

            case "DEPOSIT":
                if (ConfigValues.isDepositSoundEnabled()) {
                    try {
                        String[] pathSlitted = ConfigValues.getDepositSound().split(",");
                        String soundType = pathSlitted[0];
                        int volume = Integer.parseInt(pathSlitted[1]);
                        int pitch = Integer.parseInt(pathSlitted[2]);
                        p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
                    } catch (NullPointerException | IllegalArgumentException exception) {
                        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.color("&a&lBank&9&lPlus &cInvalid SoundType at: &fGeneral.Deposit-Sound.Sound"));
                    }
                }
                break;

            case "VIEW":
                if (ConfigValues.isViewSoundEnabled()) {
                    try {
                        String[] pathSlitted = ConfigValues.getViewSound().split(",");
                        String soundType = pathSlitted[0];
                        int volume = Integer.parseInt(pathSlitted[1]);
                        int pitch = Integer.parseInt(pathSlitted[2]);
                        p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
                    } catch (NullPointerException | IllegalArgumentException exception) {
                        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.color("&a&lBank&9&lPlus &cInvalid SoundType at: &fGeneral.View-Sound.Sound"));
                    }
                }
                break;

            case "PERSONAL":
                if (ConfigValues.isPersonalSoundEnabled()) {
                    try {
                        String[] pathSlitted = ConfigValues.getPersonalSound().split(",");
                        String soundType = pathSlitted[0];
                        int volume = Integer.parseInt(pathSlitted[1]);
                        int pitch = Integer.parseInt(pathSlitted[2]);
                        p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
                    } catch (NullPointerException | IllegalArgumentException exception) {
                        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.color("&a&lBank&9&lPlus &cInvalid SoundType at: &fGeneral.Personal-Sound.Sound"));
                    }
                }
                break;
        }
    }

    public static int ticksInMinutes(int delay) {
        return delay * 1200;
    }

    public static void withdraw(Player p, long amount, BankPlus plugin) {
        final EconomyManager economy = new EconomyManager(plugin);
        final MessageManager messMan = new MessageManager(plugin);
        final long bankBalance = economy.getBankBalance(p);
        final long maxWithdrawAmount = ConfigValues.getMaxWithdrawAmount();
        if (bankBalance <= 0) {
            messMan.insufficientMoneyWithdraw(p);
            return;
        }
        if (maxWithdrawAmount != 0) {
            if (amount >= maxWithdrawAmount) {
                amount = maxWithdrawAmount;
            }
        }
        if (bankBalance - amount <= 0) {
            economy.withdraw(p, bankBalance);
            messMan.successWithdraw(p, bankBalance);
            MethodUtils.playSound("WITHDRAW", p, plugin);
            return;
        }
        economy.withdraw(p, amount);
        messMan.successWithdraw(p, amount);
        MethodUtils.playSound("WITHDRAW", p, plugin);
    }

    public static void deposit(Player p, long amount, BankPlus plugin) {
        final EconomyManager economy = new EconomyManager(plugin);
        final MessageManager messMan = new MessageManager(plugin);
        final long bankBalance = economy.getBankBalance(p);
        final long money = (long) plugin.getEconomy().getBalance(p);
        final long maxDepositAmount = ConfigValues.getMaxDepositAmount();
        final long maxBankCapacity = ConfigValues.getMaxBankCapacity();
        if (money <= 0) {
            messMan.insufficientMoneyDeposit(p);
            return;
        }
        if (money < amount) {
            economy.deposit(p, money);
            messMan.successDeposit(p, money);
            MethodUtils.playSound("DEPOSIT", p, plugin);
            return;
        }
        if (maxBankCapacity != 0) {
            if (bankBalance >= maxBankCapacity) {
                messMan.cannotDepositMore(p);
                return;
            }
            if (bankBalance + amount >= maxBankCapacity) {
                economy.deposit(p, maxBankCapacity - bankBalance);
                messMan.successDeposit(p, maxBankCapacity - bankBalance);
            } else {
                if (maxDepositAmount != 0) {
                    if (amount >= maxDepositAmount) {
                        economy.deposit(p, maxDepositAmount);
                        messMan.successDeposit(p, maxDepositAmount);
                    } else {
                        economy.deposit(p, amount);
                        messMan.successDeposit(p, amount);
                    }
                } else {
                    economy.deposit(p, amount);
                    messMan.successDeposit(p, amount);
                }
            }
        } else {
            if (maxDepositAmount != 0) {
                if (amount >= maxDepositAmount) {
                    economy.deposit(p, maxDepositAmount);
                    messMan.successDeposit(p, maxDepositAmount);
                } else {
                    economy.deposit(p, amount);
                    messMan.successDeposit(p, amount);
                }
            } else {
                economy.deposit(p, amount);
                messMan.successDeposit(p, amount);
            }
        }
        MethodUtils.playSound("DEPOSIT", p, plugin);
    }
}