package me.pulsi_.bankplus.loanSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LoanRegistry {

    private final List<BPLoan> loans = new ArrayList<>();
    private final HashMap<UUID, UUID> requestsReceived = new HashMap<>();
    private final HashMap<UUID, BPLoan> requestsSent = new HashMap<>();

    /**
     * List to track each loan made by players.
     * @return A list of loans.
     */
    public List<BPLoan> getLoans() {
        return loans;
    }

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