package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class BetPlacedMessage {
    String type;       // "BET_PLACED"
    String messageId;  // unique per message
    String timestamp;  // ISO string
    BetPlaced payload;

    @Builder
    @Value
    public static class BetPlaced {
        String roundId;
        String betId;
        Integer stake;
        List<Bet> betsList;
        Double newBalance;
    }
}