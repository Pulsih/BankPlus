package me.pulsi_.bankplus.commands;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.managers.ConfigManager;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPMethods;
import me.pulsi_.bankplus.values.Values;
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
        this.permission = "bankplus." + identifier;

        this.aliases = new String[aliases.length - 1];
        for (int i = 1; i < aliases.length; i++)
            this.aliases[i - 1] = aliases[i];

        FileConfiguration config = BankPlus.INSTANCE.getConfigManager().getConfig(ConfigManager.Type.COMMANDS);

        needConfirm = config.getBoolean(identifier + ".need-confirm");
        hasCooldown = config.getBoolean(identifier + ".has-cooldown");
        confirmCooldown = config.getInt(identifier + ".confirm-cooldown");
        cooldown = config.getInt(identifier + ".cooldown");
        confirmMessage = config.getString(identifier + ".confirm-message");
        cooldownMessage = config.getString(identifier + ".cooldown-message");
        String singleUsage = config.getString(identifier + ".usage.single");
        String multiUsage = config.getString(identifier + ".usage.multi");

        if ((!Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled() && singleUsage == null) || (Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled() && multiUsage == null))
            usage = config.getString(identifier + ".usage");
        else
            usage = Values.MULTIPLE_BANKS.isMultipleBanksModuleEnabled() ? multiUsage : singleUsage;
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
        MainCmd.commands.put(identifier, this);
        for (String alias : aliases)
            MainCmd.commands.put(alias, this);
    }

    public boolean preExecute(CommandSender s, String args[], boolean playerOnly, boolean has1Arg) {
        if (!BPMethods.hasPermission(s, getPermission()) || (playerOnly && !BPMethods.isPlayer(s))) return false;

        if (args.length == 1 && !has1Arg) {
            if (getUsage() != null && !getUsage().equals("")) BPMessages.send(s, getUsage(), true);
            return false;
        }

        if (needConfirm()) {
            if (!confirm.contains(s.getName())) {
                Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> confirm.remove(s.getName()), getConfirmCooldown() * 20);
                if (getConfirmMessage() != null && !getConfirmMessage().equals(""))
                    BPMessages.send(s, getConfirmMessage(), true);
                confirm.add(s.getName());
                return false;
            }
            confirm.remove(s.getName());
        }

        if (hasCooldown() && getCooldown() > 0 && !(s instanceof ConsoleCommandSender)) {
            if (cooldownMap.containsKey(s.getName()) && cooldownMap.get(s.getName()) > System.currentTimeMillis()) {
                if (getCooldownMessage() != null && !getCooldownMessage().equals(""))
                    BPMessages.send(s, getCooldownMessage(), true);
                return false;
            }
            cooldownMap.put(s.getName(), System.currentTimeMillis() + (getCooldown() * 1000));
            Bukkit.getScheduler().runTaskLater(BankPlus.INSTANCE, () -> cooldownMap.remove(s.getName()), getCooldown() * 20);
        }
        return true;
    }

    public abstract void execute(CommandSender s, String args[]);

    public abstract List<String> tabCompletion(CommandSender s, String args[]);
}