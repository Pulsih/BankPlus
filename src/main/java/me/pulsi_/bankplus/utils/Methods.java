package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.values.Values;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class Methods {

    public static String formatTime(int cooldown) {
        if (cooldown < 60) {
            if (cooldown == 1) {
                return cooldown + Values.CONFIG.getMinute();
            } else {
                return cooldown + Values.CONFIG.getMinutes();
            }
        }

        if (cooldown < 1440) {
            if (cooldown == 60) {
                return cooldown / 60 + Values.CONFIG.getHour();
            } else {
                return cooldown / 60 + Values.CONFIG.getHours();
            }
        }

        if (cooldown == 1440) {
            return cooldown / 1440 + Values.CONFIG.getDay();
        } else {
            return cooldown / 1440 + Values.CONFIG.getDays();
        }
    }

    public static String formatLong(long i) {
        float balance = (float) i;
        if (balance < 1000L) return "" + balance;
        if (balance < 1000000L) return Math.round(balance / 1000L) + Values.CONFIG.getK();
        if (balance < 1000000000L) return Math.round(balance / 1000000L) + Values.CONFIG.getM();
        if (balance < 1000000000000L) return Math.round(balance / 1000000000L) + Values.CONFIG.getB();
        if (balance < 1000000000000000L) return Math.round(balance / 1000000000000L) + Values.CONFIG.getT();
        if (balance < 1000000000000000000L) return Math.round(balance / 1000000000000000L) + Values.CONFIG.getQ();
        return "0";
    }

    public static String format(double balance) {
        if (balance < 1000L) return formatString(balance);
        if (balance >= 1000L && balance < 1000000L) return formatString(balance / 1000L) + Values.CONFIG.getK();
        if (balance >= 1000000L && balance < 1000000000L) return formatString(balance / 1000000L) + Values.CONFIG.getM();
        if (balance >= 1000000000L && balance < 1000000000000L) return formatString(balance / 1000000000L) + Values.CONFIG.getB();
        if (balance >= 1000000000000L && balance < 1000000000000000L) return formatString(balance / 1000000000000L) + Values.CONFIG.getT();
        if (balance >= 1000000000000000L && balance < 1000000000000000000L) return formatString(balance / 1000000000000000L) + Values.CONFIG.getQ();
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

    public static void customWithdraw(Player p) {
        SetUtils.playerWithdrawing.add(p);
        if (Values.CONFIG.isTitleCustomAmountEnabled())
            Methods.sendTitle("Title-Custom-Amount.Title-Withdraw", p);
        MessageManager.chatWithdraw(p);
        p.closeInventory();
    }

    public static void customDeposit(Player p) {
        SetUtils.playerDepositing.add(p);
        if (Values.CONFIG.isTitleCustomAmountEnabled())
            Methods.sendTitle("Title-Custom-Amount.Title-Deposit", p);
        MessageManager.chatDeposit(p);
        p.closeInventory();
    }

    public static void sendTitle(String path, Player p) {
        String title = BankPlus.getInstance().messages().getString(path);
        if (title == null) return;
        if (title.contains(",")) {
            String[] titles = title.split(",");
            String title1 = titles[0];
            String title2 = titles[1];
            p.sendTitle(ChatUtils.color(title1), ChatUtils.color(title2));
        } else {
            p.sendTitle(ChatUtils.color(title), "");
        }
    }

    public static void playSound(String sound, Player p) {
        switch (sound) {
            case "WITHDRAW": {
                if (!Values.CONFIG.isWithdrawSoundEnabled()) return;
                String withdrawSound = Values.CONFIG.getWithdrawSound();
                if (withdrawSound == null) {
                    BPLogger.warn("You are missing a string! &8(&ePath: General.Withdraw-Sound.Sound in config.yml&8)");
                    return;
                }
                String[] pathSlitted = withdrawSound.split(",");

                String soundType = pathSlitted[0];
                int volume = Integer.parseInt(pathSlitted[1]);
                int pitch = Integer.parseInt(pathSlitted[2]);
                try {
                    p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
                } catch (IllegalArgumentException e) {
                    BPLogger.warn("Invalid SoundType (NO ERROR): " + e.getMessage());
                    BPLogger.warn("Please change it in the config! &8(&aPath: General.Withdraw-Sound.Sound&8)");
                }
            }
            break;

            case "DEPOSIT": {
                if (!Values.CONFIG.isDepositSoundEnabled()) return;
                String depositSound = Values.CONFIG.getDepositSound();
                if (depositSound == null) {
                    BPLogger.warn("You are missing a string! &8(&ePath: General.Deposit-Sound.Sound in config.yml&8)");
                    return;
                }
                String[] pathSlitted = depositSound.split(",");

                String soundType = pathSlitted[0];
                int volume = Integer.parseInt(pathSlitted[1]);
                int pitch = Integer.parseInt(pathSlitted[2]);
                try {
                    p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
                } catch (IllegalArgumentException e) {
                    BPLogger.warn("Invalid SoundType (NO ERROR): " + e.getMessage());
                    BPLogger.warn("Please change it in the config! &8(&aPath: General.Deposit-Sound.Sound&8)");
                }
            }
            break;

            case "VIEW": {
                if (!Values.CONFIG.isViewSoundEnabled()) return;
                String viewSound = Values.CONFIG.getViewSound();
                if (viewSound == null) {
                    BPLogger.warn("You are missing a string! &8(&ePath: General.View-Sound.Sound in config.yml&8)");
                    return;
                }
                String[] pathSlitted = viewSound.split(",");

                String soundType = pathSlitted[0];
                int volume = Integer.parseInt(pathSlitted[1]);
                int pitch = Integer.parseInt(pathSlitted[2]);
                try {
                    p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
                } catch (IllegalArgumentException e) {
                    BPLogger.warn("Invalid SoundType (NO ERROR): " + e.getMessage());
                    BPLogger.warn("Please change it in the config! &8(&aPath: General.View-Sound.Sound&8)");
                }
            }
            break;

            case "PERSONAL": {
                if (!Values.CONFIG.isPersonalSoundEnabled()) return;
                String personalSound = Values.CONFIG.getViewSound();
                if (personalSound == null) {
                    BPLogger.warn("You are missing a string! &8(&ePath: General.Personal-Sound.Sound in config.yml&8)");
                    return;
                }
                String[] pathSlitted = personalSound.split(",");

                String soundType = pathSlitted[0];
                int volume = Integer.parseInt(pathSlitted[1]);
                int pitch = Integer.parseInt(pathSlitted[2]);
                try {
                    p.playSound(p.getLocation(), Sound.valueOf(soundType), volume, pitch);
                } catch (IllegalArgumentException e) {
                    BPLogger.warn("Invalid SoundType (NO ERROR): " + e.getMessage());
                    BPLogger.warn("Please change it in the config! &8(&aPath: General.Personal-Sound.Sound&8)");
                }
            }
            break;
        }
    }

    public static int ticksInMinutes(int delay) {
        return delay * 1200;
    }

    public static void withdraw(Player p, long amount) {
        long bankBalance = EconomyManager.getInstance().getBankBalance(p);
        long maxWithdrawAmount = Values.CONFIG.getMaxWithdrawAmount();

        if (amount < 0) {
            MessageManager.cannotUseNegativeNumber(p);
            return;
        }

        if (amount < Values.CONFIG.getMinimumAmount()) {
            MessageManager.minimumAmountAlert(p);
            return;
        }

        if (bankBalance <= 0) {
            MessageManager.insufficientMoneyWithdraw(p);
            return;
        }

        if (maxWithdrawAmount != 0 && amount >= maxWithdrawAmount) amount = maxWithdrawAmount;

        if (bankBalance - amount <= 0) {
            EconomyManager.getInstance().withdraw(p, bankBalance);
            MessageManager.successWithdraw(p, bankBalance);
            Methods.playSound("WITHDRAW", p);
            return;
        }

        EconomyManager.getInstance().withdraw(p, amount);
        MessageManager.successWithdraw(p, amount);
        Methods.playSound("WITHDRAW", p);
    }

    public static void deposit(Player p, long amount) {
        long bankBalance = EconomyManager.getInstance().getBankBalance(p);
        long money = (long) BankPlus.getEconomy().getBalance(p);
        long maxDepositAmount = Values.CONFIG.getMaxDepositAmount();
        long maxBankCapacity = Values.CONFIG.getMaxBankCapacity();

        if (amount < 0) {
            MessageManager.cannotUseNegativeNumber(p);
            return;
        }

        if (amount < Values.CONFIG.getMinimumAmount()) {
            MessageManager.minimumAmountAlert(p);
            return;
        }

        if (money <= 0) {
            MessageManager.insufficientMoneyDeposit(p);
            return;
        }

        if (money < amount) {
            EconomyManager.getInstance().deposit(p, money);
            MessageManager.successDeposit(p, money);
            Methods.playSound("DEPOSIT", p);
            return;
        }

        if (maxBankCapacity != 0) {
            if (bankBalance >= maxBankCapacity) {
                MessageManager.cannotDepositMore(p);
                return;
            }
            if (bankBalance + amount >= maxBankCapacity) {
                EconomyManager.getInstance().deposit(p, maxBankCapacity - bankBalance);
                MessageManager.successDeposit(p, maxBankCapacity - bankBalance);
            } else {
                if (maxDepositAmount != 0) {
                    if (amount >= maxDepositAmount) {
                        EconomyManager.getInstance().deposit(p, maxDepositAmount);
                        MessageManager.successDeposit(p, maxDepositAmount);
                    } else {
                        EconomyManager.getInstance().deposit(p, amount);
                        MessageManager.successDeposit(p, amount);
                    }
                } else {
                    EconomyManager.getInstance().deposit(p, amount);
                    MessageManager.successDeposit(p, amount);
                }
            }
        } else {
            if (maxDepositAmount != 0) {
                if (amount >= maxDepositAmount) {
                    EconomyManager.getInstance().deposit(p, maxDepositAmount);
                    MessageManager.successDeposit(p, maxDepositAmount);
                } else {
                    EconomyManager.getInstance().deposit(p, amount);
                    MessageManager.successDeposit(p, amount);
                }
            } else {
                EconomyManager.getInstance().deposit(p, amount);
                MessageManager.successDeposit(p, amount);
            }
        }
        Methods.playSound("DEPOSIT", p);
    }
}