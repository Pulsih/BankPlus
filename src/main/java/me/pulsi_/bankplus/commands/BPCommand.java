package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.utils.BPUtils;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

import static me.pulsi_.bankplus.commands.BPCmdRegistry.commands;

public abstract class BPCommand {

    public final String identifier, permission;

    public final String[] aliases;

    public final int confirmCooldown, cooldown;

    public final List<String> confirmMessage, cooldownMessage, usage;

    private boolean hasChangedConfig = false;

    public BPCommand(FileConfiguration config, String... cmdAndAliases) {
        identifier = cmdAndAliases[0];

        String id = identifier.toLowerCase();
        permission = "bankplus." + id;

        aliases = new String[cmdAndAliases.length - 1];
        System.arraycopy(cmdAndAliases, 1, aliases, 0, cmdAndAliases.length - 1);

        usage = getListOrSetDefault(config, id + ".usage", defaultUsage());
        confirmCooldown = getIntOrSetDefault(config, id + ".confirm-cooldown", defaultConfirmCooldown());
        confirmMessage = getListOrSetDefault(config, id + ".confirm-message", defaultConfirmMessage());
        cooldown = getIntOrSetDefault(config, id + ".cooldown", defaultCooldown());
        cooldownMessage = getListOrSetDefault(config, id + ".cooldown-message", defaultCooldownMessage());
    }

    private final HashMap<String, Long> cooldowns = new HashMap<>();

    private final Set<String> confirm = new HashSet<>();

    private List<String> getListOrSetDefault(FileConfiguration config, String path, List<String> defaultValue) {
        List<String> result = defaultValue;
        if (BPUtils.pathExist(config, path)) result = BPMessages.getPossibleMessages(config, path);
        else {
            config.set(path, result);
            hasChangedConfig = true;
        }
        return result;
    }

    private int getIntOrSetDefault(FileConfiguration config, String path, int defaultValue) {
        int result = defaultValue;
        if (BPUtils.pathExist(config, path)) result = config.getInt(path);
        else {
            config.set(path, result);
            hasChangedConfig = true;
        }
        return result;
    }

    /**
     * Register the selected command to the bankplus system, and checks if the commands file has been modified.
     * @return true if the config file has been modified, false otherwise.
     */
    public boolean register() {
        commands.put(identifier.toLowerCase(), this);
        for (String alias : aliases)
            commands.put(alias.toLowerCase(), this);
        return hasChangedConfig;
    }

    /**
     * Check if the command sender has confirmed to cmd. If
     * the confirmation is not active, it will just return true.
     *
     * @param s The command sender.
     * @return true if he has confirmed, false otherwise.
     */
    public boolean hasConfirmed(CommandSender s) {
        if (confirmCooldown > 0) {
            String name = s.getName();
            if (!confirm.contains(name)) {
                Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> confirm.remove(name), confirmCooldown * 20L);
                for (String message : confirmMessage) BPMessages.send(s, message, true);
                confirm.add(name);
                return true;
            }
            confirm.remove(name);
        }
        return false;
    }

    /**
     * Execute the command, only, and only if it passes all the required checks (permission, cooldown, confirm).
     *
     * @param s    The command sender.
     * @param args The cmd arguments.
     */
    public void execute(CommandSender s, String[] args) {
        if (!BPUtils.hasPermission(s, permission) || (playerOnly() && !BPUtils.isPlayer(s))) return;

        if (!skipUsageWarn() && args.length == 1) {
            for (String usage : usage) BPMessages.send(s, usage, true);
            return;
        }
        if (isInCooldown(s) || !preCmdChecks(s, args) || !hasConfirmed(s)) return;

        onSuccessExecution(s, args);

        if (cooldown > 0 && !(s instanceof ConsoleCommandSender)) {
            cooldowns.put(s.getName(), System.currentTimeMillis() + (cooldown * 1000L));
            Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> cooldowns.remove(s.getName()), cooldown * 20L);
        }
    }

    public boolean isInCooldown(CommandSender s) {
        String name = s.getName();
        long get = cooldowns.get(name), cur = System.currentTimeMillis();
        if (!cooldowns.containsKey(name) || get <= cur) return false;

        for (String message : cooldownMessage)
            BPMessages.send(s, message, true, "%time%$" + BPFormatter.formatTime(get - cur));
        return true;
    }

    public abstract List<String> defaultUsage();

    public abstract int defaultConfirmCooldown();

    public abstract List<String> defaultConfirmMessage();

    public abstract int defaultCooldown();

    public abstract List<String> defaultCooldownMessage();

    public abstract boolean playerOnly();

    public abstract boolean skipUsageWarn();

    /**
     * Makes all the needed checks before executing the cmd.
     *
     * @param s    The command sender.
     * @param args The cmd arguments.
     * @return true if the cmd pass the checks, false otherwise.
     */
    public abstract boolean preCmdChecks(CommandSender s, String[] args);

    /**
     * Method ran when the @{#preCmdChecks} returns true.
     *
     * @param s    The command sender.
     * @param args The cmd arguments.
     */
    public abstract void onSuccessExecution(CommandSender s, String[] args);

    public abstract List<String> tabCompletion(CommandSender s, String[] args);
}