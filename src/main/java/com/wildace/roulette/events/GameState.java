package com.wildace.roulette.events;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class GameState {
    String roundId;
    String status;
    Integer secondsRemaining;
}
