package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.BankManager;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.utils.BPArgs;
import me.pulsi_.bankplus.utils.BPMessages;
import me.pulsi_.bankplus.utils.BPUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlaceholdersCmd extends BPCommand {

    public PlaceholdersCmd(String... aliases) {
        super(aliases);
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public boolean skipUsageWarn() {
        return true;
    }

    @Override
    public boolean onCommand(CommandSender s, String[] args) {
        List<String> placeholders =  BankPlus.INSTANCE().getBpPlaceholders().getRegisteredPlaceholders();
        int size = placeholders.size();

        BPMessages.send(s, "%prefix% &7Currently registered placeholders &8(&a" + size + "&8)&7:", true);

        StringBuilder builder = new StringBuilder("  &8* ");
        for (int i = 0; i < size; i++) {
            builder.append("&8[&a").append(placeholders.get(i));

            if (i + 1 >= size) builder.append("&8]&7.");
            else builder.append("&8]&7, ");
        }
        BPMessages.send(s, builder.toString(), true);
        return true;
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        Player p = (Player) s;

        if (args.length == 2)
            return BPArgs.getArgs(args, BankManager.getAvailableBanks(p));
        return null;
    }
}