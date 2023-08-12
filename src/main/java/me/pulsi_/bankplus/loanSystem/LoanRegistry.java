package me.pulsi_.bankplus.loanSystem;

import java.util.HashMap;
import java.util.UUID;

public class LoanRegistry {

    private final HashMap<UUID, UUID> requestsReceived = new HashMap<>();
    private final HashMap<UUID, BPLoan> requestsSent = new HashMap<>();

    /**
     * Used to track the player who sent the request, with the player that received the loan request as key, and the player that sent the request as value.
     * @return List of player's UUIDs
     */
    public HashMap<UUID, UUID> getRequestsReceived() {
        return requestsReceived;
    }

    /**
     * A hashmap holding all player requests, with the sender as key, and loan request as value.
     * The loan will contain useful information such as loan target, amount and other..
     * @return
     */
    public HashMap<UUID, BPLoan> getRequestsSent() {
        return requestsSent;
    }
}