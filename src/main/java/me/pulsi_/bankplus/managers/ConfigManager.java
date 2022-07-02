package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.IllegalPluginAccessException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConfigManager {

    private int commentsCount = 0;
    private int spacesCount = 0;
    private final String commentIdentifier = "bankplus_comment";
    private final String spaceIdentifier = "bankplus_space";

    private final BankPlus plugin;
    private File configFile, messagesFile, playersFile, bankFile;
    private FileConfiguration config, messages, players, bank;
    private String guiSettings = null;

    public static boolean guiHasMovedFile = false;

    public ConfigManager(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void createConfigs() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        playersFile = new File(plugin.getDataFolder(), "players.yml");
        bankFile = new File(plugin.getDataFolder(), "bank.yml");

        if (!configFile.exists()) plugin.saveResource("config.yml", false);
        if (!messagesFile.exists()) plugin.saveResource("messages.yml", false);
        if (!playersFile.exists()) plugin.saveResource("players.yml", false);
        if (!bankFile.exists()) plugin.saveResource("bank.yml", false);

        config = new YamlConfiguration();
        messages = new YamlConfiguration();
        players = new YamlConfiguration();
        bank = new YamlConfiguration();

        reloadConfig("config");
        reloadConfig("messages");
        reloadConfig("players");
        reloadConfig("bank");

        buildConfig();
        buildMessages();
        recreateFile(bankFile, guiSettings);
    }

    public FileConfiguration getConfig(String type) {
        switch (type) {
            case "config":
                return config;
            case "messages":
                return messages;
            case "players":
                return players;
            case "bank":
                return bank;
            default:
                return null;
        }
    }

    public void reloadConfig(String type) {
        switch (type) {
            case "config":
                try {
                    config.load(configFile);
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error(e.getMessage());
                }
                break;

            case "messages":
                try {
                    messages.load(messagesFile);
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error(e.getMessage());
                }
                break;

            case "players":
                try {
                    players.load(playersFile);
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error(e.getMessage());
                }
                break;

            case "bank":
                try {
                    bank.load(bankFile);
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error(e.getMessage());
                }
                break;
        }
    }

    public void saveConfig(String type) {
        switch (type) {
            case "config":
                try {
                    config.save(configFile);
                } catch (IOException e) {
                    BPLogger.warn(e.getMessage());
                }
                break;

            case "messages":
                try {
                    messages.save(messagesFile);
                } catch (IOException e) {
                    BPLogger.warn(e.getMessage());
                }
                break;

            case "players":
                try {
                    players.save(playersFile);
                } catch (IOException e) {
                    BPLogger.warn(e.getMessage());
                }
                break;

            case "bank":
                try {
                    bank.save(bankFile);
                } catch (IOException e) {
                    BPLogger.warn(e.getMessage());
                }
                break;
        }
    }

    public void savePlayers() {
        try {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveConfig("players"));
        } catch (Exception e) {
            saveConfig("players");
        }
    }

    private String getFileAsString(File file) {
        List<String> lines = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) lines.add(scanner.nextLine());
        } catch (FileNotFoundException e) {
            BPLogger.error(e.getMessage());
            return null;
        }

        StringBuilder config = new StringBuilder();
        for (String line : lines) {
            if (line.contains(commentIdentifier)) {
                int from = line.indexOf(commentIdentifier);
                int to = from + commentIdentifier.length();

                int pointsPosition = line.indexOf(":");
                int numbersLength = pointsPosition - to;
                String identifier = line.substring(from, to + numbersLength);

                line = line.replace(identifier, "").replaceFirst(":", "#");

                String comment = line.split("# ")[1];
                if (comment.equals("''")) {
                    line = line.split("# ")[0] + "#";
                } else if (comment.startsWith("'") && comment.endsWith("'")) {
                    int firstAccent = comment.indexOf("'");
                    int lastAccent = comment.lastIndexOf("'");

                    String newComment = comment.substring(firstAccent + 1, lastAccent);
                    line = line.split("# ")[0] + "# " + newComment;
                }
            }
            if (line.contains(spaceIdentifier)) line = "";

            config.append(line).append("\n");
        }

        return config.toString();
    }

    private void loadGuiForBankFileFromConfigForOlderVersions() {
        List<String> lines = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(configFile);
            while (scanner.hasNextLine()) lines.add(scanner.nextLine());
        } catch (FileNotFoundException e) {
            BPLogger.error(e.getMessage());
            return;
        }

        boolean hasReachedGuiPart = false;
        StringBuilder config = new StringBuilder();
        for (String line : lines) {
            if (!hasReachedGuiPart) {
                if (line.contains("Gui:")) hasReachedGuiPart = true;
                else continue;
            }
            if (line.startsWith("Gui:")) continue;
            if (line.length() >= 2) line = line.substring(2);
            config.append(line).append("\n");
        }
        if (!hasReachedGuiPart) return;

        guiHasMovedFile = true;
        guiSettings = config.toString();
    }

    public void recreateFile(File file) {
        String configuration = getFileAsString(file);
        if (configuration == null) return;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(configuration);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            BPLogger.error(e.getMessage());
        }
    }

    public void recreateFile(File file, String configuration) {
        if (configuration == null) return;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(configuration);
            writer.flush();
            writer.close();
            reloadConfig("bank");
        } catch (IOException e) {
            BPLogger.error(e.getMessage());
        }
    }

    public void buildMessages() {
        File newMessagesFile = new File(plugin.getDataFolder(), "messages.yml");
        FileConfiguration newMessages = new YamlConfiguration();

        addComments(newMessages,
                "Messages File of BankPlus",
                "Made by Pulsi_, Version v" + plugin.getDescription().getVersion());
        addSpace(newMessages);

        addComments(newMessages,
                "Local Placeholders",
                "These placeholders will work only in some of these messages, do",
                "not use it for gui or any other things because they won't work!",
                "",
                "%amount% -> Number Formatted with commas",
                "%amount_long% -> Raw Number",
                "%amount_formatted% -> Number Formatted",
                "%amount_formatted_long% -> Number formatted without \".\"",
                "%player_name% -> Player name");
        addSpace(newMessages);

        addComments(newMessages,
                "Enable or disable to send to a player an",
                "alert message when a message is missing.");
        validatePath(messages, newMessages, "Alert-Missing-Message", true);
        addSpace(newMessages);

        addComment(newMessages, "The main plugin prefix.");
        validatePath(messages, newMessages, "Prefix", "&a&lBank&9&lPlus");
        addSpace(newMessages);

        addComment(newMessages, "System");
        validatePath(messages, newMessages, "Reload", "%prefix% &aPlugin reloaded!");
        validatePath(messages, newMessages, "Unknown-Command", "%prefix% &cUnknown Command!");
        validatePath(messages, newMessages, "Interest-Restarted", "%prefix% &aInterest Restarted!");
        validatePath(messages, newMessages, "Interest-Disabled", "%prefix% &cThe interest is disabled!");
        validatePath(messages, newMessages, "BankTop-Disabled", "%prefix% &cThe banktop is disabled!");
        addSpace(newMessages);

        addComment(newMessages, "Errors");
        validatePath(messages, newMessages, "Specify-Number", "%prefix% &cPlease specify a number!");
        validatePath(messages, newMessages, "Specify-Player", "%prefix% &cPlease specify a player!");
        validatePath(messages, newMessages, "Not-Player", "%prefix% &cYou are not a player!");
        validatePath(messages, newMessages, "Invalid-Player", "%prefix% &cPlease choose a valid player!");
        validatePath(messages, newMessages, "Invalid-Number", "%prefix% &cPlease choose a valid number!");
        validatePath(messages, newMessages, "Insufficient-Money", "%prefix% &cYou don't have sufficient money!");
        validatePath(messages, newMessages, "No-Permission", "%prefix% &cYou don't have the permission!");
        validatePath(messages, newMessages, "Cannot-Deposit-Anymore", "%prefix% &cYou can't deposit anymore money!");
        validatePath(messages, newMessages, "Cannot-Use-Bank-Here", "%prefix% &cSorry, the bank is disabled in this world!");
        validatePath(messages, newMessages, "Cannot-Use-Negative-Number", "%prefix% &cYou can't use a negative number!");
        validatePath(messages, newMessages, "Bank-Full", "%prefix% &cThe bank of %player% is full!");
        validatePath(messages, newMessages, "Minimum-Number", "%prefix% &cPlease use an higher number for this action! ( min: %minimum% )");
        addSpace(newMessages);

        addComment(newMessages, "Plugin");
        validatePath(messages, newMessages, "Personal-Bank", "%prefix% &aYou have &f%amount_formatted% &amoney in your bank.");
        validatePath(messages, newMessages, "Success-Withdraw", "%prefix% &aSuccessfully withdrew &f%amount_formatted% &amoney!");
        validatePath(messages, newMessages, "Success-Deposit", "%prefix% &aSuccessfully deposited &f%amount_formatted% &amoney!");
        validatePath(messages, newMessages, "Bank-Others", "%prefix% &f%player_name% &ahas &f%amount_formatted% Money &ain their bank!");
        validatePath(messages, newMessages, "Set-Message", "%prefix% &aYou have set &f%player_name%'s &abank balance to &f%amount_formatted%&a!");
        validatePath(messages, newMessages, "Add-Message", "%prefix% &aYou have added &f%amount_formatted% Money &ato &f%player_name%'s &abank balance!");
        validatePath(messages, newMessages, "Remove-Message", "%prefix% &aYou have removed &f%amount_formatted% &amoney to &f%player_name%'s &abank balance!");
        validatePath(messages, newMessages, "Pay-Message", "%prefix% &aYou have added &f%amount_formatted% Money &ato &f%player_name%'s &abank balance!");
        validatePath(messages, newMessages, "Chat-Deposit", "%prefix% &aType an amount in chat to deposit, type 'exit' to exit");
        validatePath(messages, newMessages, "Chat-Withdraw", "%prefix% &aType an amount in chat to withdraw, type 'exit' to exit");
        validatePath(messages, newMessages, "Payment-Sent", "%prefix% &aYou have successfully sent &f%player% %amount_formatted% &amoney!");
        validatePath(messages, newMessages, "Payment-Received", "%prefix% &aYou have received &f%amount_formatted% &amoney from &f%player%!");
        validatePath(messages, newMessages, "Interest-Time", "%prefix% &aWait more &f%time% &ato get the interest.");
        addSpace(newMessages);

        addComment(newMessages, "Titles");
        validatePath(messages, newMessages, "Title-Custom-Amount.Enabled", true);
        validatePath(messages, newMessages, "Title-Custom-Amount.Title-Deposit", "%prefix% &fType in &achat &fan, amount to &adeposit");
        validatePath(messages, newMessages, "Title-Custom-Amount.Title-Withdraw", "%prefix% &fType in &achat &fan, amount to &awithdraw");
        addSpace(newMessages);

        addComment(newMessages, "Interest Messages");
        validatePath(messages, newMessages, "Interest-Broadcast.Enabled", true);
        validatePath(messages, newMessages, "Interest-Broadcast.Message", "%prefix% &aYou have earned &f%amount_formatted% Money &ain interest!");
        validatePath(messages, newMessages, "Interest-Broadcast.No-Money", "%prefix% &aSadly, you received 0 money from interest.");
        validatePath(messages, newMessages, "Interest-Broadcast.Bank-Full", "%prefix% &cYou can't earn anymore money from interest because your bank is full!");
        addSpace(newMessages);

        addComment(newMessages, "Help Message");
        List<String> helpMessages = new ArrayList<>();
        helpMessages.add("%prefix% &aHelp page");
        helpMessages.add("&a/bank deposit <amount> &7Deposit an amount of Money.");
        helpMessages.add("&a/bank withdraw <amount> &7Withdraw an amount of Money.");
        helpMessages.add("&a/bank view <player> &7View the balance of a player.");
        helpMessages.add("&7Plugin made by Pulsi_");
        helpMessages.add("&aRate 5 Star!");
        validatePath(messages, newMessages, "Help-Message", helpMessages);

        try {
            newMessages.save(newMessagesFile);
        } catch (IOException e) {
            BPLogger.error(e.getMessage());
        }
        recreateFile(newMessagesFile);
        reloadConfig("messages");
    }

    public void buildConfig() {
        loadGuiForBankFileFromConfigForOlderVersions();

        File newConfigFile = new File(plugin.getDataFolder(), "config.yml");
        FileConfiguration newConfig = new YamlConfiguration();

        addComments(newConfig,
                "Configuration File of BankPlus",
                "Made by Pulsi_, Version v" + plugin.getDescription().getVersion());
        addSpace(newConfig);

        addComment(newConfig, "Check for new updates of the plugin.");
        validatePath(config, newConfig, "Update-Checker", true);
        addSpace(newConfig);

        addComments(newConfig,
                "Interest will increase players bank balance",
                "by giving a % of their bank money.",
                "",
                "To restart the interest type /bank restartInterest.");
        addCommentUnder(newConfig, "Interest", "Enable or disable the interest feature.");
        validatePath(config, newConfig, "Interest.Enabled", true);
        addSpace(newConfig, "Interest");

        addCommentUnder(newConfig, "Interest.AFK-Settings", "If a player is AFK, it won't receive the interest.");
        validatePath(config, newConfig, "Interest.AFK-Settings.Ignore-AFK-Players", false);
        addSpace(newConfig, "Interest.AFK-Settings");

        addCommentsUnder(newConfig, "Interest.AFK-Settings",
                "Choose if using the EssentialsX AFK.",
                "(You will need to install EssentialsX)");
        validatePath(config, newConfig, "Interest.AFK-Settings.Use-EssentialsX-AFK", false);
        addSpace(newConfig, "Interest.AFK-Settings");

        addCommentsUnder(newConfig, "Interest.AFK-Settings",
                "The time, in minutes, that will pass",
                "before marking a player as AFK");
        validatePath(config, newConfig, "Interest.AFK-Settings.AFK-Time", 5);
        addSpace(newConfig, "Interest.AFK-Settings");

        addCommentUnder(newConfig, "Interest", "( it will add: balance x Money-Given )");
        validatePath(config, newConfig, "Interest.Money-Given", 0.05);
        addSpace(newConfig, "Interest");

        addCommentsUnder(newConfig, "Interest",
                "This is the interest cooldown.",
                "You can choose the delay between:",
                "  seconds (time s), minutes (time m)",
                "  hours (time h), days (time d)",
                "If no time will be specified, it",
                "will automatically choose minutes.");
        validatePath(config, newConfig, "Interest.Delay", "5 m");
        addSpace(newConfig, "Interest");

        addCommentUnder(newConfig, "Interest", "The max amount that you can receive with interest.");
        validatePath(config, newConfig, "Interest.Max-Amount", 500000);
        addSpace(newConfig, "Interest");

        addCommentUnder(newConfig, "Interest", "Choose if also giving interest to offline players.");
        validatePath(config, newConfig, "Interest.Give-To-Offline-Players", false);
        addSpace(newConfig, "Interest");

        addCommentUnder(newConfig, "Interest", "The permission for offline players to receive interest.");
        validatePath(config, newConfig, "Interest.Offline-Permission", "bankplus.receive.interest");
        addSpace(newConfig);

        addCommentsUnder(newConfig, "General",
                "The amount that a player will receive",
                "when joining for the first time");
        validatePath(config, newConfig, "General.Join-Start-Amount", 500);
        addSpace(newConfig, "General");

        addCommentsUnder(newConfig, "General",
                "Store player's money using UUIDs,",
                "otherwise the plugin will use names.");
        validatePath(config, newConfig, "General.Use-UUIDs", true);
        addSpace(newConfig, "General");

        addCommentsUnder(newConfig, "General",
                "In minutes, the delay to save all players balances. It is used",
                "to prevent players from losing their money if the server crashes.",
                "Put 0 to disable this option.");
        validatePath(config, newConfig, "General.Save-Delay", 10);
        addSpace(newConfig, "General");

        addCommentsUnder(newConfig, "General",
                "Choose if sending a message to the console",
                "when the plugin save all balances. (Only console)");
        validatePath(config, newConfig, "General.Save-Broadcast", true);
        addSpace(newConfig, "General");

        addCommentUnder(newConfig, "General", "The max amount that a player can deposit, use 0 to disable.");
        validatePath(config, newConfig, "General.Max-Bank-Capacity", 500000000);
        addSpace(newConfig, "General");

        addCommentUnder(newConfig, "General", "The max amount of decimals that a player balance can have.");
        validatePath(config, newConfig, "General.Max-Decimals-Amount", 2);
        addSpace(newConfig, "General");

        addCommentUnder(newConfig, "General", "The max amount to withdraw per time, use 0 to disable.");
        validatePath(config, newConfig, "General.Max-Withdrawn-Amount", 0);
        addSpace(newConfig, "General");

        addCommentUnder(newConfig, "General", "The max amount to deposit per time, use 0 to disable.");
        validatePath(config, newConfig, "General.Max-Deposit-Amount", 0);
        addSpace(newConfig, "General");

        addCommentsUnder(newConfig, "General",
                "The minimum amount that a player needs to",
                "put to withdraw / deposit, put 0 to disable.");
        validatePath(config, newConfig, "General.Minimum-Amount", 0);
        addSpace(newConfig, "General");

        addCommentsUnder(newConfig, "General",
                "Enabling this option, it will reopen the bank after",
                "typing in chat when depositing / withdraw money.");
        validatePath(config, newConfig, "General.Reopen-Bank-After-Chat", true);
        addSpace(newConfig, "General");

        addCommentsUnder(newConfig, "General",
                "The message that a player has to type",
                "to stop typing the custom amount.");
        validatePath(config, newConfig, "General.Chat-Exit-Message", "exit");
        addSpace(newConfig, "General");

        addCommentsUnder(newConfig, "General",
                "These commands will be executed when leaving from typing",
                "in chat while using the custom withdraw / deposit." +
                        "",
                "You can put as many commands as you want.");
        validatePath(config, newConfig, "General.Chat-Exit-Commands", "[]");
        addCommentsUnder(newConfig, "General",
                "- \"[CONSOLE] tell %player% You closed the bank!\"",
                "- \"[PLAYER] say I closed the bank!\"");
        addSpace(newConfig, "General");

        addCommentUnder(newConfig, "General", "Worlds where the bank won't work");
        validatePath(config, newConfig, "General.Worlds-Blacklist", new ArrayList<>().add("noBankWorld"));
        addSpace(newConfig, "General");

        addCommentsUnder(newConfig, "General",
                "Send an alert message to show the player how",
                "much money has earned while being offline.");
        validatePath(config, newConfig, "General.Offline-Interest-Earned-Message.Enabled", true);
        addCommentUnder(newConfig, "General.Offline-Interest-Earned-Message", "In seconds, put 0 to disable the delay.");
        validatePath(config, newConfig, "General.Offline-Interest-Earned-Message.Delay", 2);
        validatePath(config, newConfig, "General.Offline-Interest-Earned-Message.Message",
                "&a&lBank&9&lPlus &aYou have earned &f%amount% money &awhile being offline!");
        addSpace(newConfig, "General");

        validatePath(config, newConfig, "General.Withdraw-Sound.Enabled", true);
        addCommentUnder(newConfig, "General.Withdraw-Sound", "Sound-Type,Volume,Pitch.");
        validatePath(config, newConfig, "General.Withdraw-Sound.Sound", Methods.getSoundBasedOnServerVersion());
        addSpace(newConfig, "General");

        validatePath(config, newConfig, "General.Deposit-Sound.Enabled", true);
        validatePath(config, newConfig, "General.Deposit-Sound.Sound", Methods.getSoundBasedOnServerVersion());
        addSpace(newConfig, "General");

        validatePath(config, newConfig, "General.View-Sound.Enabled", true);
        validatePath(config, newConfig, "General.View-Sound.Sound", Methods.getSoundBasedOnServerVersion());
        addSpace(newConfig, "General");

        validatePath(config, newConfig, "General.Personal-Sound.Enabled", true);
        validatePath(config, newConfig, "General.Personal-Sound.Sound", Methods.getSoundBasedOnServerVersion());
        addSpace(newConfig);

        addCommentUnder(newConfig, "BankTop", "Enable or not the feature.");
        validatePath(config, newConfig, "BankTop.Enabled", true);
        addSpace(newConfig, "BankTop");

        addCommentUnder(newConfig, "BankTop", "The size of the banktop.");
        validatePath(config, newConfig, "BankTop.Size", 10);
        addSpace(newConfig, "BankTop");

        addCommentUnder(newConfig, "BankTop", "In ticks, the delay before the top will update.");
        validatePath(config, newConfig, "BankTop.Update-Delay", 12000);
        addSpace(newConfig, "BankTop");

        addCommentsUnder(newConfig, "BankTop.Update-Broadcast",
                "Choose if sending a message to the console",
                "when the plugin save all balances.");
        validatePath(config, newConfig, "BankTop.Update-Broadcast.Enabled", true);
        addSpace(newConfig, "BankTop.Update-Broadcast");

        addCommentUnder(newConfig, "BankTop.Update-Broadcast", "Choose if the alert will be sent only to the console.");
        validatePath(config, newConfig, "BankTop.Update-Broadcast.Only-Console", false);
        addSpace(newConfig, "BankTop.Update-Broadcast");

        addCommentUnder(newConfig, "BankTop.Update-Broadcast", "The message that will be sent when updating.");
        validatePath(config, newConfig, "BankTop.Update-Broadcast.Message", "%prefix% &aThe BankTop has been updated!");
        addSpace(newConfig, "BankTop");

        addCommentsUnder(newConfig, "BankTop",
                "The format that will be used to",
                "display the money in the banktop.",
                "You can choose between:",
                "  default_amount, amount_long,",
                "  amount_formatted, amount_formatted_long");
        validatePath(config, newConfig, "BankTop.Money-Format", "amount_formatted");
        addSpace(newConfig, "BankTop");

        addCommentUnder(newConfig, "BankTop", "The message to display the banktop.");
        List<String> banktopFormat = new ArrayList<>();
        banktopFormat.add("&8&m---------&8[&a &lBank&9&lPlus &aBankTop &8]&m---------");
        banktopFormat.add("&61# &6%bankplus_banktop_name_1%&8: &a%bankplus_banktop_money_1%");
        banktopFormat.add("&22# &2%bankplus_banktop_name_2%&8: &a%bankplus_banktop_money_2%");
        banktopFormat.add("&a3# &a%bankplus_banktop_name_3%&8: &a%bankplus_banktop_money_3%");
        banktopFormat.add("&74# &7%bankplus_banktop_name_4%&8: &a%bankplus_banktop_money_4%");
        banktopFormat.add("&75# &7%bankplus_banktop_name_5%&8: &a%bankplus_banktop_money_5%");
        banktopFormat.add("&76# &7%bankplus_banktop_name_6%&8: &a%bankplus_banktop_money_6%");
        banktopFormat.add("&77# &7%bankplus_banktop_name_7%&8: &a%bankplus_banktop_money_7%");
        banktopFormat.add("&78# &7%bankplus_banktop_name_8%&8: &a%bankplus_banktop_money_8%");
        banktopFormat.add("&79# &7%bankplus_banktop_name_9%&8: &a%bankplus_banktop_money_9%");
        banktopFormat.add("&710# &7%bankplus_banktop_name_10%&8: &a%bankplus_banktop_money_10%");
        banktopFormat.add("  &7&o(( The BankTop will update every 10m ))");
        validatePath(config, newConfig, "BankTop.Format", banktopFormat);
        addSpace(newConfig);

        validatePath(config, newConfig, "Placeholders.Money.Thousands", "K");
        validatePath(config, newConfig, "Placeholders.Money.Millions", "M");
        validatePath(config, newConfig, "Placeholders.Money.Billions", "B");
        validatePath(config, newConfig, "Placeholders.Money.Trillions", "T");
        validatePath(config, newConfig, "Placeholders.Money.Quadrillions", "Q");
        validatePath(config, newConfig, "Placeholders.Money.Quintillions", "QQ");
        addSpace(newConfig, "Placeholders");

        validatePath(config, newConfig, "Placeholders.Time.Second", "Second");
        validatePath(config, newConfig, "Placeholders.Time.Seconds", "Seconds");
        validatePath(config, newConfig, "Placeholders.Time.Minute", "Minute");
        validatePath(config, newConfig, "Placeholders.Time.Minutes", "Minutes");
        validatePath(config, newConfig, "Placeholders.Time.Hour", "Hour");
        validatePath(config, newConfig, "Placeholders.Time.Hours", "Hours");
        validatePath(config, newConfig, "Placeholders.Time.Day", "Days");
        validatePath(config, newConfig, "Placeholders.Time.Days", "Days");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Only-Seconds", "%seconds% %seconds_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Only-Minutes", "%minutes% %minutes_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Only-Hours", "%hours% %hours_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Only-Days", "%days% %days_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Seconds-Minutes", "%seconds% %seconds_placeholder% and %minutes% %minutes_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Seconds-Hours", "%seconds% %seconds_placeholder% and %hours% %hours_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Seconds-Days", "%seconds% %seconds_placeholder% and %days% %days_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Seconds-Minutes-Hours", "%seconds% %seconds_placeholder%, %minutes% %minutes_placeholder% and %hours% %hours_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Seconds-Hours-Days", "%seconds% %seconds_placeholder%, %hours% %hours_placeholder% and %days% %days_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Seconds-Minutes-Days", "%seconds% %seconds_placeholder%, %minutes% %minutes_placeholder% and %days% %days_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Seconds-Minutes-Hours-Days", "%seconds% %seconds_placeholder%, %minutes% %minutes_placeholder%, %hours% %hours_placeholder% and %days% %days_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Minutes-Hours", "%minutes% %minutes_placeholder% and %hours% %hours_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Minutes-Days", "%minutes% %minutes_placeholder% and %days% %days_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Minutes-Hours-Days", "%minutes% %minutes_placeholder%, %hours% %hours_placeholder% and %days% %days_placeholder%");
        validatePath(config, newConfig, "Placeholders.Time.Interest-Time.Hours-Days", "%hours% %hours_placeholder% and %days% %days_placeholder%");

        try {
            newConfig.save(newConfigFile);
        } catch (IOException e) {
            BPLogger.error(e.getMessage());
        }
        recreateFile(newConfigFile);
        reloadConfig("config");
    }

    private void addSpace(FileConfiguration config) {
        config.set(spaceIdentifier + spacesCount, "");
        spacesCount++;
    }

    private void addSpace(FileConfiguration config, String path) {
        config.set(path + "." + spaceIdentifier + spacesCount, "");
        spacesCount++;
    }

    private void addComment(FileConfiguration config, String comment) {
        config.set(commentIdentifier + commentsCount, comment);
        commentsCount++;
    }

    private void addComments(FileConfiguration config, String... comments) {
        for (String comment : comments) {
            config.set(commentIdentifier + commentsCount, comment);
            commentsCount++;
        }
    }

    private void addCommentUnder(FileConfiguration config, String path, String comment) {
        config.set(path + "." + commentIdentifier + commentsCount, comment);
        commentsCount++;
    }

    private void addCommentsUnder(FileConfiguration config, String path, String... comments) {
        for (String comment : comments) {
            config.set(path + "." + commentIdentifier + commentsCount, comment);
            commentsCount++;
        }
    }

    private void validatePath(FileConfiguration from, FileConfiguration to, String path, Object valuePath) {
        Object value = from.get(path);
        if (value == null) to.set(path, valuePath);
        else to.set(path, value);
    }
}