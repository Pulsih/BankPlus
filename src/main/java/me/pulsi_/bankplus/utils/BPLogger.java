package me.pulsi_.bankplus.utils;

public class BPLogger {

    public static void error(String error) {
        ChatUtils.log("&a&lBank&9&lPlus &8[&cERROR&8] &c" + error);
    }

    public static void warn(String warn) {
        ChatUtils.log("&a&lBank&9&lPlus &8[&eWARN&8] &e" + warn);
    }

    public static void info(String info) {
        ChatUtils.log("&a&lBank&9&lPlus &8[&9INFO&8] &9" + info);
    }
}