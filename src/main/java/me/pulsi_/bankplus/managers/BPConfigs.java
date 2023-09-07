package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPLogger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class BPConfigs {

    private File configFile, messagesFile, multipleBanksFile, commandsFile, savesFile;
    private FileConfiguration config, messagesConfig, multipleBanksConfig, commandsConfig, savesConfig;
    private boolean autoUpdateFiles, updated = true;

    public enum Type {
        CONFIG("config.yml"),
        MESSAGES("messages.yml"),
        MULTIPLE_BANKS("multiple_banks.yml"),
        SAVES("saves.yml"),
        COMMANDS("commands.yml");

        public String name;

        Type(String name) {
            this.name = name;
        }
    }

    private final BankPlus plugin;

    public BPConfigs(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void setupConfigs() {
        savesFile = new File(plugin.getDataFolder(), Type.SAVES.name);

        commandsFile = new File(plugin.getDataFolder(), Type.COMMANDS.name);
        configFile = new File(plugin.getDataFolder(), Type.CONFIG.name);
        messagesFile = new File(plugin.getDataFolder(), Type.MESSAGES.name);
        multipleBanksFile = new File(plugin.getDataFolder(), Type.MULTIPLE_BANKS.name);

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

    public File getFile(Type type) {
        switch (type) {
            case CONFIG:
                return configFile;
            case MESSAGES:
                return messagesFile;
            case MULTIPLE_BANKS:
                return multipleBanksFile;
            case COMMANDS:
                return commandsFile;
            case SAVES:
                return savesFile;
            default:
                return null;
        }
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
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error("Could not load " + type.name() + " config! (Error: " + e.getMessage().replace("\n", "") + ")");
                    return false;
                }
                break;

            case MESSAGES:
                try {
                    messagesConfig.load(messagesFile);
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error("Could not load " + type.name() + " config! (Error: " + e.getMessage().replace("\n", "") + ")");
                    return false;
                }
                break;

            case MULTIPLE_BANKS:
                try {
                    multipleBanksConfig.load(multipleBanksFile);
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error("Could not load " + type.name() + " config! (Error: " + e.getMessage().replace("\n", "") + ")");
                    return false;
                }
                break;

            case COMMANDS:
                try {
                    commandsConfig.load(commandsFile);
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error("Could not load " + type.name() + " config! (Error: " + e.getMessage().replace("\n", "") + ")");
                    return false;
                }
                break;

            case SAVES:
                try {
                    savesConfig.load(savesFile);
                } catch (IOException | InvalidConfigurationException e) {
                    BPLogger.error("Could not load " + type.name() + " config! (Error: " + e.getMessage().replace("\n", "") + ")");
                    return false;
                }
        }
        return true;
    }

    public void setupSavesFile() {
        if (!savesFile.exists()) {
            File file = new File(BankPlus.INSTANCE.getDataFolder(), Type.SAVES.name);
            try {
                file.getParentFile().mkdir();
                file.createNewFile();
            } catch (IOException e) {
                BPLogger.error("Failed to to create the " + Type.SAVES.name + " file! " + e.getMessage());
            }
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
        if (alreadyExist) {
            reloadConfig(Type.CONFIG);

            autoUpdateFiles = config.get("General-Settings.Auto-Update-Files") == null || config.getBoolean("General-Settings.Auto-Update-Files");
            updateFile = !updated && autoUpdateFiles;
        }

        if (updateFile) setupFile(Type.CONFIG);
    }

    public void setupMessages() {
        boolean updateFile = true, alreadyExist = messagesFile.exists();
        if (alreadyExist) updateFile = !updated && autoUpdateFiles;

        if (updateFile) setupFile(Type.MESSAGES);
    }

    public void setupMultipleBanks() {
        boolean updateFile = true, alreadyExist = multipleBanksFile.exists();
        if (alreadyExist) updateFile = !updated && autoUpdateFiles;

        if (updateFile) setupFile(Type.MULTIPLE_BANKS);
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

    public void setupFile(Type type) {
        String fileName = type.name;
        File folderFile = getFile(type);
        if (!folderFile.exists()) {
            plugin.saveResource(fileName, true);
            reloadConfig(type);
            return;
        }

        HashMap<Integer, FileLine> file = new HashMap<>();
        int positions = 1;

        List<String> fileAsList = new ArrayList<>();
        Scanner scanner = new Scanner(new InputStreamReader(plugin.getResource(fileName)));
        while (scanner.hasNext()) fileAsList.add(scanner.nextLine());

        for (int i = 0; i < fileAsList.size(); i++) {
            String line = fileAsList.get(i);
            if (isListContent(line)) continue;

            if (!line.isEmpty() && !isComment(line) && line.contains(":")) {
                String[] split = line.split(":");

                boolean isValue = split.length > 1 && !isComment(split[1]);
                boolean isHeader = split.length == 1 || isComment(split[1]);
                boolean isList = isHeader && i + 1 < fileAsList.size() && isListContent(fileAsList.get(i + 1));
                file.put(positions, new FileLine(line, isValue, isHeader, isList));
                positions++;
                continue;
            }

            file.put(positions, new FileLine(line, false, false, false));
            positions++;
        }

        YamlConfiguration folderConfig = YamlConfiguration.loadConfiguration(folderFile), jarConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(fileName)));

        boolean hasChanges = false;
        for (String key : jarConfig.getKeys(true)) {
            if (folderConfig.get(key) != null) continue;

            folderConfig.set(key, jarConfig.get(key));
            hasChanges = true;
        }

        if (!hasChanges) {
            reloadConfig(type);
            return;
        }

        StringBuilder builder = new StringBuilder();
        HashMap<Integer, String> headers = new HashMap<>();

        for (int pos = 1; pos < positions; pos++) {
            FileLine fileLine = file.get(pos);

            String line = fileLine.getLine();
            if (!fileLine.isValue() && !fileLine.isHeader()) {
                builder.append(line).append("\n");
                continue;
            }

            int spaces = 0;
            for (char c : line.toCharArray()) {
                if (c == ' ') spaces++;
                else break;
            }

            int point = spaces / 2;
            String identifier = line.substring(spaces).split(":")[0];
            if (fileLine.isHeader()) headers.put(point, identifier);

            if (fileLine.isValue()) {
                StringBuilder path = new StringBuilder();

                for (int i = 0; i <= point - 1; i++) {
                    String header = headers.get(i);
                    if (header != null) path.append(header).append(".");
                }
                path.append(identifier);

                for (int i = 0; i < spaces; i++) builder.append(" ");
                builder.append(identifier).append(": ");

                Object value = folderConfig.get(path.toString());
                if (value instanceof String) builder.append("\"").append(value).append("\"\n");
                else builder.append(value).append("\n");
                continue;
            }

            if (fileLine.isList()) {
                StringBuilder path = new StringBuilder();

                for (int i = 0; i <= point - 1; i++) {
                    String header = headers.get(i);
                    if (header != null) path.append(header).append(".");
                }
                path.append(identifier);

                for (int i = 0; i < spaces; i++) builder.append(" ");
                builder.append(identifier).append(":");

                List<String> value = folderConfig.getStringList(path.toString());

                if (value.isEmpty()) builder.append(" []\n");
                else {
                    builder.append("\n");
                    for (String listLine : value) {
                        for (int i = 0; i < spaces; i++) builder.append(" ");
                        builder.append("- ").append(listLine).append("\n");
                    }
                }
                continue;
            }

            builder.append(line).append("\n");
        }
        recreateFile(folderFile, builder.toString());
        reloadConfig(type);
    }

    public void recreateFile(File file, String fileBuilder) {
        if (fileBuilder == null) return;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(fileBuilder);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            BPLogger.error(e, e.getMessage());
        }
    }

    private boolean isComment(String s) {
        return s.replace(" ", "").startsWith("#");
    }

    private boolean isListContent(String s) {
        return s.replace(" ", "").startsWith("-");
    }

    private static class FileLine {

        private final String line;
        private final boolean isValue, isHeader, isList;

        public FileLine(String line, boolean isValue, boolean isHeader, boolean isList) {
            this.line = line;
            this.isValue = isValue;
            this.isHeader = isHeader;
            this.isList = isList;
        }

        public String getLine() {
            return line;
        }

        public boolean isValue() {
            return isValue;
        }

        public boolean isHeader() {
            return isHeader;
        }

        public boolean isList() {
            return isList;
        }
    }
}