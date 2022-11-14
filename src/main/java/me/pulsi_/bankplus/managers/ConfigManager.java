package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.utils.BPVersions;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;

public class ConfigManager {

    private int commentsCount = 0;
    private int spacesCount = 0;
    private final String commentIdentifier = "bankplus_comment";
    private final String spaceIdentifier = "bankplus_space";

    private final BankPlus plugin;
    private File configFile, messagesFile, multipleBanksFile;
    private FileConfiguration config, messagesConfig, multipleBanksConfig;

    public enum Type {
        CONFIG,
        MESSAGES,
        MULTIPLE_BANKS
    }

    public ConfigManager(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void createConfigs() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        multipleBanksFile = new File(plugin.getDataFolder(), "multiple_banks.yml");

        if (!configFile.exists()) plugin.saveResource("config.yml", false);
        if (!messagesFile.exists()) plugin.saveResource("messages.yml", false);
        if (!multipleBanksFile.exists()) plugin.saveResource("multiple_banks.yml", false);

        config = new YamlConfiguration();
        messagesConfig = new YamlConfiguration();
        multipleBanksConfig = new YamlConfiguration();

        reloadConfig(Type.CONFIG);
        reloadConfig(Type.MESSAGES);
        reloadConfig(Type.MULTIPLE_BANKS);

        buildConfig();
        buildMessages();
        buildMultipleBanks();

        plugin.getDataManager().reloadPlugin();
    }

    public FileConfiguration getConfig(Type type) {
        switch (type) {
            case CONFIG:
                return config;
            case MESSAGES:
                return messagesConfig;
            case MULTIPLE_BANKS:
                return multipleBanksConfig;
            default:
                return null;
        }
    }

    public boolean reloadConfig(Type type) {
        switch (type) {
            case CONFIG:
                try {
                    config.load(configFile);
                    return true;
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error(e.getMessage());
                    return false;
                }

            case MESSAGES:
                try {
                    messagesConfig.load(messagesFile);
                    return true;
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error(e.getMessage());
                    return false;
                }

            case MULTIPLE_BANKS:
                try {
                    multipleBanksConfig.load(multipleBanksFile);
                    return true;
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error(e.getMessage());
                    return false;
                }
        }
        return false;
    }

