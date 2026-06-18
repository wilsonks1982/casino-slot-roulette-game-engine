package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class PlaceBetMessage {
    String type;
    PlaceBet payload;

    @Builder
    @Value
    public static class PlaceBet {
        String roundId;
        Integer stake;
        List<Bet> betsList;
    }
}
