package me.pulsi_.bankplus.utils;

import me.pulsi_.bankplus.BankPlus;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BPChat {

    private final static Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");

    public static String prefix = "&a&lBank&9&lPlus";

    public static String color(String message) {
        if (BankPlus.getServerVersionInt() >= 16) {
            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                String color = message.substring(matcher.start(), matcher.end());
                message = message.replace(color, ChatColor.of(color) + "");
                matcher = pattern.matcher(message);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}