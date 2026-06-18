package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class BalanceUpdatedMessage {
    String type;      // "BALANCE_UPDATED"
    String messageId;
    String timestamp;
    Payload payload;

    @Builder
    @Value
    public static class Payload {
        String roundId;
        Double newBalance;
        String reason;
    }
}