    public void saveConfig(Type type) {
        switch (type) {
            case CONFIG:
                try {
                    config.save(configFile);
                } catch (IOException e) {
                    BPLogger.warn(e.getMessage());
                }
                break;

            case MESSAGES:
                try {
                    messagesConfig.save(messagesFile);
                } catch (IOException e) {
                    BPLogger.warn(e.getMessage());
                }
                break;

            case MULTIPLE_BANKS:
                try {
                    multipleBanksConfig.save(multipleBanksFile);
                } catch (IOException e) {
                    BPLogger.warn(e.getMessage());
                }
                break;
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

    public void recreateFile(File file) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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
        });
    }

    public void buildConfig() {
        // Create a copy of the config file
        File newConfigFile = new File(plugin.getDataFolder(), "config.yml");
        FileConfiguration oldConfig = new YamlConfiguration();
        FileConfiguration newConfig = new YamlConfiguration();

        BPVersions.renameGeneralSection(newConfigFile);

        try {
            oldConfig.load(newConfigFile);
        } catch (Exception ignored) { }

        addComments(newConfig,
                "Configuration File of BankPlus",
                "Made by Pulsi_, Version v" + plugin.getDescription().getVersion());
        addSpace(newConfig);

        addComments(newConfig, "Check for new updates of the plugin.");
        validatePath(oldConfig, newConfig, "Update-Checker", true);
        addSpace(newConfig);

        /* Interest */

        addComments(newConfig,
                "Interest will increase players bank balance",
                "by giving a % of their bank money.",
                "",
                "To restart the interest type /bank restartInterest.",
                "",
                "Players must have the \"bankplus.receive.interest\"",
                "permission to receive the interest.");
        addCommentsUnder(newConfig, "Interest", "Enable or disable the interest feature.");
        validatePath(oldConfig, newConfig, "Interest.Enabled", true);
        addSpace(newConfig, "Interest");

        addCommentsUnder(newConfig, "Interest.AFK-Settings", "If a player is AFK, it won't receive the interest.");
        validatePath(oldConfig, newConfig, "Interest.AFK-Settings.Ignore-AFK-Players", false);
        addSpace(newConfig, "Interest.AFK-Settings");

        addCommentsUnder(newConfig, "Interest.AFK-Settings",
                "Choose if using the EssentialsX AFK.",
                "(You will need to install EssentialsX)");
        validatePath(oldConfig, newConfig, "Interest.AFK-Settings.Use-EssentialsX-AFK", false);
        addSpace(newConfig, "Interest.AFK-Settings");

        addCommentsUnder(newConfig, "Interest.AFK-Settings",
                "The time, in minutes, that will pass",
                "before marking a player as AFK");
        validatePath(oldConfig, newConfig, "Interest.AFK-Settings.AFK-Time", 5);
        addSpace(newConfig, "Interest.AFK-Settings");

        addCommentsUnder(newConfig, "Interest", "The percentage of money given.");
        validatePath(oldConfig, newConfig, "Interest.Money-Given", "5%");
        addSpace(newConfig, "Interest");

        addCommentsUnder(newConfig, "Interest",
                "This is the interest cooldown.",
                "You can choose the delay between:",
                "  seconds (time s), minutes (time m)",
                "  hours (time h), days (time d)",
                "If no time will be specified, it",
                "will automatically choose minutes.",
                "(You must put the space to specify the time!)");
        validatePath(oldConfig, newConfig, "Interest.Delay", "5 m");
        addSpace(newConfig, "Interest");

        addCommentsUnder(newConfig, "Interest", "The max amount that you can receive with interest.");
        validatePath(oldConfig, newConfig, "Interest.Max-Amount", "500000");
        addSpace(newConfig, "Interest");

        addCommentsUnder(newConfig, "Interest", "Choose if also giving interest to offline players.");
        validatePath(oldConfig, newConfig, "Interest.Give-To-Offline-Players", false);
        addSpace(newConfig, "Interest");

        addCommentsUnder(newConfig, "Interest", "The permission for offline players to receive interest.");
        validatePath(oldConfig, newConfig, "Interest.Offline-Permission", "bankplus.receive.interest");
        addSpace(newConfig);

        /* Deposit settings */

        addCommentsUnder(newConfig, "General-Settings",
                "You need to restart the server",
                "to apply these changes.",
                "",
                "Priorities: LOWEST, LOW, NORMAL, HIGH, HIGHEST");
        validatePath(oldConfig, newConfig, "General-Settings.Event-Priorities.PlayerChat", "NORMAL");
        validatePath(oldConfig, newConfig, "General-Settings.Event-Priorities.BankClick", "NORMAL");
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings",
                "The amount that a player will receive",
                "when joining for the first time");
        validatePath(oldConfig, newConfig, "General-Settings.Join-Start-Amount", "500");
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings",
                "Enable or not the guis module.",
                "",
                "If the module is not enabled, you won't",
                "be able to use the multiple gui and gui",
                "settings features.");
        validatePath(oldConfig, newConfig, "General-Settings.Enable-Guis", true);
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings",
                "This is really important, you must have 1",
                "main gui selected, based on the names of",
                "the files in the guis folder.");
        validatePath(oldConfig, newConfig, "General-Settings.Main-Gui", "bank");
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings",
                "Store player's money using UUIDs,",
                "otherwise the plugin will use names.");
        validatePath(oldConfig, newConfig, "General-Settings.Use-UUIDs", true);
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings",
                "In minutes, the delay to save all players balances. It is used",
                "to prevent players from losing their money if the server crashes.",
                "Put 0 to disable this option.");
        validatePath(oldConfig, newConfig, "General-Settings.Save-Delay", 10);
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings",
                "Choose if sending a message to the console",
                "when the plugin save all balances. (Only console)");
        validatePath(oldConfig, newConfig, "General-Settings.Save-Broadcast", true);
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings",
                "The max amount that a player can deposit, use 0 to disable.",
                "",
                "IMPORTANT! The bank capacity is defined in the BANK FILE!",
                "This option is only a fallback value in case the",
                "level of the bank does not specify its capacity!");
        validatePath(oldConfig, newConfig, "General-Settings.Max-Bank-Capacity", "500000000");
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings",
                "The max amount of decimals that a player balance can have.",
                "",
                "You can put 0 to use an economy without decimals.");
        validatePath(oldConfig, newConfig, "General-Settings.Max-Decimals-Amount", 2);
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings",
                "Enabling this option, it will reopen the bank after",
                "typing in chat when depositing / withdrawing money.");
        validatePath(oldConfig, newConfig, "General-Settings.Reopen-Bank-After-Chat", true);
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings",
                "The message that a player has to type",
                "to stop typing the custom amount.");
        validatePath(oldConfig, newConfig, "General-Settings.Chat-Exit-Message", "exit");
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings",
                "These commands will be executed when leaving from typing",
                "in chat while using the custom withdraw / deposit.",
                "",
                "You can put as many commands as you want.");
        validatePath(oldConfig, newConfig, "General-Settings.Chat-Exit-Commands", new ArrayList<>());
        addCommentsUnder(newConfig, "General-Settings",
                "- \"[CONSOLE] tell %player% You typed in chat!\"",
                "- \"[PLAYER] say I typed in chat!\"");
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings", "Worlds where the bank won't work");
        validatePath(oldConfig, newConfig, "General-Settings.Worlds-Blacklist", new ArrayList<>(Collections.singletonList("noBankWorld")));
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings",
                "Send an alert message to show the player how",
                "much money has earned while being offline.");
        validatePath(oldConfig, newConfig, "General-Settings.Offline-Interest-Earned-Message.Enabled", true);
        addCommentsUnder(newConfig, "General-Settings.Offline-Interest-Earned-Message", "In seconds, put 0 to disable the delay.");
        validatePath(oldConfig, newConfig, "General-Settings.Offline-Interest-Earned-Message.Delay", 2);
        validatePath(oldConfig, newConfig, "General-Settings.Offline-Interest-Earned-Message.Message",
                "&a&lBank&9&lPlus &aYou have earned &f%amount% money &awhile being offline!");
        addSpace(newConfig, "General-Settings");

        validatePath(oldConfig, newConfig, "General-Settings.Withdraw-Sound.Enabled", true);
        addCommentsUnder(newConfig, "General-Settings.Withdraw-Sound", "Sound-Type,Volume,Pitch.");
        validatePath(oldConfig, newConfig, "General-Settings.Withdraw-Sound.Sound", BPMethods.getSoundBasedOnServerVersion());
        addSpace(newConfig, "General-Settings");

        validatePath(oldConfig, newConfig, "General-Settings.Deposit-Sound.Enabled", true);
        validatePath(oldConfig, newConfig, "General-Settings.Deposit-Sound.Sound", BPMethods.getSoundBasedOnServerVersion());
        addSpace(newConfig, "General-Settings");

        validatePath(oldConfig, newConfig, "General-Settings.View-Sound.Enabled", true);
        validatePath(oldConfig, newConfig, "General-Settings.View-Sound.Sound", BPMethods.getSoundBasedOnServerVersion());
        addSpace(newConfig, "General-Settings");

        validatePath(oldConfig, newConfig, "General-Settings.Personal-Sound.Enabled", true);
        validatePath(oldConfig, newConfig, "General-Settings.Personal-Sound.Sound", BPMethods.getSoundBasedOnServerVersion());
        addSpace(newConfig);

        /* Deposit settings */

        addComments(newConfig,
                "The player needs to have the permission",
                "\"bankplus.deposit\" to be able to deposit.");
        addCommentsUnder(newConfig, "Deposit-Settings", "The max amount to deposit per time, use 0 to disable.");
        validatePath(oldConfig, newConfig, "Deposit-Settings.Max-Deposit-Amount", getValueFromOldPath(oldConfig,
                "General-Settings.Max-Deposit-Amount", "Deposit-Settings.Max-Deposit-Amount", "0"));
        addSpace(newConfig, "Deposit-Settings");

        addCommentsUnder(newConfig, "Deposit-Settings", "The minimum amount to deposit per time, use 0 to disable.");
        validatePath(oldConfig, newConfig, "Deposit-Settings.Minimum-Deposit-Amount", getValueFromOldPath(oldConfig,
                "General-Settings.Minimum-Amount", "Deposit-Settings.Minimum-Deposit-Amount", "0"));
        addSpace(newConfig, "Deposit-Settings");

        addCommentsUnder(newConfig, "Deposit-Settings",
                "The money that a player will loose for taxes",
                "when depositing, use 0 to disable.",
                "",
                "Use the permission \"bankplus.deposit.bypass-taxes\"",
                "to bypass the deposit taxes.");
        validatePath(oldConfig, newConfig, "Deposit-Settings.Deposit-Taxes", getValueFromOldPath(oldConfig,
                "General-Settings.Deposit-Taxes", "Deposit-Settings.Deposit-Taxes", "0%"));
        addSpace(newConfig);

        /* Withdraw settings */

        addComments(newConfig,
                "The player needs to have the permission",
                "\"bankplus.withdraw\" to be able to deposit.");
        addCommentsUnder(newConfig, "Withdraw-Settings", "The max amount to withdraw per time, use 0 to disable.");
        validatePath(oldConfig, newConfig, "Withdraw-Settings.Max-Withdraw-Amount", getValueFromOldPath(oldConfig,
                "General-Settings.Max-Withdraw-Amount", "Withdraw-Settings.Max-Withdraw-Amount", "0"));
        addSpace(newConfig, "Withdraw-Settings");

        addCommentsUnder(newConfig, "Withdraw-Settings", "The minimum amount to withdraw per time, use 0 to disable.");
        validatePath(oldConfig, newConfig, "Withdraw-Settings.Minimum-Withdraw-Amount", getValueFromOldPath(oldConfig,
                "General-Settings.Minimum-Amount", "Withdraw-Settings.Minimum-Withdraw-Amount", "0"));
        addSpace(newConfig, "Withdraw-Settings");

        addCommentsUnder(newConfig, "Withdraw-Settings",
                "The money that a player will loose for taxes",
                "when withdrawing, use 0 to disable.",
                "",
                "Use the permission \"bankplus.withdraw.bypass-taxes\"",
                "to bypass the deposit taxes.");
        validatePath(oldConfig, newConfig, "Withdraw-Settings.Withdraw-Taxes", getValueFromOldPath(oldConfig,
                "General-Settings.Withdraw-Taxes", "Withdraw-Settings.Withdraw-Taxes" , "0%"));
        addSpace(newConfig);

        /* BankTop settings */

        addCommentsUnder(newConfig, "BankTop", "Enable or not the feature.");
        validatePath(oldConfig, newConfig, "BankTop.Enabled", true);
        addSpace(newConfig, "BankTop");

        addCommentsUnder(newConfig, "BankTop", "The size of the banktop.");
        validatePath(oldConfig, newConfig, "BankTop.Size", 10);
        addSpace(newConfig, "BankTop");

        addCommentsUnder(newConfig, "BankTop", "In ticks, the delay before the top will update.");
        validatePath(oldConfig, newConfig, "BankTop.Update-Delay", 12000);
        addSpace(newConfig, "BankTop");

        addCommentsUnder(newConfig, "BankTop.Update-Broadcast",
                "Choose if broadcasting to the server",
                "when the plugin updates the banktop.");
        validatePath(oldConfig, newConfig, "BankTop.Update-Broadcast.Enabled", true);
        addSpace(newConfig, "BankTop.Update-Broadcast");

        addCommentsUnder(newConfig, "BankTop.Update-Broadcast", "Choose if the broadcast will be sent only to the console.");
        validatePath(oldConfig, newConfig, "BankTop.Update-Broadcast.Only-Console", false);
        addSpace(newConfig, "BankTop.Update-Broadcast");

        addCommentsUnder(newConfig, "BankTop.Update-Broadcast", "The message that will be sent when updating.");
        validatePath(oldConfig, newConfig, "BankTop.Update-Broadcast.Message", "%prefix% &aThe BankTop has been updated!");
        addSpace(newConfig, "BankTop");

        addCommentsUnder(newConfig, "BankTop",
                "The format that will be used to",
                "display the money in the banktop.",
                "You can choose between:",
                "  default_amount, amount_long,",
                "  amount_formatted, amount_formatted_long");
        validatePath(oldConfig, newConfig, "BankTop.Money-Format", "amount_formatted");
        addSpace(newConfig, "BankTop");

        addCommentsUnder(newConfig, "BankTop", "The message to display the banktop.");
        validatePath(oldConfig, newConfig, "BankTop.Format", new ArrayList<>(Arrays.asList(
                "&8&m---------&8[&a &lBank&9&lPlus &aBankTop &8]&m---------",
                "&61# &6%bankplus_banktop_name_1%&8: &a%bankplus_banktop_money_1%",
                "&61# &6%bankplus_banktop_name_2%&8: &a%bankplus_banktop_money_2%",
                "&61# &6%bankplus_banktop_name_3%&8: &a%bankplus_banktop_money_3%",
                "&61# &6%bankplus_banktop_name_4%&8: &a%bankplus_banktop_money_4%",
                "&61# &6%bankplus_banktop_name_5%&8: &a%bankplus_banktop_money_5%",
                "&61# &6%bankplus_banktop_name_6%&8: &a%bankplus_banktop_money_6%",
                "&61# &6%bankplus_banktop_name_7%&8: &a%bankplus_banktop_money_7%",
                "&61# &6%bankplus_banktop_name_8%&8: &a%bankplus_banktop_money_8%",
                "&61# &6%bankplus_banktop_name_9%&8: &a%bankplus_banktop_money_9%",
                "&61# &6%bankplus_banktop_name_10%&8: &a%bankplus_banktop_money_10%",
                "  &7&o(( The BankTop will update every 10m ))"
        )));
        addSpace(newConfig);

        /* Placeholders section */

        validatePath(oldConfig, newConfig, "Placeholders.Money.Thousands", "K");
        validatePath(oldConfig, newConfig, "Placeholders.Money.Millions", "M");
        validatePath(oldConfig, newConfig, "Placeholders.Money.Billions", "B");
        validatePath(oldConfig, newConfig, "Placeholders.Money.Trillions", "T");
        validatePath(oldConfig, newConfig, "Placeholders.Money.Quadrillions", "Q");
        validatePath(oldConfig, newConfig, "Placeholders.Money.Quintillions", "QQ");
        addSpace(newConfig, "Placeholders");

        validatePath(oldConfig, newConfig, "Placeholders.Time.Second", "Second");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Seconds", "Seconds");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Minute", "Minute");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Minutes", "Minutes");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Hour", "Hour");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Hours", "Hours");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Day", "Days");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Days", "Days");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Only-Seconds", "%seconds% %seconds_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Only-Minutes", "%minutes% %minutes_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Only-Hours", "%hours% %hours_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Only-Days", "%days% %days_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Seconds-Minutes", "%seconds% %seconds_placeholder% and %minutes% %minutes_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Seconds-Hours", "%seconds% %seconds_placeholder% and %hours% %hours_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Seconds-Days", "%seconds% %seconds_placeholder% and %days% %days_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Seconds-Minutes-Hours", "%seconds% %seconds_placeholder%, %minutes% %minutes_placeholder% and %hours% %hours_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Seconds-Hours-Days", "%seconds% %seconds_placeholder%, %hours% %hours_placeholder% and %days% %days_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Seconds-Minutes-Days", "%seconds% %seconds_placeholder%, %minutes% %minutes_placeholder% and %days% %days_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Seconds-Minutes-Hours-Days", "%seconds% %seconds_placeholder%, %minutes% %minutes_placeholder%, %hours% %hours_placeholder% and %days% %days_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Minutes-Hours", "%minutes% %minutes_placeholder% and %hours% %hours_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Minutes-Days", "%minutes% %minutes_placeholder% and %days% %days_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Minutes-Hours-Days", "%minutes% %minutes_placeholder%, %hours% %hours_placeholder% and %days% %days_placeholder%");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Interest-Time.Hours-Days", "%hours% %hours_placeholder% and %days% %days_placeholder%");
        addSpace(newConfig, "Placeholders");

        validatePath(oldConfig, newConfig, "Placeholders.Upgrades.Max-Level", "&cMaxed");

        commentsCount = 0;
        spacesCount = 0;

        try {
            newConfig.save(newConfigFile);
        } catch (IOException e) {
            BPLogger.error(e.getMessage());
        }
        reloadConfig(Type.CONFIG);
        recreateFile(newConfigFile);
    }

    public void buildMessages() {
        File newMessagesFile = new File(plugin.getDataFolder(), "messages.yml");
        FileConfiguration newMessagesConfig = new YamlConfiguration();

        addComments(newMessagesConfig,
                "Messages File of BankPlus",
                "Made by Pulsi_, Version v" + plugin.getDescription().getVersion());
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig,
                "Local Placeholders",
                "These placeholders will work only in some of these messages, do",
                "not use it for gui or any other things because they won't work!",
                "",
                "%amount% -> Number Formatted with commas",
                "%amount_long% -> Raw Number",
                "%amount_formatted% -> Number Formatted",
                "%amount_formatted_long% -> Number formatted without \".\"",
                "%player% -> Player name");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "The main plugin prefix.");
        validatePath(messagesConfig, newMessagesConfig, "Prefix", "&a&lBank&9&lPlus");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig,
                "You can use a message as single or multiple:",
                "MessageIdentifier: \"A single message\"",
                "MessageIdentifier:",
                "  - \"Multiple messages\"",
                "  - \"in just one! :)\"");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "System");
        validatePath(messagesConfig, newMessagesConfig, "Reload", "%prefix% &aPlugin reloaded!");
        validatePath(messagesConfig, newMessagesConfig, "Interest-Restarted", "%prefix% &aInterest Restarted!");
        validatePath(messagesConfig, newMessagesConfig, "Interest-Disabled", "%prefix% &cThe interest is disabled!");
        validatePath(messagesConfig, newMessagesConfig, "BankTop-Disabled", "%prefix% &cThe banktop is disabled!");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "Plugin");
        validatePath(messagesConfig, newMessagesConfig, "Personal-Bank", "%prefix% &aYou have &f%amount_formatted% &amoney in your bank.");
        validatePath(messagesConfig, newMessagesConfig, "Multiple-Personal-Bank", "%prefix% &aYou have a total amount of &f%amount_formatted% &amoney in your banks.");
        validatePath(messagesConfig, newMessagesConfig, "Success-Withdraw", "%prefix% &aSuccessfully withdrew &f%amount_formatted% &amoney! (&f%taxes_formatted% &alost in taxes)");
        validatePath(messagesConfig, newMessagesConfig, "Success-Deposit", "%prefix% &aSuccessfully deposited &f%amount_formatted% &amoney! (&f%taxes_formatted% &alost in taxes)");
        validatePath(messagesConfig, newMessagesConfig, "Bank-Others", "%prefix% &f%player% &ahas &f%amount_formatted% Money &ain their bank!");
        validatePath(messagesConfig, newMessagesConfig, "Multiple-Bank-Others", "%prefix% &f%player% &ahas a total amount of &f%amount_formatted% Money &ain their banks!");
        validatePath(messagesConfig, newMessagesConfig, "Set-Message", "%prefix% &aYou have set &f%player%'s &abank balance to &f%amount_formatted%&a!");
        validatePath(messagesConfig, newMessagesConfig, "Add-Message", "%prefix% &aYou have added &f%amount_formatted% Money &ato &f%player%'s &abank balance!");
        validatePath(messagesConfig, newMessagesConfig, "Remove-Message", "%prefix% &aYou have removed &f%amount_formatted% &amoney to &f%player%'s &abank balance!");
        validatePath(messagesConfig, newMessagesConfig, "Set-Level-Message", "%prefix% &aYou have set &f%player%'s &abank level to &f%level%&a!");
        validatePath(messagesConfig, newMessagesConfig, "Pay-Message", "%prefix% &aYou have added &f%amount_formatted% Money &ato &f%player%'s &abank balance!");
        validatePath(messagesConfig, newMessagesConfig, "Chat-Deposit", "%prefix% &aType an amount in chat to deposit, type 'exit' to exit");
        validatePath(messagesConfig, newMessagesConfig, "Chat-Withdraw", "%prefix% &aType an amount in chat to withdraw, type 'exit' to exit");
        validatePath(messagesConfig, newMessagesConfig, "Payment-Sent", "%prefix% &aYou have successfully sent to &f%player% %amount_formatted% &amoney!");
        validatePath(messagesConfig, newMessagesConfig, "Payment-Received", "%prefix% &aYou have received &f%amount_formatted% &amoney from &f%player%!");
        validatePath(messagesConfig, newMessagesConfig, "Interest-Time", "%prefix% &aWait more &f%time% &ato get the interest.");
        validatePath(messagesConfig, newMessagesConfig, "Balances-Saved", "%prefix% &aSuccessfully saved all player balances to the file!");
        validatePath(messagesConfig, newMessagesConfig, "BankTop-Updated", "%prefix% &aSuccessfully updated the banktop!");
        validatePath(messagesConfig, newMessagesConfig, "Bank-Upgraded", "%prefix% &aSuccessfully upgraded the bank!");
        validatePath(messagesConfig, newMessagesConfig, "Force-Open", "%prefix% &aSuccessfully forced &f%player% &ato open the bank!");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "Titles");
        validatePath(messagesConfig, newMessagesConfig, "Title-Custom-Transaction.Enabled", true);
        validatePath(messagesConfig, newMessagesConfig, "Title-Custom-Transaction.Title-Deposit", "%prefix% &fType in &achat &fan, amount to &adeposit,10,40,10");
        validatePath(messagesConfig, newMessagesConfig, "Title-Custom-Transaction.Title-Withdraw", "%prefix% &fType in &achat &fan, amount to &awithdraw,10,40,10");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "Interest Messages");
        validatePath(messagesConfig, newMessagesConfig, "Interest-Broadcast.Enabled", true);
        validatePath(messagesConfig, newMessagesConfig, "Interest-Broadcast.Message", "%prefix% &aYou have earned &f%amount_formatted% Money &ain interest!");
        validatePath(messagesConfig, newMessagesConfig, "Interest-Broadcast.Multi-Message", "%prefix% &aYou have earned a total amount of &f%amount_formatted% Money &ain interest!");
        validatePath(messagesConfig, newMessagesConfig, "Interest-Broadcast.No-Money", "%prefix% &aSadly, you received 0 money from interest.");
        validatePath(messagesConfig, newMessagesConfig, "Interest-Broadcast.Bank-Full", "%prefix% &cYou can't earn anymore money from interest because your bank is full!");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "Help Message");
        List<String> helpMessages = new ArrayList<>();
        helpMessages.add("%prefix% &aHelp page");
        helpMessages.add("&a/bank deposit <amount> &7Deposit an amount of Money.");
        helpMessages.add("&a/bank withdraw <amount> &7Withdraw an amount of Money.");
        helpMessages.add("&a/bank view <player> &7View the balance of a player.");
        helpMessages.add("&7Plugin made by Pulsi_");
        helpMessages.add("&aRate 5 Star!");
        validatePath(messagesConfig, newMessagesConfig, "Help-Message", helpMessages);
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "Errors");
        validatePath(messagesConfig, newMessagesConfig, "Specify-Number", "%prefix% &cPlease specify a number!");
        validatePath(messagesConfig, newMessagesConfig, "Specify-Player", "%prefix% &cPlease specify a player!");
        validatePath(messagesConfig, newMessagesConfig, "Specify-Bank", "%prefix% &cPlease specify a bank!");
        validatePath(messagesConfig, newMessagesConfig, "Invalid-Number", "%prefix% &cPlease choose a valid number!");
        validatePath(messagesConfig, newMessagesConfig, "Invalid-Player", "%prefix% &cPlease choose a valid player!");
        validatePath(messagesConfig, newMessagesConfig, "Invalid-Bank", "%prefix% &cPlease choose a valid bank!");
        validatePath(messagesConfig, newMessagesConfig, "Invalid-Bank-Level", "%prefix% &cPlease choose a valid bank level!");
        validatePath(messagesConfig, newMessagesConfig, "Cannot-Deposit-Anymore", "%prefix% &cYou can't deposit anymore money!");
        validatePath(messagesConfig, newMessagesConfig, "Cannot-Use-Bank-Here", "%prefix% &cSorry, the bank is disabled in this world!");
        validatePath(messagesConfig, newMessagesConfig, "Cannot-Use-Negative-Number", "%prefix% &cYou can't use a negative number!");
        validatePath(messagesConfig, newMessagesConfig, "Bank-Full", "%prefix% &cThe bank of %player% is full!");
        validatePath(messagesConfig, newMessagesConfig, "Bank-Empty", "%prefix% &cThe bank of %player% is empty!");
        validatePath(messagesConfig, newMessagesConfig, "Cannot-Access-Bank", "%prefix% &cYou can't access to this bank!");
        validatePath(messagesConfig, newMessagesConfig, "Cannot-Access-Bank-Others", "%prefix% &c%player% can't access to this bank!");
        validatePath(messagesConfig, newMessagesConfig, "Bank-Max-Level", "%prefix% &cThe bank is already at the max level!");
        validatePath(messagesConfig, newMessagesConfig, "Minimum-Number", "%prefix% &cPlease use an higher number for this action! ( Minimum: 10 )");
        validatePath(messagesConfig, newMessagesConfig, "Not-Player", "%prefix% &cYou are not a player!");
        validatePath(messagesConfig, newMessagesConfig, "Insufficient-Money", "%prefix% &cYou don't have enough money!");
        validatePath(messagesConfig, newMessagesConfig, "Gui-Module-Disabled", "%prefix% &cThe gui module is disabled!");
        validatePath(messagesConfig, newMessagesConfig, "Internal-Error", "%prefix% &cAn internal error has occurred, try again later!");
        validatePath(messagesConfig, newMessagesConfig, "Failed-Reload", "%prefix% &cBankPlus has failed his reload task, please check the console for more info. (This is usually not a bankplus problem!)");
        validatePath(messagesConfig, newMessagesConfig, "Unknown-Command", "%prefix% &cUnknown Command!");
        validatePath(messagesConfig, newMessagesConfig, "No-Permission", "%prefix% &cYou don't have the permission! (%permission%)");
        addSpace(newMessagesConfig);

        commentsCount = 0;
        spacesCount = 0;

        try {
            newMessagesConfig.save(newMessagesFile);
        } catch (IOException e) {
            BPLogger.error(e.getMessage());
        }
        reloadConfig(Type.MESSAGES);
        recreateFile(newMessagesFile);
    }

    public void buildMultipleBanks() {
        File newMultipleBanksFile = new File(plugin.getDataFolder(), "multiple_banks.yml");
        FileConfiguration newMultipleBanksConfig = new YamlConfiguration();

        addComments(newMultipleBanksConfig,
                "Put this to true to enable the multiple-banks feature.",
                "",
                "If this feature is enabled, typing /bank won't open the",
                "main bank but a gui with a list of all available banks.",
                "",
                "When enabling this option, many commands will",
                "change due to the multiple bank options (Ex: The",
                "command to set money will require to specify the",
                "bank to set the money in the selected bank )",
                "",
                "Remember that must specify a main gui in the config file.");
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Enabled", false);
        addSpace(newMultipleBanksConfig);

        addComments(newMultipleBanksConfig,
                "Choose if showing or hiding a",
                "bank if it's not accessible.");
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Shows-Not-Available-Banks", true);
        addSpace(newMultipleBanksConfig);

        addComments(newMultipleBanksConfig,
                "If having only 1 bank available, this option will",
                "make the player directly open that one instead of",
                "opening the banks-gui with just the 1 gui available.");
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Directly-Open-If-1-Is-Available", false);
        addSpace(newMultipleBanksConfig);

        addComments(newMultipleBanksConfig,
                "The gui that contains the different banks.",
                "",
                "To add more banks go to the \"banks\" folder and",
                "add more files following the default bank format.");
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Title", "&a&lBANKS LIST");
        addSpace(newMultipleBanksConfig, "Banks-Gui");

        addCommentsUnder(newMultipleBanksConfig, "Banks-Gui",
                "If the number of banks is higher",
                "than the gui slots, it will",
                "separate the banks in more pages.");
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Lines", 1);
        addSpace(newMultipleBanksConfig, "Banks-Gui");

        addCommentsUnder(newMultipleBanksConfig, "Banks-Gui", "In ticks.");
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Update-Delay", 20);
        addSpace(newMultipleBanksConfig, "Banks-Gui");

        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Filler.Enabled", true);
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Filler.Material", "WHITE_STAINED_GLASS_PANE");
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Filler.Glowing", false);
        addSpace(newMultipleBanksConfig, "Banks-Gui");

        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Previous-Page.Material", "ARROW");
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Previous-Page.Displayname", "&aPrevious page &8(&7%previous_page%/%all_pages%&8)");
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Previous-Page.Lore", new ArrayList<>(Collections.singleton("&7Go to the previous page")));
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Previous-Page.Glowing", false);
        addCommentsUnder(newMultipleBanksConfig, "Banks-Gui",
                "It will show the item only if the",
                "banks are more than the empty slots.");
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Previous-Page.Slot", 1);
        addSpace(newMultipleBanksConfig, "Banks-Gui");

        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Next-Page.Material", "ARROW");
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Next-Page.Displayname", "&aNext page &8(&7%nex_page%/%all_pages%&8)");
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Next-Page.Lore", new ArrayList<>(Collections.singleton("&7Go to the next page")));
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Next-Page.Glowing", false);
        validatePath(multipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Next-Page.Slot", 9);

        commentsCount = 0;
        spacesCount = 0;

        try {
            newMultipleBanksConfig.save(newMultipleBanksFile);
        } catch (IOException e) {
            BPLogger.error(e.getMessage());
        }
        reloadConfig(Type.MULTIPLE_BANKS);
        recreateFile(newMultipleBanksFile);
    }

    private void addSpace(FileConfiguration config) {
        config.set(spaceIdentifier + spacesCount, "");
        spacesCount++;
    }

    private void addSpace(FileConfiguration config, String path) {
        config.set(path + "." + spaceIdentifier + spacesCount, "");
        spacesCount++;
    }

    private void addComments(FileConfiguration config, String... comments) {
        for (String comment : comments) {
            config.set(commentIdentifier + commentsCount, comment);
            commentsCount++;
        }
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

    private Object getValueFromOldPath(FileConfiguration from, String path, String fallbackPath, Object fallbackValue) {
        return from.get(path) == null ? fallbackValue : from.get(path);
    }
}