package me.pulsi_.bankplus.economy;

public abstract class Transaction {

    private boolean hasEnded = false;

    public boolean hasEnded() {
        return hasEnded;
    }

    public void setHasEnded(boolean hasEnded) {
        this.hasEnded = hasEnded;
    }
}