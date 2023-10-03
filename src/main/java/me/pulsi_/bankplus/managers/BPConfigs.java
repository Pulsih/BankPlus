package me.pulsi_.bankplus.managers;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.FileUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class BPConfigs {

    private static boolean updated = false;
    private boolean autoUpdateFiles;

    public enum Type {
        CONFIG("config.yml"),
        MESSAGES("messages.yml"),
        MULTIPLE_BANKS("multiple_banks.yml"),
        SAVES("saves.yml"),
        COMMANDS("commands.yml");

        public final String name;
        Type(String name) {
            this.name = name;
        }
    }

    private final BankPlus plugin;

    public BPConfigs(BankPlus plugin) {
        this.plugin = plugin;
    }

    public void setupConfigs() {
        setupSavesFile();
        setupCommands();
        setupConfig();
        setupMessages();
        setupMultipleBanks();
    }

    public void setupSavesFile() {
        File file = new File(BankPlus.INSTANCE.getDataFolder(), Type.SAVES.name);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdir();
                file.createNewFile();
            } catch (IOException e) {
                BPLogger.error("Failed to to create the " + Type.SAVES.name + " file! " + e.getMessage());
            }
            updated = false;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (updated) {
            String v = config.getString("version");
            updated = v != null && v.equals(plugin.getDescription().getVersion());
        }

        config.options().header("DO NOT EDIT / REMOVE THIS FILE OR BANKPLUS MAY GET RESET!");
        config.set("version", plugin.getDescription().getVersion());

        try {
            config.save(file);
        } catch (IOException e) {
            BPLogger.error("Could not save \"saves\" file! (Error: " + e.getMessage().replace("\n", "") + ")");
        }
    }

    public void setupConfig() {
        boolean updateFile = true, alreadyExist = getFile(Type.CONFIG.name).exists();
        if (alreadyExist) {
            FileConfiguration config = getConfig(Type.CONFIG.name);

            autoUpdateFiles = config.get("General-Settings.Auto-Update-Files") == null || config.getBoolean("General-Settings.Auto-Update-Files");
            updateFile = !updated && autoUpdateFiles;
        }

        if (updateFile) setupFile(Type.CONFIG, alreadyExist);
    }

    public void setupMessages() {
        boolean updateFile = true, alreadyExist = getFile(Type.MESSAGES.name).exists();
        if (alreadyExist) updateFile = !updated && autoUpdateFiles;

        if (updateFile) setupFile(Type.MESSAGES, alreadyExist);
    }

    public void setupMultipleBanks() {
        boolean updateFile = true, alreadyExist = getFile(Type.MULTIPLE_BANKS.name).exists();
        if (alreadyExist) updateFile = !updated && autoUpdateFiles;

        if (updateFile) setupFile(Type.MULTIPLE_BANKS, alreadyExist);
    }

    public void setupCommands() {
        File file = getFile(Type.COMMANDS.name);
        if (!file.exists()) plugin.saveResource("commands.yml", true);

        InputStreamReader internalFile = new InputStreamReader(BankPlus.INSTANCE.getResource("commands.yml"), StandardCharsets.UTF_8);
        YamlConfiguration internalConfig = YamlConfiguration.loadConfiguration(internalFile), externalConfig = YamlConfiguration.loadConfiguration(file);

        boolean hasChanges = false;
        for (String key : internalConfig.getKeys(true)) {
            if (externalConfig.contains(key)) continue;

            hasChanges = true;
            externalConfig.set(key, internalConfig.get(key));
        }

        if (!hasChanges) return;

        try {
            externalConfig.save(file);
        } catch (Exception e) {
            BPLogger.error("Could not save file changes to commands.yml! (Error: " + e.getMessage() + ")");
        }
    }

    public File getFile(String path) {
        return new File(plugin.getDataFolder(), path);
    }

    public FileConfiguration getConfig(String path) {
        return YamlConfiguration.loadConfiguration(getFile(path));
    }

    public void setupFile(Type type, boolean backup) {
        String fileName = type.name;
        File folderFile = getFile(type.name);
        if (!folderFile.exists()) {
            plugin.saveResource(fileName, true);
            return;
        }

        if (backup) {
            File copyFile = new File(BankPlus.INSTANCE.getDataFolder(), "backups" + File.separator + fileName);
            if (!copyFile.exists()) {
                try {
                    copyFile.getParentFile().mkdirs();
                    copyFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            FileUtil.copy(folderFile, copyFile);
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
        if (!hasChanges) return;

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
                        builder.append("- \"").append(listLine).append("\"\n");
                    }
                }
                continue;
            }

            builder.append(line).append("\n");
        }
        recreateFile(folderFile, builder.toString());
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

    public void backupFile() {

    }

    public static boolean isUpdated() {
        return updated;
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