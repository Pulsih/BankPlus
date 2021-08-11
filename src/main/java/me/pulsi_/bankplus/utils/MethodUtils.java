package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Locale;

public class MethodUtils {

    public static String formatTime(int cooldown, BankPlus plugin) {
        String minute = plugin.getConfiguration().getString("Placeholders.Time.Minute");
        String minutes = plugin.getConfiguration().getString("Placeholders.Time.Minutes");
        String hour = plugin.getConfiguration().getString("Placeholders.Time.Hour");
        String hours = plugin.getConfiguration().getString("Placeholders.Time.Hours");
        String day = plugin.getConfiguration().getString("Placeholders.Time.Day");
        String days = plugin.getConfiguration().getString("Placeholders.Time.Days");
        if (cooldown < 60) {
            if (cooldown == 1) {
                return cooldown + minute;
            } else {
                return cooldown + minutes;
            }
        }
        if (cooldown >= 60 && cooldown < 1440) {
            if (cooldown == 60 && cooldown < 120) {
                return cooldown / 60 + hour;
            } else {
                return cooldown / 60 + hours;
            }
        }
        if (cooldown >= 1440) {
            if (cooldown == 1440 && cooldown < 2880) {
                return cooldown / 1440 + day;
            } else {
                return cooldown / 1440 + days;
            }
        }
        return null;
    }

    public static String formatLong(long balance, BankPlus plugin) {

        String k = plugin.getConfiguration().getString("Placeholders.Money.Thousands");
        String m = plugin.getConfiguration().getString("Placeholders.Money.Millions");
        String b = plugin.getConfiguration().getString("Placeholders.Money.Billions");
        String t = plugin.getConfiguration().getString("Placeholders.Money.Trillions");
        String q = plugin.getConfiguration().getString("Placeholders.Money.Quadrillions");

        if (balance < 1000L) {
            return "" + balance;
        }
        if (balance >= 1000L && balance < 1000000L) {
            return Math.round(balance / 1000L) + k;
        }
        if (balance >= 1000000L && balance < 1000000000L) {
            return Math.round(balance / 1000000L) + m;
        }
        if (balance >= 1000000000L && balance < 1000000000000L) {
            return Math.round(balance / 1000000000L) + b;
        }
        if (balance >= 1000000000000L && balance < 1000000000000000L) {
            return Math.round(balance / 1000000000000L) + t;
        }
        if (balance >= 1000000000000000L && balance < 1000000000000000000L) {
            return Math.round(balance / 1000000000000000L) + q;
        }
        return null;
    }

    public static String format(double balance, BankPlus plugin) {

        String k = plugin.getConfiguration().getString("Placeholders.Money.Thousands");
        String m = plugin.getConfiguration().getString("Placeholders.Money.Millions");
        String b = plugin.getConfiguration().getString("Placeholders.Money.Billions");
        String t = plugin.getConfiguration().getString("Placeholders.Money.Trillions");
        String q = plugin.getConfiguration().getString("Placeholders.Money.Quadrillions");

        if (balance < 1000L) {
            return formatString(balance);
        }
        if (balance >= 1000L && balance < 1000000L) {
            return formatString(Double.parseDouble(String.valueOf(balance)) / 1000L) + k;
        }
        if (balance >= 1000000L && balance < 1000000000L) {
            return formatString(Double.parseDouble(String.valueOf(balance)) / 1000000L) + m;
        }
        if (balance >= 1000000000L && balance < 1000000000000L) {
            return formatString(Double.parseDouble(String.valueOf(balance)) / 1000000000L) + b;
        }
        if (balance >= 1000000000000L && balance < 1000000000000000L) {
            return formatString(Double.parseDouble(String.valueOf(balance)) / 1000000000000L) + t;
        }
        if (balance >= 1000000000000000L && balance < 1000000000000000000L) {
            return formatString(Double.parseDouble(String.valueOf(balance)) / 1000000000000000L) + q;
        }
        return null;
    }

    private static String formatString(double balance) {
        NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(0);
        return format.format(balance);
    }

    public static void sendTitle(String path, Player p, BankPlus plugin) {
        try {
            String[] pathSlitted = plugin.getMessages().getString(path).split(",");
            String title1 = pathSlitted[0];
            String title2 = pathSlitted[1];
            p.sendTitle(ChatUtils.c(title1), ChatUtils.c(title2));
        } catch (NullPointerException | IllegalArgumentException e) {
            plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&a&lBank&9&lPlus &cInvalid Title at: &f" + path));
        }
    }

