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

    private final String confirmMessage, cooldownMessage;
    private final List<String> usage;

    public BPCommand(String... aliases) {
        this.identifier = aliases[0];

        String id = identifier.toLowerCase();
        this.permission = "bankplus." + id;

        this.aliases = new String[aliases.length - 1];
        System.arraycopy(aliases, 1, this.aliases, 0, aliases.length - 1);

        FileConfiguration config = BankPlus.INSTANCE().getConfigs().getConfig(BPConfigs.Type.COMMANDS.name);

        needConfirm = config.getBoolean(id + ".need-confirm");
        hasCooldown = config.getBoolean(id + ".has-cooldown");
        confirmCooldown = config.getInt(id + ".confirm-cooldown");
        cooldown = config.getInt(id + ".cooldown");
        confirmMessage = config.getString(id + ".confirm-message");
        cooldownMessage = config.getString(id + ".cooldown-message");
        usage = BPMessages.getPossibleMessages(config, id + ".usage");
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

    public List<String> getUsage() {
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
     * @param s The command sender.
     * @return true if it has to confirm, false otherwise.
     */
    public boolean skipToConfirm(CommandSender s) {
        if (needConfirm()) {
            if (!confirm.contains(s.getName())) {
                Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> confirm.remove(s.getName()), getConfirmCooldown() * 20L);
                BPMessages.send(s, getConfirmMessage(), true);
                confirm.add(s.getName());
                return true;
            }
            confirm.remove(s.getName());
        }
        return false;
    }

    public void execute(CommandSender s, String[] args) {
        if (!BPUtils.hasPermission(s, getPermission()) || (playerOnly() && !BPUtils.isPlayer(s))) return;

        if (!skipUsageWarn() && args.length == 1) {
            for (String usage : getUsage()) BPMessages.send(s, usage, true);
            return;
        }
        if (isInCooldown(s)) return;

        if (!onCommand(s, args)) return;

        if (hasCooldown() && getCooldown() > 0 && !(s instanceof ConsoleCommandSender)) {
            cooldownMap.put(s.getName(), System.currentTimeMillis() + (getCooldown() * 1000L));
            Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE(), () -> cooldownMap.remove(s.getName()), getCooldown() * 20L);
        }
    }

    public boolean isInCooldown(CommandSender s) {
        if (!cooldownMap.containsKey(s.getName()) || cooldownMap.get(s.getName()) <= System.currentTimeMillis()) return false;
        BPMessages.send(s, getCooldownMessage().replace("%time%", BPUtils.formatTime(cooldownMap.get(s.getName()) - System.currentTimeMillis())), true);
        return true;
    }

    public abstract boolean playerOnly();

    public abstract boolean skipUsageWarn();

    public abstract boolean onCommand(CommandSender s, String[] args);

    public abstract List<String> tabCompletion(CommandSender s, String[] args);
}