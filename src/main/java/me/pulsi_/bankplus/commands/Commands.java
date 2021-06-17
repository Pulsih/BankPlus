package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.guis.GuiBank;
import me.pulsi_.bankplus.managers.EconomyManager;
import me.pulsi_.bankplus.managers.MessageManager;
import me.pulsi_.bankplus.utils.ChatUtils;
import me.pulsi_.bankplus.utils.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class Commands implements CommandExecutor {

    private EconomyManager economyManager;
    private BankPlus plugin;

    public Commands(BankPlus plugin) {
        this.plugin = plugin;
        this.economyManager = new EconomyManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {

        if (args.length == 0) {
            if (s instanceof Player) {
                Player p = (Player) s;
                if (plugin.getConfiguration().getBoolean("Gui.Enabled")) {
                    GuiBank gui = new GuiBank(plugin);
                    gui.openGui(p);
                } else {
                    p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Personal-Bank")
                        .replace("%amount%", String.valueOf(economyManager.getPersonalBalance(p)))
                        .replace("%amount_formatted%", MethodUtils.format(economyManager.getPersonalBalance(p), plugin))
                        .replace("%amount_formatted_long%", String.valueOf(MethodUtils.formatLong(economyManager.getPersonalBalance(p), plugin)))));
                }
                String soundPath = plugin.getConfiguration().getString("General.Personal-Sound.Sound");
                boolean soundBoolean = plugin.getConfiguration().getBoolean("General.Personal-Sound.Enabled");
                MethodUtils.playSound(soundPath, p, plugin, soundBoolean);
            } else {
                s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Not-Player")));
            }
        }

        if (args.length == 1) {
            switch (args[0]) {
                case "reload":
                    if (s.hasPermission("bankplus.reload")) {
                        plugin.reloadConfigs();
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Reload")));
                    } else {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
                    }
                    break;

                case "help":
                    if (s.hasPermission("bankplus.help")) {
                        for (String helpMessage : plugin.getMessages().getStringList("Help-Message")) {
                            s.sendMessage(ChatUtils.c(helpMessage));
                        }
                    } else {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
                    }
                    break;

                case "view":
                    if (s.hasPermission("bankplus.view")) {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Specify-Player")));
                    } else {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
                    }

                case "withdraw":
                    if (s.hasPermission("bankplus.withdraw")) {
                        if (s instanceof Player) {
                            Player p = (Player) s;
                            p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Specify-Number")));
                        } else {
                            s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Not-Player")));
                        }

                        try {
                            plugin.savePlayers();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
                    }
                    break;

                case "deposit":
                    if (s.hasPermission("bankplus.deposit")) {
                        if (s instanceof Player) {
                            Player p = (Player) s;
                            p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Specify-Number")));
                        } else {
                            s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Not-Player")));
                        }

                        try {
                            plugin.savePlayers();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
                    }
                    break;

                case "set":
                    if (s.hasPermission("bankplus.set")) {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Specify-Player")));
                    } else {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
                    }
                    break;

                case "add":
                    if (s.hasPermission("bankplus.add")) {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Specify-Player")));
                    } else {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
                    }
                    break;

                case "remove":
                    if (s.hasPermission("bankplus.remove")) {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Specify-Player")));
                    } else {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
                    }
                    break;

                case "interest":
                    if (s.hasPermission("bankplus.interest.restart")) {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Interest-Usage")));
                    } else {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
                    }
                    break;

                default:
                    s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Unknown-Command")));
                    break;
            }
        }

        if (args.length == 2) {
            switch (args[0]) {
                case "view":
                    if (s.hasPermission("bankplus.view")) {
                        try {
                            if (s instanceof Player) {
                                Player p = (Player) s;
                                MethodUtils.playSound(plugin.getConfiguration().getString("General.View-Sound.Sound"), p, plugin, plugin.getConfiguration().getBoolean("General.View-Sound.Enabled"));
                            }
                            Player target = Bukkit.getPlayerExact(args[1]);
                            MessageManager.bankOthers(s, plugin, target, economyManager);
                        } catch (Error err) {
                            MessageManager.cannotFindPlayer(s, plugin);
                        }
                    } else {
                        MessageManager.noPermission(s, plugin);
                    }
                    break;

                case "withdraw":
                    if (s.hasPermission("bankplus.withdraw")) {
                        if (s instanceof Player) {
                            Player p = (Player) s;
                            try {
                                long withdraw = Long.parseLong(args[1]);
                                economyManager.withdraw(p, withdraw);
                            } catch (NumberFormatException | IOException ex) {
                                MessageManager.invalidNumber(s, plugin);
                            }
                        } else {
                            MessageManager.notPlayer(s, plugin);
                        }
                    } else {
                        MessageManager.noPermission(s, plugin);
                    }
                    break;

                case "deposit":
                    if (s.hasPermission("bankplus.deposit")) {
                        if (s instanceof Player) {
                            Player p = (Player) s;
                            try {
                                long deposit = Long.parseLong(args[1]);
                                economyManager.deposit(p, deposit);
                            } catch (NumberFormatException | IOException ex) {
                                MessageManager.invalidNumber(s, plugin);
                            }
                        } else {
                            MessageManager.notPlayer(s, plugin);
                        }
                    } else {
                        MessageManager.noPermission(s, plugin);
                    }
                    break;

                case "set":
                    if (s.hasPermission("bankplus.set")) {
                        MessageManager.specifyNumber(s, plugin);
                    } else {
                        MessageManager.noPermission(s, plugin);
                    }
                    break;

                case "interest":
                    if (args[1].equalsIgnoreCase("restart")) {
                        if (s.hasPermission("bankplus.interest.restart")) {
                            if (plugin.getConfiguration().getBoolean("Interest.Enabled")) {
                                try {
                                    String interestCooldown = plugin.getConfiguration().getString("Interest.Delay");
                                    plugin.getPlayers().set("Interest-Cooldown", interestCooldown);
                                    plugin.savePlayers();
                                    MessageManager.interestRestarted(s, plugin);
                                } catch (NullPointerException | IOException ex) {
                                    MessageManager.internalError(s, plugin);
                                }
                            } else {
                                MessageManager.interestIsDisabled(s, plugin);
                            }
                        } else {
                            MessageManager.noPermission(s, plugin);
                        }
                    }
                    break;

                default:
                    MessageManager.unknownCommand(s, plugin);
                    break;
            }
        }

        if (args.length == 3) {
            switch (args[0]) {
                case "set":
                    if (s.hasPermission("bankplus.set")) {
                        try {
                            Player target = Bukkit.getPlayerExact(args[1]);
                            long amount = Long.parseLong(args[2]);
                            economyManager.setPlayerBankBalance(s, target, amount);
                        } catch (NumberFormatException ex) {
                            MessageManager.invalidNumber(s, plugin);
                        } catch (Error | IOException err) {
                            MessageManager.cannotFindPlayer(s, plugin);
                        }
                    } else {
                        MessageManager.noPermission(s, plugin);
                    }
                    break;

                case "add":
                    if (s.hasPermission("bankplus.add")) {
                        try {
                            Player target = Bukkit.getPlayerExact(args[1]);
                            long amount = Long.parseLong(args[2]);
                            economyManager.addPlayerBankBalance(s, target, amount);
                        } catch (NumberFormatException ex) {
                            MessageManager.invalidNumber(s, plugin);
                        } catch (Error err) {
                            MessageManager.cannotFindPlayer(s, plugin);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        MessageManager.noPermission(s, plugin);
                    }
                    break;

                case "remove":
                    if (s.hasPermission("bankplus.remove")) {
                        try {
                            Player target = Bukkit.getPlayerExact(args[1]);
                            long amount = Long.parseLong(args[2]);
                            economyManager.removePlayerBankBalance(s, target, amount);
                        } catch (NumberFormatException ex) {
                            MessageManager.invalidNumber(s, plugin);
                        } catch (Error err) {
                            MessageManager.cannotFindPlayer(s, plugin);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        MessageManager.noPermission(s, plugin);
                    }
                    break;

                default:
                    MessageManager.unknownCommand(s, plugin);
                    break;
            }
        }
        return true;
    }
}