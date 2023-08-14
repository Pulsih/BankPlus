package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.BPVersions;
import me.pulsi_.bankplus.xSeries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConfigManager {

    private int commentsCount = 0;
    private int spacesCount = 0;
    private final String commentIdentifier = "bankplus_comment";
    private final String spaceIdentifier = "bankplus_space";
    private File configFile, messagesFile, multipleBanksFile, commandsFile, savesFile;
    private FileConfiguration config, messagesConfig, multipleBanksConfig, commandsConfig, savesConfig;
    private boolean autoUpdateFiles, updated = true;

    public enum Type {
        CONFIG("config"),
        MESSAGES("messages"),
        MULTIPLE_BANKS("multiple_banks"),
        SAVES("saves"),
        COMMANDS("commands");

        public String name;

        Type(String name) {
            this.name = name;
        }
    }

    private final BankPlus plugin;

    public ConfigManager(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void setupConfigs() {
        savesFile = new File(plugin.getDataFolder(), Type.SAVES.name + ".yml");

        commandsFile = new File(plugin.getDataFolder(), Type.COMMANDS.name + ".yml");
        configFile = new File(plugin.getDataFolder(), Type.CONFIG.name + ".yml");
        messagesFile = new File(plugin.getDataFolder(), Type.MESSAGES.name + ".yml");
        multipleBanksFile = new File(plugin.getDataFolder(), Type.MULTIPLE_BANKS.name + ".yml");

        savesConfig = new YamlConfiguration();

        commandsConfig = new YamlConfiguration();
        config = new YamlConfiguration();
        messagesConfig = new YamlConfiguration();
        multipleBanksConfig = new YamlConfiguration();

        setupSavesFile();

        setupCommands();
        setupConfig();
        setupMessages();
        setupMultipleBanks();
    }

    public FileConfiguration getConfig(Type type) {
        switch (type) {
            case CONFIG:
                return config;
            case MESSAGES:
                return messagesConfig;
            case MULTIPLE_BANKS:
                return multipleBanksConfig;
            case COMMANDS:
                return commandsConfig;
            case SAVES:
                return savesConfig;
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
                    BPLogger.error("Could not load " + type.name() + " config! (Error: " + e.getMessage().replace("\n", "") + ")");
                    return false;
                }

            case MESSAGES:
                try {
                    messagesConfig.load(messagesFile);
                    return true;
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error("Could not load " + type.name() + " config! (Error: " + e.getMessage().replace("\n", "") + ")");
                    return false;
                }

            case MULTIPLE_BANKS:
                try {
                    multipleBanksConfig.load(multipleBanksFile);
                    return true;
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error("Could not load " + type.name() + " config! (Error: " + e.getMessage().replace("\n", "") + ")");
                    return false;
                }

            case COMMANDS:
                try {
                    commandsConfig.load(commandsFile);
                    return true;
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error("Could not load " + type.name() + " config! (Error: " + e.getMessage().replace("\n", "") + ")");
                    return false;
                }

            case SAVES:
                try {
                    savesConfig.load(savesFile);
                    return true;
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error("Could not load " + type.name() + " config! (Error: " + e.getMessage().replace("\n", "") + ")");
                    return false;
                }
        }
        return false;
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

    public void setupSavesFile() {
        if (!savesFile.exists()) {
            createFile(Type.SAVES.name);
            updated = false;
        }

        reloadConfig(Type.SAVES);

        if (updated) updated = savesConfig.get("version") != null && savesConfig.getString("version").equals(plugin.getDescription().getVersion());

        savesConfig.options().header("DO NOT EDIT / REMOVE THIS FILE OR BANKPLUS MAY GET RESET!");
        savesConfig.set("version", plugin.getDescription().getVersion());

        try {
            savesConfig.save(savesFile);
        } catch (IOException e) {
            BPLogger.error("Could not save \"saves\" file! (Error: " + e.getMessage().replace("\n", "") + ")");
        }
    }

    public void setupConfig() {
        boolean updateFile = true, alreadyExist = configFile.exists();

        if (!alreadyExist) createFile(Type.CONFIG.name);
        else {
            reloadConfig(Type.CONFIG);

            autoUpdateFiles = config.get("General-Settings.Auto-Update-Files") == null || config.getBoolean("General-Settings.Auto-Update-Files");
            updateFile = !updated && autoUpdateFiles;
        }
        if (!updateFile) return;

        FileConfiguration oldConfig = new YamlConfiguration();
        FileConfiguration newConfig = new YamlConfiguration();

        BPVersions.renameGeneralSection(configFile);

        try {
            oldConfig.load(configFile);
        } catch (Exception e) {
            BPLogger.error(
                    "BankPlus was unable to load config.yml to check for changes! (Error: " + e.getLocalizedMessage().replace("\n", "") + ")"
            );
            return;
        }

        if (alreadyExist) {
            File backupFile = createFile("backup/" + Type.CONFIG.name);
            try {
                oldConfig.save(backupFile);
            } catch (IOException e) {
                BPLogger.error("Could not create backup " + Type.CONFIG.name + " file! (Error: " + e.getMessage().replace("\n", "") + ")");
            }
        }

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

        addCommentsUnder(newConfig, "Interest",
                "The percentage of money given. ( default: 5% )",
                "",
                "IMPORTANT! This amount is defined in the BANK FILE!",
                "This option is only a fallback value in case the",
                "level of the bank does not specify its amount!");
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

        addCommentsUnder(newConfig, "Interest",
                "Choose if the interest rate for offline players",
                "will be different from the default one.");
        validatePath(oldConfig, newConfig, "Interest.Different-Offline-Rate", false);
        addSpace(newConfig, "Interest");

        addCommentsUnder(newConfig, "Interest",
                "If 'Different-Offline-Rate' is enabled, the offline players",
                "will receive this interest rate. It works the same as the",
                "'Money-Given' above, you can edit this in the bank file.");
        validatePath(oldConfig, newConfig, "Interest.Offline-Money-Given", "5%");
        addSpace(newConfig, "Interest");

        addCommentsUnder(newConfig, "Interest",
                "The permission for offline players to receive interest.",
                "You can put \"\" if you don't want to use a permission.");
        validatePath(oldConfig, newConfig, "Interest.Offline-Permission", "bankplus.receive.interest");
        addSpace(newConfig);

        addCommentsUnder(newConfig, "General-Settings",
                "Choose if automatically update your files",
                "when a new version is downloaded, you can",
                "disable this option if you see that the",
                "plugin is failing at updating the files",
                "itself, the old affected files will be",
                "saved in a folder as backup.");
        validatePath(oldConfig, newConfig, "General-Settings.Auto-Update-Files", true);
        addSpace(newConfig, "General-Settings");

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
                "The text displayed when the capacity is 0 (infinite).");
        validatePath(oldConfig, newConfig, "General-Settings.Infinite-Capacity-Text", "Infinite");
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

        // To fix the config problem from 5.7 where the list was written with "true", make a check.
        addCommentsUnder(newConfig, "General-Settings", "Worlds where the banks won't work");
        boolean bugged = oldConfig.getBoolean("General-Settings.Worlds-Blacklist");
        if (bugged)
            newConfig.set("General-Settings.Worlds-Blacklist", new ArrayList<>(Collections.singletonList("noBankWorld")));
        else
            validatePath(oldConfig, newConfig, "General-Settings.Worlds-Blacklist", new ArrayList<>(Collections.singletonList("noBankWorld")));
        addSpace(newConfig, "General-Settings");

        addCommentsUnder(newConfig, "General-Settings",
                "Choose if using the bank or the vault",
                "balance to upgrade the bank levels.");
        validatePath(oldConfig, newConfig, "General-Settings.Use-Bank-Balance-To-Upgrade", true);
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
        validatePath(oldConfig, newConfig, "General-Settings.Withdraw-Sound.Sound", XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound().name() + ",5,1");
        addSpace(newConfig, "General-Settings");

        validatePath(oldConfig, newConfig, "General-Settings.Deposit-Sound.Enabled", true);
        validatePath(oldConfig, newConfig, "General-Settings.Deposit-Sound.Sound", XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound().name() + ",5,1");
        addSpace(newConfig, "General-Settings");

        validatePath(oldConfig, newConfig, "General-Settings.View-Sound.Enabled", true);
        validatePath(oldConfig, newConfig, "General-Settings.View-Sound.Sound", XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound().name() + ",5,1");
        addSpace(newConfig, "General-Settings");

        validatePath(oldConfig, newConfig, "General-Settings.Personal-Sound.Enabled", true);
        validatePath(oldConfig, newConfig, "General-Settings.Personal-Sound.Sound", XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound().name() + ",5,1");
        addSpace(newConfig);

        /* Deposit settings */

        addComments(newConfig,
                "The player needs to have the permission",
                "\"bankplus.deposit\" to be able to deposit.");
        addCommentsUnder(newConfig, "Deposit-Settings", "The max amount to deposit per time, use 0 to disable.");
        validatePath(oldConfig, newConfig, "Deposit-Settings.Max-Deposit-Amount",
                getValueFromOldPath(oldConfig, "General-Settings.Max-Deposit-Amount", "Deposit-Settings.Max-Deposit-Amount", "0"));
        addSpace(newConfig, "Deposit-Settings");

        addCommentsUnder(newConfig, "Deposit-Settings", "The minimum amount to deposit per time, use 0 to disable.");
        validatePath(oldConfig, newConfig, "Deposit-Settings.Minimum-Deposit-Amount",
                getValueFromOldPath(oldConfig, "General-Settings.Minimum-Amount", "Deposit-Settings.Minimum-Deposit-Amount", "0"));
        addSpace(newConfig, "Deposit-Settings");

        addCommentsUnder(newConfig, "Deposit-Settings",
                "The money that a player will loose for taxes",
                "when depositing, use 0 to disable.",
                "",
                "Use the permission \"bankplus.deposit.bypass-taxes\"",
                "to bypass the deposit taxes.");
        validatePath(oldConfig, newConfig, "Deposit-Settings.Deposit-Taxes",
                getValueFromOldPath(oldConfig, "General-Settings.Deposit-Taxes", "Deposit-Settings.Deposit-Taxes", "0%"));
        addSpace(newConfig);

        /* Withdraw settings */

        addComments(newConfig,
                "The player needs to have the permission",
                "\"bankplus.withdraw\" to be able to deposit.");
        addCommentsUnder(newConfig, "Withdraw-Settings", "The max amount to withdraw per time, use 0 to disable.");
        validatePath(oldConfig, newConfig, "Withdraw-Settings.Max-Withdraw-Amount",
                getValueFromOldPath(oldConfig, "General-Settings.Max-Withdraw-Amount", "Withdraw-Settings.Max-Withdraw-Amount", "0"));
        addSpace(newConfig, "Withdraw-Settings");

        addCommentsUnder(newConfig, "Withdraw-Settings", "The minimum amount to withdraw per time, use 0 to disable.");
        validatePath(oldConfig, newConfig, "Withdraw-Settings.Minimum-Withdraw-Amount",
                getValueFromOldPath(oldConfig, "General-Settings.Minimum-Amount", "Withdraw-Settings.Minimum-Withdraw-Amount", "0"));
        addSpace(newConfig, "Withdraw-Settings");

        addCommentsUnder(newConfig, "Withdraw-Settings",
                "The money that a player will loose for taxes",
                "when withdrawing, use 0 to disable.",
                "",
                "Use the permission \"bankplus.withdraw.bypass-taxes\"",
                "to bypass the deposit taxes.");
        validatePath(oldConfig, newConfig, "Withdraw-Settings.Withdraw-Taxes",
                getValueFromOldPath(oldConfig, "General-Settings.Withdraw-Taxes", "Withdraw-Settings.Withdraw-Taxes", "0%"));
        addSpace(newConfig);

        /* Loan settings */

        addComments(newConfig,
                "Loans are a way to give money to a player and then",
                "having them back after a period of time.",
                "If a player can't afford to give the money back he",
                "will receive a debt, every money put in the bank",
                "with a debt will be removed till the debt reach 0");
        addCommentsUnder(newConfig, "Loan-Settings", "The max amount to give as a loan.");
        validatePath(oldConfig, newConfig, "Loan-Settings.Max-Amount", "5000");
        addSpace(newConfig, "Loan-Settings");

        addCommentsUnder(newConfig, "Loan-Settings", "The default interest for loans.");
        validatePath(oldConfig, newConfig, "Loan-Settings.Interest", "5%");
        addSpace(newConfig, "Loan-Settings");

        addCommentsUnder(newConfig, "Loan-Settings", "In how many times the loan will be repaid.");
        validatePath(oldConfig, newConfig, "Loan-Settings.Installments", 3);
        addSpace(newConfig, "Loan-Settings");

        addCommentsUnder(newConfig, "Loan-Settings", "The time in ticks between payments (20 ticks = 1 second).");
        validatePath(oldConfig, newConfig, "Loan-Settings.Delay", 1200);
        addSpace(newConfig, "Loan-Settings");

        addCommentsUnder(newConfig, "Loan-Settings", "The time in seconds before the loan request will be deleted.");
        validatePath(oldConfig, newConfig, "Loan-Settings.Accept-Time", 5);
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

        addComments(newConfig, "You can use color codes.");
        validatePath(oldConfig, newConfig, "Placeholders.Money.Thousands", "K");
        validatePath(oldConfig, newConfig, "Placeholders.Money.Millions", "M");
        validatePath(oldConfig, newConfig, "Placeholders.Money.Billions", "B");
        validatePath(oldConfig, newConfig, "Placeholders.Money.Trillions", "T");
        validatePath(oldConfig, newConfig, "Placeholders.Money.Quadrillions", "Q");
        validatePath(oldConfig, newConfig, "Placeholders.Money.Quintillions", "QQ");
        addSpace(newConfig, "Placeholders");

        validatePath(oldConfig, newConfig, "Placeholders.Time.Second", "s");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Seconds", "s");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Minute", "m");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Minutes", "m");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Hour", "h");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Hours", "h");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Day", "d");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Days", "d");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Format", "%d%h%m%s");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Separator", ", ");
        validatePath(oldConfig, newConfig, "Placeholders.Time.Final-Separator", " and ");
        addSpace(newConfig, "Placeholders");

        validatePath(oldConfig, newConfig, "Placeholders.Upgrades.Max-Level", "Maxed");
        addSpace(newConfig, "Placeholders");

        validatePath(oldConfig, newConfig, "Placeholders.BankTop.Player-Not-Found", "Player not found.");

        commentsCount = 0;
        spacesCount = 0;

        try {
            newConfig.save(configFile);
        } catch (Exception e) {
            BPLogger.error("Could not save file changes to config.yml! (Error: " + e.getMessage() + ")");
            return;
        }
        recreateFile(configFile);
    }

    public void setupMessages() {
        boolean updateFile = true, alreadyExist = messagesFile.exists();

        if (!alreadyExist) createFile(Type.MESSAGES.name);
        else updateFile = !updated && autoUpdateFiles;

        if (!updateFile) return;

        FileConfiguration oldMessagesConfig = new YamlConfiguration();
        FileConfiguration newMessagesConfig = new YamlConfiguration();

        try {
            oldMessagesConfig.load(messagesFile);
        } catch (Exception e) {
            BPLogger.error(
                    "BankPlus was unable to load messages.yml to check for changes! (Error: " + e.getLocalizedMessage().replace("\n", "") + ")"
            );
            return;
        }

        if (alreadyExist) {
            File backupFile = createFile("backup/" + Type.MESSAGES.name);
            try {
                oldMessagesConfig.save(backupFile);
            } catch (IOException e) {
                BPLogger.error("Could not create backup " + Type.MESSAGES.name + " file! (Error: " + e.getMessage().replace("\n", "") + ")");
            }
        }

        addComments(newMessagesConfig,
                "Messages File of BankPlus",
                "Made by Pulsi_, Version v" + plugin.getDescription().getVersion());
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig,
                "* Placeholders *",
                "BankPlus messages support placeholderapi and you can",
                "use every type of placeholder inside the messages.");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "Enable the alert message when a message is missing in the file.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Enable-Missing-Message-Alert", true);
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "The main plugin prefix.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Prefix", "&a&lBank&9&lPlus");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig,
                "You can use a message as single or multiple:",
                "MessageIdentifier: \"A single message\"",
                "MessageIdentifier:",
                "  - \"Multiple messages\"",
                "  - \"in just one! :)\"",
                "",
                "If you don't want a message to show put:",
                "Message: \"\"");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "System");
        validatePath(oldMessagesConfig, newMessagesConfig, "Reload", "%prefix% &aPlugin reloaded!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Interest-Restarted", "%prefix% &aInterest Restarted!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Interest-Disabled", "%prefix% &cThe interest is disabled!");
        validatePath(oldMessagesConfig, newMessagesConfig, "BankTop-Disabled", "%prefix% &cThe banktop is disabled!");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "Plugin");
        validatePath(oldMessagesConfig, newMessagesConfig, "Personal-Bank", "%prefix% &aYou have &f%amount_formatted% &amoney in your bank.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Multiple-Personal-Bank", "%prefix% &aYou have a total amount of &f%amount_formatted% &amoney in your banks.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Success-Withdraw", "%prefix% &aSuccessfully withdrew &f%amount_formatted% &amoney! (&f%taxes_formatted% &alost in taxes)");
        validatePath(oldMessagesConfig, newMessagesConfig, "Success-Deposit", "%prefix% &aSuccessfully deposited &f%amount_formatted% &amoney! (&f%taxes_formatted% &alost in taxes)");
        validatePath(oldMessagesConfig, newMessagesConfig, "Bank-Others", "%prefix% &f%player% &ahas &f%amount_formatted% Money &ain their bank!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Multiple-Bank-Others", "%prefix% &f%player% &ahas a total amount of &f%amount_formatted% Money &ain their banks!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Set-Message", "%prefix% &aYou have set &f%player%'s &abank balance to &f%amount_formatted%&a!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Add-Message", "%prefix% &aYou have added &f%amount_formatted% Money &ato &f%player%'s &abank balance!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Remove-Message", "%prefix% &aYou have removed &f%amount_formatted% &amoney to &f%player%'s &abank balance!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Set-Level-Message", "%prefix% &aYou have set &f%player%'s &abank level to &f%level%&a!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Pay-Message", "%prefix% &aYou have added &f%amount_formatted% Money &ato &f%player%'s &abank balance!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Chat-Deposit", "%prefix% &aType an amount in chat to deposit, type 'exit' to exit");
        validatePath(oldMessagesConfig, newMessagesConfig, "Chat-Withdraw", "%prefix% &aType an amount in chat to withdraw, type 'exit' to exit");
        validatePath(oldMessagesConfig, newMessagesConfig, "Payment-Sent", "%prefix% &aYou have successfully sent to &f%player% %amount_formatted% &amoney!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Payment-Received", "%prefix% &aYou have received &f%amount_formatted% &amoney from &f%player%!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Interest-Time", "%prefix% &aWait more &f%time% &ato get the interest.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Balances-Saved", "%prefix% &aSuccessfully saved all player balances to the file!");
        validatePath(oldMessagesConfig, newMessagesConfig, "BankTop-Updated", "%prefix% &aSuccessfully updated the banktop!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Bank-Upgraded", "%prefix% &aSuccessfully upgraded the bank!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Request-Sent", "%prefix% &aSuccessfully sent the loan request to &f%player%&a, waiting for a confirm...");
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Request-Received", Arrays.asList(
                "                       &6&l!! LOAN REQUEST !!",
                "",
                "         &f%player% &asent you a loan of &f%amount_formatted% &amoney",
                "&aType &2/bank loan accept&a to accept or &c/bank loan deny&a to deny.",
                "&7&o(( The money will be automatically be payed back with 5% interest ))"
        ));
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Request-Sent-Accepted", "%prefix% &f%player% &ahas accepted your loan request!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Request-Received-Accepted", "%prefix% &aSuccessfully accepted &f%player%&a's loan, &f%amount_formatted% &amoney have been added to your bank.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Request-Received-Accepted-Full", "%prefix% &aSuccessfully accepted &f%player%&a's loan, &f%amount_formatted% &amoney have been added to your bank but since it was full, &f%extra_formatted% &amoney went to your wallet.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Request-Sent-Denied", "%prefix% &f%player% &chas denied your loan request!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Request-Received-Denied", "%prefix% &aSuccessfully denied &f%player%&a's loan.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Request-Sent-Cancelled", "%prefix% &aYou have cancelled your loan request.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Request-Received-Cancelled", "%prefix% &f%player% &chas cancelled the loan request.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Payback", "%prefix% &aYou have received &f%amount_formatted% &afrom the loan given to &f%player%.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Payback-Full", "%prefix% &aYou have received &f%amount_formatted% &afrom the loan given to &f%player% &abut your bank was full so &f%extra_formatted% &amoney has been added to your wallet.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Returned", "%prefix% &aYou have returned &f%amount_formatted% &amoney from &f%player%&a's loan.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Returned-Debt", "%prefix% &cSadly, you did not have enough money to pay back &f%player%&c, so you are now in debt for %f%amount_formatted%&c.");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "Titles");
        validatePath(oldMessagesConfig, newMessagesConfig, "Title-Custom-Transaction.Enabled", true);
        validatePath(oldMessagesConfig, newMessagesConfig, "Title-Custom-Transaction.Title-Deposit", "%prefix% &fType in &achat &fan, amount to &adeposit,10,40,10");
        validatePath(oldMessagesConfig, newMessagesConfig, "Title-Custom-Transaction.Title-Withdraw", "%prefix% &fType in &achat &fan, amount to &awithdraw,10,40,10");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "Interest Messages");
        validatePath(oldMessagesConfig, newMessagesConfig, "Interest-Broadcast.Enabled", true);
        validatePath(oldMessagesConfig, newMessagesConfig, "Interest-Broadcast.Message", "%prefix% &aYou have earned &f%amount_formatted% Money &ain interest!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Interest-Broadcast.Multi-Message", "%prefix% &aYou have earned a total amount of &f%amount_formatted% Money &ain interest!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Interest-Broadcast.No-Money", "%prefix% &aSadly, you received 0 money from interest.");
        validatePath(oldMessagesConfig, newMessagesConfig, "Interest-Broadcast.Bank-Full", "%prefix% &cYou can't earn anymore money from interest because your bank is full!");
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "Help Message");
        List<String> helpMessages = new ArrayList<>();
        helpMessages.add("%prefix% &aHelp page");
        helpMessages.add("&a/bank deposit <amount> &7Deposit an amount of Money.");
        helpMessages.add("&a/bank withdraw <amount> &7Withdraw an amount of Money.");
        helpMessages.add("&a/bank view <player> &7View the balance of a player.");
        helpMessages.add("&7Plugin made by Pulsi_");
        helpMessages.add("&aRate 5 Star!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Help-Message", helpMessages);
        addSpace(newMessagesConfig);

        addComments(newMessagesConfig, "Errors");
        validatePath(oldMessagesConfig, newMessagesConfig, "Specify-Number", "%prefix% &cPlease specify a number!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Specify-Player", "%prefix% &cPlease specify a player!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Specify-Bank", "%prefix% &cPlease specify a bank!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Invalid-Number", "%prefix% &cPlease choose a valid number!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Invalid-Player", "%prefix% &cPlease choose a valid player!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Invalid-Bank", "%prefix% &cPlease choose a valid bank!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Invalid-Bank-Level", "%prefix% &cPlease choose a valid bank level!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Cannot-Deposit-Anymore", "%prefix% &cYou can't deposit anymore money!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Cannot-Use-Bank-Here", "%prefix% &cSorry, the bank is disabled in this world!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Cannot-Use-Negative-Number", "%prefix% &cYou can't use a negative number!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Bank-Full", "%prefix% &cThe bank of %player% is full!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Bank-Empty", "%prefix% &cThe bank of %player% is empty!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Cannot-Access-Bank", "%prefix% &cYou can't access to this bank!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Cannot-Access-Bank-Others", "%prefix% &c%player% can't access to this bank!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Bank-Max-Level", "%prefix% &cThe bank is already at the max level!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Minimum-Number", "%prefix% &cPlease use an higher number for this action! ( Minimum: 10 )");
        validatePath(oldMessagesConfig, newMessagesConfig, "Not-Player", "%prefix% &cYou are not a player!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Insufficient-Money", "%prefix% &cYou don't have enough money!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Gui-Module-Disabled", "%prefix% &cThe gui module is disabled!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Internal-Error", "%prefix% &cAn internal error has occurred, try again later!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Failed-Reload", "%prefix% &cBankPlus has failed his reload task, please check the console for more info. (This is usually not a bankplus problem!)");
        validatePath(oldMessagesConfig, newMessagesConfig, "Unknown-Command", "%prefix% &cUnknown Command!");
        validatePath(oldMessagesConfig, newMessagesConfig, "No-Permission", "%prefix% &cYou don't have the permission! (%permission%)");
        validatePath(oldMessagesConfig, newMessagesConfig, "No-Loan-Requests", "%prefix% &cYou haven't received any loan requests!");
        validatePath(oldMessagesConfig, newMessagesConfig, "No-Loan-Sent", "%prefix% &cYou haven't sent any loan!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Loan-Already-Sent", "%prefix% &cYou have already sent a loan request!");
        validatePath(oldMessagesConfig, newMessagesConfig, "Cannot-Afford-Loan", "%prefix% &c%player% can't afford for this loan!");

        commentsCount = 0;
        spacesCount = 0;

        try {
            newMessagesConfig.save(messagesFile);
        } catch (Exception e) {
            BPLogger.error("Could not save file changes to messages.yml! (Error: " + e.getMessage() + ")");
            return;
        }
        recreateFile(messagesFile);
    }

    public void setupMultipleBanks() {
        boolean updateFile = true, alreadyExist = multipleBanksFile.exists();

        if (!alreadyExist) createFile(Type.MULTIPLE_BANKS.name);
        else updateFile = !updated && autoUpdateFiles;

        if (!updateFile) return;

        FileConfiguration oldMultipleBanksConfig = new YamlConfiguration();
        FileConfiguration newMultipleBanksConfig = new YamlConfiguration();

        try {
            oldMultipleBanksConfig.load(multipleBanksFile);
        } catch (Exception e) {
            BPLogger.error(
                    "BankPlus was unable to load multiple_banks.yml to check for changes! (Error: " + e.getLocalizedMessage().replace("\n", "") + ")"
            );
            return;
        }

        if (alreadyExist) {
            File backupFile = createFile("backup/" + Type.MULTIPLE_BANKS.name);
            try {
                oldMultipleBanksConfig.save(backupFile);
            } catch (IOException e) {
                BPLogger.error("Could not create backup " + Type.MULTIPLE_BANKS.name + " file! (Error: " + e.getMessage().replace("\n", "") + ")");
            }
        }

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
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Enabled", false);
        addSpace(newMultipleBanksConfig);

        addComments(newMultipleBanksConfig,
                "Choose if showing or hiding a",
                "bank if it's not accessible.");
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Shows-Not-Available-Banks", true);
        addSpace(newMultipleBanksConfig);

        addComments(newMultipleBanksConfig,
                "If having only 1 bank available, this option will",
                "make the player directly open that one instead of",
                "opening the banks-gui with just the 1 gui available.");
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Directly-Open-If-1-Is-Available", false);
        addSpace(newMultipleBanksConfig);

        addComments(newMultipleBanksConfig,
                "This is a new feature used to automatically unlock",
                "new banks when a player has maxed the selected bank.",
                "",
                "The format is \"the bank name:the command to run from console\"",
                "Since the access to banks works with permissions, use",
                "your own permission plugin to make the player access the",
                "new bank, this action will be run as soon as the",
                "player upgrade the selected bank to the max level",
                "(In this example, when you max the \"bank\" bank it will run",
                "the \"lp user %player% permission set bankplus.bank2\" cmd):",
                "Auto-Banks-Unlocker:",
                "- \"bank:lp user %player% permission set bankplus.bank2\"",
                "",
                "You can also use more than 1 cmd:",
                "Auto-Banks-Unlocker",
                "- \"bank:cmd1:cmd2:cmd3\"");
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Auto-Banks-Unlocker", new ArrayList<>());

        addSpace(newMultipleBanksConfig);

        addComments(newMultipleBanksConfig,
                "The gui that contains the different banks.",
                "",
                "To add more banks go to the \"banks\" folder and",
                "add more files following the default bank format.");
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Title", "&a&lBANKS LIST");
        addSpace(newMultipleBanksConfig, "Banks-Gui");

        addCommentsUnder(newMultipleBanksConfig, "Banks-Gui",
                "If the number of banks is higher",
                "than the gui slots, it will",
                "separate the banks in more pages.");
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Lines", 1);
        addSpace(newMultipleBanksConfig, "Banks-Gui");

        addCommentsUnder(newMultipleBanksConfig, "Banks-Gui", "In ticks.");
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Update-Delay", 20);
        addSpace(newMultipleBanksConfig, "Banks-Gui");

        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Filler.Enabled", true);
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Filler.Material", "WHITE_STAINED_GLASS_PANE");
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Filler.Glowing", false);
        addSpace(newMultipleBanksConfig, "Banks-Gui");

        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Previous-Page.Material", "ARROW");
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Previous-Page.Displayname", "&aPrevious page &8(&7%previous_page%/%all_pages%&8)");
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Previous-Page.Lore", new ArrayList<>(Collections.singleton("&7Go to the previous page")));
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Previous-Page.Glowing", false);
        addCommentsUnder(newMultipleBanksConfig, "Banks-Gui",
                "It will show the item only if the",
                "banks are more than the empty slots.");
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Previous-Page.Slot", 1);
        addSpace(newMultipleBanksConfig, "Banks-Gui");

        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Next-Page.Material", "ARROW");
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Next-Page.Displayname", "&aNext page &8(&7%nex_page%/%all_pages%&8)");
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Next-Page.Lore", new ArrayList<>(Collections.singleton("&7Go to the next page")));
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Next-Page.Glowing", false);
        validatePath(oldMultipleBanksConfig, newMultipleBanksConfig, "Banks-Gui.Next-Page.Slot", 9);

        commentsCount = 0;
        spacesCount = 0;

        try {
            newMultipleBanksConfig.save(multipleBanksFile);
        } catch (Exception e) {
            BPLogger.error("Could not save file changes to multiple_banks.yml! (Error: " + e.getMessage() + ")");
        }
        recreateFile(multipleBanksFile);
    }

    public void setupCommands() {
        if (!commandsFile.exists()) plugin.saveResource("commands.yml", true);

        InputStreamReader internalFile = new InputStreamReader(BankPlus.INSTANCE.getResource("commands.yml"), StandardCharsets.UTF_8);
        YamlConfiguration internalConfig = YamlConfiguration.loadConfiguration(internalFile), externalConfig = YamlConfiguration.loadConfiguration(commandsFile);

        boolean hasChanges = false;
        for (String key : internalConfig.getKeys(true)) {
            if (externalConfig.contains(key)) continue;

            hasChanges = true;
            externalConfig.set(key, internalConfig.get(key));
        }

        if (!hasChanges) return;

        try {
            externalConfig.save(commandsFile);
        } catch (Exception e) {
            BPLogger.error("Could not save file changes to commands.yml! (Error: " + e.getMessage() + ")");
        }
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

    private void validatePath(FileConfiguration from, FileConfiguration to, String path, Object fallbackValue) {
        Object value = from.get(path);
        if (value == null) to.set(path, fallbackValue);
        else to.set(path, value);
    }

    private void validatePath(FileConfiguration from, FileConfiguration to, String path, String fallbackValue) {
        String value = from.getString(path);
        if (value == null) to.set(path, fallbackValue);
        else to.set(path, value);
    }

    private Object getValueFromOldPath(FileConfiguration from, String oldPath, String newPath, Object fallbackValue) {
        return from.get(oldPath) == null ? from.get(newPath) == null ? fallbackValue : from.get(newPath) : from.get(oldPath);
    }

    private File createFile(String name) {
        File file = new File(BankPlus.INSTANCE.getDataFolder(), name + ".yml");
        try {
            file.getParentFile().mkdir();
            file.createNewFile();
        } catch (IOException e) {
            BPLogger.error("Failed to to create the " + name + " file! " + e.getMessage());
        }
        return file;
    }
}