    public static void playSound(String sound, Player p, BankPlus plugin) {
        switch (sound) {
            case "WITHDRAW":
                if (plugin.getConfiguration().getBoolean("General.Withdraw-Sound.Enabled")) {
                    try {
                        String[] pathSlitted = plugin.getConfiguration().getString("General.Withdraw-Sound.Sound").split(",");
                        String soundType = pathSlitted[0];
                        int volume = Integer.parseInt(pathSlitted[1]);
                        int pitch = Integer.parseInt(pathSlitted[2]);
                        p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
                    } catch (NullPointerException | IllegalArgumentException exception) {
                        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&a&lBank&9&lPlus &cInvalid SoundType at: &fGeneral.Withdraw-Sound.Sound"));
                    }
                }
                break;

            case "DEPOSIT":
                if (plugin.getConfiguration().getBoolean("General.Deposit-Sound.Enabled")) {
                    try {
                        String[] pathSlitted = plugin.getConfiguration().getString("General.Deposit-Sound.Sound").split(",");
                        String soundType = pathSlitted[0];
                        int volume = Integer.parseInt(pathSlitted[1]);
                        int pitch = Integer.parseInt(pathSlitted[2]);
                        p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
                    } catch (NullPointerException | IllegalArgumentException exception) {
                        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&a&lBank&9&lPlus &cInvalid SoundType at: &fGeneral.Deposit-Sound.Sound"));
                    }
                }
                break;

            case "VIEW":
                if (plugin.getConfiguration().getBoolean("General.View-Sound.Enabled")) {
                    try {
                        String[] pathSlitted = plugin.getConfiguration().getString("General.View-Sound.Sound").split(",");
                        String soundType = pathSlitted[0];
                        int volume = Integer.parseInt(pathSlitted[1]);
                        int pitch = Integer.parseInt(pathSlitted[2]);
                        p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
                    } catch (NullPointerException | IllegalArgumentException exception) {
                        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&a&lBank&9&lPlus &cInvalid SoundType at: &fGeneral.View-Sound.Sound"));
                    }
                }
                break;

            case "PERSONAL":
                if (plugin.getConfiguration().getBoolean("General.Personal-Sound.Enabled")) {
                    try {
                        String[] pathSlitted = plugin.getConfiguration().getString("General.Personal-Sound.Sound").split(",");
                        String soundType = pathSlitted[0];
                        int volume = Integer.parseInt(pathSlitted[1]);
                        int pitch = Integer.parseInt(pathSlitted[2]);
                        p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
                    } catch (NullPointerException | IllegalArgumentException exception) {
                        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.c("&a&lBank&9&lPlus &cInvalid SoundType at: &fGeneral.Personal-Sound.Sound"));
                    }
                }
                break;
        }
    }

    public static int ticksInMinutes(int delay) {
        return delay * 1200;
    }

    public static void withdraw(Player p, long amount, BankPlus plugin) {
        EconomyManager economy = new EconomyManager(plugin);
        long bankBalance = economy.getBankBalance(p);
        long maxWithdrawAmount = plugin.getConfiguration().getLong("General.Max-Withdrawn-Amount");
        if (bankBalance <= 0) {
            MessageManager.insufficientMoneyWithdraw(p, plugin);
            return;
        }
        if (maxWithdrawAmount != 0) {
            if (amount >= maxWithdrawAmount) {
                amount = maxWithdrawAmount;
            }
        }
        if (bankBalance - amount <= 0) {
            economy.withdraw(p, bankBalance);
            MessageManager.successWithdraw(p, bankBalance, plugin);
            MethodUtils.playSound("WITHDRAW", p, plugin);
            return;
        }
        economy.withdraw(p, amount);
        MessageManager.successWithdraw(p, amount, plugin);
        MethodUtils.playSound("WITHDRAW", p, plugin);
    }

    public static void deposit(Player p, long amount, BankPlus plugin) {
        EconomyManager economy = new EconomyManager(plugin);
        long bankBalance = economy.getBankBalance(p);
        long money = (long) plugin.getEconomy().getBalance(p);
        long maxDepositAmount = plugin.getConfiguration().getLong("General.Max-Deposit-Amount");
        long maxBankCapacity = plugin.getConfiguration().getLong("General.Max-Bank-Capacity");
        if (money <= 0) {
            MessageManager.insufficientMoneyDeposit(p, plugin);
            return;
        }
        if (money < amount) {
            economy.deposit(p, money);
            MessageManager.successDeposit(p, money, plugin);
            MethodUtils.playSound("DEPOSIT", p, plugin);
            return;
        }
        if (maxBankCapacity != 0) {
            if (bankBalance >= maxBankCapacity) {
                MessageManager.cannotDepositMore(p, plugin);
                return;
            }
            if (bankBalance + amount >= maxBankCapacity) {
                economy.deposit(p, maxBankCapacity - bankBalance);
                MessageManager.successDeposit(p, maxBankCapacity - bankBalance, plugin);
            } else {
                if (maxDepositAmount != 0) {
                    if (amount >= maxDepositAmount) {
                        economy.deposit(p, maxDepositAmount);
                        MessageManager.successDeposit(p, maxDepositAmount, plugin);
                    } else {
                        economy.deposit(p, amount);
                        MessageManager.successDeposit(p, amount, plugin);
                    }
                } else {
                    economy.deposit(p, amount);
                    MessageManager.successDeposit(p, amount, plugin);
                }
            }
        } else {
            if (maxDepositAmount != 0) {
                if (amount >= maxDepositAmount) {
                    economy.deposit(p, maxDepositAmount);
                    MessageManager.successDeposit(p, maxDepositAmount, plugin);
                } else {
                    economy.deposit(p, amount);
                    MessageManager.successDeposit(p, amount, plugin);
                }
            } else {
                economy.deposit(p, amount);
                MessageManager.successDeposit(p, amount, plugin);
            }
        }
        MethodUtils.playSound("DEPOSIT", p, plugin);
    }
}