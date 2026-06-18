package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class BetRejectedMessage {
    String type;       // "BET_REJECTED"
    String messageId;
    String timestamp;
    BetRejected payload;

    @Builder
    @Value
    public static class BetRejected {
        String roundId;
        String betId;
        Integer stake;
        List<Bet> betsList;
        String reason;
        Double newBalance;
    }
}
