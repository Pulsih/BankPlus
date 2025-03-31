package me.pulsi_.bankplus.utils.texts;

import me.pulsi_.bankplus.BankPlus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BPChat {

    public static String prefix = "<b><green>Bank<blue>Plus";

    public static Component color(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}