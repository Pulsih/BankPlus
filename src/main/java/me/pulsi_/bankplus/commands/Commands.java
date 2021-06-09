package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.guis.GuiBank;
import me.pulsi_.bankplus.managers.EconomyManager;
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
                            .replace("%money%", String.valueOf(economyManager.getPersonalBalance(p)))
                            .replace("%money_formatted%", String.valueOf(MethodUtils.formatter(economyManager.getPersonalBalance(p))))));
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

                default:
                    s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Unknown-Command")));
            }
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("withdraw")) {
            if (s.hasPermission("bankplus.withdraw")) {
                if (s instanceof Player) {
                    Player p = (Player) s;
                    try {
                        int withdraw = Integer.parseInt(args[1]);
                        economyManager.withdraw(p, withdraw);
                        try {
                            plugin.savePlayers();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (NumberFormatException ex) {
                        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Invalid-Number")));
                    }
                } else {
                    s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Not-Player")));
                }
            } else {
                s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
            }
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("deposit")) {
            if (s.hasPermission("bankplus.deposit")) {
                if (s instanceof Player) {
                    Player p = (Player) s;
                    try {
                        int deposit = Integer.parseInt(args[1]);
                        economyManager.deposit(p, deposit);
                        try {
                            plugin.savePlayers();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (NumberFormatException ex) {
                        p.sendMessage(ChatUtils.c(plugin.getMessages().getString("Invalid-Number")));
                    }
                } else {
                    s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Not-Player")));
                }
            } else {
                s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
            }
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
            if (s.hasPermission("bankplus.view")) {
                try {
                    if (s instanceof Player) {
                        Player p = (Player) s;
                        Player target = Bukkit.getPlayerExact(args[1]);
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Bank-Others")
                                .replace("%player%", target.getName())
                                .replace("%money%", String.valueOf(economyManager.getOthersBalance(target)))
                                .replace("%money_formatted%", String.valueOf(MethodUtils.formatter(economyManager.getOthersBalance(target))))));
                        String soundPath = plugin.getConfiguration().getString("General.View-Sound.Sound");
                        boolean soundBoolean = plugin.getConfiguration().getBoolean("General.View-Sound.Enabled");
                        MethodUtils.playSound(soundPath, p, plugin, soundBoolean);
                    } else {
                        Player target = Bukkit.getPlayerExact(args[1]);
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Bank-Others")
                                .replace("%player%", target.getName())
                                .replace("%money%", String.valueOf(economyManager.getOthersBalance(target)))
                                .replace("%money_formatted%", String.valueOf(MethodUtils.formatter(economyManager.getOthersBalance(target))))));
                    }
                } catch (Error err) {
                    s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Cannot-Find-Player")));
                }
            } else {
                s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
                if (s.hasPermission("bankplus.set")) {
                    s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Specify-Number")));
                } else {
                    s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
                }
            }

            if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
                if (s.hasPermission("bankplus.set")) {
                    try {
                        Player target = Bukkit.getPlayerExact(args[1]);
                        int amount = Integer.parseInt(args[2]);
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Set-Message")
                                .replace("%amount%", String.valueOf(amount))
                                .replace("%amount_formatted%", String.valueOf(MethodUtils.formatter(amount)))));
                        economyManager.setPlayerBankBalance(target, amount);
                    } catch (NumberFormatException ex) {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Invalid-Number")));
                    } catch (Error err) {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Cannot-Find-Player")));
                    }
                } else {
                    s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
                }
            }

            if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
                if (s.hasPermission("bankplus.add")) {
                    try {
                        Player target = Bukkit.getPlayerExact(args[1]);
                        int amount = Integer.parseInt(args[2]);
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Add-Message"))
                                .replace("%amount%", String.valueOf(amount))
                                .replace("%amount_formatted%", String.valueOf(MethodUtils.formatter(amount))));
                        economyManager.addPlayerBankBalance(target, amount);
                    } catch (NumberFormatException ex) {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Invalid-Number")));
                    } catch (Error err) {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Cannot-Find-Player")));
                    }
                } else {
                    s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
                }
            }

            if (args.length == 3 && args[0].equalsIgnoreCase("remove")) {
                if (s.hasPermission("bankplus.remove")) {
                    try {
                        Player target = Bukkit.getPlayerExact(args[1]);
                        int amount = Integer.parseInt(args[2]);
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Remove-Message")
                                .replace("%amount%", String.valueOf(amount))
                                .replace("%amount_formatted%", String.valueOf(MethodUtils.formatter(amount)))));
                        economyManager.removePlayerBankBalance(target, amount);
                    } catch (NumberFormatException ex) {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Invalid-Number")));
                    } catch (Error err) {
                        s.sendMessage(ChatUtils.c(plugin.getMessages().getString("Cannot-Find-Player")));
                    }
                } else {
                    s.sendMessage(ChatUtils.c(plugin.getMessages().getString("No-Permission")));
                }
            }
        }
        return true;
    }
}