package me.pulsi_.bankplus.placeholders.list;

import me.pulsi_.bankplus.placeholders.BPPlaceholder;
import org.bukkit.entity.Player;

public class NamePlaceholder extends BPPlaceholder {
    @Override
    public String getIdentifier() {
        return "[name/displayname]";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        String[] args = getOptions(identifier);
        String name;

        if (p == null) return "Invalid player!";

        if (args[0].equals("name")) {
            name = p.getName();
        } else {
            name = p.getDisplayName();
        }
        return name;
    }

    @Override
    public boolean hasPlaceholders() {
        return false;
    }
}
