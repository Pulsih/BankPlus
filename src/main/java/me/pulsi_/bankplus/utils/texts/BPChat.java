package me.pulsi_.bankplus.utils.texts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class BPChat {

    public static final String PREFIX = "<b><green>Bank<blue>Plus</blue></green></b>";

    public static Component color(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}