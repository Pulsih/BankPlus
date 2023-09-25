package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.BPConfigs;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class BPCommand {

    private final String identifier, permission;

    private final String[] aliases;

    private final boolean needConfirm, hasCooldown;

    private final int confirmCooldown, cooldown;

    private final String confirmMessage, cooldownMessage, usage;

    public BPCommand(String... aliases) {
        this.identifier = aliases[0];
        this.permission = "bankplus." + identifier.toLowerCase();

        this.aliases = new String[aliases.length - 1];
        System.arraycopy(aliases, 1, this.aliases, 0, aliases.length - 1);

        FileConfiguration config = BankPlus.INSTANCE.getConfigManager().getConfig(BPConfigs.Type.COMMANDS.name);

        needConfirm = config.getBoolean(identifier.toLowerCase() + ".need-confirm");
        hasCooldown = config.getBoolean(identifier.toLowerCase() + ".has-cooldown");
        confirmCooldown = config.getInt(identifier.toLowerCase() + ".confirm-cooldown");
        cooldown = config.getInt(identifier.toLowerCase() + ".cooldown");
        confirmMessage = config.getString(identifier.toLowerCase() + ".confirm-message");
        cooldownMessage = config.getString(identifier.toLowerCase() + ".cooldown-message");
        usage = config.getString(identifier.toLowerCase() + ".usage");
    }

    private final HashMap<String, Long> cooldownMap = new HashMap<>();

    private final List<String> confirm = new ArrayList<>();

    public boolean needConfirm() {
        return needConfirm;
    }

    public boolean hasCooldown() {
        return hasCooldown;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getConfirmCooldown() {
        return confirmCooldown;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getPermission() {
        return permission;
    }

    public String getUsage() {
        return usage;
    }

    public String getConfirmMessage() {
        return confirmMessage;
    }

    public String getCooldownMessage() {
        return cooldownMessage;
    }

    public void register() {
        MainCmd.commands.put(identifier.toLowerCase(), this);
        for (String alias : aliases)
            MainCmd.commands.put(alias.toLowerCase(), this);
    }

    /**
     * Call this function when the command is ready to run to make the confirmation, if this
     * function is not called, even if the confirmation in the config is set to true, it won't work.
     * @param s The command sender
     * @return true if it has to confirm, false otherwise
     */
    public boolean confirm(CommandSender s) {
        if (needConfirm()) {
            if (!confirm.contains(s.getName())) {
                Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> confirm.remove(s.getName()), getConfirmCooldown() * 20);
                if (getConfirmMessage() != null && !getConfirmMessage().equals(""))
                    BPMessages.send(s, getConfirmMessage(), true);
                confirm.add(s.getName());
                return true;
            }
            confirm.remove(s.getName());
        }
        return false;
    }

    public void execute(CommandSender s, String args[]) {
        if (!BPUtils.hasPermission(s, getPermission()) || (playerOnly() && !BPUtils.isPlayer(s))) return;

        if (!skipUsageWarn() && args.length == 1) {
            if (getUsage() != null && !getUsage().equals("")) BPMessages.send(s, getUsage(), true);
            return;
        }

        if (!onCommand(s, args)) return;

        if (hasCooldown() && getCooldown() > 0 && !(s instanceof ConsoleCommandSender)) {
            if (cooldownMap.containsKey(s.getName()) && cooldownMap.get(s.getName()) > System.currentTimeMillis()) {
                if (getCooldownMessage() != null && !getCooldownMessage().equals(""))
                    BPMessages.send(s, getCooldownMessage(), true);
                return;
            }
            cooldownMap.put(s.getName(), System.currentTimeMillis() + (getCooldown() * 1000));
            Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> cooldownMap.remove(s.getName()), getCooldown() * 20);
        }
    }

    public abstract boolean playerOnly();

    public abstract boolean skipUsageWarn();

    public abstract boolean onCommand(CommandSender s, String args[]);

    public abstract List<String> tabCompletion(CommandSender s, String args[]);
}