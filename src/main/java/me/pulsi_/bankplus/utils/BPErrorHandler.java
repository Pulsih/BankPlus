package me.pulsi_.bankplus.utils;

public class BPErrorHandler extends Error {

    private final String message;

    public BPErrorHandler(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}